# StockDate Project Structure

This guide describes the StockDate project structure and identifies which
features are implemented or planned. It is documentation, not application
source code, and is not required for the application to run.

## Project overview

```text
Stockdate/
├── .gradle/                              # Local Gradle state and caches
├── .idea/                                # Local IntelliJ IDEA settings
├── build/                                # Previous/generated build output
├── docs/
│   └── explicacion.md                    # This project structure guide
├── gradle/wrapper/                       # Gradle Wrapper files
├── start.bat                              # Windows application launcher
├── start.sh                               # macOS/Linux application launcher
├── run.bat                                # Windows launcher implementation
├── Information/                          # Project reference documents
├── src/
│   ├── main/
│   │   ├── stockdate/                    # Java package root
│   │   │   ├── config/
│   │   │   ├── persistence/
│   │   │   │   ├── converter/
│   │   │   │   ├── entity/
│   │   │   │   └── repository/
│   │   │   ├── presentation/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/request/
│   │   │   │   ├── dto/response/
│   │   │   ├── service/
│   │   │   ├── ExpirationDateType.java
│   │   │   └── StockdateApplication.java
│   │   └── resources/                    # Only other direct folder in main/
│   │       ├── static/
│   │       │   ├── css/
│   │       │   ├── js/
│   │       │   ├── index.html
│   │       │   ├── main.html
│   │       │   └── productos.html
│   │       ├── application.properties
│   │       └── schema.sql
│   └── test/                              # No test sources currently
├── .gitattributes
├── .gitignore
├── build.gradle
├── gradlew
├── gradlew.bat
├── README.md
├── run.bat
├── settings.gradle
├── stockdate.db                         # Local SQLite database, if present
└── Stockdate.iml
```

## Application source: `src/main`

### Java source: `src/main/stockdate`

Gradle's `main` Java source set uses `src/main` as its source root. This keeps
the Java package folder (`stockdate/`) directly beside `resources/`, as
required by this project's source layout. There is no `src/main/java/`
directory; `src/main/` contains only `stockdate/` and `resources/`.

- `StockdateApplication.java`: application entry point; starts Spring Boot and
  its web server.
- `ExpirationDateType.java`: defines supported date types, including `USE_BY`
  and `BEST_BEFORE`.

#### `config/`

Contains application support components:

- `BrowserLauncher.java`: attempts to open `http://localhost:8080` in the
  default browser once the server has started.
- SQLite's Hibernate dialect and datasource settings are configured in
  `application.properties`; there is no separate dialect configuration class.
- Spring Security and its generated login page are not included. Authentication
  and roles are future work.

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
#### `service/`

Contains application and inventory rules:

- `InventoryManager.java`: validates batches, uses `InventoryRepository` to
  persist them, and calculates the time remaining until expiration and the
  batch status.
- `ExpirationAlertService.java`: placeholder for future expiration alerts.
- `ReportService.java`: placeholder for future report generation.
- `UserService.java`: placeholder for future user management.

#### `persistence/`

Groups database-related classes. Inventory persistence uses Spring Data JPA
with the SQLite database and the SQL schema.

- `entity/`: planned data models.
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
  name, automatic browser launch, and SQLite connection.
- `schema.sql`: creates the SQLite `batch` table if it does not already exist.
- `static/`: frontend files served by Spring Boot.
  - `index.html`: redirects the application root to the dashboard.
  - `main.html`: home dashboard with inventory and expiration summaries.
  - `productos.html`: searchable product list and batch add/edit form, with a
    **Main menu** link back to the dashboard.
  - `css/styles.css`: shared layout, colors, and responsive styles.
  - `js/main.js`: loads the dashboard data from the inventory API.
  - `js/app.js`: loads/searches products and handles batch creation and editing
    through the same inventory API.

## Tests: `src/test`

There are currently no test source files in this directory. The Gradle `test`
task is configured to use JUnit Platform, but reports `NO-SOURCE` until tests
are added.

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
  tasks. On Windows, it writes build output to
  `%LOCALAPPDATA%\Stockdate\Stockdate\build` to avoid synchronization issues
  in locations such as OneDrive.
- `settings.gradle`: sets the Gradle project name.
- `.gitignore`: lists local and generated files that should be excluded from
  Git.
- `.gitattributes`: defines how Git handles file attributes.
- `README.md`: the project guide, including requirements and run
  instructions.
- `docs/explicacion.md`: this detailed project-structure guide.
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
`gradle/wrapper/`, the Gradle configuration files, and the launcher scripts.
A JDK 17 or later is required, and Gradle may need internet access on the first
run to download dependencies.
