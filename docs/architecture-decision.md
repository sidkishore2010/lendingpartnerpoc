# Architecture decision: Java/Spring Boot on ECS Fargate

**Status:** Accepted for the POC direction  
**Scope:** Lending backend and future production deployment  
**Date:** 2026-10-25

## Context

The lending platform accepts a synchronous quote request, validates it, calls an external commission provider, and maps the provider response into a stable application contract. The external provider may have variable response times, intermittent failures, and vendor-specific authentication and response details.

The current POC has a 5-second vendor response timeout and a separate mock-vendor process. The architecture should support a future real vendor whose latency and operational characteristics may differ, without coupling the browser to the provider.

## Decision

Use Java 21 and Spring Boot 3 for the lending backend, with Spring MVC for the initial synchronous REST API. Package the backend as an executable JAR and target Amazon ECS on Fargate for a future hosted deployment.

Keep the vendor integration behind the `CommissionQuoteClient` abstraction. The backend owns request validation, vendor authentication, timeout behavior, error mapping, correlation IDs, and the stable frontend response contract. The mock vendor remains a development/test service and is not required as a production component once a real vendor is integrated.

## Why Java and Spring Boot

- **Suitable for long-lived service processes:** a Spring Boot service runs as a managed process with a predictable lifecycle. It can make outbound HTTP requests using explicit connection and response timeouts and can be scaled as a service independently of the frontend.
- **Clear REST and validation support:** Spring MVC and Jakarta Bean Validation provide familiar patterns for typed request DTOs, HTTP endpoints, status mapping, and consistent error responses.
- **Established operational integrations:** Spring Boot Actuator provides health endpoints, and the ecosystem supports metrics, tracing, structured logging, and managed configuration as deployment requirements grow.
- **Maintainable integration boundary:** constructor-injected clients and application services isolate provider-specific HTTP/authentication/mapping from the business-facing API. A real vendor can be added behind the existing abstraction.
- **Compatibility with the team and POC constraints:** Java 21 and Maven are available locally, and the services can be run as ordinary JARs without Docker during development.

## Why ECS Fargate rather than Lambda for this workload

The expected vendor call can take an unpredictable amount of time. A continuously running ECS service is a better initial fit when the application needs a long-lived HTTP process, explicit resource allocation, predictable connection handling, and service-level health checks. It avoids making the design depend on Lambda's per-invocation execution model and fixed maximum invocation duration (currently 15 minutes).

ECS also gives the team direct control over task CPU/memory, desired service count, deployment rollouts, networking, and process-level observability. This is useful if vendor response time, throughput, connection reuse, or future resilience controls require tuning.

This is not a claim that ECS makes synchronous requests unbounded. The browser, load balancer, HTTP client, and vendor may each impose their own timeouts. The backend must retain finite connection/read timeouts and map slow or unavailable vendor calls to appropriate errors. If a future quote operation can exceed the user-facing request budget, redesign it as an asynchronous workflow (submit, return an operation ID, poll or notify) rather than holding an HTTP request open indefinitely.

Lambda remains a reasonable alternative if measured vendor latency and the complete request path reliably fit within the invocation and upstream client/API timeouts, traffic is bursty enough to benefit from scale-to-zero, and the stateless invocation model fits operational requirements. Re-evaluate with measured latency, request volume, cost, and operational constraints before production deployment.

## Consequences and trade-offs

### Benefits

- Long-lived process and service-level resource controls.
- Explicit HTTP timeouts and centralized vendor error mapping.
- One stable backend contract regardless of vendor contract changes.
- Straightforward local development with Java/Maven and executable JARs.

### Costs and limitations

- ECS tasks require service deployment, health checks, scaling configuration, networking, and patching/operations.
- A running service may cost more than a scale-to-zero function at very low or sporadic volume.
- Synchronous processing is still limited by end-to-end timeouts and consumes a request thread while waiting for the vendor.
- The POC's `api-key` headers are not staff authentication. The development lending key is bundled into frontend code and is visible to browser users; production access control must use an appropriate identity/authentication mechanism.
- Development-only failure simulation must be disabled or removed from production configuration.

## Deployment direction

For the future ECS deployment:

1. Build the lending backend JAR into an immutable deployment artifact and run it in an ECS Fargate service.
2. Serve the frontend separately as static assets (for example, from an object store/CDN).
3. Store real vendor credentials in AWS Secrets Manager or SSM Parameter Store and inject them into the backend task.
4. Use the platform's private networking for outbound vendor connectivity where possible; expose only the required public application endpoint.
5. Configure health checks, task CPU/memory, desired count/autoscaling, logging, metrics, tracing, and alarms.
6. Set finite timeouts at every network boundary and ensure load-balancer/client timeouts align with the user-facing service budget.
7. Deploy the mock vendor only in isolated development/test environments.

The POC does not include container definitions, infrastructure-as-code, staff authentication, production secrets management, autoscaling policy, or operational dashboards.
