package apache.ignite.services.loadbalancing.tests;

import apache.ignite.services.loadbalancing.IgniteAdaptiveLoadBalancing;
import apache.ignite.services.loadbalancing.IgniteLoadBalancerService;
import apache.ignite.services.loadbalancing.IgniteServiceLocator;
import apache.ignite.services.loadbalancing.LoadBalancer;
import apache.ignite.services.loadbalancing.ServiceLocator;
import apache.ignite.services.loadbalancing.testobjects.Storage;
import apache.ignite.services.loadbalancing.testobjects.StorageService;
import apache.ignite.services.loadbalancing.testobjects.TestCluster;
import org.apache.ignite.Ignite;
import org.apache.ignite.cluster.ClusterNode;
import org.junit.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertArrayEquals;

/** Test Adaptive load balancing strategy on a real Ignite cluster running locally. */
public class AdaptiveLoadBalancingSystemTests {
    /** Assign calls to nodes with lowest CPU utilisation. */
    @Test
    public void assignCallsToNodesWithLowestCpuUtilisation() {
        try (TestCluster testCluster = new TestCluster(5)) {
            final String SVC_NAME = "Storage";

            Ignite bestNode = testCluster.ignite(3);

            ServiceLocator svcLocator = new IgniteServiceLocator(testCluster.ignite(0).name());

            testCluster.ignite(0).services().deployNodeSingleton(
                LoadBalancer.class.getSimpleName(),
                new IgniteLoadBalancerService().setLoadBalancingConfiguration(
                    Stream.of(
                        new SimpleEntry<>(
                            "*",
                            new IgniteAdaptiveLoadBalancing()
                                .setLoadProbe(new TestLoadProbe(bestNode.cluster().localNode().consistentId()))
                        )
                    ).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue))
                )
            );

            testCluster.ignite(0).services().deployNodeSingleton(SVC_NAME, new StorageService());

            Storage storage = svcLocator.getService(SVC_NAME, Storage.class);

            Integer[] expVals = new Integer[] {0, 1, 2};

            Stream.of(expVals).forEach(storage::store);

            Integer[] actualVals = testCluster.ignite(3).services().serviceProxy(SVC_NAME, Storage.class, false)
                .contents();

            assertArrayEquals(expVals, actualVals);
        }
    }

    /** Test load probe. */
    private static class TestLoadProbe implements Function<ClusterNode, Double> {
        /** Best node ID. */
        private final Object bestNodeId;

        /** Constructor. */
        TestLoadProbe(Object bestNodeId) {
            this.bestNodeId = bestNodeId;
        }

        /** {@inheritDoc} */
        @Override public Double apply(ClusterNode node) {
            // Get smaller load for the "best" node
            return node.consistentId().equals(bestNodeId) ? 0.1 : 0.8;
        }
    }
}
