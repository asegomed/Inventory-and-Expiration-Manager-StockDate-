# StockDate: Inventory and Expiration Manager

StockDate is a web application for tracking food inventory batches and their
expiration dates. The current MVP provides a dashboard and a product page for
viewing, searching, adding, and editing batches stored in a local SQLite
database.

## Current status

Implemented: dashboard summaries, batch listing and search, batch creation and
editing, expiration status calculation, and persistence through Spring Data
JPA.

Not implemented yet: login and role permissions, batch deletion, stock
dispatch/discard workflows, expiration-alert services, and reports. The
application currently has no login prompt.

## Technology

- Java 17+ and Spring Boot
- HTML, CSS, and JavaScript
- SQLite through Spring Data JPA and Hibernate
- Gradle Wrapper

## Run

From `Stockdate/` run `start.bat` on Windows or `./start.sh` on macOS/Linux.
The application is served at `http://localhost:8080`. See
[`Stockdate/README.md`](Stockdate/README.md) for detailed operating
instructions, API endpoints, and project structure. The explanation of the
Java layers and folders is in
[`Stockdate/docs/explicacion.md`](Stockdate/docs/explicacion.md).

## Project layout

```text
StockDate/
├── Esquemas_Phase1/             # Planning and reference material
├── Stockdate/
│   ├── docs/                    # Project documentation
│   ├── src/
│   │   └── main/
│   │       ├── stockdate/       # Java packages (package root: stockdate)
│   │       │   ├── config/
│   │       │   ├── persistence/
│   │       │   ├── presentation/
│   │       │   └── service/
│   │       └── resources/       # Configuration, schema, and web assets
│   │           └── static/      # HTML, CSS, and JavaScript pages
│   ├── build.gradle
│   ├── start.bat / start.sh
│   └── stockdate.db             # Local inventory database
└── README.md
```

`src/main/` contains exactly two directories: `stockdate/` for Java code and
`resources/` for application configuration and frontend assets.

## Team

- Hugo Cobo Diaz Guerra
- Ángel Luis Segovia Medina
- Francisco Ingelmo
- Enrique Barrera Ruperez
