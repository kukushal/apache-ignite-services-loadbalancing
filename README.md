# apache-ignite-services-loadbalancing

Apache Ignite service calls load balancing

## Motivation

Unlike Apache Ignite Compute Grid providing [multiple load balancing policies out of the box](https://apacheignite.readme.io/docs/load-balancing])
and allowing [plugable custom load balancing policies](https://ignite.apache.org/releases/latest/javadoc/org/apache/ignite/spi/loadbalancing/LoadBalancingSpi.html),
the Ignite Service Grid [describes Load Balancing](https://apacheignite.readme.io/docs/service-grid#section-load-balancing) 
in terms of deployment as: 

> Ignite will automatically make sure that about an equal number of services are deployed on each node within the cluster.

If we consider load balancing as distributing service calls among deployed Ignite services, then Ignite provides
only two policies with [Sticky vs Not-Sticky Proxies](https://apacheignite.readme.io/docs/service-example#section-sticky-vs-not-sticky-proxies).
The `sticky` proxy directs all the service calls to the same service instance and the `non-sticky` proxy uses a random
load balancing to distribute the service calls among the service instances.

A custom load balancing strategy can be implemented on top of the Ignite services API by deploying a service on 
every node (see [Node Singleton](https://apacheignite.readme.io/docs/cluster-singletons#node-singleton)), evaluating 
a target node before every service call and creating a stick service proxy to that specific node.

Ignite .NET-Java services interoperability is another thing to consider: the custom load balancing implementation is
duplicated in .NET in Java.

The goal of this component is to provide Ignite service calls load balancing framework:
* Having out of the box implementation of popular load balancing policies like round robin, adaptive and affinity.
* Extensible with custom policies.
* Single strategy implementation in Java available from .NET.