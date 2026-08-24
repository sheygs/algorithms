# Circuit Breaker

## Description

Let's assume we have a web service, called **Service A**, which handles API requests. Each time an API request is sent to Service A, it makes a request to 2 other services:

- Service B
- Service C

```text
                     ┌───────────┐
                     │ Service B │
                    ↗└───────────┘
┌───────────┐
│ Service A │
└───────────┘
                    ↘┌───────────┐
                     │ Service C │
                     └───────────┘
```

If one of Service B or C has a problem, and we keep making API requests to them, it would create extra pressure on the failing service which would only make the problem worse.

Ideally, if we encounter too many failures under a certain period of time, we should block making requests to give time for the service to recover.

To fix the problem, implement a version of a Web API client that:

> When a number of failures happen, we stop making API requests to that service for a while.

For this problem, we should **stop calling a Service if we get 3 failures (e.g. Internal Server Error, Timeout) in the last 10 minutes**, but **after 5 minutes we want to open the calls again to the service**.

---

## Example Scenario

Example requests being made to Service B and Service C:

| Request time | Service / API | Response (status) |
|---|---|---|
| 12:01 AM | GET Service B | Success (200) |
| 12:02 AM | GET Service B | Failed (500) |
| 12:03 AM | GET Service B | Failed (500) |
| 12:04 AM | GET Service B | Failed (500) |
| 12:05 AM | GET Service B | Blocked — not making request to Service B |
| 12:07 AM | POST Service C | Success (200) |
| 12:09 AM | GET Service B | Blocked — not making request to Service B |
| 12:10 AM | GET Service B | Success (200) |

The example also shows that circuit-breaker behaviour should be **independent for each downstream service**. Service B can be blocked while Service C continues receiving calls.

---

## Supplied Java Starter Code

The screenshots show Java 21 and the following supplied structure:

```java
import java.io.*;
import java.util.*;
import java.text.*;
import java.util.stream.*;
import java.net.*;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;


// You don't need to make any changes for this class
abstract class Request {

    String host;

    abstract Response call(); // This makes request to external server
}


// You don't need to make any changes for this class
class Response {

    int status;

    String body;
}


/*
 * This class will be used multiple times to make API requests
 * to different services.
 *
 * Example:
 *
 * Request requestToServiceB = buildRequest("service-b.com");
 * Response responseFromServiceB =
 *         webClient.execute(requestToServiceB);
 *
 * Request requestToServiceC = buildRequest("service-c.com");
 * Response responseFromServiceC =
 *         webClient.execute(requestToServiceC);
 */
class WebClient {

    private int getCurrentTimeInMinutes() {

        return Clock.getCurrentTimeInMinutes();
    }


    public Response execute(Request request) throws Exception {

        // You have to implement the solution by replacing the next line.
        return request.call();
    }
}
```

The main implementation point is:

```java
public Response execute(Request request) throws Exception
```

The candidate is expected to replace the current direct call:

```java
return request.call();
```

with the required circuit-breaker behaviour.

---

## Supplied Test Clock

The screenshots also show a test clock supplied by HackerRank:

```java
class Clock {

    private static int currentTimeInMinutes = 0;


    protected static int getCurrentTimeInMinutes() {

        return currentTimeInMinutes;
    }


    protected static void setCurrentTime(int mins) {

        currentTimeInMinutes = mins;
    }
}
```

This means the implementation should use:

```java
getCurrentTimeInMinutes()
```

inside `WebClient` rather than relying on real wall-clock time.

---

## Task

Using the supplied `Request`, `Response`, `Clock`, and `WebClient` structure:

- Track failures independently for each downstream `host`.
- Stop calling a service after **3 failures within the last 10 minutes**.
- Failures include examples such as:
  - Internal Server Error
  - Timeout
- While a service is blocked, **do not invoke `request.call()`** for that service.
- Requests to other downstream hosts must continue independently.
- After the service has been blocked for **5 minutes**, allow calls to that service again.
- Implement the behaviour inside:

```java
public Response execute(Request request) throws Exception
```

- Do not modify the supplied `Request` and `Response` classes.

---

## Clarifications the Original Screenshots Do Not Fully Specify

The screenshots do not explicitly define every edge case. These should be clarified with the interviewer rather than silently assumed.

Important examples:

1. Do the 3 failures need to be consecutive, or is it any 3 failures within the rolling 10-minute period?
2. What exactly counts as a failure besides HTTP 500? Do thrown timeout/network exceptions count?
3. Does the request that causes failure #3 still return its actual downstream failure response?
4. What should `execute()` return or throw when a request is blocked?
5. At the exact 5-minute cooldown boundary, is the next request allowed?
6. If the first request after cooldown fails, should the service immediately become blocked again?

---

## Note

This README is a reconstruction from the provided HackerRank screenshots. The visible screenshots do not show the complete automated test harness, so unseen test code has not been invented here.
