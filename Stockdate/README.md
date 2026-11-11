# StockDate

StockDate is a Spring Boot web application for tracking food inventory batches
and their expiration dates. The frontend is served by Spring Boot, and batch
data is stored in the local SQLite database `stockdate.db`.

## Requirements

- JDK 17 or later (Gradle itself also needs Java 17+ to run).
- Gradle is provided by the included Wrapper.
- Internet access may be required on first launch to download Gradle and
  project dependencies.

## Run the application

Open a terminal in this directory (the one containing `settings.gradle`) and
run:

```text
Windows:     start.bat
macOS/Linux: ./start.sh
```

The application starts at `http://localhost:8080` and attempts to open the
address in the default browser. If it does not open automatically, navigate
there manually. Stop the application with `Ctrl+C`.

## Build, tests and code quality

```text
Windows:     gradlew.bat build
macOS/Linux: ./gradlew build
```

`build` compiles the code, runs the tests, Checkstyle and PMD, and generates
the JaCoCo coverage report. To run only the tests use `gradlew test`.

| Tool | Configuration | Report (Windows) |
|---|---|---|
| JUnit 5 + Mockito | `src/test/java/stockdate/` | `build/reports/tests/test/index.html` |
| JaCoCo | `build.gradle` (runs after `test`) | `build/reports/jacoco/test/html/index.html` |
| Checkstyle | `config/checkstyle/checkstyle.xml` | `build/reports/checkstyle/main.html` |
| PMD | `config/pmd/ruleset.xml` | `build/reports/pmd/main.html` |

On Windows the `build/` folder is placed under
`%LOCALAPPDATA%\Stockdate\Stockdate\build`; elsewhere Gradle uses its normal
`build/` directory.

## Pages

- `/` and `/main.html`: home dashboard with batch totals, upcoming expirations,
  and the nearest expiration.
- `/productos.html`: searchable product list and form to add or edit batches.
  Use the **Main menu** button to return to the dashboard.
- `/index.html`: redirects to `/main.html`.

## REST API

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/inventory` | List batches, ordered by expiration date (FEFO) |
| `POST` | `/api/inventory` | Register a batch |
| `PUT` | `/api/inventory/{id}` | Update an existing batch |
| `GET` | `/api/dashboard` | Dashboard summary: count per tier and pinned batches (Expired and Red) |
| `GET` | `/actuator/health` | Application health check |

Invalid batch data returns HTTP 400; updating a missing batch returns HTTP
404. Every batch is returned with `daysUntilExpiration`, a `status`
(`EXPIRED`, `RED`, `YELLOW` or `GREEN`) and `dispatchable` (`false` for a
use-by batch that has already expired).

The tier thresholds are configured in `application.properties`:

```properties
stockdate.alert.red-days=7
stockdate.alert.yellow-days=14
```

## Architecture

The application follows a layered architecture. Java sources are in
`src/main/java/stockdate/` and tests in `src/test/java/stockdate/`:

```text
src/main/
├── java/stockdate/
│   ├── config/                   # Security, browser launcher
│   ├── persistence/
│   │   ├── converter/            # LocalDate <-> ISO text for SQLite
│   │   ├── entity/               # JPA entities (tables)
│   │   └── repository/           # Spring Data JPA repositories
│   ├── presentation/
│   │   ├── controller/           # REST controllers and error handler
│   │   └── dto/                  # Request and response objects
│   └── service/                  # Business logic
│       ├── factory/              # Factory pattern
│       ├── observer/             # Observer pattern
│       └── strategy/             # Strategy pattern
└── resources/
    ├── static/                   # HTML, CSS and JavaScript
    ├── application.properties
    └── schema.sql
```

- `presentation/controller/` receives HTTP requests and delegates to services.
  It only exchanges DTOs, never entities.
- `service/` contains the business rules (validation, expiration status,
  dashboard summary).
- `persistence/` maps Java objects to the SQLite database through Spring Data
  JPA. `schema.sql` creates the tables; Hibernate schema generation is
  disabled (`ddl-auto=none`).
- `config/SecurityConfig.java` adds Spring Security with every route open for
  now, and provides the BCrypt `PasswordEncoder` for the future login.

## Design patterns

| Pattern | Files | Role |
|---|---|---|
| **Observer** | `service/observer/Observer.java`, `InventoryManager` (subject), `ExpirationAlertService` (observer) | After a batch is saved, `InventoryManager` notifies its observers. `ExpirationAlertService` invalidates its cached dashboard asynchronously (`@Async`); the summary is also rebuilt when the date changes. |
| **Strategy** | `service/strategy/Strategy.java`, `UseByStrategy`, `BestBeforeStrategy` | Rules that depend on the date type: whether a past date is accepted and whether an expired batch can be dispatched. |
| **Factory** | `service/factory/Factory.java` | Single place that selects the strategy for a date type and creates `BatchEntity` and `BatchDTO` objects (days left, tier and `dispatchable`). |

Each pattern has its own unit tests (`ObserverTests`, `StrategyTests`,
`FactoryTests`) that run without a database.

## Current status

Implemented:

- Batch registration, editing and listing (UC-02, partly UC-03).
- Dashboard summary endpoint with cache (partly UC-05).
- Observer, Strategy and Factory design patterns.
- Build with tests, JaCoCo, Checkstyle and PMD.

Not implemented yet:

- Login, roles and user management (UC-01, UC-08). Spring Security is
  included but every route is open.
- Dispatch with FEFO (UC-04), discards (UC-06) and monthly reports (UC-07).
- Normalized database schema: only the `batch` table exists; `users`,
  `categories` and `stock_movements` are still to be created.
- The frontend still calculates the dashboard itself instead of calling
  `/api/dashboard`.

## Local data

The SQLite database is `stockdate.db` in this project directory. Keep it when
backing up or moving the application if its inventory data should be retained.

See [`docs/explicacion.md`](docs/explicacion.md) for a detailed guide to the
project folders and files.
