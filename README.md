# Issue with Eureka

CloudEurekaClient is being instantiated twice.

It's causing two issues:

1) The health endpoint constantly shows `Eureka discovery client has not yet successfully connected to a Eureka server` since the first bean never gets updated with the proper refresh info
2) The list of configured services in the registry is stale after the first registration becuase subsequent refresh calls update the service list for the new bean and not the old one. 

It gets autoconfigured, registers, the rest of the Spring context and Spring Boot
starts, an auto configuration `EurekaDiscoveryClientConfigServiceAutoConfiguration.java`
immediately shuts down and then at some point a new CloudEurekaClient is initialized
and it re-registers



EurekaClientAutoConfiguration starts the initial creation of the bean. 


Initial creation of bean:

![](/readme/01a_eureka.png)


Bean created: `this = Cloud EurekaClient@4539`

![](/readme/01_eureka.png)


`EurekaDiscoveryClientConfigServiceAutoConfiguration.java` shuts that client down

A new bean created: `this = Cloud EurekaClient@8519`

![](/readme/02_eureka.png)


When calling the actuator/health endpoint, it still has a reference to the OLD/shutdown
bean (`CloudEurekaClient@4539`).  The `lastFetch` is permanently `-1` and subsequently
the health endpoint always returns
` "description": "Eureka discovery client has not yet successfully connected to a Eureka server",`

![](/readme/03_eureka.png)


The health check shows up, but it always says 
`"Eureka discovery client has not yet successfully connected to a Eureka server"`
and furthermore, the details section contains the original list of servers
will never update because the refresh updates the new bean and this one.  This
is causing the list of configured services never to be updated.


![](/readme/04eureka.png)
