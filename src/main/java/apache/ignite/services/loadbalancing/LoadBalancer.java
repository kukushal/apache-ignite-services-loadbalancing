package apache.ignite.services.loadbalancing;

/**
 * Ignite service call load balancer interface.
 */
public interface LoadBalancer {
    /**
     * @return ID of Ignite node to execute specified operation of the specified Ignite service on or {@code null} if
     * no node was found.
     */
    String getNode(String svcName, String svcOpName, Object[] args);
}