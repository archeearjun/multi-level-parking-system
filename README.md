# Multi-Level Parking System

Spring Boot REST API for managing a multi-level parking facility with:

- optimized O(log n) slot allocation
- EV-charging prioritization
- live occupancy and revenue analytics
- deterministic simulation workloads
- naive vs optimized allocator benchmarking
- Swagger/OpenAPI documentation
- Docker packaging

This version is portfolio-ready and interview-ready: it is not just a console demo. It exposes real HTTP endpoints, has integration tests, packages as a Boot jar, and is structured so the core parking domain can later move to a database-backed production system.

## Tech Stack
- Java 17
- Spring Boot 3.5.7
- Spring Web
- Spring Validation
- Spring Boot Actuator
- springdoc OpenAPI / Swagger UI
- JUnit 5 / Spring Boot Test
- Maven Wrapper
- Docker

## Project Highlights
- Supports motorcycles, cars, SUVs, trucks, EV cars, and EV SUVs
- Manages multiple parking levels and heterogeneous slot inventories
- Uses ordered `TreeSet` slot pools for best-fit allocation with O(log n) insert/remove
- Preserves EV-enabled slots for charging demand where possible
- Tracks entries, exits, active tickets, revenue, occupancy, rejection reasons, and EV utilization
- Runs deterministic simulation scenarios from seeded configs
- Compares naive and optimized allocators on the exact same demand stream
- Exposes all major flows through JSON APIs and Swagger UI

## Architecture
### Core domain
- `com.parking.model`
- `com.parking.allocator`
- `com.parking.pricing`
- `com.parking.analytics`
- `com.parking.service`
- `com.parking.simulation`

These packages hold the business logic. They are intentionally independent from HTTP and can later be reused behind persistence, messaging, or other entry points.

### API layer
- `com.parking.api.controller`
- `com.parking.api.dto`
- `com.parking.api.mapper`
- `com.parking.api.exception`

This layer exposes clean request/response contracts instead of returning mutable domain objects directly.

### Runtime design
- The live API uses a single optimized in-memory parking manager.
- Simulation and benchmark endpoints use fresh isolated managers so synthetic workloads do not contaminate the live lot state.
- `POST /api/v1/operations/reset` resets the live state to the seeded layout for repeatable demos.

## Why Allocation Is O(log n)
The optimized allocator maintains separate ordered pools of free slots:

- EV-enabled free slots by `SlotType`
- non-EV free slots by `SlotType`

Each pool is a `TreeSet` ordered by:

1. level number
2. convenience rank
3. slot id

This gives:

- best-slot selection: `pollFirst()`
- slot release / reinsertion: `add()`
- complexity: `O(log n)` for mutation of the ordered pool

Because each vehicle type only checks a constant number of compatible slot categories, the strategy stays efficient while keeping the logic readable.

## EV Prioritization Policy
- EV vehicles requesting charging first try EV-enabled compatible slots.
- If no EV-enabled compatible slot is available, they fall back to a normal compatible slot.
- EV vehicles not requesting charging prefer normal compatible slots first.
- Non-EV vehicles only consume EV-enabled slots after normal compatible inventory is exhausted.

This preserves charger supply and gives measurable EV demand vs supply analytics.

## API Endpoints
### Live parking operations
- `POST /api/v1/parking/entries`
- `POST /api/v1/parking/exits`
- `GET /api/v1/parking/active-tickets`
- `GET /api/v1/parking/availability`

### Analytics
- `GET /api/v1/analytics/dashboard`
- `GET /api/v1/analytics/summary`
- `GET /api/v1/analytics/revenue`

### Simulation and benchmarking
- `POST /api/v1/operations/simulations`
- `POST /api/v1/operations/benchmarks/allocators`
- `POST /api/v1/operations/reset`

### Platform endpoints
- `GET /swagger-ui.html`
- `GET /v3/api-docs`
- `GET /actuator/health`

## Example Requests
### Park a vehicle
```bash
curl -X POST http://localhost:8080/api/v1/parking/entries \
  -H "Content-Type: application/json" \
  -d '{
    "licensePlate": "KA01AB1234",
    "vehicleType": "EV_CAR",
    "chargingRequested": true,
    "entryTime": "2026-03-17T09:30:00"
  }'
```

### Exit a vehicle
```bash
curl -X POST http://localhost:8080/api/v1/parking/exits \
  -H "Content-Type: application/json" \
  -d '{
    "licensePlate": "KA01AB1234",
    "exitTime": "2026-03-17T12:45:00"
  }'
```

### Run allocator benchmark
```bash
curl -X POST http://localhost:8080/api/v1/operations/benchmarks/allocators \
  -H "Content-Type: application/json" \
  -d '{
    "scenarioName": "allocator-benchmark",
    "startTime": "2026-03-17T08:00:00",
    "totalSteps": 24,
    "stepMinutes": 5,
    "arrivalProbability": 1.0,
    "maxArrivalsPerStep": 3,
    "minParkingMinutes": 30,
    "maxParkingMinutes": 150,
    "seed": 98765
  }'
```

## Example Response
```json
{
  "success": true,
  "message": "Vehicle parked successfully in slot L1-C01E",
  "allocationStrategy": "Optimized Best-Fit",
  "allocationNanos": 12800,
  "ticket": {
    "ticketId": "T000001",
    "licensePlate": "KA01AB1234",
    "vehicleType": "EV_CAR",
    "slotId": "L1-C01E",
    "levelId": "L1",
    "entryTime": "2026-03-17T09:30:00",
    "exitTime": null,
    "chargingRequested": true,
    "chargingAllocated": true,
    "parkedMinutes": 0,
    "totalFee": null
  }
}
```

## How To Run
### Windows PowerShell
```powershell
.\mvnw.cmd spring-boot:run
```

### macOS / Linux
```bash
./mvnw spring-boot:run
```

Once the app starts:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`

## Build And Test
### Run tests
```powershell
.\mvnw.cmd test
```

### Package executable jar
```powershell
.\mvnw.cmd package
```

### Run the packaged jar
```powershell
java -jar target\multi-level-parking-system-1.0.0-SNAPSHOT.jar
```

## Docker
### Build image
```bash
docker build -t multi-level-parking-system .
```

### Run container
```bash
docker run -p 8080:8080 multi-level-parking-system
```

The Docker image uses a multi-stage build and packages the Spring Boot jar in a lightweight runtime image.

## Render Deployment
This repository includes a Render Blueprint at [`render.yaml`](/c:/Users/arche/Desktop/Multi%20Level%20Parking%20System/render.yaml) for Docker-based deployment.

Why Docker on Render:

- Render recommends Docker for JVM-based applications such as Java, Kotlin, and Scala.
- Render web services are publicly reachable at an `onrender.com` subdomain.
- Render recommends binding the app to the `PORT` environment variable. This project already does that in [`application.yml`](/c:/Users/arche/Desktop/Multi%20Level%20Parking%20System/src/main/resources/application.yml).

Quick deploy flow:

1. Push this repository to GitHub.
2. In Render, create a new Blueprint or Web Service from that GitHub repository.
3. If using the Blueprint, Render reads [`render.yaml`](/c:/Users/arche/Desktop/Multi%20Level%20Parking%20System/render.yaml).
4. Deploy and open:
   - `https://<your-render-service>.onrender.com/actuator/health`
   - `https://<your-render-service>.onrender.com/swagger-ui.html`

## Postman Collection
Import:

- [`postman/Multi-Level-Parking-System.postman_collection.json`](/c:/Users/arche/Desktop/Multi%20Level%20Parking%20System/postman/Multi-Level-Parking-System.postman_collection.json)

It includes health, parking, dashboard, simulation, benchmark, and reset requests.

## Testing Coverage
The test suite covers:

- best-fit allocation correctness
- incompatible vehicle rejection
- EV charging prioritization and fallback
- pricing calculation
- slot release and reuse after exit
- analytics accuracy
- benchmark result generation
- REST endpoint behavior through MockMvc integration tests

Key tests:

- [`src/test/java/com/parking/ParkingManagerTest.java`](/c:/Users/arche/Desktop/Multi%20Level%20Parking%20System/src/test/java/com/parking/ParkingManagerTest.java)
- [`src/test/java/com/parking/PricingServiceTest.java`](/c:/Users/arche/Desktop/Multi%20Level%20Parking%20System/src/test/java/com/parking/PricingServiceTest.java)
- [`src/test/java/com/parking/BenchmarkRunnerTest.java`](/c:/Users/arche/Desktop/Multi%20Level%20Parking%20System/src/test/java/com/parking/BenchmarkRunnerTest.java)
- [`src/test/java/com/parking/api/ParkingApiIntegrationTest.java`](/c:/Users/arche/Desktop/Multi%20Level%20Parking%20System/src/test/java/com/parking/api/ParkingApiIntegrationTest.java)

## Benchmarking Approach
The benchmark does not hardcode improvement claims.

Instead it:

1. generates a deterministic arrival stream from a seeded scenario
2. runs the stream once on the naive allocator
3. runs the same stream again on the optimized allocator
4. reports measured latency, throughput, occupancy efficiency, rejection rate, and EV fulfillment

This makes the benchmark explainable in interviews and honest on a resume.

## Deployment Notes
This is a backend service, not a static website. The strongest deployment options are:

- Render
- Railway
- Fly.io
- AWS Elastic Beanstalk / ECS / EC2
- any Docker-capable host

If you later add a frontend dashboard, that frontend can be hosted separately while this API remains the system-of-record backend.

## Interview Explanation
### Why the design scales
- allocation, pricing, analytics, simulation, and HTTP contracts are separated
- the REST layer is thin and does not own business logic
- persistence can later replace the in-memory repository without rewriting controllers
- new policies can be added via new `AllocationStrategy` or `PricingService` implementations

### Trade-offs
- the current system is in-memory and optimized for demonstration, not distributed concurrency
- the live lot is stateful within a single application instance
- a production version would likely add database persistence, authentication, and optimistic locking or queue-based coordination

### How analytics helps capacity and revenue planning
- occupancy by level shows pressure hotspots
- slot-type utilization shows whether compact, large, or truck inventory is undersized
- rejected vehicles reveal lost demand
- EV fulfillment exposes charging-capacity shortages
- revenue by vehicle type helps pricing and floor-premium tuning

## Resume-Ready Bullets
- Built a Spring Boot REST API for a multi-level parking management system with optimized O(log n) slot allocation, EV-charging prioritization, and modular pricing and analytics services.
- Designed benchmarking and simulation workflows to compare naive and optimized parking allocators using deterministic workloads and measured throughput, rejection, and utilization metrics.
- Delivered a portfolio-ready backend with Swagger/OpenAPI documentation, Maven wrapper, Docker packaging, unit tests, and API integration tests.
