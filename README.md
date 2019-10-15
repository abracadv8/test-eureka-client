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


![](/readme/04_eureka.png)



Excerpt of logs:

```

Connected to the target VM, address: '127.0.0.1:62139', transport: 'socket'
2019-10-15 10:51:20.872 [INFO] [main] [o.s.c.s.PostProcessorRegistrationDelegate$BeanPostProcessorChecker] 
     Bean 'org.springframework.cloud.autoconfigure.ConfigurationPropertiesRebinderAutoConfiguration' of type 
     [org.springframework.cloud.autoconfigure.ConfigurationPropertiesRebinderAutoConfiguration$$EnhancerBySpringCGLIB$$bdd9e543] 
     is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)
2019-10-15 10:51:24.135 [INFO] [main] [o.s.c.c.u.InetUtils] Cannot determine local hostname
2019-10-15 10:51:24.166 [INFO] [main] [o.s.c.n.e.InstanceInfoFactory] Setting initial instance status as: STARTING
2019-10-15 10:52:41.184 [INFO] [main] [c.n.d.DiscoveryClient] Initializing Eureka in region us-east-1
2019-10-15 10:52:41.865 [INFO] [main] [c.n.d.p.DiscoveryJerseyProvider] Using JSON encoding codec LegacyJacksonJson
2019-10-15 10:52:41.866 [INFO] [main] [c.n.d.p.DiscoveryJerseyProvider] Using JSON decoding codec LegacyJacksonJson
2019-10-15 10:52:42.093 [INFO] [main] [c.n.d.p.DiscoveryJerseyProvider] Using XML encoding codec XStreamXml
2019-10-15 10:52:42.093 [INFO] [main] [c.n.d.p.DiscoveryJerseyProvider] Using XML decoding codec XStreamXml
2019-10-15 10:52:42.492 [INFO] [main] [c.n.d.s.r.a.ConfigClusterResolver] Resolving eureka endpoints via configuration
2019-10-15 10:52:43.011 [INFO] [main] [c.n.d.DiscoveryClient] Disable delta property : false
2019-10-15 10:52:43.012 [INFO] [main] [c.n.d.DiscoveryClient] Single vip registry refresh property : null
2019-10-15 10:52:43.012 [INFO] [main] [c.n.d.DiscoveryClient] Force full registry fetch : false
2019-10-15 10:52:43.012 [INFO] [main] [c.n.d.DiscoveryClient] Application is null : false
2019-10-15 10:52:43.012 [INFO] [main] [c.n.d.DiscoveryClient] Registered Applications size is zero : true
2019-10-15 10:52:43.012 [INFO] [main] [c.n.d.DiscoveryClient] Application version is -1: true
2019-10-15 10:52:43.012 [INFO] [main] [c.n.d.DiscoveryClient] Getting all instance registry info from the eureka server
2019-10-15 10:52:43.660 [INFO] [main] [c.n.d.DiscoveryClient] The response status is 200
2019-10-15 10:52:43.667 [INFO] [main] [c.n.d.DiscoveryClient] Not registering with Eureka server per configuration
2019-10-15 10:52:43.676 [INFO] [main] [c.n.d.DiscoveryClient] Discovery Client initialized at timestamp 1571151163673 with initial instances count: 44
2019-10-15 10:52:43.741 [INFO] [main] [o.s.c.n.e.s.EurekaServiceRegistry] Registering application TEST-EUREKA-CLIENT with eureka with status UP

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.1.5.RELEASE)

2019-10-15 10:52:45.706 [INFO] [main] [c.t.Main] The following profiles are active: QA
2019-10-15 10:52:47.169 [WARN] [main] [o.s.b.a.e.EndpointId] Endpoint ID 'service-registry' contains invalid characters, please migrate to a valid format.
2019-10-15 10:52:47.566 [INFO] [main] [o.s.c.c.s.GenericScope] BeanFactory id=2eefaf6b-1d8e-3aed-a21c-22476e296c3e
2019-10-15 10:52:47.790 [INFO] [main] [o.s.c.s.PostProcessorRegistrationDelegate$BeanPostProcessorChecker] 
      Bean 'org.springframework.cloud.autoconfigure.ConfigurationPropertiesRebinderAutoConfiguration' of type 
      [org.springframework.cloud.autoconfigure.ConfigurationPropertiesRebinderAutoConfiguration$$EnhancerBySpringCGLIB$$bdd9e543] 
      is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)
2019-10-15 10:52:48.478 [INFO] [main] [o.s.b.w.e.t.TomcatWebServer] Tomcat initialized with port(s): 9447 (http)
2019-10-15 10:52:48.514 [INFO] [main] [o.a.c.h.Http11NioProtocol] Initializing ProtocolHandler ["http-nio-9447"]
2019-10-15 10:52:48.533 [INFO] [main] [o.a.c.c.StandardService] Starting service [Tomcat]
2019-10-15 10:52:48.534 [INFO] [main] [o.a.c.c.StandardEngine] Starting Servlet engine: [Apache Tomcat/9.0.19]
2019-10-15 10:52:49.491 [INFO] [main] [o.a.c.c.C.[.[.[/]] Initializing Spring embedded WebApplicationContext
2019-10-15 10:52:49.491 [INFO] [main] [o.s.w.c.ContextLoader] Root WebApplicationContext: initialization completed in 3760 ms
2019-10-15 10:52:49.799 [INFO] [main] [c.n.c.s.URLConfigurationSource] URLs to be used as dynamic configuration source: [file:/C:/Users/username/git-ghe/test-eureka-client/target/classes/config.properties]
2019-10-15 10:52:49.837 [INFO] [main] [c.n.c.DynamicPropertyFactory] DynamicPropertyFactory is initialized with configuration sources: com.netflix.config.ConcurrentCompositeConfiguration@22361e23
2019-10-15 10:52:50.736 [INFO] [main] [c.n.c.s.URLConfigurationSource] URLs to be used as dynamic configuration source: [file:/C:/Users/username/git-ghe/test-eureka-client/target/classes/config.properties]
2019-10-15 10:52:51.128 [INFO] [main] [o.s.s.c.ThreadPoolTaskExecutor] Initializing ExecutorService 'applicationTaskExecutor'
2019-10-15 10:52:53.864 [INFO] [main] [o.s.c.c.u.InetUtils] Cannot determine local hostname
2019-10-15 10:52:55.873 [INFO] [main] [o.s.c.c.u.InetUtils] Cannot determine local hostname
2019-10-15 10:52:56.010 [INFO] [main] [c.n.d.DiscoveryClient] Shutting down DiscoveryClient ...    <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< SHUTS DOWN
2019-10-15 10:52:56.026 [INFO] [main] [c.n.d.DiscoveryClient] Completed shut down of DiscoveryClient
2019-10-15 10:52:56.048 [INFO] [main] [o.s.b.a.e.w.EndpointLinksResolver] Exposing 19 endpoint(s) beneath base path '/actuator'
2019-10-15 10:52:56.328 [INFO] [main] [o.s.c.n.e.InstanceInfoFactory] Setting initial instance status as: STARTING   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< STARTS BACK UP
2019-10-15 10:53:19.028 [INFO] [main] [c.n.d.DiscoveryClient] Initializing Eureka in region us-east-1
2019-10-15 10:53:19.036 [INFO] [main] [c.n.d.p.DiscoveryJerseyProvider] Using JSON encoding codec LegacyJacksonJson
2019-10-15 10:53:19.036 [INFO] [main] [c.n.d.p.DiscoveryJerseyProvider] Using JSON decoding codec LegacyJacksonJson
2019-10-15 10:53:19.036 [INFO] [main] [c.n.d.p.DiscoveryJerseyProvider] Using XML encoding codec XStreamXml
2019-10-15 10:53:19.036 [INFO] [main] [c.n.d.p.DiscoveryJerseyProvider] Using XML decoding codec XStreamXml
2019-10-15 10:53:19.139 [INFO] [main] [c.n.d.s.r.a.ConfigClusterResolver] Resolving eureka endpoints via configuration
2019-10-15 10:53:19.141 [INFO] [main] [c.n.d.DiscoveryClient] Disable delta property : false
2019-10-15 10:53:19.141 [INFO] [main] [c.n.d.DiscoveryClient] Single vip registry refresh property : null
2019-10-15 10:53:19.141 [INFO] [main] [c.n.d.DiscoveryClient] Force full registry fetch : false
2019-10-15 10:53:19.141 [INFO] [main] [c.n.d.DiscoveryClient] Application is null : false
2019-10-15 10:53:19.141 [INFO] [main] [c.n.d.DiscoveryClient] Registered Applications size is zero : true
2019-10-15 10:53:19.141 [INFO] [main] [c.n.d.DiscoveryClient] Application version is -1: true
2019-10-15 10:53:19.141 [INFO] [main] [c.n.d.DiscoveryClient] Getting all instance registry info from the eureka server
2019-10-15 10:53:19.307 [INFO] [main] [c.n.d.DiscoveryClient] The response status is 200
2019-10-15 10:53:19.309 [INFO] [main] [c.n.d.DiscoveryClient] Not registering with Eureka server per configuration
2019-10-15 10:53:19.311 [INFO] [main] [c.n.d.DiscoveryClient] Discovery Client initialized at timestamp 1571151199310 with initial instances count: 44
2019-10-15 10:53:19.313 [INFO] [main] [o.s.c.n.e.s.EurekaServiceRegistry] Registering application TEST-EUREKA-CLIENT with eureka with status UP
2019-10-15 10:53:19.336 [INFO] [main] [o.a.c.h.Http11NioProtocol] Starting ProtocolHandler ["http-nio-9447"]
2019-10-15 10:53:19.477 [INFO] [main] [o.s.b.w.e.t.TomcatWebServer] Tomcat started on port(s): 9447 (http) with context path ''
2019-10-15 10:53:19.478 [INFO] [main] [o.s.c.n.e.s.EurekaAutoServiceRegistration] Updating port to 9447
2019-10-15 10:53:19.478 [INFO] [main] [o.s.c.n.e.s.EurekaAutoServiceRegistration] Updating port to 9447
2019-10-15 10:53:19.481 [INFO] [main] [c.t.Main] Started Main in 122.604 seconds (JVM running for 125.065)
2019-10-15 10:53:26.601 [INFO] [http-nio-9447-exec-1] [o.a.c.c.C.[.[.[/]] Initializing Spring DispatcherServlet 'dispatcherServlet'
2019-10-15 10:53:26.601 [INFO] [http-nio-9447-exec-1] [o.s.w.s.DispatcherServlet] Initializing Servlet 'dispatcherServlet'
2019-10-15 10:53:26.623 [INFO] [http-nio-9447-exec-1] [o.s.w.s.DispatcherServlet] Completed initialization in 22 ms
2019-10-15 10:58:19.144 [INFO] [AsyncResolver-bootstrap-executor-0] [c.n.d.s.r.a.ConfigClusterResolver] Resolving eureka endpoints via configuration
2019-10-15 11:03:19.146 [INFO] [AsyncResolver-bootstrap-executor-0] [c.n.d.s.r.a.ConfigClusterResolver] Resolving eureka endpoints via configuration
2019-10-15 11:08:19.148 [INFO] [AsyncResolver-bootstrap-executor-0] [c.n.d.s.r.a.ConfigClusterResolver] Resolving eureka endpoints via configuration
2019-10-15 11:13:19.153 [INFO] [AsyncResolver-bootstrap-executor-0] [c.n.d.s.r.a.ConfigClusterResolver] Resolving eureka endpoints via configuration
2019-10-15 11:18:19.157 [INFO] [AsyncResolver-bootstrap-executor-0] [c.n.d.s.r.a.ConfigClusterResolver] Resolving eureka endpoints via configuration


```
