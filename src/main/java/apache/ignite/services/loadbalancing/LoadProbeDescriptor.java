package apache.ignite.services.loadbalancing;

import org.apache.ignite.cluster.ClusterNode;

import java.util.Objects;
import java.util.function.Function;

/** Load probe descriptor. */
public class LoadProbeDescriptor {
    /** Weight. */
    private double weight = 1.0;

    /** Max. */
    private double max = 1.0;

    /** Probe. */
    private Function<ClusterNode, Double> probe;

    /** @return Weight. */
    public double getWeight() {
        return weight;
    }

    /** Set weight. */
    public LoadProbeDescriptor setWeight(double weight) {
        if (weight < 0 || weight > 1)
            throw new IllegalArgumentException("Invalid weight " + weight + ". Weight must be in the [0, 1] range.");

        this.weight = weight;

        return this;
    }

    /** @return Max value. */
    public double getMax() {
        return max;
    }

    /** Set max value. */
    public LoadProbeDescriptor setMax(double max) {
        if (max <= 0)
            throw new IllegalArgumentException("Invalid max value " + max + ". Max value must be a positive number.");

        this.max = max;

        return this;
    }

    /** @return Probe. */
    public Function<ClusterNode, Double> getProbe() {
        return probe;
    }

    /** Set probe. */
    public LoadProbeDescriptor setProbe(Function<ClusterNode, Double> probe) {
        Objects.requireNonNull(probe);

        this.probe = probe;

        return this;
    }
}
