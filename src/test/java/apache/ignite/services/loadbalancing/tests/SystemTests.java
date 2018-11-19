package apache.ignite.services.loadbalancing.tests;

import org.junit.Test;

/** Test on a real Ignite cluster running locally. */
public class SystemTests {
    /** Round Robin Algorithm (RRA) assigns tasks sequentially and evenly to all the nodes. */
    @Test
    public void roundRobinLoadBalancingAssignsCallsSequentiallyAndEvenly() {
        try (TestCluster testCluster = TestCluster.create(3, Counter.class)) {

        }
    }
}
