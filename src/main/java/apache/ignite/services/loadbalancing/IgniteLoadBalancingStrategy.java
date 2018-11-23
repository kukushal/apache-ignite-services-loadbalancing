package apache.ignite.services.loadbalancing;

import org.apache.ignite.Ignite;

/** Load balancing strategy using {@link Ignite}. */
public interface IgniteLoadBalancingStrategy extends LoadBalancingStrategy {
    /** {@link Ignite} Ignite instance must be injected upon initialization. */
    void setIgnite(Ignite ignite);
}
