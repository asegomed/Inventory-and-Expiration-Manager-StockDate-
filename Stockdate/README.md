# StockDate

StockDate is a Spring Boot web application for tracking food inventory batches
and their expiration dates. The frontend is served by Spring Boot, and batch
data is stored in the local SQLite database `stockdate.db`.

## Requirements

- JDK 17 or later.
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

To run the Gradle verification task:

```text
Windows:     gradlew.bat test
macOS/Linux: sh gradlew test
```

There are currently no test source files under `src/test`, so Gradle reports
`NO-SOURCE` for the test task until tests are added.

## Pages

- `/` and `/main.html`: home dashboard with batch totals, upcoming expirations,
  and the nearest expiration.
- `/productos.html`: searchable product list and form to add or edit batches.
  Use the **Main menu** button to return to the dashboard.
- `/index.html`: redirects to `/main.html`.

The product page and dashboard use the same inventory API:

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/inventory` | List batches, ordered by expiration date |
| `POST` | `/api/inventory` | Register a batch |
| `PUT` | `/api/inventory/{id}` | Update an existing batch |

Invalid batch data returns HTTP 400; updating a missing batch returns HTTP
404. The UI validates required fields, positive quantity, and past `USE_BY`
dates. Expiration statuses are calculated as `EXPIRED`, `RED`, `YELLOW`, or
`GREEN`.

## Architecture

The Java sources are under `src/main/java/com/example/stockdate/`, matching
their declared package names:

- `presentation/controller/` receives HTTP requests and delegates to services.
- `presentation/dto/` defines request and response data structures.
- `service/` contains inventory validation and expiration-status logic.
- `persistence/entity/` maps Java objects to database tables.
- `persistence/repository/` provides Spring Data JPA database operations.
- `persistence/converter/` maps SQLite ISO date text to and from `LocalDate`.
- `config/` contains application support such as browser startup.

`InventoryManager` depends on `InventoryRepository` rather than containing
SQL. `BatchEntity` maps the existing `batch` table. The date converter is
necessary because SQLite stores `expiration_date` as `YYYY-MM-DD` text.
`schema.sql` creates the table when it is absent; Hibernate schema generation
is disabled to preserve the existing database schema.

The web assets are in `src/main/resources/static/`: `main.html` is the
dashboard, `productos.html` is the product-management page, `css/styles.css`
contains shared styling, and `js/main.js` / `js/app.js` implement the respective
page behavior.

## Authentication and planned features

Login and role-based permissions are not implemented. Spring Security is not
included, so there is no framework-generated login prompt. User management,
batch deletion, dispatch/discard workflows, expiration-alert services, and
reports remain future work.

## Build and local data

`build.gradle` configures Java 17, Spring Boot, Spring Data JPA, SQLite,
validation, Actuator, and the JaCoCo, PMD, and Checkstyle plugins. On Windows,
Gradle build output is stored under
`%LOCALAPPDATA%\Stockdate\Stockdate\build`; elsewhere Gradle uses its normal
build directory.

The SQLite database is `stockdate.db` in this project directory. Keep it when
backing up or moving the application if its inventory data should be retained.

See [`docs/explicacion.md`](docs/explicacion.md) for a detailed guide to the
project folders and files.
