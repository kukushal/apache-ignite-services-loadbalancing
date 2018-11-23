package apache.ignite.services.loadbalancing;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicLong;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Round-robin implementation of {@link LoadBalancingStrategy} for Ignite. */
public class IgniteRoundRobinLoadBalancing implements IgniteLoadBalancingStrategy {
    /** Node index. */
    private IgniteAtomicLong nodeIdx;

    /** Ignite. */
    private Ignite ignite;

    /** {@inheritDoc} */
    @Override public Collection<String> selectNodes(Collection<String> nodesIds, Object[] args) {
        List<String> sortedNodeIds = new ArrayList<>(nodesIds);
        sortedNodeIds.sort(Comparator.comparing(id -> id));

        if (nodeIdx == null)
            nodeIdx = ignite.atomicLong(IgniteRoundRobinLoadBalancing.class.getName(), -1, true);

        int i = (int)nodeIdx.incrementAndGet();

        if (i >= sortedNodeIds.size()) {
            i = 0;
            nodeIdx.getAndSet(0);
        }

        return Collections.singleton(sortedNodeIds.get(i));
    }

    /** {@inheritDoc} */
    @Override public void setIgnite(Ignite ignite) {
        this.ignite = ignite;
    }
}
