package apache.ignite.services.loadbalancing.testobjects;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Cluster under test. */
public final class TestCluster implements AutoCloseable {
    /** Nodes. */
    private List<Ignite> nodes;

    /** Creates Ignite cluster with the specified number of server nodes. */
    public TestCluster(int clusterSize) {
        nodes = IntStream.range(0, clusterSize).boxed().parallel()
            .map(i -> Ignition.start(createIgniteConfiguration(i)))
            .collect(Collectors.toList());
    }

    /** {@inheritDoc} */
    @Override public void close() {
        nodes.parallelStream().forEach(Ignite::close);
    }

    /** @return Ignite node with the specified index. */
    public Ignite ignite(int idx) {
        return nodes.get(idx);
    }

    /** */
    private IgniteConfiguration createIgniteConfiguration(int idx) {
        return new IgniteConfiguration()
            .setIgniteInstanceName("apache.ignite.services.loadbalancing.testobjects-" + idx)
            .setDiscoverySpi(
                new TcpDiscoverySpi().setIpFinder(
                    new TcpDiscoveryVmIpFinder().setAddresses(Collections.singleton("127.0.0.1:47500"))
                )
            );
    }
}
