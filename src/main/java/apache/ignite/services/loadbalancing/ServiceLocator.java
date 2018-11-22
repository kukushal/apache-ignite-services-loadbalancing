package apache.ignite.services.loadbalancing;

/** Interface to locate services. */
public interface ServiceLocator {
    /** @return Proxy of a service with the specified name and interface. */
    <T> T getService(String name, Class<T> svcItf);
}
