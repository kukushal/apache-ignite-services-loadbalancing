package apache.ignite.services.loadbalancing.tests;

import org.apache.ignite.Ignite;
import org.apache.ignite.cluster.ClusterNode;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

/** Test on a real Ignite cluster running locally. */
public class SystemTests {
    /** Round Robin Algorithm (RRA) assigns tasks sequentially and evenly to all the nodes. */
    @Test
    public void roundRobinLoadBalancingAssignsCallsSequentiallyAndEvenly() {
        final int CLUSTER_SIZE = 3;
        final int CALLS_PER_SVC = 10;

        try (TestCluster testCluster = TestCluster.create(CLUSTER_SIZE, Storage.class)) {
            Storage storage = ServiceLocator.getService(Storage.class);

            IntStream.range(0, CALLS_PER_SVC * CLUSTER_SIZE).forEach(i -> storage.store(i));

            int[][] contents = IntStream.range(0, CLUSTER_SIZE).map(i -> {
               Ignite ignite = testCluster.ignite(i);
               return ignite.services(ignite.cluster().forLocal()).contents();
            }).toArray();

            assertTrue(IntStream.range(0, CLUSTER_SIZE).allMatch(i -> {
                if (contents[i].length != CALLS_PER_SVC)
                    return false;

                
            }));

            IntStream.range(0, CLUSTER_SIZE).allMatch(n -> IntStream.range(0, CALLS_PER_SVC).);

            IntStream mergedContents = Stream.concat(IntStream.range(0, CLUSTER_SIZE).boxed().map(i -> contents[i]));

            assertTrue(IntStream.range(0, CALLS_PER_SVC * CLUSTER_SIZE).allMatch(i -> Arrays.in mergedContents.))
        }
    }
}
