package apache.ignite.services.loadbalancing;

import org.apache.ignite.Ignite;
import org.apache.ignite.cluster.ClusterNode;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;
import java.util.function.Function;

/** Adaptive implementation of {@link LoadBalancingStrategy} for Ignite. */
public class IgniteAdaptiveLoadBalancing implements IgniteLoadBalancingStrategy {
    /** Ignite. */
    private Ignite ignite;

    /** Load probe. */
    private Function<ClusterNode, Double> loadProbe;

    /** {@inheritDoc} */
    @Override public void setIgnite(Ignite ignite) {
        this.ignite = ignite;
    }

    /** {@inheritDoc} */
    @Override public Collection<String> selectNodes(Collection<String> nodesIds, Object[] args) {
        return nodesIds.parallelStream()
            .map(id -> new SimpleEntry<>(id, getLoadProbe().apply(ignite.cluster().node(UUID.fromString(id)))))
            .min(Comparator.comparingDouble(SimpleEntry::getValue))
            .map(kv -> Collections.singleton(kv.getKey()))
            .orElse(Collections.emptySet());
    }

    /** @return Load probe. {@link IgniteCpuLoadProbe} is used by default. */
    public Function<ClusterNode, Double> getLoadProbe() {
        if (loadProbe == null)
            loadProbe = new IgniteCpuLoadProbe();

        return loadProbe;
    }

    /** Set load probe. */
    public IgniteAdaptiveLoadBalancing setLoadProbe(Function<ClusterNode, Double> loadProbe) {
        this.loadProbe = loadProbe;

        return this;
    }
}
