# StockDate Project Structure

This guide describes the StockDate project structure and identifies which
features are implemented or planned. It is documentation, not application
source code, and is not required for the application to run.

## Project overview

```text
Stockdate/
├── .gradle/                              # Local Gradle state and caches
├── build/                                # Generated build output (not on Windows, see below)
├── config/
│   ├── checkstyle/checkstyle.xml         # Checkstyle rules
│   └── pmd/ruleset.xml                   # PMD rules
├── docs/
│   └── explicacion.md                    # This project structure guide
├── gradle/wrapper/                       # Gradle Wrapper files
├── Information/                          # Project reference documents (Phase 1 PDF)
├── src/
│   ├── main/
│   │   ├── java/stockdate/               # Java package root
│   │   │   ├── config/
│   │   │   ├── persistence/
│   │   │   │   ├── converter/
│   │   │   │   ├── entity/
│   │   │   │   └── repository/
│   │   │   ├── presentation/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/request/
│   │   │   │   └── dto/response/
│   │   │   ├── service/
│   │   │   │   ├── factory/              # Factory pattern
│   │   │   │   ├── observer/             # Observer pattern
│   │   │   │   └── strategy/             # Strategy pattern
│   │   │   ├── ExpirationDateType.java
│   │   │   └── StockdateApplication.java
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/
│   │       │   ├── js/
│   │       │   ├── index.html
│   │       │   ├── main.html
│   │       │   └── productos.html
│   │       ├── application.properties
│   │       └── schema.sql
│   └── test/java/stockdate/              # JUnit 5 and Mockito tests
├── .gitattributes
├── .gitignore
├── build.gradle
├── gradlew
├── gradlew.bat
├── README.md
├── run.bat                               # Windows launcher implementation
├── settings.gradle
├── start.bat                             # Windows application launcher
├── start.sh                              # macOS/Linux application launcher
└── stockdate.db                          # Local SQLite database, if present
```

## Application source: `src/main`

### Java source: `src/main/java/stockdate`

The project uses the standard Gradle/Maven layout: Java sources in
`src/main/java`, resources in `src/main/resources` and tests in
`src/test/java`. Each folder matches its declared package (for example
`src/main/java/stockdate/service/` is `package stockdate.service`).

- `StockdateApplication.java`: application entry point; starts Spring Boot and
  its web server. `@EnableAsync` enables asynchronous methods (used by the
  Observer notification).
- `ExpirationDateType.java`: defines supported date types, `USE_BY` and
  `BEST_BEFORE`.

#### `config/`

Contains application support components:

- `BrowserLauncher.java`: attempts to open `http://localhost:8080` in the
  default browser once the server has started.
- `ClockConfig.java`: provides the `Clock` bean, the single source of "today"
  for the services, so tests can use a fixed date.
- `SecurityConfig.java`: Spring Security configuration. For now every route is
  open and CSRF is disabled, so the application works as before until login
  (UC-01) and roles are implemented. It already provides the BCrypt
  `PasswordEncoder` that user management will use.
- SQLite's Hibernate dialect and datasource settings are configured in
  `application.properties`; there is no separate dialect configuration class.

#### `presentation/`

Contains the HTTP API layer that communicates with the frontend and other
clients. It only exchanges DTOs, never entities:

- `controller/`: handles HTTP requests and responses.
  - `InventoryController.java`: exposes batch listing
    (`GET /api/inventory`), creation (`POST /api/inventory`), updates
    (`PUT /api/inventory/{id}`) and the dashboard summary
    (`GET /api/dashboard`, delegated to `ExpirationAlertService`).
  - `ApiExceptionHandler.java`: converts invalid-request exceptions to HTTP
    400 responses and missing-batch exceptions to HTTP 404 responses.
  - `AdminController.java`, `AuthController.java`, and `ReportController.java`:
    placeholders for administration, authentication, and reporting; these
    features are not implemented yet.
- `dto/`: data transfer objects used by the API.
  - `request/`: request data received from clients.
    - `CreateBatchRequest.java`: fields needed to register or edit a batch.
      Bean Validation annotations (`@NotBlank`, `@Positive`, `@NotNull`)
      declare the field rules, and `trimmed()` removes surrounding spaces.
    - `CreateUserRequest.java`: placeholder for user creation data.
    - `DiscardRequest.java`: placeholder for an inventory discard request.
    - `DispatchRequest.java`: placeholder for an inventory dispatch request.
  - `response/`: data returned to clients.
    - `ApiError.java`: error response structure.
    - `BatchDTO.java`: batch data, including days until expiration, status
      tier (`EXPIRED`, `RED`, `YELLOW`, `GREEN`) and `dispatchable`.
    - `DashboardDTO.java`: dashboard summary with the count per tier and the
      pinned batches (Expired and Red).
    - `ReportDTO.java`: placeholder for report response data.
    - `UserDTO.java`: placeholder for user response data.

#### `service/`

Contains the business rules:

- `InventoryManager.java`: validates batches with the Bean Validation
  `Validator` and the date strategy, saves them through
  `InventoryRepository` and notifies its observers after each change. It is
  the subject of the Observer pattern.
- `ExpirationAlertService.java`: builds the dashboard summary and keeps it in a
  cache. It is the observer of the Observer pattern: a stock change or a new
  day invalidates the cache.
- `observer/Observer.java`: **Observer pattern** interface
  (`onStockChanged()`).
- `strategy/`: **Strategy pattern**, rules that depend on the date type.
  - `Strategy.java`: interface with `validateExpirationDate()` and
    `canBeDispatched()`.
  - `UseByStrategy.java`: a use-by date cannot be in the past and an expired
    batch cannot be dispatched.
  - `BestBeforeStrategy.java`: past dates are accepted and the batch can still
    be dispatched.
- `factory/Factory.java`: **Factory pattern**, selects the strategy for a date
  type and creates `BatchEntity` and `BatchDTO` objects. It reads the tier
  thresholds (`stockdate.alert.red-days`, `stockdate.alert.yellow-days`) from
  `application.properties`.
- `ReportService.java`: placeholder for future report generation.
- `UserService.java`: placeholder for future user management.

#### `persistence/`

Groups database-related classes. Persistence uses Spring Data JPA with the
SQLite database and the SQL schema.

- `entity/`: data models.
  - `BatchEntity.java`: JPA entity mapped to the existing `batch` table.
  - `CategoryEntity.java`: planned category model.
  - `StockMovementEntity.java`: planned inventory movement model.
  - `UserEntity.java`: planned user model.
- `converter/`: conversions between Java types and database representations.
  - `LocalDateStringConverter.java`: converts SQLite `YYYY-MM-DD` text to and
    from Java `LocalDate`, matching how dates are stored in the existing table.
- `repository/`: Spring Data JPA data access interfaces.
  - `InventoryRepository.java`: Spring Data JPA repository for batches.
  - `MovementRepository.java`: planned stock movement repository.
  - `UserRepository.java`: planned user repository.

### Web interface and resources: `src/main/resources`

- `application.properties`: Spring Boot settings, including the application
  name, automatic browser launch, SQLite connection, Hibernate dialect and the
  expiration tier thresholds.
- `schema.sql`: creates the SQLite `batch` table if it does not already exist.
  Hibernate schema generation is disabled (`ddl-auto=none`).
- `static/`: frontend files served by Spring Boot.
  - `index.html`: redirects the application root to the dashboard. It will
    become the login page with UC-01.
  - `main.html`: home dashboard with inventory and expiration summaries.
  - `productos.html`: searchable product list and batch add/edit form, with a
    **Main menu** link back to the dashboard.
  - `css/styles.css`: shared layout, colors, and responsive styles.
  - `js/main.js`: loads the dashboard data from the inventory API. It still
    calculates the summary itself instead of calling `/api/dashboard`.
  - `js/app.js`: loads/searches products and handles batch creation and editing
    through the same inventory API.

## Tests: `src/test/java/stockdate`

Tests use JUnit 5 and Mockito. JaCoCo measures their coverage after every
`gradlew test`.

- `StockdateApplicationTests.java`: checks that the Spring context starts.
- `presentation/controller/InventoryControllerTests.java`: HTTP tests for the
  inventory and dashboard endpoints (in-memory SQLite).
- `service/InventoryManagerTests.java`: batch registration, listing, update and
  validation (in-memory SQLite).
- `service/ObserverTests.java`: the subject notifies every observer, and the
  dashboard cache is invalidated on stock or date changes (mocked repository).
- `service/strategy/StrategyTests.java`: use-by and best-before rules.
- `service/factory/FactoryTests.java`: strategy selection, entity creation and
  tier classification.

## Build tools, documentation, and root files

- `gradle/wrapper/gradle-wrapper.jar` and
  `gradle/wrapper/gradle-wrapper.properties`: provide the pinned Gradle
  Wrapper version.
- `gradlew` and `gradlew.bat`: start the Gradle Wrapper on macOS/Linux and
  Windows, respectively.
- `start.sh`: starts the application on macOS/Linux using the Gradle Wrapper.
- `start.bat`: starts the application on Windows through `run.bat`.
- `run.bat`: finds an available JDK on Windows and starts the application with
  Gradle.
- `build.gradle`: defines plugins, dependencies, Java compatibility, and build
  tasks. Main dependencies: Spring Web, Spring Data JPA, Validation, Actuator,
  Spring Security, SQLite JDBC and the Hibernate community dialects. It also
  configures JaCoCo, Checkstyle and PMD. On Windows, it writes build output to
  `%LOCALAPPDATA%\Stockdate\Stockdate\build` to avoid synchronization issues
  in locations such as OneDrive.
- `config/checkstyle/checkstyle.xml`: Checkstyle code style rules (naming,
  imports, braces, line length).
- `config/pmd/ruleset.xml`: PMD rules (best practices, error-prone code,
  multithreading and security).
- `settings.gradle`: sets the Gradle project name.
- `.gitignore`: lists local and generated files that should be excluded from
  Git.
- `.gitattributes`: defines how Git handles file attributes.
- `README.md`: the project guide, including requirements, run instructions,
  API and design patterns.
- `docs/explicacion.md`: this detailed project-structure guide.
- `Information/`: project reference documents.
- `stockdate.db`: SQLite inventory data created at runtime, if present. Keep
  it to retain the current database contents.

## Local and generated folders

- `.gradle/`: local Gradle caches and state; Gradle can regenerate them.
- `.idea/` and `*.iml`: IntelliJ IDEA settings that may differ between
  developers; they are ignored by Git.
- `build/`: generated output (compiled classes, test, coverage and analysis
  reports). It is not source code and can be regenerated. On Windows, build
  output is stored under the local application data directory described above.

These folders are not required when sharing the source code. Include `src/`,
`config/`, `gradle/wrapper/`, the Gradle configuration files, and the launcher
scripts. A JDK 17 or later is required, and Gradle may need internet access on
the first run to download dependencies.
