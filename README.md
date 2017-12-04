# Session to cookie
This is a simple servlet filter that just reads and writes everything inside an existing session into a cookie and vice versa.

## Description
This library makes it easy for your (probably legacy) application to become stateless without the need to rewrite all parts that use a session. This enables your app to be deployed in modern platforms (like Kubernetes) without the need for session stickyness (which is pretty bad or sometimes not even possible) or session replication (better than sticky sessions, but still kinda wrong).
Of course, clients need to support cookies for this to work. But if they don't, sessions wouldn't work anyways, right?
The session data is serialized (any object in sessions must be serializable anyways), a checksum is added, then compressed, encrypted and encoded. This is to make sure that if the application should talk http only (you should do https btw), the data in the cookie is still encrypted, preventing leaking sensible information that may be present in the session.

**For this to work, the servlet response is wrapped in a buffered response - that means that the server may eat up more memory, depending on what kind of responses and concurrent threads you have!**

**Since the session data is compressed and encrypted, it may use up a little bit more cpu than before, depending on how much data is inside the session**

Of course, you should try to remove session usage in your application completely, so you won't need this - but it is not always possible. See this as a library that enables you to work around this issue **temporarily**.

## Structure
The project is composed of a maven parent (-parent) the library itself and a spring boot based test application (-testapp) including integration tests.

# Usage
Currently the filter is only usable with spring-enabled applications (see TODOs).
For using the filter in a spring boot application, you just need the dependency (with enabled component-scan).
For any other spring application, you need the dependency, and either component-scan or an explicit configuration of the filter and all needed beans.

## Requirements
* spring-web >=4.1.3 - since everything used in the filter is a spring bean and we use spring's ContentCachingResponseWrapper for caching/buffering the response.
* guava - here and there (may be easily refactored to not use guava)
* jasypt - used for encryption (may be easily replaced by any other implementation/algorithm)
* slf4j-api - rtm.

## Configuration
For using in your application, you should provide a shared secret for all instances of the application that are to en- and decrypt the cookies. Therefore, set the property "sessiontocookie.sharedsecret" 

# Building & Running
It's a maven based application, so for building its
	
	$ mvn clean install
	
For running the testapp locally, use the local profile which brings some default config, e.g. for testing manually on the provided WebController

	$ cd session-to-cookie-testapp
	$ mvn spring-boot:run -Drun.jvmArguments="-Dspring.profiles.active=local"
	
# Need a demo?
In session-to-cookie-testapp you'll find a dockerfile, a "howto\_demo\_with\_minikube" file and a yaml file. Read the howto to see how you can create a docker image out of the testapp, create a local minikube cluster and how to deploy it on that cluster. See the endpoints in the WebController class on what calls to do for demoing.

# TODOs
* make cookie valid as long as server session timeouts would be (with configurable override, if you want other timeouts)? Since we use the cookie as a session cookie, this should not be necessary.


## nice-to-have 
* make sure / find out what maximum size the cookie can be - provide workarounds, e.g. splitting into several cookies
** https://stackoverflow.com/questions/640938/what-is-the-maximum-size-of-a-web-browsers-cookies-key
** should all cookies including all data (name, value) not exceed the size of 4096 bytes?
** http://browsercookielimits.squawky.net/ => suggests that 4096 is max per cookie
* make a non-spring variant of the filter?

# License
See [LICENSE.txt](LICENSE.txt)


##### by alejandro alanis 2017 - https://github.com/alecalanis
