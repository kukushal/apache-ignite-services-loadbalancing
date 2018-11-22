package apache.ignite.services.loadbalancing;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteServices;
import org.apache.ignite.Ignition;
import org.apache.ignite.cluster.ClusterGroup;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

/** Ignite service grid implementation of {@link ServiceLocator}. */
public class IgniteServiceLocator implements ServiceLocator {
    /** Ignite instance name. */
    private final String igniteInstanceName;

    /** Ignite. */
    private Ignite ignite;

    /** Load balancer. */
    private LoadBalancer loadBalancer;

    /** Creates an instance of {@link IgniteServiceLocator} operating from the default no-name Ignite node. */
    public IgniteServiceLocator() {
        this(null);
    }

    /**
     * Creates an instance of {@link IgniteServiceLocator} operating from Ignite node with the specified instance name.
     */
    public IgniteServiceLocator(String igniteInstanceName) {
        this.igniteInstanceName = igniteInstanceName;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override public <T> T getService(String name, Class<T> svcItf) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(svcItf);

        return (T)Proxy.newProxyInstance(
            svcItf.getClassLoader(),
            new Class[] {svcItf},
            new ServiceHandler(name, svcItf, getIgnite(), getLoadBalancer(0))
        );
    }

    /** @return Lazily initialized {@link Ignite}. */
    private Ignite getIgnite() {
        if (ignite == null)
            ignite = igniteInstanceName == null || igniteInstanceName.isEmpty() ?
                Ignition.ignite() :
                Ignition.ignite(igniteInstanceName);

        return ignite;
    }

    /** @return Lazily initialized {@link LoadBalancer}. */
    private LoadBalancer getLoadBalancer(int attempt) {
        final String LB_SVC_NAME = IgniteLoadBalancerService.class.getSimpleName();

        if (attempt > 1)
            throw new RuntimeException("Failed to deploy " + LB_SVC_NAME);

        if (loadBalancer == null) {
            Function<IgniteServices, LoadBalancer> getProxy = igniteSvcs ->
                igniteSvcs.serviceDescriptors().stream().anyMatch(s -> LB_SVC_NAME.equals(s.name())) ?
                    igniteSvcs.serviceProxy(LB_SVC_NAME, LoadBalancer.class, false) :
                    null;

            Ignite ignite = getIgnite();

            // Check load balancer on the same host first
            IgniteServices locHostSvcs = ignite.services(ignite.cluster().forHost(ignite.cluster().localNode()));
            loadBalancer = getProxy.apply(locHostSvcs);

            if (loadBalancer != null)
                return loadBalancer;

            // Check load balancer on any host
            loadBalancer = getProxy.apply(ignite.services());

            if (loadBalancer != null)
                return loadBalancer;

            // Load balancer is not deployed: deploy
            locHostSvcs.deployNodeSingleton(LB_SVC_NAME, new IgniteLoadBalancerService());

            // Load balancer has been deployed: the next attempt must succeed
            return getLoadBalancer(attempt + 1);
        }

        return loadBalancer;
    }

    /** {@link #getService(String, Class)} invocation handler. */
    private static class ServiceHandler<T> implements InvocationHandler {
        /** Ignite. */
        private final Ignite ignite;

        /** Service name. */
        private final String svcName;

        /** Load balancer. */
        private final LoadBalancer loadBalancer;

        /** Service interface. */
        private Class<T> svcItf;

        /** Constructor. */
        ServiceHandler(String svcName, Class<T> svcItf, Ignite ignite, LoadBalancer loadBalancer) {
            this.svcName = svcName;
            this.svcItf = svcItf;
            this.ignite = ignite;
            this.loadBalancer = loadBalancer;
        }

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override public T invoke(Object proxy, Method mtd, Object[] args) throws Exception {
            String nodeId = loadBalancer.getNode(svcName, mtd.getName(), args);

            ClusterGroup clusterGrp = ignite.cluster().forNodeId(UUID.fromString(nodeId));

            Object targetSvc = ignite.services(clusterGrp).serviceProxy(svcName, svcItf, false);

            Method targetMtd = targetSvc.getClass().getMethod(
                mtd.getName(),
                Stream.of(args).map(Object::getClass).toArray(Class[]::new)
            );

            return (T)targetMtd.invoke(targetSvc, args);
        }
    }
}
