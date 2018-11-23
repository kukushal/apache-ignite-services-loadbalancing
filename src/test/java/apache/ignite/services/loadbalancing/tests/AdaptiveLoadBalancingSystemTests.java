package apache.ignite.services.loadbalancing.tests;

import apache.ignite.services.loadbalancing.CompositeLoadProbe;
import apache.ignite.services.loadbalancing.IgniteAdaptiveLoadBalancing;
import apache.ignite.services.loadbalancing.IgniteLoadBalancerService;
import apache.ignite.services.loadbalancing.IgniteServiceLocator;
import apache.ignite.services.loadbalancing.LoadBalancer;
import apache.ignite.services.loadbalancing.LoadProbeDescriptor;
import apache.ignite.services.loadbalancing.ServiceLocator;
import apache.ignite.services.loadbalancing.testobjects.Storage;
import apache.ignite.services.loadbalancing.testobjects.StorageService;
import apache.ignite.services.loadbalancing.testobjects.TestCluster;
import org.apache.ignite.cluster.ClusterNode;
import org.junit.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertArrayEquals;

/** Test Adaptive load balancing strategy on a real Ignite cluster running locally. */
public class AdaptiveLoadBalancingSystemTests {
    /** Assign calls to nodes with lowest utilisation. */
    @Test
    public void assignCallsToNodesWithLowestUtilisation() {
        try (TestCluster testCluster = new TestCluster(5)) {
            final String SVC_NAME = "Storage";

            Object bestNodeId = testCluster.ignite(3).cluster().localNode().consistentId();

            ServiceLocator svcLocator = new IgniteServiceLocator(testCluster.ignite(0).name());

            testCluster.ignite(0).services().deployNodeSingleton(
                LoadBalancer.class.getSimpleName(),
                new IgniteLoadBalancerService().setLoadBalancingConfiguration(
                    Stream.of(
                        new SimpleEntry<>(
                            "*",
                            new IgniteAdaptiveLoadBalancing()
                                .setLoadProbe(
                                    new TestLoadProbe(bestNodeId, 0.1, 0.8)
                                )
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

    /** Assign calls to nodes according to multiple factors. */
    @Test
    public void assignCallsToNodesAccordingToMultipleFactors() {
        try (TestCluster testCluster = new TestCluster(5)) {
            final String SVC_NAME = "Storage";

            Object node1Id = testCluster.ignite(1).cluster().localNode().consistentId();
            Object node3Id = testCluster.ignite(3).cluster().localNode().consistentId();

            ServiceLocator svcLocator = new IgniteServiceLocator(testCluster.ignite(0).name());

            testCluster.ignite(0).services().deployNodeSingleton(
                LoadBalancer.class.getSimpleName(),
                new IgniteLoadBalancerService().setLoadBalancingConfiguration(
                    Stream.of(
                        new SimpleEntry<>(
                            "*",
                            new IgniteAdaptiveLoadBalancing()
                                // The below setup must result in this load:
                                // node 1: (1 - 0.1) * 10.0 / 100 + (1 - 0.98) * 0.7 / 1.0 = 0.104
                                // node 3: (1 - 0.1) * 90.0 / 100 + (1 - 0.98) * 0.3 / 1.0 = 0.816
                                // others: (1 - 0.1) * 90.0 / 100 + (1 - 0.98) * 0.7 / 1.0 = 0.824
                                // Thus, node 1 must be "the best node" having lowest utilisation.
                                .setLoadProbe(new CompositeLoadProbe().setProbes(Arrays.asList(
                                    new LoadProbeDescriptor()
                                        .setWeight(0.1)
                                        .setMax(100)
                                        .setProbe(new TestLoadProbe(node1Id, 10.0, 90.0)),
                                    new LoadProbeDescriptor()
                                        .setWeight(0.98)
                                        .setMax(1)
                                        .setProbe(new TestLoadProbe(node3Id, 0.3, 0.7))
                                )))
                        )
                    ).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue))
                )
            );

            testCluster.ignite(0).services().deployNodeSingleton(SVC_NAME, new StorageService());

            Storage storage = svcLocator.getService(SVC_NAME, Storage.class);

            Integer[] expVals = new Integer[] {0, 1, 2};

            Stream.of(expVals).forEach(storage::store);

            Integer[] actualVals = testCluster.ignite(1).services().serviceProxy(SVC_NAME, Storage.class, false)
                .contents();

            assertArrayEquals(expVals, actualVals);
        }
    }

    /** Test load probe. */
    private static class TestLoadProbe implements Function<ClusterNode, Double> {
        /** Best node ID. */
        private final Object bestNodeId;

        /** Best load. */
        private final double bestLoad;

        /** Default load. */
        private final double dfltLoad;

        /** Constructor. */
        TestLoadProbe(Object bestNodeId, double bestLoad, double dfltLoad) {
            this.bestNodeId = bestNodeId;
            this.bestLoad = bestLoad;
            this.dfltLoad = dfltLoad;
        }

        /** {@inheritDoc} */
        @Override public Double apply(ClusterNode node) {
            // Get smaller load for the "best" node
            return node.consistentId().equals(bestNodeId) ? bestLoad : dfltLoad;
        }
    }
}
