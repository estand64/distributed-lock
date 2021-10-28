# distributed-lock

For a in depth guide please go to https://tanzu.vmware.com/developer/guides/spring-integration-lock/

This code base is a quick example of how to use the distributed lock that is included in the [Spring Integration](https://spring.io/projects/spring-integration) package. The basis of this app is Spring Boot so if you are using a different DI platform you'll have to modify the setup code to your DI package's specifications, also note that there are other Spring packages at work here that may or may not need to change based upon how your app is configured. The distributed lock does require either a Spring JDBC compliant database or a Redis database to be available.
