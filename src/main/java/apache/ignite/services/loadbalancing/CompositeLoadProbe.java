package apache.ignite.services.loadbalancing;

import org.apache.ignite.cluster.ClusterNode;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

/**
 * Calculate load using multiple indicators. The probe is configured with multiple {@link LoadProbeDescriptor}.
 * <p>
 * Every {@link LoadProbeDescriptor} specifies:
 * <ul>
 * <li>
 * {@link LoadProbeDescriptor#getWeight()} - the probe weight relative to other probes from 0.0+ (lowest weight) to 1.0
 * (highest weight).
 * </li>
 * <li>{@link LoadProbeDescriptor#getMax()} - maximum value that the probe can retrieve.</li>
 * <li>{@link LoadProbeDescriptor#getProbe()} - the probe.</li>
 * </ul>
 * </p>
 */
public class CompositeLoadProbe implements Function<ClusterNode, Double> {
    /** Probes. */
    private Collection<LoadProbeDescriptor> probes;

    /** {@inheritDoc} */
    @Override public Double apply(ClusterNode node) {
        return probes.parallelStream()
            .map(p -> (1.0 - p.getWeight()) * p.getProbe().apply(node) / p.getMax())
            .reduce(0.0, (a, b) -> a + b);
    }

    /** @return Probes. */
    public Collection<LoadProbeDescriptor> getProbes() {
        return probes;
    }

    /** Set probes. */
    public CompositeLoadProbe setProbes(Collection<LoadProbeDescriptor> probes) {
        Objects.requireNonNull(probes);

        if (probes.size() == 0)
            throw new IllegalArgumentException("At least one probe must be specified.");

        this.probes = probes;

        return this;
    }
}
