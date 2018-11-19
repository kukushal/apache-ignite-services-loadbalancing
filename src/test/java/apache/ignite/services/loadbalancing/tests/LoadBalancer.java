package apache.ignite.services.loadbalancing.tests;

/**
 * Ignite service call load balancer interface.
 */
public interface LoadBalancer {
    /**
     * @return ID of Ignite node to execute specified operation of the specified Ignite service on.
     */
    String getNode(String svcName, String svcOpName, Object[] args);
}