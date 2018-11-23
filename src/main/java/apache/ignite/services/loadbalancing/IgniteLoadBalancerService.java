package apache.ignite.services.loadbalancing;

import org.apache.ignite.Ignite;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.services.Service;
import org.apache.ignite.services.ServiceContext;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

/** Ignite {@link LoadBalancer} implementation. */
public class IgniteLoadBalancerService implements LoadBalancer, Service {
    /** Ignite. */
    @IgniteInstanceResource Ignite ignite;

    /** Load balancing configuration. */
    private Map<String, LoadBalancingStrategy> loadBalancingCfg;

    /** Default load balancing strategy. */
    private LoadBalancingStrategy dfltStgy;

    /** {@inheritDoc} */
    @Override public String getNode(String svcName, String svcOpName, Object[] args) {
        Collection<ClusterNode> allNodes = ignite.cluster().forServers().nodes();

        Iterator<String> selectedNodesIter = getStrategy(svcName, svcOpName)
            .selectNodes(allNodes.stream().map(n -> n.id().toString()).collect(Collectors.toList()), args)
            .iterator();

        return selectedNodesIter.hasNext() ? selectedNodesIter.next() : null;
    }

    /** {@inheritDoc} */
    @Override public void cancel(ServiceContext ctx) {
    }

    /** {@inheritDoc} */
    @Override public void init(ServiceContext ctx) {
    }

    /** {@inheritDoc} */
    @Override public void execute(ServiceContext ctx) {
    }

    /**
     * @return A map of service method qualifiers to {@link LoadBalancingStrategy} implementations. Examples of
     * service method qualifiers in the order of precedence:
     * <ul>
     *     <li>service name#operation name - use specified strategy for specific service operation.</li>
     *     <li>service name - use specified strategy for any operation of the service</li>
     *     <li>* - use specified strategy for any service and operation</li>
     * </ul>
     */
    public Map<String, LoadBalancingStrategy> getLoadBalancingConfiguration() {
        return loadBalancingCfg;
    }

    /** Set load balancing configuration - see {@link #getLoadBalancingConfiguration()} for details. */
    public IgniteLoadBalancerService setLoadBalancingConfiguration(Map<String, LoadBalancingStrategy> cfg) {
        loadBalancingCfg = cfg;

        return this;
    }

    /**
     * @return {@link LoadBalancingStrategy} for specified service operation or
     * {@link IgniteRoundRobinLoadBalancing} if the load balancing configuration was not found.
     */
    private LoadBalancingStrategy getStrategy(String svcName, String svcOpName) {
        final String MTD_SEPARATOR = "#";
        final String MATCH_ALL_SYMBOL = "*";

        LoadBalancingStrategy res = null;

        if (loadBalancingCfg != null) {
            if (svcName != null && svcName.trim().length() > 0) {
                if (svcOpName != null && svcOpName.trim().length() > 0)
                    res = loadBalancingCfg.get(svcName.trim() + MTD_SEPARATOR + svcOpName.trim());

                if (res == null)
                    res = loadBalancingCfg.get(svcName.trim());
            }

            if (res == null)
                res = loadBalancingCfg.get(MATCH_ALL_SYMBOL);
        }

        if (res == null) {
            if (dfltStgy == null)
                dfltStgy = new IgniteRoundRobinLoadBalancing();

            res = dfltStgy;
        }

        if (res instanceof IgniteLoadBalancingStrategy)
            ((IgniteLoadBalancingStrategy)res).setIgnite(ignite);

        return res;
    }
}
