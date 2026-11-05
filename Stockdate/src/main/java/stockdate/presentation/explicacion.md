# StockDate Project Structure

This guide describes the current StockDate project skeleton, including the
purpose of its folders and files and which features are implemented or planned.
It is stored in `presentation/` as documentation; it is not Java source code
and is not required for the application to run.

## Project overview

```text
Stockdate/
├── .gradle/                              # Local Gradle state and caches
├── .idea/                                # Local IntelliJ IDEA settings
├── build/                                # Previous/generated build output
├── gradle/wrapper/                       # Gradle Wrapper files
├── Information/                          # Project reference documents
├── src/
│   ├── main/
│   │   ├── java/stockdate/
│   │   │   ├── config/
│   │   │   ├── persistence/
│   │   │   │   ├── entity/
│   │   │   │   └── repository/
│   │   │   ├── presentation/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/request/
│   │   │   │   ├── dto/response/
│   │   │   │   └── README.md
│   │   │   ├── service/
│   │   │   ├── ExpirationDateType.java
│   │   │   └── StockdateApplication.java
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/
│   │       │   ├── js/
│   │       │   └── index.html
│   │       ├── application.properties
│   │       └── schema.sql
│   └── test/java/com/example/stockdate/
├── .gitattributes
├── .gitignore
├── build.gradle
├── gradlew
├── gradlew.bat
├── HELP.md
├── README.md
├── run.bat
├── settings.gradle
├── stockdate.db                         # Local SQLite database, if present
└── Stockdate.iml
```

## Application source: `src/main`

### Java source: `src/main/java/stockdate`

- `StockdateApplication.java`: application entry point; starts Spring Boot and
  its web server.
- `ExpirationDateType.java`: defines supported date types, including `USE_BY`
  and `BEST_BEFORE`.

#### `config/`

Contains application configuration and supporting components:

- `BrowserLauncher.java`: attempts to open `http://localhost:8080` in the
  default browser once the server has started.
- `SecurityConfig.java`: placeholder for future security configuration; it
  does not currently implement authentication or authorization.
- `SQLiteDialectConfig.java`: placeholder for future SQLite configuration.
  The current database connection is configured in `application.properties`.

#### `presentation/`

Contains the HTTP API layer that communicates with the frontend and other
clients:

- `controller/`: handles HTTP requests and responses.
  - `InventoryController.java`: exposes batch listing (`GET`), creation
    (`POST`), and updates (`PUT /api/inventory/{id}`).
  - `ApiExceptionHandler.java`: converts invalid-request exceptions to HTTP
    400 responses and missing-batch exceptions to HTTP 404 responses.
  - `AdminController.java`, `AuthController.java`, and `ReportController.java`:
    placeholders for administration, authentication, and reporting; these
    features are not implemented yet.
- `dto/`: data transfer objects used by the API.
  - `request/`: request data received from clients.
    - `CreateBatchRequest.java`: fields needed to register an inventory batch.
    - `CreateUserRequest.java`: placeholder for user creation data.
    - `DiscardRequest.java`: placeholder for an inventory discard request.
    - `DispatchRequest.java`: placeholder for an inventory dispatch request.
  - `response/`: data returned to clients.
    - `ApiError.java`: error response structure.
    - `BatchDTO.java`: batch data, including its expiration status.
    - `DashboardDTO.java`: placeholder for dashboard response data.
    - `ReportDTO.java`: placeholder for report response data.
    - `UserDTO.java`: placeholder for user response data.
- `README.md`: this guide to the complete project skeleton.

#### `service/`

Contains application and inventory rules:

- `InventoryManager.java`: validates, stores, and sorts batches using SQLite,
  updates existing records, and calculates the time remaining until expiration
  and the batch status.
- `ExpirationAlertService.java`: placeholder for future expiration alerts.
- `ReportService.java`: placeholder for future report generation.
- `UserService.java`: placeholder for future user management.

#### `persistence/`

Groups database-related classes. The `entity/` and `repository/` classes are
currently placeholders; the implemented inventory persistence uses
`JdbcTemplate` and the SQL schema instead.

- `entity/`: planned data models.
  - `BatchEntity.java`: planned batch model.
  - `CategoryEntity.java`: planned category model.
  - `StockMovementEntity.java`: planned inventory movement model.
  - `UserEntity.java`: planned user model.
- `repository/`: planned data access classes.
  - `InventoryRepository.java`: planned batch repository.
  - `MovementRepository.java`: planned stock movement repository.
  - `UserRepository.java`: planned user repository.

### Web interface and resources: `src/main/resources`

- `application.properties`: Spring Boot settings, including the application
  name, automatic browser launch, and SQLite connection.
- `schema.sql`: creates the SQLite `batch` table if it does not already exist.
- `static/`: frontend files served by Spring Boot.
  - `index.html`: page structure and interface sections.
  - `css/styles.css`: layout, colors, and responsive styles.
  - `js/app.js`: navigation, inventory loading and filtering, expiration
    summaries, batch form submission, and editing existing batches through the
    API.

## Automated tests: `src/test`

Contains tests that verify application behavior; these are not part of the
normal application server:

- `java/com/example/stockdate/StockdateApplicationTests.java`: checks that the
  Spring Boot application context starts.
- `service/InventoryManagerTests.java`: tests inventory operations and
  validation rules.
- `presentation/controller/InventoryControllerTests.java`: tests inventory
  HTTP requests, including batch creation, retrieval, and invalid input.
- `service/package-info.java` and
  `presentation/controller/package-info.java`: preserve test package
  structure.

## Build tools, documentation, and root files

- `gradle/wrapper/gradle-wrapper.jar` and
  `gradle/wrapper/gradle-wrapper.properties`: provide the pinned Gradle
  Wrapper version.
- `gradlew` and `gradlew.bat`: start the Gradle Wrapper on macOS/Linux and
  Windows, respectively.
- `run.bat`: finds an available JDK on Windows and starts the application with
  Gradle.
- `build.gradle`: defines plugins, dependencies, Java compatibility, and build
  tasks. On Windows, it writes build output to
  `%LOCALAPPDATA%\Stockdate\Stockdate\build` to avoid synchronization issues
  in locations such as OneDrive.
- `settings.gradle`: sets the Gradle project name.
- `.gitignore`: lists local and generated files that should be excluded from
  Git.
- `.gitattributes`: defines how Git handles file attributes.
- `README.md`: the main project guide, including requirements and run
  instructions.
- `HELP.md`: generic documentation supplied by the Spring Boot project
  template.
- `Stockdate.iml`: local IntelliJ IDEA module settings; not needed to run the
  application with the Gradle Wrapper.
- `Information/`: project reference documents.
- `stockdate.db`: SQLite inventory data created at runtime, if present. Keep
  it to retain the current database contents.

## Local and generated folders

- `.gradle/`: local Gradle caches and state; Gradle can regenerate them.
- `.idea/`: IDE settings that may differ between developers.
- `build/`: generated output from earlier builds. It is not source code and
  can be regenerated. On Windows, new build output is stored under the local
  application data directory described above.

These folders are not required when sharing the source code. Include `src/`,
`gradle/wrapper/`, the Gradle configuration files, and `run.bat`. A JDK 17 or
later is required, and Gradle may need internet access on the first run to
download dependencies.
