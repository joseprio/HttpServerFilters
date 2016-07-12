## HttpServerFilters
Since Java 6, the JRE ships with a simple built-in HTTP/S server.

This projects implements some filters that add new functionalities
to that server:
* ErrorFilter: show a proper error page for unhandled errors
* GzipFilter: if the client supports it, compress the output with
Gzip, tipically resulting in faster loads.

## Usage
After creating an `HttpContext`, just obtain the filters list and
add the ones to be used:
```java
HttpContext hc = server.createContext("/example", new GzipHandler());
hc.getFilters().add(new GzipFilter());`
```

## Example
A simple implementation is included in the 
`com.joseprio.example.FiltersHttpServer` class; it may be run from
the command line or from your favorite IDE.
