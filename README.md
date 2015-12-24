# Thyme Framework

## Introduction

Thyme is a lightweight and, above all, practical web-application framework for Java. The framework is open source and free. It is available under [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

Even though Thyme is a general purpose web-application framework, when we developed it we had a certain type of web-application in mind. Thyme is an application that faces the general public on the Internet, allows users to sign in, and provides certain services to registered, authenticated users as well as certain limited services to anonymous, unauthenticated visitors. The application data is mainly stored on the back-end in a relational database. Also, the application can connect to other third-party services on the back-end to provide some of its functionality.

The framework is not attempting to address all possible types of web-applications, so it is accepted that there are some applications out there for which the Thyme framework is not the best choice. However, a reasonable level of specialization allows the framework to be clean, lean, efficient, and easy to explore and understand.

* [API Reference](http://www.boylesoftware.com/thyme/site/apidocs)
* [Maven Generated Project Information](http://www.boylesoftware.com/thyme/site)
* [Project on GitHub](https://github.com/boylesoftware/thyme)

Here are some of the distinctive features of the framework:

 * **Works in a Java Servlet container**

	Java Servlet containers have had a lot of time to evolve and, even though from the developer's perspective the Servlet APIs are still rather inconvenient (hence the need for a framework), from the deployment and maintenance point of view they offer a well established, tested, mature solution.

	The Thyme framework does not need a full-featured Java EE platform to run. It can run under a simple Servlet container, such as [Apache Tomcat](http://tomcat.apache.org/) or [Jetty](http://www.eclipse.org/jetty/).

* **Provides Java API and development environment**

	The Thyme framework is written in Java and provides Java API to custom applications. Even though there are some other excellent programming languages available for web-application development, including those based on the JVM technology, Java remains the most practical choice, especially when it comes to finding qualified developers for your project that can develop stable, high quality code.

* **Minimal footprint**

	The Thyme framework attempts to be as lean as possible. There are two aspects to the framework's minimal footprint:

	* *The framework's dependencies:* There are frameworks that pull megabytes of dependencies with them making the resulting web-applications unnecessarily huge. Thyme, on the other hand, is rather small itself and its only required dependency is the Apache Commons Logging library that it uses for debug logging. Because the resulting web-application is smaller, its deployments are faster and easier.

	* *JVM runtime memory usage:* A significant effort has been undertaken to give the framework a minimal memory footprint, thereby giving the custom application more room. Internally, instead of allocating new objects and and letting the garbage collector handle it, the framework wherever possible uses fast object instance pooling, re-using allocated objects for processing request after request. It also offers object pooling tools to the custom application, if it chooses to use them.

* **Uses JPA to access the database**

	There are several persistent storage approaches available to web-applications these days. The Thyme framework, however, is targeted at those applications that use a SQL database as the primary, persistent storage. This choice limits the number of applicable use-cases, but makes things significantly simpler to the applications that do use a SQL database for the back-end - and that is the goal. To access the database, Thyme uses JPA. There are several efficient, mature, and stable JPA implementations available, including [Hibernate](http://www.hibernate.org/), [EclipseLink](http://www.eclipse.org/eclipselink/) and [Apache OpenJPA](http://openjpa.apache.org/).

* **Asynchronous request processing**

	The Servlet API, starting with version 3.0, offers asynchronous request processing. Thyme uses it transparently for the custom application so that application developers do not have to deal with the complexities of the asynchronous Servlet API. All requests that need access to the back-end systems, including the database, are automatically processed asynchronously using a special configurable thread pool. This frees up the Servlet container's threads used to accept client connections, making the web-application more stable and scalable.

* **Utilizes latest Java technologies**

	Thyme is a new framework and it does not have to carry any legacy code to maintain compatibility with older versions. Thyme uses Java 7 and the Servlet API 3.1. At the moment, the latter has not yet been widely implemented. Apache offers Tomcat 8, which is still in its alpha stage and is unstable. Luckily, the framework has been tested on Tomcat 7 as well, and it works out of the box without any problems. When Tomcat 8 is finally released, it should offer significant performance improvements, particularly in the asynchronous, non-blocking request processing utilized by the framework.

* **Practical in development and production environments**

	The Thyme framework is the result of years of experience in developing Java web-applications for all kinds of industries and purposes. Boyle Software's core business is consulting, which has exposed us to many different client requirements, infrastructure set-ups, project scales, and modes of operation. Thyme's main goal is to be practical and efficient.

## Project Setup

### Download

You can download the Thyme framework jar from our [Maven repository](http://www.boylesoftware.com/maven/repo-os/com/boylesoftware/thyme/). It is a single jar that you need to place in your web-application's */WEB-INF/lib* directory. There are two dependencies that you need to download and place there as well: [Apache Commons Logging](http://commons.apache.org/proper/commons-logging/) and [ANTLR Java runtime binary](http://www.antlr.org/download.html).

### Maven

If your project uses Maven, here is the dependency: (NOTE: Replace the version below - 1.0.0 -  with the latest available.)

```xml
<repository>
    <id>boylesoftware-os</id>
    <url>http://www.boylesoftware.com/maven/repo-os</url>
</repository>

...

<dependency>
    <groupId>com.boylesoftware.thyme</groupId>
    <artifactId>thyme</artifactId>
    <version>1.0.0</version>
</dependency>
```

You will also have to provide your application with a JPA and Bean Validation framework implementations. Below are some examples of Maven project configurations. Note that these are only examples. Check for newer versions before use.

#### JPA Implementation

##### Hibernate

```xml
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-entitymanager</artifactId>
    <version>4.3.0.Beta3</version>
    <scope>runtime</scope>
</dependency>
```

Note that Hibernate supports JPA version 2.1 only starting from version 4.3.0.

See http://www.hibernate.org/.

##### EclipseLink

```xml
<dependency>
    <groupId>org.eclipse.persistence</groupId>
    <artifactId>org.eclipse.persistence.jpa</artifactId>
    <version>2.5.0</version>
    <scope>runtime</scope>
</dependency>
```

See http://www.eclipse.org/eclipselink/.

##### Apache OpenJPA

```xml
<build>

    ...

    <plugins>

        ...

        <plugin>
            <groupId>org.apache.openjpa</groupId>
            <artifactId>openjpa-maven-plugin</artifactId>
            <version>2.2.2</version>
            <configuration>
                <includes>**/entities/*.class</includes>
            </configuration>
            <executions>
                <execution>
                    <phase>process-classes</phase>
                    <goals>
                        <goal>enhance</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>

        ...

    </plugins>

    ...

</build>

...

<dependencies>

    ...

    <dependency>
        <groupId>org.apache.openjpa</groupId>
        <artifactId>openjpa</artifactId>
        <version>2.2.2</version>
        <scope>runtime</scope>
    </dependency>

    ...

</dependencies>
```

This example includes the build time entity enhancement (see http://openjpa.apache.org/entity-enhancement.html).

See http://openjpa.apache.org/.

#### Bean Validation Implementation

##### Hibernate

A standard implementation is provided by Hibernate. See http://www.hibernate.org/subprojects/validator.html.

Here is a Maven dependency example:

```xml
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-validator</artifactId>
    <version>5.0.1.Final</version>
    <scope>runtime</scope>
</dependency>
```

This implementation includes some useful, non-standard validation constraints. If your beans use those, change the dependency's scope from "runtime" to "compile."

### Servlet Container

A web-application that uses Thyme runs under a Servlet container. Thyme encourages writing your application in such a way that a single, compiled binary of your web-application can be deployed in different environments, such as development, QA, production, etc. This means all the environment-specific configurations must be provided to the application by the container. In the Servlet container's world, the most fitting approach to providing an environment-specific configuration is via JNDI.

The framework itself uses several configuration entries, some of which are required.

#### Ports

The framework needs to know to which port(s) the application is listening for requests. The ports are used to generate appropriate URLs for the application pages.

* **httpPort** *(required)*

	This is the port through which the application accepts plain HTTP requests.

* **httpsPort** *(required)*

	This is the port through which the application accepts secure HTTPS requests.

First, the environment's dependencies must be declared in the application's deployment descriptor *web.xml*. For example:

```xml
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
    version="3.1">

    ...

    <env-entry>
        <env-entry-name>httpPort</env-entry-name>
        <env-entry-type>java.lang.Integer</env-entry-type>
        <env-entry-value>MUST BE SET</env-entry-value>
    </env-entry>
    <env-entry>
        <env-entry-name>httpsPort</env-entry-name>
        <env-entry-type>java.lang.Integer</env-entry-type>
        <env-entry-value>MUST BE SET</env-entry-value>
    </env-entry>

    ...

</web-app>
```

Note that we intentionally use invalid values in the *web.xml* to force the deployer to provide the values in the container's configuration.

Different containers use different ways to bind values to JNDI environment entries. For example, if you use [Apache Tomcat](http://tomcat.apache.org/), the values can be specified in the application's context XML file:

```xml
<Context>

    ...

    <Environment name="httpPort"
        value="80"
        type="java.lang.Integer"
        override="false"/>
    <Environment name="httpsPort"
        value="443"
        type="java.lang.Integer"
        override="false"/>

    ...

</Context>
```

See http://tomcat.apache.org/tomcat-8.0-doc/config/context.html#Environment_Entries for details.

Here is a similar example for [Jetty](http://www.eclipse.org/jetty/):

```xml
<Configure id="wac" class="org.eclipse.jetty.webapp.WebAppContext">

    ...

    <New class="org.eclipse.jetty.plus.jndi.EnvEntry">
        <Arg><Ref refid="wac"/></Arg>
        <Arg>httpPort</Arg>
        <Arg type="java.lang.Integer">80</Arg>
        <Arg type="boolean">true</Arg>
    </New>
    <New class="org.eclipse.jetty.plus.jndi.EnvEntry">
        <Arg><Ref refid="wac"/></Arg>
        <Arg>httpsPort</Arg>
        <Arg type="java.lang.Integer">443</Arg>
        <Arg type="boolean">true</Arg>
    </New>

    ...

</Configure>
```

See http://www.eclipse.org/jetty/documentation/current/jndi-configuration.html#configuring-jndi-env-entries for details.

#### The Database

Thyme uses [JPA 2.1](http://jcp.org/en/jsr/detail?id=338) (currently, [JPA 2.0](http://jcp.org/en/jsr/detail?id=317) is also supported) for back-end database access, which needs to be configured for your application. First, you need to configure the persistence unit by placing the *persistence.xml* file in your application's *META-INF* directory. Here is an example:

```xml
<persistence xmlns="http://xmlns.jcp.org/xml/ns/persistence"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_1.xsd"
    version="2.1">
    <persistence-unit name="pu" transaction-type="RESOURCE_LOCAL">
        <non-jta-data-source>java:comp/env/jdbc/myDS</non-jta-data-source>

        ...

    </persistence-unit>
</persistence>
```

Note, that the framework manages database transactions on its own and does not use [JTA](http://jcp.org/en/jsr/detail?id=907).

The example above refers to a JNDI datasource, which also needs to be provided. In the application's *web.xml* we declare the required reference:

```xml
<resource-ref>
    <res-ref-name>jdbc/myDS</res-ref-name>
    <res-type>javax.sql.DataSource</res-type>
    <res-auth>Container</res-auth>
    <res-sharing-scope>Shareable</res-sharing-scope>
</resource-ref>
```

And then the datasource needs to be provided by the container. See http://tomcat.apache.org/tomcat-8.0-doc/jndi-resources-howto.html#JDBC_Data_Sources and http://tomcat.apache.org/tomcat-8.0-doc/jndi-datasource-examples-howto.html for Apache Tomcat configuration and http://www.eclipse.org/jetty/documentation/current/jndi-configuration.html#configuring-datasources for Jetty.

#### JavaMail

If your application sends e-mails, Thyme can provide it with a [JavaMail](http://jcp.org/en/jsr/detail?id=919) session. This dependency is optional. If your application does not need it, Thyme will work without a JavaMail session configured in the JNDI. Otherwise, first you declare the dependency in the *web.xml*:

```xml
<resource-ref>
    <res-ref-name>mail/session</res-ref-name>
    <res-type>javax.mail.Session</res-type>
    <res-auth>Container</res-auth>
</resource-ref>
```

And then you configure the session in your container. See http://tomcat.apache.org/tomcat-8.0-doc/jndi-resources-howto.html#JavaMail_Sessions for Apache Tomcat and http://www.eclipse.org/jetty/documentation/current/jndi-configuration.html#configuring-mail-with-jndi for Jetty.

#### Development Environment

During application development it is important to make it easy to view and test modifications quickly. In the past, Servlet containers were not very good at this. However, these days those issues have been addressed. All modern Servlet container implementations support automatic and manual application reload upon changes in the classes, libraries, and configuration. The JSPs, if used, are recompiled on the fly. In addition to this, containers include some features and tools that allow running your application from your project source tree without fully assembling it each time you change the source. For Apache Tomcat see http://tomcat.apache.org/tomcat-8.0-doc/config/resources.html (or http://tomcat.apache.org/tomcat-7.0-doc/config/context.html#Virtual_webapp for version 7). For Jetty, see the Jetty Maven plugin: http://www.eclipse.org/jetty/documentation/current/maven-and-jetty.html.

## Application Development

In Thyme, there are four major types of objects that your custom application needs to implement its logic:

* The application class, which represents your custom web-application and is responsible for the application start up, shut down, configuration, and access to the used APIs and services.
* The JPA entities, which represent the persistent entities your application stores in its databases.
* User input beans, which represent data entered by users and passed to the controllers for processing. Normally, the data is entered using HTML forms.
* The controllers, which provide custom application logic behind the resources made available by your application at various URIs. This is where the most of the application logic is implemented.

In addition to these four objects, the application also includes various configuration files (such as *web.xml* and *persistence.xml*), message resources (such as *ValidationMessages.properties*), and view templates. The view templates may be JSPs, but the framework allows using other view templating technologies, such as [FreeMarker](http://freemarker.org/).

### The Application

The first step when you develop a Thyme application is to define your custom application class. You do so by extending the abstract `com.boylesoftware.web.AbstractWebApplication` class provided by the framework. The class must be registered as a Servlet context listener either in the *web.xml* or using `@WebListener` annotation:

```java
package my.app.web;

import javax.servlet.annotation.WebListener;
import com.boylesoftware.web.AbstractWebApplication;

@WebListener
public class MyApplication extends AbstractWebApplication {

    ...
}
```

Or in *web.xml*:

```xml
<listener>
    <listener-class>my.app.web.MyApplication</listener-class>
</listener>
```

Being a Servlet context listener allows the application object to perform the application initialization and shutdown according to the deployed web-application life-cycle. It also allows it to use `@Resource` annotated members to easily get access to any custom configuration objects from the JNDI.

Be aware that Jetty has an issue with injecting resources into listeners that are installed using the `@WebListener` annotation. Unless the listener is installed using *web.xml*, Jetty does not inject resources in the fields annotated with `@Resource`. Our interpretation of this behavior is that it is a bug, so it may be fixed in future versions of Jetty.

#### Initialization and Shutdown

`AbstractWebApplication` offers two extension points to give the application a chance to perform custom initialization and shutdown logic:

* `init()` is called immediately after the framework initialization and before the application starts responding to requests.
* `destroy()` is called after the framework stops responding to new requests and all pending requests have been processed, but before the framework executes its own shutdown logic.

#### Configuration

The application object provides configuration for the rest of the application. There are two types of configuration: First, there is configuration used by the application custom code. This configuration is part of the API that the application provides to its controllers. Second, there are configuration properties used to customize the behavior of the framework components. Those properties are part of the framework's SPI for various framework component implementations.

##### Custom Configuration Elements for the Application

Since the application object is easily available anywhere in the application code (such as in the controllers), the best way to provide the application code with additional configuration is to define corresponding "get" methods on the application class itself. The initialization of the values behind those "get" methods can be performed in the overridden `init()` method.

For example, let's consider a case where your application needs a secret key for the symmetric encryption of data and the key must be provided by the application's environment. The key can be a JNDI environment entry, represented by a string in hexadecimal encoding. Your custom application class may look like this:

```java
@WebListener
public class MyApplication extends AbstractWebApplication {

    ...

    // the secret key as a hexadecimal string from the JNDI
    @Resource(name="secretKey")
    private String secretKeyStr;

    // the secret key
    private Key secretKey;

    ...

    @Override
    protected void init() {

        this.secretKey = new SecretKeySpec(Hex.decode(this.secretKeyStr), "AES");
    }

    ...

    /**
     * Get application secret key for symmetrical cryptography.
     *
     * @return The secret key.
     */
    public Key getSecretKey() {

        return this.secretKey;
    }

    ...
}
```

The JNDI environment entry needs to be declared in the application's *web.xml*:

```xml
<env-entry>
    <env-entry-name>secretKey</env-entry-name>
    <env-entry-type>java.lang.String</env-entry-type>
    <env-entry-value>MUST BE SET</env-entry-value>
</env-entry>
```

This way the `getSecretKey()` method can be called on the `MyApplication` object anywhere that the key is needed.

##### Configuration Properties for the Framework Components

The configuration for the framework's components must be provided in a different way since the components do not know anything about your custom application subclass. The configuration properties for the components are made available via the `AbstractWebApplication`'s `getConfigProperty()` method. The method is actually a part of the `com.boylesoftware.web.ApplicationConfiguration` interface, which `AbstractWebApplication` implements.

The configuration properties are identified by property names. Framework components use their own specific property names. Some of the standard property names, however, can be found among string constants declared in the `ApplicationConfiguration` interface. For other properties, you must see the specific components' documentation.

The place to customize application configuration properties is the `AbstractWebApplication`'s `configure()` method, which can be overridden in the custom subclass. For example, the default implementation of the `AbstractWebApplication`'s `getEntityManagerFactory()` method, which provides access to the JPA persistence unit, uses the `ApplicationConfiguration.PU_NAME` configuration property for the persistence unit name. There is a default value for the persistence unit name, but if the application needs a different name, it can override the `configure()` method this way:

```java
@WebListener
public class MyApplication extends AbstractWebApplication {

    ...

    @Override
    protected void configure(Map<String, Object> config) {

        config.put(ApplicationConfiguration.PU_NAME, "MyPersistenceUnit");
    }

    ...
}
```

The overridden `configure()` method can also read the configuration from an external file and load it into the provided configuration map.

#### APIs and Services

The same way the application object provides configuration, it manages and provides access to other APIs and services used by the framework components and application custom code. If the application uses a service, which is not provided by the framework out of the box, it can perform service initialization in the overridden `init()` method, service shutdown in the `destroy()` method, and it can define a public method or methods that give the application code access to the service.

The application object is also responsible for providing a number of standard services used by the framework. Each such service is initialized by a protected "get" method on the `AbstractWebApplication` class. The methods are called during the application initialization before the custom `init()` method is called. Each method has a default implementation, but can be overridden in the custom application subclass. See examples below.

##### Custom Validation Messages Resource Bundle

The `AbstractWebApplication`'s `getValidatorFactory()` method is responsible for creating the validator factory used, in particular, for user input beans validation. By default, according to the Bean Validation specification, the validation error messages are taken from the *ValidationMessages* resource bundle in the root of the application's classpath. Often, that location is inconvenient and needs to be customized.

The application can override the `getValidatorFactory()` method and perform the complete validation framework initialization and configuration on its own. However, if only the location of the messages resource bundle needs to be changed, the `getValidatorMessageInterpolator()` method, which is called from the default implementation of the `getValidatorFactory()` method, can be overridden instead. For an application using Hibernate Validator, below is an exzample of how to customize the location of the messages resource bundle:

```java
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;

@WebListener
public class MyApplication extends AbstractWebApplication {

    ...

    @Override
    protected MessageInterpolator getValidatorMessageInterpolator(ServletContext sc,
            ApplicationConfiguration config) {

        return new ResourceBundleMessageInterpolator(
            new PlatformResourceBundleLocator("com.boylesoftware.talkbuilder.resources.Messages"));
    }

    ...
}
```

The `ResourceBundleMessageInterpolator` and `PlatformResourceBundleLocator` classes are specific to the Hibernate implementation.

##### Custom User Locale Finder

To create an internationalized application, you must associate a specific locale with each request so that you can serve the view correctly localized. The framework's `com.boylesoftware.web.spi.UserLocaleFinder` component is responsible for finding the locale for each request.

The Servlet API gives us the `ServletRequest.getLocale()` method, which determines the locale based on the meta-data provided with the request by the user's browser. The default `UserLocaleFinder` simply uses this method. But what if the user's language is part of the user profile? In that case, your application must provide a custom `UserLocaleFinder` implementation. Here is an example:

```java
@WebListener
public class MyApplication extends AbstractWebApplication {

    ...

    @Override
    protected UserLocaleFinder<User> getUserLocaleFinder(ServletContext sc,
            ApplicationConfiguration config) {

        return new UserLocaleFinder<User>() {

            @Override
            public Locale getLocale(HttpServletRequest request, User user) {

                if (user != null) {
                    String lang = user.getLanguage();
                    if (lang != null)
                        return Locale.forLanguageTag(lang);
                }

                return request.getLocale();
            }
        };
    }

    ...
}
```

The code above assumes that the application uses the entity class `User` to represent user profiles and that the class has the `getLanguage()` method that returns an optional user-preferred language from the profile.

#### User Authentication Service

User authentication service is used to associate a certain registered user with a request. It is also used to establish and break authenticated sessions that attribute all requests to the same user between the user login and either logout or session expiration. Usually, such association is implemented using HTTP cookies.

The default implementation of the `AbstractWebApplication`'s `getAuthenticationService()` method returns a `com.boylesoftware.web.impl.auth.SessionlessAuthenticationService` instance, which is the most commonly used authentication service implementation provided by the framework. This implementation does not rely on the Servlet specification's `HttpSession` functionality and is completely stateless, which makes applications that use it able to work in clusters of servers without any special setup such as "sticky" sessions. The special encrypted HTTP cookie contains the authenticated user's id, which allows the framework to find the user record each time it receives a request.

The `SessionlessAuthenticationService` is an authentication service implementation that assumes that the application keeps registered user account records in its database. This type of authentication service implementation is characterized by the `getAuthenticator()` method returning an implementation of the `com.boylesoftware.web.api.UserRecordAuthenticator` interface, which is an extension of the basic `com.boylesoftware.web.api.Authenticator` interface.

User records are specific to the application, so to access them the user record authentication service uses a custom `com.boylesoftware.web.spi.UserRecordHandler` implementation provided by the application. The `AbstractWebApplication`'s `getUserRecordHandler()` method must be overridden for this to work correctly. Here is an example:

```java
@WebListener
public class MyApplication extends AbstractWebApplication {
 
    ...

    @Override
    protected UserRecordHandler<User> getUserRecordHandler(ServletContext sc,
            ApplicationConfiguration config) {

        return new AbstractUserRecordHandler<User>(User.class) {

            @Override
            public User getUserByLoginNameAndPassword(EntityManager em, String loginName, String password) {

                try {

                    return em
                        .createNamedQuery("User.findByEmailAndPasswordDigest", User.class)
                        .setParameter("email", loginName.toLowerCase())
                        .setParameter("passwordDigest", this.digestPassword(password, "SHA-1"))
                        .getSingleResult();

                } catch (NoResultException e) {
                    return null;
                }
            }

            @Override
            public int getUserId(User user) {

                return user.getId();
            }

            @Override
            public int getUserSalt(User user) {

                return user.getSalt();
            }
        };
    }

    ...
}
```

The example above uses a convenient `AbstractUserRecordHandler` provided by the framework as the base class for the custom user record handler implementation.

The corresponding user account record class might look like the following:

```java
@Entity
@NamedQueries({
    @NamedQuery(name="User.findByEmailAndPasswordDigest",
        query="SELECT u FROM User u WHERE u.email = :email" +
                    " AND u.passwordDigest = :passwordDigest")
})
public class User {

	// user id
	@Id
	@GeneratedValue
	private int id;

	// user secret "salt"
	@Column(nullable=false)
	private int salt;

	// user e-mail address
	@Column(length=50, nullable=false, unique=true, updatable=false)
	private String email;

	// SHA-1 digest of the user password as a hexadecimal string
	@Column(length=40, nullable=false)
	private String passwordDigest;

	// other properties, getters and setters
	...
}
```

The class above uses user's e-mail address as the login name. The secret "salt" field is used for additional security of the encrypted authentication cookie. It is a random number, unknown to the user, associated once with the user account and included in the authentication cookie. Each time the cookie is decrypted, the "salt" value is matched against the one associated with the user account.

Note, that `SessionlessAuthenticationService` requires a 128-bit AES secret key in the JNDI under "java:comp/env/secretKey". The value must be a string in hexadecimal encoding.

If the application does not require user authentication, there is a special NOP authentication service implementation included in the framework. It can be used this way:

```java
@WebListener
public class MyApplication extends AbstractWebApplication {
 
    ...

    @Override
    protected AuthenticationService<?> getAuthenticationService(ServletContext sc, ApplicationConfiguration config) {

        return new NopAuthenticationService();
    }

    ...
}
```

This authentication service reports to the rest of the framework that all requests are unauthenticated.

#### User Records Cache

If the application uses a user record based authentication service, such as the default `SessionlessAuthenticationService`, each time a new request is received the authentication service must look up the corresponding user record in the database. To improve performance, the authentication service can use a cache. The default implementation of `AbstractWebApplication`'s `getUserRecordsCache()` method returns a stub cache implementation that does not do any caching. This is the safest "cache" implementation and that is why it is used as the default. There are several other implementations available in the `com.boylesoftware.web.impl.auth` package. Note that as soon as the application moves to a clustered environment, special care must be taken about persistent records caching. Not all cache implementations are suitable for distributed environments since not all implementations provide functionality for synchronizing cache instances.

Because of the user record caching, the `com.boylesoftware.web.api.Authenticator` API includes methods that invalidate cached user records. Controllers that modify user records, especially data that affects authentication and authorization, must use those methods to notify the cache about the changes.

### Router Configuration

The router is the core component of Thyme framework: it maps incoming request URIs to the corresponding request processing logic. The idea behind the framework is that there is a *resource* behind each URI and the application provides up to three HTTP methods to work with the resource:

* The "GET" method requests a representation of the resource, such as an HTML view.
* The "POST" allows modification of the resource. Normally, there is special object called *user input bean* attached to the "POST" request. The user input bean encapsulates data entered by the user, usually using an HTML form on the resource's view. The user input bean is a subject to validation before it can be processed by the controller.
* The "DELETE" method requests to delete the resource. Since HTML forms, unfortunately, do not support the "DELETE" method, some applications may choose to consider resource deletion a "modification" and use the "POST" method. Otherwise, a "DELETE" request can be sent from the browser using the `XMLHttpRequest` object.

#### Router Filter

The router is represented by the `com.boylesoftware.web.RouterFilter` class and installed in the web-application as a filter. Normally, unless your web-application's *web.xml* has the `metadata-complete="true"` attribute on the `web-app` element, the filter is installed automatically and applied to all incoming requests thanks to the `@WebFilter` annotation in the class:

```java
@WebFilter(
    filterName="RouterFilter",
    asyncSupported=true,
    dispatcherTypes={ DispatcherType.REQUEST, DispatcherType.ASYNC },
    urlPatterns={ "/*" }
)
public class RouterFilter implements Filter {

    ...
}
```

Sometimes, the custom application may use some other filters and it becomes important in what order those filters are invoked. The annotations do not allow such ordering, so in that case the application needs to define the filter chain in its *web.xml*, which overrides the annotations:

```xml
<filter>
    <filter-name>RouterFilter</filter-name>
    <filter-class>com.boylesoftware.web.RouterFilter</filter-class>
    <async-supported>true</async-supported>
</filter>
<filter-mapping>
    <filter-name>RouterFilter</filter-name>
    <url-pattern>/*</url-pattern>
    <dispatcher>REQUEST</dispatcher>
    <dispatcher>ASYNC</dispatcher>
</filter-mapping>
```

NOTE: It is important that the filter is defined to support asynchronous mode (because it uses it) and that it is associated with two dispatchers: "REQUEST" and "ASYNC."

If the application does not configure a route for a certain URI, the router filter simply passes the request down the chain, which allows it to be transparent while being mapped to all URIs. The filter intercepts only those requests for which it can find a configured route.

#### Route Mappings

A route mapping is a configuration component that maps a specified URI pattern to the request processing logic. The logic is defined as a collection of components of several types which are used during various phases of the request processing. The components include:

* The optional *route script* is a piece of logic executed by the framework each time it receives a matching request. The script is executed in a separate thread used to process the request, and in a JPA transaction which spans the whole request processing iteration. The purpose of the script is to preload referred entities from the database and save them in the request attributes for further use in the controller and the view. This script can also have user permission verification logic.
* The optional *controller* contains the main request processing logic for those of the three HTTP methods that are applicable. The controller is also executed in the asynchronous request processing thread and within the request processing JPA transaction. The controller may also have an optional view preparation method, which is called whenever the view is about to be sent back to the client. The method may prepare some data needed by the view and place it in the request attributes.
* The optional *view script* is a piece of logic executed by the framework before it sends the view to the client as a response to a request. The script can be used to prepare any objects used by the view. It is executed in the same asynchronous thread and is included in the transaction.
* The *view* is used to represent the resource. Normally, the view is a template, such as a JSP. In the Thyme framework, every mapped resource must have a view. The assumption is made that every resource can be displayed this or that way, even if the application does not include the resource's page in its regular collection of pages.

In addition to the URI pattern and the logic components listed above, a route mapping has a *route id* that identifies the route in the `com.boylesoftware.web.api.Routes` API and allows building URLs for the route.

It also has a *security mode* that tells if access to the mapped resource requires a secure HTTPS connection or an authenticated user. If it requires an authenticated user and an anonymous request is received for the resource, the framework will automatically redirect the user to the user login page.

#### Router Configuration File

The router configuration, which consists mostly of route definitions, is provided by `AbstractWebApplication`'s `getRouterConfiguration()` method. The default implementation returns the configuration loaded from the */WEB-INF/routes* file in the web-application. The */WEB-INF/routes* file is a text file that has a special format described here.

The file contains two types of statements: declarations and route mapping definitions.

##### Minimal Mapping Definition

The simplest route mapping definition maps a URI pattern to the view:

```
/home.html => /WEB-INF/jsp/html/home.jsp
```

This mapping instructs the router that if a request is received for URI "/home.html" (context-relative), then send the view provided by the JSP in */WEB-INF/jsp/html/home.jsp*.

##### View ID

The string "/WEB-INF/jsp/html/home.jsp" in the example above is actually a view id, interpreted by the framework component called *view sender*, an implementation of the `com.boylesoftware.web.spi.ViewSender` interface. The default router configuration implementation uses view sender returned by `AbstractWebApplication`'s `getViewSender()` method. The `com.boylesoftware.web.impl.view.MultiplexViewSender` returned by default by the `getViewSender()` method is configured to recognize view technology by the view id pattern. It knows that if the view id ends with ".jsp", it should use `com.boylesoftware.web.impl.view.DispatchViewSender`, which handles JSPs.

Often, view ids start with the same prefix. For example, all application JSPs may reside under */WEB-INF/jsp/html*. Instead of typing the prefix for each mapping, there is a statement that declares a view id prefix:

```
viewsBase: /WEB-INF/jsp/html/

/home.html => home.jsp
```

The prefix is applied to all subsequent route mappings until another declaration changes it or the end of file is reached.

##### Explicit Route ID

For the example above, the route id will be automatically generated from the URI pattern. The id will be "/home.html". If the URI pattern changes in the future versions of the application, the route id changes too. And if some parts of the application use the route id via the `Routes` API to build URLs to the mapped page, those parts will have to be changed as well. To avoid that, the application can give the route an explicit id:

```
@homePage
/home.html => home.jsp
```

Now, the home page's route id is "homePage" and it will not change if the URI pattern changes.

##### Controller

The example home page mapping does not associate a controller with the resource. Here is an example of a mapping with a controller:

```
/password.html
    com.mycompany.myapp.controllers.PasswordResetRequestController
    => password.jsp
```

As with the view ids, often controllers reside in a single Java package. Instead of typing it each time, there is a declaration that applies to all subsequent mapping definitions:

```
controllerPackages: com.mycompany.myapp.controllers

/password.html
    PasswordResetRequestController => password.jsp
```

It is possible to declare multiple controller packages as well:

```
controllerPackages:
    com.mycompany.myapp.controllers,
    com.boylesoftware.web.stk

/password.html
    PasswordResetRequestController => password.jsp
```

In the most cases the whitespace in the routes configuration file includes new lines and is either ignored or used to separate elements in a statement. So, it is not important if elements of a statement are all on one line or on multiple lines. One exception is multi-line declaration statements. The line following a declaration line is attributed to the same declaration only if it starts with some whitespace.

##### Security Mode

The example mapping above is for a page that lets users request a password reset. The page most likely asks the user to enter sensitive information, such as an e-mail address. The page, therefore, must only be accessible via HTTPS. To declare this, we must add a flag to the mapping:

```
/password.html +S
    PasswordResetRequestController => password.jsp
```

If an insecure HTTP request is received for this page, the framework will redirect the client browser to the HTTPS URL.

Another security mode flag is useful when the mapping requires an authenticated user. For example:

```
/secure/profile.html +U
    => profile.jsp
```

If an unauthenticated request is received, the framework will redirect the client browser to the user login page.

##### Protected and Public Pages

Usefully, a group of URIs can be identified as requiring an authenticated user by the URI prefix. For example, in our application we could have all such pages under "/secure/". Instead of adding "+U" flag to all such mappings, we can use a blanket declaration:

```
protectedPages:
    /secure/.*

/secure/profile.html
    => profile.jsp
```

The `protectedPages` declaration takes a regular expression for the URIs in question. As opposed to the previously seen `viewsBase` and `controllerPackages` declarations, the `protectedPages` declaration can appear in the configuration file only once and it must come before any mapping definitions.

The `protectedPages` declaration has a counterpart: the `publicPages` declaration. If both are present, pages matching the protected pages pattern are considered protected unless they also match the public pages pattern. If only the protected pages pattern is specified, all pages are public except those matching the pattern. If only public pages pattern is specified, all pages are protected except those matching the pattern. If neither is specified, all pages are public. The "+U" flag specified on a mapping overrides the applicable blanket patterns.

##### Controller Parameters

In the password reset page example above, the controller instance is created using the default constructor of the `com.mycompany.myapp.controllers.PasswordResetRequestController` class. But sometimes a generic controller needs to be customized. The Thyme Framework allows the passing of certain simple, literal parameters to the controller's constructor. For example, the framework's STK includes a standard implementation of a controller for a user logout page. After successfully logging out, the browser is redirected to a certain application page. Since the controller is generic, it does not know which page the application uses for that purpose. It needs a parameter with the corresponding view id:

```
/secure/logout.html
    LogoutController("/home.html") => logout.jsp
```

Or we can use the explicit route id for the home page:

```
/secure/logout.html
    LogoutController("homePage") => logout.jsp
```

##### Login Page

As mentioned above, if an unauthenticated request is received for a protected page, Thyme responds with a redirect to the user login page. So the framework needs to know the URL of the login page. If the login page is part of the application and there is a route mapping for it, the route mapping can be marked with a flag:

```
/login.html +L
    LoginController => login.jsp
```

Only one mapping can be marked with "+L" flag.

Alternatively, a declaration can be used:

```
loginPage: /myapp/login.html
```

The declaration can appear in the file only once and it must be before any mapping definition. The "+L" flag cannot be used if the declaration is used. The declaration allows the login page to exist outside the application.

##### URI Parameters

Mapping URI patterns can have placeholders for the URI parameters. Each parameter is extracted from the URI path and converted to a request parameter. Each parameter placeholder is surrounded with curly braces and contains the name for the corresponding request parameter. Optionally, it can also contain a regular expression for the parameter values. If a regular expression is present, it must follow a colon after the parameter name. The expression must not contain any capturing groups and any curly brace character in it must be strictly balanced. For example:

```
/secure/posts/{postId}.html
    => posts.jsp
```

A request URI "/secure/posts/123.html" will make a request parameter named "postId" available with a value of "123."

To add a regular expression:

```
/secure/posts/{postId:[1-9][0-9]*|new}.html
    => posts.jsp
```

The mapping above will match only if the post id is a positive integer number or word "new."

##### Route Script

It is possible to include certain logic right in the mapping definition. The route script, associated with a mapping, is executed each time the mapping is invoked. It is a good place to verify user permissions and to fetch the referred entities from the database.

Here is an example of a mapping for a blog post page:

```
entityPackages: com.mycompany.myapp.entities

/secure/posts/{postId:[1-9][0-9]*|new}.html
    PostController {
        if (postId == "new") {
            abort if (DELETE)
            post = new Post
        } else {
            post = Post(postId)
            forbid if (DELETE & post.author.id != authedUser.id)
        }
    } => post.jsp
```

The route script comes after the controller, if any, and before the "=>" pointing at the view id. The script is enclosed in curly braces.

There are three types of statements used in route scripts. All are represented in the example above.

* *Conditional statement:* This allows conditional logic in the script and can have an optional "else" clause.
* *Permission statement:* This stops request processing in certain conditions.
* *Assignment statement:* This allows the storing of objects in request attributes to make them available for the controller and the view.

Let's have a look at the example above. The first line in the script begins a conditional statement and checks if the URI parameter "postId", defined in the mapping's URI pattern, equals "new", which means a page for creating a new post is being requested.

The conditional statement form is:

```
if (<conditional expression>) { <script> }
```

Or:

```
if (<conditional expression>) { <script> } else { <script> }
```

If creating a new post, on the second line of the script we check if the request HTTP method is "DELETE". We cannot delete a nonexistent post, so the "abort if" statement returns the HTTP error code 400: "Bad Request." The "abort" statement form is:

```
abort if (<conditional expression>)
```

Or, alternatively:

```
abort unless (<conditional expression>)
```

The conditional expressions can use operators "!", "&" (or "&&"), "|" (or "||"), "==", "!=", use value expressions described below, or use HTTP method tests "GET", "POST" or "DELETE".

Continuing the script, if the method is not "DELETE", we create a new instance of the entity class `Post` and save it in the request attribute named "post." The package for the entity `Post` is taken from a previous `entityPackages` declaration. Alternatively, a fully qualified entity class name can be used.

This is an assignment statement, which takes the form:

```
<request attribute name> = <value expression>
```

The "else" clause that starts on line 4 of the script corresponds to the case when the "postId" is an existing post's id, so the page allows for the viewing and editing of an existing post.

First, we try to fetch the entity with the provided id and store it in the request attribute named "post." If the entity does not exist, this statement will result in the HTTP error code 404: "Not Found."

Second, the "forbid if" statement makes sure that if the HTTP method is "DELETE", the currently authenticated user is the post's author, so nobody else can delete the post but its author. If the method is "DELETE" and the user is not the author, the "forbid if" statement results in the HTTP error code 403: "Forbidden."

The "forbid if" statement's syntax is similar to that of the "abort if" statement:

```
forbid if (<conditional expression>)
```

Or:

```
forbid unless (<conditional expression>)
```

The value expressions can refer to request parameters and request attributes by name. For request attributes, nested properties can be accessed using the dot notation. Simple string, number, and Boolean literals can be used. Also, entity expressions can be used. The entity expressions are:

* **New entity**

	`new <entity class>`

	Creates a new instance of the entity class using the default constructor.

* **Entity by id**

	`<entity class>(<value expr>)`

	Fetches the specified entity from the database using the provided value expression's result as the entity id. If entity does not exist, fail with the HTTP error code 404: "Not Found."

* **Entity reference**

	`ref <entity class>(<value expr>)`

	Gets entity reference by id.

* **Single entity query**

	`<entity class>:<query name>(<query parameters>)`

	Executes the specified named query and returns a single entity. If not found, fail with the HTTP error code 404: "Not Found." Query parameters are a comma-separated list of value expressions. If named parameters are used instead of ordinal parameters, each parameter expression can be prefixed with the query parameter name followed by a colon. If query does not have any parameters, the list is empty.

* **Entity list query**

	```
	<entity class>:<query name>(<query parameters>).list
	<entity class>:<query name>(<query parameters>).firstResult(<value expr>).list
	<entity class>:<query name>(<query parameters>).maxResults(<value expr>).list
	<entity class>:<query name>(<query parameters>).firstResult(<value expr>).maxResults(<value expr>).list
	```

	Executes the query and returns the list of results, which can be empty.

See the example in the next paragraph.

##### View Script

The view script is executed each time the view is sent to the client. It is used to fetch additional data used by the view. The view script is specified in the mapping after the view id and has the same syntax as the route script, except the view script does not allow permission statements.

In the edit post page mapping example from the previous section, what if the page also needs to display the list of the most recent posts from the same author? Here is an example mapping definition:

```
/secure/posts/{postId:[1-9][0-9]*|new}.html
    PostController {
        if (postId == "new") {
            abort if (DELETE)
            post = new Post
        } else {
            post = Post(postId)
            forbid if (DELETE & post.author.id != authedUser.id)
        }
    }
    => post.jsp {
        posts = Post:Post.findForAuthor(authorId: authedUser.id).maxResults(20).list
    }
```

The script uses a query named "Post.findForAuthor". It sets a named query parameter "authorId" to the id of the currently authenticated user, made available by the framework in the "authedUser" request attribute. It also sets the maximum returned results to 20. The query will return posts, ordered by post date, in descending order.

Note how in this mapping definition the fetching of referred objects is split into two scripts: the route script and the view script. Normally, when a "POST" is processed successfully, the user receives a redirect response so that refreshing the page does not cause the transaction resubmission. The route script is executed to obtain the selected post, but there is no need to execute the view script if the response is a redirect. However, if the request is a "GET", or if it is a "POST" but the submitted form data failed to validate, the page needs to be displayed, and on the page we need to show the list of recent posts. Fetching the posts list is therefore placed in the view script and is executed only if the view is displayed as a result of the request.

### JPA Entities

The framework does not impose any special requirements on JPA entities used by the application. Thyme uses only standard JPA APIs and does not require any specific JPA implementation. Also, the framework encourages application developers to use JPA entity beans only for a single purpose, making them more straightforward.

### User Input Beans

User input beans are used to encapsulate data entered by a user and attached to a "POST" request. Usually the data comes from an HTML form. The user input bean also specifies the [Bean Validation](http://jcp.org/en/jsr/detail?id=349) constraints used to validate the user input before starting an expensive database transaction and passing the bean to the controller.

Even though it is painful to write Java beans - a clear shortcoming of Java as a language - and every Java web-application developer has felt the temptation to combine entity beans and the beans representing HTML forms, in the Thyme framework the decision has been made to use separate user input beans. Sorry, blame Java and type those silly getters and setters.

For a simple HTML form like this:

```html
<form method="post">
<table>
    <tr>
        <td><label for="emailInput">E-mail:</label></td>
        <td><input type="email" name="email" id="emailInput"/></td>
    </tr>
    <tr>
        <td><label for="passwordInput">Password:</label></td>
        <td><input type="password" name="password" id="passwordInput"/></td>
    </tr>
    <tr>
        <td><label for="rememberMeCheckbox">Remember Me?</label></td>
        <td><input type="checkbox" name="rememberMe" id="rememberMeCheckbox"/></td>
    </tr>
    <tr>
        <td colspan="2"><button type="submit">Submit</button></td>
    </tr>
</table>
</form>
```

We could define the following user input bean:

```java
public class LoginData {

    // the e-mail address
    @NotNull(message="{error.email.empty}")
    @Email(message="{error.email.invalid}")
    private String email;

    // the password
    @NoTrim
    @NotNull(message="{error.password.empty}")
    private String password;

    // "remember me" flag
    private boolean rememberMe;

    // getters and setters
    ...
}
```

The framework set user input bean properties from the request parameters with the same names. The framework automatically performs the conversion of string values of the request parameters to the target property types using so called *binders*, which are implementations of the `com.boylesoftware.web.input.Binder` interface. There is a collection of standard binders in the `com.boylesoftware.web.input.binders` package. A custom binder can be applied to a field using the `com.boylesoftware.web.input.Bind` annotation.

So there are two types of annotations used in user input beans: the validation constraints and annotations related to the binding process. Together with the constraints provided by the validation framework implementation, Thyme adds several frequently used constraints in the `com.boylesoftware.web.input.validation.constraints` package. An example of a binding process related annotation is `com.boylesoftware.web.input.NoTrim` annotation as used in the example above on the "password" field. Normally, any input value going to a string user input bean property is trimmed (leading and trailing whitespace characters are removed) and if the resulting string is empty, the property is set to `null`. The password needs to be processed "as is" so we mark it with a `@NoTrim` annotation so that the trimming logic is not applied to it.

Additional user input bean validation can be performed in the controller. However, it is better to perform as much validation as possible using bean validation constraints, because the automatic validation is performed before the control is passed to the controller. If the route does not have either the route or the view script, and the user input is invalid, then there is no need for the framework to start an expensive JPA transaction, because the controller does not get called before the view is re-displayed with the appropriate validation error messages. There are cases, however, when user input validation requires access to the database. Those checks must be made in the controller.

### Controllers

Controllers are where the main request processing logic for a resource happens. Any class can be a controller. For Thyme to use it as a controller, it does not have to extend or implement anything.

#### Controller Methods

From the framework's point of view, a controller can define up to four methods: three for the three HTTP methods - "GET", "POST", and "DELETE" - and one for additional view preparation logic. The methods are discovered and called by the framework via reflection and require certain names and return types to be properly identified. Here are the methods:

Method Name     | Method Return Type | Description
----------------|--------------------|------------
**get**         | void               | <p>Processes an HTTP "GET" request. After the method successfully returns, the view associated with the route is sent back to the client in a 200 "OK" HTTP response body. This method rarely needs to be implemented, as all the view preparation logic can be defined in the route and view scripts as well as in the `prepareView` controller method (see below). If the method is undefined in a controller, "GET" requests are processed without calling the controller.</p>
**post**        | java.lang.String   | <p>Processes an HTTP "POST" request. The framework assumes that if the "POST" was processed successfully, the response sent back to the client is a redirect response (the framework sends a 303 "See Other" response). The method must return the redirect URI for the response's "Location" header. It can be a server-root relative URL starting with a "/" or an absolute URL.</p><p>The `post` controller method is the only method that allows a user input bean as one of its arguments (see about controller method arguments below). If the method needs to perform an in-transaction user input validation and the user input is invalid, the method can return `null` instead of a redirect URL and the framework will re-display the route's view in response (with the HTTP status code 400: "Bad Request").</p><p>If the method is undefined, any "POST" request to the mapped resource will result in a 405 "Method Not Allowed" response.</p>
**delete**      | java.lang.String   | <p>Processes an HTTP "DELETE" request. As with the `post` method, this method returns a redirect URL upon success, or `null` to re-display the view with a 400 "Bad Request" response. Unlike the `post` method, `delete` does not allow a user input bean to be used as an argument.</p><p>As with `post`, if the method is undefined, any "DELETE" request to the mapped resource will result in a 405 "Method Not Allowed" response.</p>
**prepareView** | void               | <p>Contains additional view preparation logic that could not be expressed in the view script. The method is called each time before the route's view needs to be sent back to the client (with either 200 or 400 HTTP response). If both the view script is defined for the route and the route's controller has a `prepareView` method, the `prepareView` method is called after the view script.</p>

Each method is called inside the request processing JPA transaction and is handled by an asynchronous request processing thread. The transaction and the thread are the same one used for the scripts.

#### Controller Method Arguments

Any controller method can have a number of arguments created and passed to it by the framework. Which arguments - and their order - is determined by the controller's needs and is irrelevant from the framework's perspective. The framework determines the meaning of each controller argument using the argument's type and/or using special annotations. The configured `com.boylesoftware.web.spi.ControllerMethodArgHandlerProvider` is responsible for this logic. Out of the box, as implemented by `com.boylesoftware.web.impl.StandardControllerMethodArgHandlerProvider`, the following argument types are supported:

* **The HTTP Request**

	This argument's type must be `javax.servlet.http.HttpServletRequest`.

* **The HTTP Response**

	This argument's type must be `javax.servlet.http.HttpServletResponse`.

* **The Application Object**

	This argument's type must be `com.boylesoftware.web.AbstractWebApplication` or its subclass. This argument is used, for example, to make additional application-specific configurations and services available to the controller. The configuration and services access methods can be defined in the custom application extension class.

* **JPA Entity Manager**

	This argument's type must be `javax.persistence.EntityManager`; it is the entity manager used to access the database. The transaction is already made active by the time the controller is called. The transaction is automatically committed by the framework if the controller method successfully returns, or rolled back if the method throws an exception.

* **The Authenticator**

	This argument's type must be `com.boylesoftware.web.api.Authenticator` or its implementation. This is the user authentication API for the controller. Controllers dealing with user authentication sessions, such as processing user login and logout, need this API.

* **User Locale**

	This argument's type must be `java.util.Locale`; it is the locale as determined by the configured `com.boylesoftware.web.spi.UserLocaleFinder`. Used for localization and internationalization purposes.

* **Flash Attributes**

	This argument's type must be `com.boylesoftware.web.api.FlashAttributes`; it is the API for "flash" attributes. The controller may set attributes in this object and the attributes will be automatically converted to request attributes with the same names and values for the next HTTP request from the same client. This is useful when the controller method causes a redirect response, but the target page needs to know about the results of the just completed transaction - for example, to display a message to the user.

	The flash attribute values must be as short as possible since all the names and values of all the flash attributes are temporarily stored on the client side as an HTTP cookie. In the example of a message, it is better to set a short code as the flash attribute value and decode it to the corresponding message in the next request's view.

* **Routes API**

	This argument's type must be `com.boylesoftware.web.api.Routes`. This API allows the controller to lookup specific route URIs, so that it can send them back for the redirect response. This API is rarely used directly. Usually an argument with `@RouteURI` annotation is used for that purpose (see next item).

* **Route URI**

	This argument's type must be either `java.lang.String` or `com.boylesoftware.web.api.RouteURIBuilder` and the argument must have a `com.boylesoftware.web.api.RouteURI` annotation. This is a convenient way to have the framework lookup a route URI for the controller using the `Routes` API. The `String` argument is used for routes without URI parameters, and a `RouteURIBuilder` argument allows for working with parameterized route URIs.

* **JavaMail Session**

	This argument's type must be `javax.mail.Session`. It is used by a controller if it needs to send e-mails. Note that the session must be configured in the JNDI.

* **Request Parameter**

	This argument's type must be either `java.lang.String` or an array of `java.lang.String`s. The argument must have a `com.boylesoftware.web.api.RequestParam` annotation.

* **Model Component**

	The type of this argument can be any but it must have a `com.boylesoftware.web.api.Model` annotation. The value for the argument is taken from the corresponding request attribute. Normally, the model component has been put into a request attribute by the route script. This is the way to fetch and pass referred objects to the controller and the view.

* **User Input Bean**

	The type of this argument can be any but it must have a `com.boylesoftware.web.api.UserInput` annotation. Only the `post` method is allowed to have a user input bean argument. The bean is validated by Thyme before passing it to the controller. If the bean is invalid, the controller is not called and the framework re-sends the route's view with a 400 "Bad Request" HTTP response automatically. The controller may perform additional in-transaction validation, or any other type of validation that cannot be expressed via validation constraint annotations, and return `null` if the bean is invalid. NOTE: Only one user input bean argument is allowed.

* **User Input Validation Errors**

	This argument's type must be `com.boylesoftware.web.api.UserInputErrors`; it is useful as a `post` method that performs in-transaction user input bean validation. It allows for the addition of error messages to the view, displayed as a result of the controller method's returning `null`. For the view, the user input validation errors object is made available in the `Attributes.USER_INPUT_ERRORS`, or "userInputErrors" request attribute.

NOTE: It is an error to specify an unsupported argument for a controller method.

#### Throwing Exceptions

A controller method can throw an exception. Usually this results in a 500 "Internal Server Error" response being sent back to the client. However, Thyme provides a collection of exceptions that extend the `com.boylesoftware.web.RequestedResourceException` abstract class. These exceptions allow for the sending of other HTTP error codes in case of errors. The codes include 400: "Bad Request," 403: "Forbidden," 405: "Method Not Allowed," 404: "Not Found," and 503: "Service Unavailable."

#### Controller Example

Let's have a look at a controller behind a blog post page. The controller allows posting a new post, editing an existing post, and deleting a post. First, here is the mapping:

```java
this.addRoute(sc,
    "postDetails",
    "/secure/posts/{postId:[1-9][0-9]*|new}.html",
    SecurityMode.DEFAULT,
    new Script() {        // route script, fetch the referred post from the database
        @Override
        public void execute(HttpServletRequest request, EntityManager em) {

            String postId = request.getParameter("postId");
            Post post = (postId.equals("new") ? new Post() :
                    em.find(Post.class, Integer.valueOf(postId)));
            request.setAttribute("post", post);
        }
    },
    new PostController(), // associate the controller with the route
    "/WEB-INF/jsp/html/post.jsp",
    null
);

// the posts list page mapping, the post controller sometimes redirects to it
this.addRoute(sc,
    "postsList",
    ...
);
```

The route script prepares the post entity bean by either fetching it from the database or by creating a new bean, and it puts in the request attribute named "post." This is the model component.

The post user input bean could look this way:

```java
public class PostData {

    // post message.
    @NotNull(message="{error.message.empty}")
    private String message;

    // getters and setters
    ...
}
```

And the post entity bean might look like this:

```java
@Entity
public class Post {

    // post id
    @Id
    @GeneratedValue
    private int id;

    // author of the post
    @ManyToOne
    @JoinColumn(nullable=false, updatable=false)
    private User author;

    // date when the post was created
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable=false, updatable=false)
    private Date postedOn;

    // the message
    @Lob
    @Column(nullable=false)
    private String message;

    // getters and setters
    ...
}
```

Now, let's have a look at the post controller:

```java
public class PostController {

    /**
     * Process POST. Update existing or save new post.
     *
     * @param postData Post data.
     * @param em Entity manager.
     * @param post The post.
     * @param user Authenticated user.
     * @param nextURI Posts page URI.
     *
     * @return URI of the page to redirect upon successful submission.
     */
    String post(
        @UserInput PostData postData,
        EntityManager em,
        @Model("post") Post post,
        @Model(Attributes.AUTHED_USER) User user,
        @RouteURI("postsList") String nextURI) {

        post.setMessage(postData.getMessage());

        if (post.getId() == 0) { // new unsaved post has id 0
            post.setAuthor(em.getReference(User.class, Integer.valueOf(user.getId())));
            post.setPostedOn(new Date());
            em.persist(post);
        }

        return nextURI;
    }

    /**
     * Process DELETE. Delete the post.
     *
     * @param em Entity manager.
     * @param post The post.
     * @param flash Flash attributes.
     * @param nextURI Posts page URI.
     *
     * @return URI of the page to redirect upon successful submission.
     */
    String delete(
        EntityManager em,
        @Model("post") Post post,
        FlashAttributes flash,
        @RouteURI("postsList") String nextURI) {

        em.remove(post);

        flash.setAttribute("message", "{message.post.deleted}");

        return nextURI;
    }
}
```

The controller does not need to define a `get` method. There is no special logic for processing a "GET" and the view can display the post data using the request attribute "post" stored in the request by the route script.

Notice how the `post` method refers to a model component named `Attributes.AUTHED_USER` (or "authedUser"). For every request that has an authenticated user, Thyme puts the authenticated user object in this request attribute. The object is returned by the authentication service and, even when represented by an entity bean, it is not associated with the entity manager and the transaction is passed to the controller. This is because the authentication service may have fetched the user record from a cache instead of loading it from the database. Whenever the authentication service needs to load a user record from the database, it is performed in a separate transaction and on a separate thread before proceeding with the rest of the request processing logic. If a controller needs an in-transaction, currently-authenticated user record, it can be re-fetched in the route script.

### Standard Toolkit (STK)

The `com.boylesoftware.web.stk` package contains a collection of controllers and user input bean implementations for typical use-cases. The application can use them instead of defining its own custom versions.

### Views

The `com.boylesoftware.web.impl.view.DispatchViewSenderProvider` can be used for JSP-based views. The framework provides a simple JSP tag library to assist in working with HTML forms and accessing other relevant functionality.

#### HTML Form JSP Tags

The tag library's URI is "http://www.boylesoftware.com/jsp/thyme". Here is an example of a simple user profile form:

```jsp
...

<!-- Import tab libraries -->
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="t" uri="http://www.boylesoftware.com/jsp/thyme" %>

...

<!-- Display user input validation errors -->
<c:if test="${!empty userInputErrors}">
<ul>
    <c:forEach items="${userInputErrors}" var="error">
    <li><c:out value="${error.fieldName}: ${error.message}"/></li>
    </c:forEach>
</ul>
</c:if>

...

<!-- The form -->
<t:form id="profileForm" bean="${user}" focus="firstName">
<table>
    <tr>
        <td><t:label name="firstName">First Name:</t:label></td>
        <td><t:input type="text" name="firstName"/></td>
    </tr>
    <tr>
        <td><t:label name="lastName">Last Name:</t:label></td>
        <td><t:input type="text" name="lastName"/></td>
    </tr>
    <tr>
        <td><t:label name="password">New Password:</t:label></td>
        <td><t:input type="password" name="password" bean="none"/></td>
    </tr>
    <tr>
        <td><t:label name="password2">Confirm New Password:</t:label></td>
        <td><t:input type="password" name="password2" bean="none"/></td>
    </tr>
    <tr>
        <td colspan="2"><button type="submit">Submit</button></td>
    </tr>
</table>
</t:form>

...
```

The `bean` attribute on the form allows it to specify an entity bean which is ultimately - through the user input bean and the controller - behind the form. This helps the JSP tags to use the correct value restricting attributes, such as `maxlength` and `required` on the generated HTML `input` elements. The `t:input` tag can override the form's `bean` attribute and can have its own, plus a `beanField` attribute to associate it with a bean field, which has a name different from the input field's name.

The `bean` attribute can take the bean, or it can be a `java.lang.String`, in which case it is interpreted as the bean class name. For an input field, it also can have a special value of "none," which disassociates the field from any entity bean property. This is used in the example above for the password inputs; the user bean does not have a password property - it has a password digest property.

The names of the input fields must be the same as the corresponding property names of the user input bean.

The `focus` attribute of the form tag allows the framework to generate HTML so that the focus is automatically set to the specified input field when the form is displayed. In case there are user input validation errors, the focus will be set to the first invalid field instead the one specified by the `focus` attribute.

#### Page Links

The tag library also provides functions used to generate links to other application pages using route ids. These functions are a facade for the `com.boylesoftware.web.api.Routes` API. See `com.boylesoftware.web.jsp.Functions` for the function definitions.

## Additional Documentation

* [API Reference](http://www.boylesoftware.com/thyme/site/apidocs)
* [Maven Generated Project Information](http://www.boylesoftware.com/thyme/site)
* [Project on GitHub](https://github.com/boylesoftware/thyme)
