package apache.ignite.services.loadbalancing;

import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.spi.loadbalancing.adaptive.AdaptiveCpuLoadProbe;

import java.util.function.Function;

/** Ignite node CPU utilization load probe. */
public class IgniteCpuLoadProbe implements Function<ClusterNode, Double> {
    /** Implementation. */
    private final AdaptiveCpuLoadProbe impl = new AdaptiveCpuLoadProbe();

    /** {@inheritDoc} */
    @Override public Double apply(ClusterNode node) {
        return impl.getLoad(node, 0);
    }

    /** See {@link AdaptiveCpuLoadProbe#isUseAverage()}. */
    public boolean isUseAverage() {
        return impl.isUseAverage();
    }

    /** See {@link AdaptiveCpuLoadProbe#setUseAverage(boolean)}. */
    public IgniteCpuLoadProbe setUseAverage(boolean useAvg) {
        impl.setUseAverage(useAvg);

        return this;
    }

    /** See {@link AdaptiveCpuLoadProbe#isUseProcessors()}. */
    public boolean isUseProcessors() {
        return impl.isUseProcessors();
    }

    /** See {@link AdaptiveCpuLoadProbe#setUseProcessors(boolean)}. */
    public IgniteCpuLoadProbe setUseProcessors(boolean useProcs) {
        impl.setUseProcessors(useProcs);

        return this;
    }

    /** See {@link AdaptiveCpuLoadProbe#getProcessorCoefficient()}. */
    public double getProcessorCoefficient() {
        return impl.getProcessorCoefficient();
    }

    /** See {@link AdaptiveCpuLoadProbe#setProcessorCoefficient(double)}. */
    public IgniteCpuLoadProbe setProcessorCoefficient(double procCoefficient) {
        impl.setProcessorCoefficient(procCoefficient);

        return this;
    }
}
