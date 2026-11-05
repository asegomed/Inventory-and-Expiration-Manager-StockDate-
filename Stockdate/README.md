# StockDate

StockDate is a web application for tracking food inventory batches and their
expiration dates. Its Java and Spring Boot backend serves an HTML, CSS, and
JavaScript interface and exposes an inventory API. Batch data is stored in a
local SQLite database, so it remains available after the application closes.

## Requirements

- JDK 17 or later. The project compiles for Java 17.
- Windows: run the application with `run.bat` from the project directory.
- Gradle is provided by the included Wrapper; a separate Gradle installation
  is not required.
- An internet connection may be needed on the first run to download Gradle and
  project dependencies.

## Run the application

Open a terminal in the directory containing `settings.gradle`, then run:

```powershell
.\run.bat
```

The application starts at `http://localhost:8080` and attempts to open that
address in the default browser. If the browser does not open automatically,
navigate to the address manually. Stop the application with `Ctrl+C`.

To run the tests on Windows:

```powershell
.\gradlew.bat test
```

## Current functionality

- View an inventory dashboard with the total number of batches, batches
  expiring within seven days, and the next expiration date.
- Browse batches ordered by expiration date and search by product, category,
  or storage location.
- Edit an existing batch and save its changes to SQLite.
- Register a batch with its product, category, quantity, storage location,
  expiration date, and date type (`USE_BY` or `BEST_BEFORE`).
- Validate required fields, positive quantities, and past `USE_BY` dates.
- Receive expiration status information for each batch.

The API provides `GET /api/inventory` to list batches, `POST /api/inventory`
to create one, and `PUT /api/inventory/{id}` to update an existing batch.
Invalid input returns HTTP 400; updating a batch that does not exist returns
HTTP 404. User authentication, roles, stock dispatch and discard operations,
and reports are not implemented yet.

## Project structure

```text
Stockdate/
├── Information/                   # Project reference documents
├── gradle/wrapper/                # Pinned Gradle Wrapper files
├── src/
│   ├── main/
│   │   ├── java/stockdate/        # Backend Java source
│   │   │   ├── config/            # Application configuration
│   │   │   ├── persistence/       # Persistence-related packages
│   │   │   ├── presentation/      # HTTP controllers and API data objects
│   │   │   └── service/           # Application and inventory logic
│   │   └── resources/
│   │       ├── static/            # Web interface (HTML, CSS, JavaScript)
│   │       ├── application.properties
│   │       └── schema.sql         # SQLite schema
│   └── test/                      # Automated tests
├── build.gradle                   # Plugins, dependencies, and build settings
├── gradlew / gradlew.bat          # Gradle Wrapper launchers
├── run.bat                        # Windows application launcher
├── settings.gradle                # Gradle project name
└── README.md                      # This guide
```

The source tree contains placeholders for planned features. See
`src/main/java/stockdate/presentation/README.md` for a file-by-file guide to
the current project skeleton.

## Build output and SQLite data

On Windows, Gradle writes build output to
`%LOCALAPPDATA%\Stockdate\Stockdate\build` instead of the project directory.
This avoids conflicts with files synchronized by services such as OneDrive.
On other systems, Gradle uses the default project `build/` directory.

The SQLite database is stored as `stockdate.db` in the project directory.
Keep this file if you want to preserve the inventory data when moving or
backing up the project. It is local application data, not source code.

## Sharing the project

Include `src/`, `gradle/wrapper/`, the Gradle build files, `run.bat`, and the
documentation. Generated `.gradle/` and `build/` folders and IDE-specific
`.idea/` settings are not required. Include `stockdate.db` only if the
recipient should receive the current inventory data. The recipient needs a
JDK 17 or later and may need internet access on the first run.
