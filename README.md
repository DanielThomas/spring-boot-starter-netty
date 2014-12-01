A prototype Netty embedded servlet bridge for Spring Boot.

Includes basic filter and servlet support. I'm fairly new to Spring Boot, but this approach seems to fit it's opinionated view of the servlet container. Async servlet support isn't yet completed.

[![Build Status](https://travis-ci.org/DanielThomas/spring-boot-starter-netty.svg?branch=master)](https://travis-ci.org/DanielThomas/spring-boot-starter-netty)

Performance
=============

Use the `runTestApp` Gradle task to start the server with the same configuration used here. Tests run with `wrk` with the following arguments:

    wrk -H 'Host: localhost' -H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8' -H 'Connection: keep-alive' -d 120 -c 32 -t 2 http://localhost:8080/plaintext

Tomcat 7.0.55
-------------

    Running 2m test @ http://localhost:8080/plaintext
      2 threads and 32 connections
      Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency    22.96ms  115.34ms 628.47ms   96.50%
        Req/Sec    16.33k     4.74k   34.44k    83.57%
      3691775 requests in 2.00m, 497.09MB read
    Requests/sec:  30764.62
    Transfer/sec:      4.14MB

Tomcat 8.0.9
-------------

    Running 2m test @ http://localhost:8080/plaintext
      2 threads and 32 connections
      Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency   114.10ms  745.91ms   5.04s    97.75%
        Req/Sec    15.67k     2.99k   27.56k    91.68%
      3553322 requests in 2.00m, 478.45MB read
      Socket errors: connect 0, read 0, write 0, timeout 3
    Requests/sec:  29611.00
    Transfer/sec:      3.99MB

Prototype Netty 4.0 Bridge
-------------

    Running 2m test @ http://localhost:8080/plaintext
      2 threads and 32 connections
      Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency     1.14ms    6.64ms 128.33ms   99.33%
        Req/Sec    19.64k     4.16k   45.22k    83.96%
      4459068 requests in 2.00m, 331.69MB read
    Requests/sec:  37158.91
    Transfer/sec:      2.76MB

Note: Difference in transfer/sec is due to Server and Date headers returned by Tomcat

Prototype Netty 4.1 Beta 1 Bridge
-------------

    Running 2m test @ http://localhost:8080/plaintext
      2 threads and 32 connections
      Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency     1.09ms    7.58ms 200.99ms   99.80%
        Req/Sec    19.14k     2.19k   39.89k    84.44%
      4366902 requests in 2.00m, 408.12MB read
    Requests/sec:  36390.84
    Transfer/sec:      3.40MB

When compared to a null servlet:

    Running 2m test @ http://localhost:8080/null
      2 threads and 32 connections
      Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency   820.84us    3.47ms  66.04ms   99.70%
        Req/Sec    26.03k     4.27k   45.44k    69.68%
      5894335 requests in 2.00m, 505.91MB read
    Requests/sec:  49119.58

And to serving the response directly from Netty:

    Running 2m test @ http://localhost:8080/nullnetty
      2 threads and 32 connections
      Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency     2.01ms   23.54ms 400.01ms   99.65%
        Req/Sec    27.21k     4.26k   41.44k    66.08%
      6164886 requests in 2.00m, 223.41MB read
    Requests/sec:  51374.16

Prototype Netty 4.1 Beta 3 Bridge (Commit eb531c6)
-------------

    Running 2m test @ http://localhost:8080/plaintext
      2 threads and 32 connections
      Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency     1.95ms   18.62ms 289.53ms   99.57%
        Req/Sec    20.48k     2.55k   37.78k    86.51%
      4637061 requests in 2.00m, 698.71MB read
    Requests/sec:  38642.24
    Transfer/sec:      5.82MB
