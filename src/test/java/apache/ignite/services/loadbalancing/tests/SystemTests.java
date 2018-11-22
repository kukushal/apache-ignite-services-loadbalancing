package apache.ignite.services.loadbalancing.tests;

import apache.ignite.services.loadbalancing.IgniteServiceLocator;
import apache.ignite.services.loadbalancing.ServiceLocator;
import apache.ignite.services.loadbalancing.testobjects.Storage;
import apache.ignite.services.loadbalancing.testobjects.StorageService;
import apache.ignite.services.loadbalancing.testobjects.TestCluster;
import org.apache.ignite.Ignite;
import org.junit.Test;

import java.util.stream.IntStream;

import static org.junit.Assert.assertTrue;

/** Test on a real Ignite cluster running locally. */
public class SystemTests {
    /** Round Robin Algorithm (RRA) assigns tasks sequentially and evenly to all the nodes. */
    @Test
    public void roundRobinLoadBalancingAssignsCallsSequentiallyAndEvenly() {
        final int CLUSTER_SIZE = 3;
        final int CALLS_PER_SVC = 10;
        final String SVC_NAME = "Storage";

        try (TestCluster testCluster = new TestCluster(CLUSTER_SIZE)) {
            ServiceLocator svcLocator = new IgniteServiceLocator(testCluster.ignite(0).name());

            testCluster.ignite(0).services().deployNodeSingleton(SVC_NAME, new StorageService());

            Storage storage = svcLocator.getService(SVC_NAME, Storage.class);

            IntStream.range(0, CALLS_PER_SVC * CLUSTER_SIZE).forEach(storage::store);

            Integer[][] contents = IntStream.range(0, CLUSTER_SIZE).boxed().map(i -> {
                Ignite ignite = testCluster.ignite(i);
                return ignite.services(ignite.cluster().forLocal())
                    .serviceProxy(SVC_NAME, Storage.class, false)
                    .contents();
            }).toArray(Integer[][]::new);

            assertTrue(IntStream.range(0, CLUSTER_SIZE).allMatch(i -> {
                // Even distribution: all services have contents of the same size
                if (contents[i].length != CALLS_PER_SVC)
                    return false;

                // Sequential distribution: difference between two neighbouring values equals the cluster size
                for (int j = 1; j < CALLS_PER_SVC; j++) {
                    if (contents[i][j] - contents[i][j - 1] != CLUSTER_SIZE)
                        return false;
                }

                return true;
            }));
        }
    }
}
