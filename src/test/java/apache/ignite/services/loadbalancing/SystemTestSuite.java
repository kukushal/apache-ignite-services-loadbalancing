package apache.ignite.services.loadbalancing;

import apache.ignite.services.loadbalancing.tests.AdaptiveLoadBalancingSystemTests;
import apache.ignite.services.loadbalancing.tests.RoundRobinLoadBalancingSystemTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/** System test suite. */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    AdaptiveLoadBalancingSystemTests.class,
    RoundRobinLoadBalancingSystemTests.class
})
public class SystemTestSuite {
}
