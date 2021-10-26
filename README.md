# distributed-lock

How to use the spring integration distributed lock

Most of the time when designing microservices we create code that can handle multiple instances of our services, however every so often we run into a situation where we only want something processed once. Since our code is running in different pods we can't use `synchronize` and need to use an external method, this is fraught with a few pitfalls. Thankfully Spring has done the hard work and all we need is to provide it a database connection, this example will show the lock with both Redis and JDBC.

## Package Imports

First we need import the necessary packages most of which are in the Spring Integration package, what else you'll need depends on whether you use the Redis or JDBC version.

##### All versions will need this:

```
<dependency\>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-integration</artifactId>
</dependency>
```

---

##### Needed if you want to run the Redis version (please note that if you want to run Jedis or Lettuce is up to you, Spring doesn't care which):

```
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.integration</groupId>
	<artifactId>spring-integration-redis</artifactId>
</dependency>
<dependency>
	<groupId>io.lettuce</groupId>
	<artifactId>lettuce-core</artifactId>
	<version>6.1.5.RELEASE</version>
</dependency>
```

---

##### If you want to run the JDBC implmentation here are the packages you'll need (again I'm using Postgres here, however there are many Relational Databases that Spring supports):

```
<dependency>
	<groupId>org.springframework.integration</groupId>
	<artifactId>spring-integration-jdbc</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
	<groupId>org.postgresql</groupId>
	<artifactId>postgresql</artifactId>
</dependency>
```

One more note about running the JDBC version of the distrubited lock is that it needs the database to have some tables and indexes set up before hand in order to work. If you don't the first time you attempt to obtain the lock a JDBC Exception will be thrown. The current collection of sql files for this can be found in the [Spring Integration JDBC github repo](https://github.com/spring-projects/spring-integration/tree/e901c89fef3eea00ddf6d503ae9926667a1d6972/spring-integration-jdbc/src/main/resources/org/springframework/integration/jdbc). In the example here I'm using [Flyway](https://flywaydb.org/) to run this script automatically, if you want to use this you'll also need to add the following dependency:

```
<dependency>
	<groupId>org.flywaydb</groupId>
	<artifactId>flyway-core</artifactId>
</dependency>
```

## Create Lock Repository

Once the nessacery packages are import you can get started on setting up you code, first order of business is creating the lock repository beans that will be used to grab the locks later. The first step of this is to create a lock repository name, you'll need to do this for every repository you want to create (this however is likely going to be only one). Just as a note I have the `@Profile` annotation list here for this code base to work, it is unnecessary when using a single LockRepository.

```
private static final String LOCK_NAME = "lock";
```

Then create the repository bean you need.

---

##### Redis lock repository setup:

```
@Bean(destroyMethod = "destroy")
@Profile("redis")
public RedisLockRegistry redisLockRegistry(RedisConnectionFactory redisConnectionFactory) {
    return new RedisLockRegistry(redisConnectionFactory, LOCK_NAME);
}
```

---

##### JDBC lock repository setup:

```
@Bean
@Profile("jdbc")
public DefaultLockRepository DefaultLockRepository(DataSource dataSource){
    return new DefaultLockRepository(dataSource);
}

@Bean
@Profile("jdbc")
public JdbcLockRegistry jdbcLockRegistry(LockRepository lockRepository){
    return new JdbcLockRegistry(lockRepository);
}
```

## Obtain a lock from the repository

There's a bit of code involved here but not much that's really logically happening, most of the code is needed for error cases.

```
private static final String MY_LOCK_KEY = "someLockKey";

private <T> T lock(LockRegistry lockRegistry, LockableActions<T> lockableActions, Time waitTime){
    Lock lock = null;
    try {
        lock = lockRegistry.obtain(MY_LOCK_KEY);
    } catch (Exception e){
        return lockableActions.onError.apply(e);
    }

    T successful;
    try{
        waitTime = waitTime == null ? DEFAULT_WAIT : waitTime;
        successful = lock.tryLock(waitTime.duration, waitTime.timeUnit) ? lockableActions.onSuccess().get() : lockableActions.onFailure().get();
    } catch (Exception e){
        successful = lockableActions.onError().apply(e);
    } finally {
        try {
            lock.unlock();
        }catch (Exception e){}
    }
    return successful;
}
```

Let's go over the key parts here.

1. `lock = lockRegistry.obtain(MY_LOCK_KEY);` This is just getting the specific lock from the database. The documentation for the registry interface list the key as an `Object` but most all of the implementations require a `String`. This also makes it easy to add an identifier to the key though in case you only care about other instances possibly running the same processing for a specific item.
2. `lock.tryLock(waitTime.duration, waitTime.timeUnit)` This is where the magic happens and a lock object is created for us hopefully. There are two versions of this method, the empty parameter one which trys once to grab the lock, or in this case a method that take a timeout and will for that time deration attempt to lock the lock every 100 milliseconds. Both of these methods return `true` if the lock was able to be locked, otherwise it will return `false`
3. `finally {....lock.unlock();)...` Being good programmers we also need to make sure that if anything happens while we are processing something inside of locked block that we always unlock it to prevent a deadlock.

And those are the basics of using the Spring Distributed lock.
