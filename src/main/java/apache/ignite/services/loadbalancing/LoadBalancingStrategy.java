package apache.ignite.services.loadbalancing;

import java.util.Collection;

/** Load balancing strategy. */
public interface LoadBalancingStrategy {
    /**
     * Select one or more nodes to redirect work to.
     *
     * @param nodesIds IDs of the nodes to select from.
     * @param args Optional arguments that some {@link LoadBalancingStrategy} implementation might analyse.
     *
     * @return IDs of node(s) to redirect work to.
     */
    Collection<String> selectNodes(Collection<String> nodesIds, Object[] args);
}
