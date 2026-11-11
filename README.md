# StockDate: Inventory and Expiration Manager

**StockDate** is a web application designed to solve a critical problem: the inefficient management of inventory and expiration dates in small businesses and local food banks. Its main objective is to drastically reduce food waste and optimize the time of volunteers and coordinators.

---

## Team Members
* **Hugo Cobo Diaz Guerra**
* **Ángel Luis Segovia Medina**
* **Francisco Ingelmo**
* **Enrique Barrera Ruperez**

---

## What are we developing? (Project Scope)
* **Batch management:** Registration, search and editing of food batches with their expiration date and date type (use-by or best-before).
* **Control Panel (Dashboard):** Automatic visual alerts (Expired, Red, Yellow, Green) to identify products nearing their expiration date at a glance.
* **FEFO dispatch and discards:** Stock exits that follow First-Expired, First-Out, and write-offs of spoiled food with a reason.
* **Waste Reports:** Monthly reports with the waste rate to optimize purchasing or donation decisions.
* **Role-Based Access Control:** Two roles: **ADMIN** (coordinator) and **VOLUNTEER** (field staff).

---

## Project Status

| Phase | Weeks | Status |
|---|---|---|
| Phase 1: Planning and Analysis | 1-5 | Done ([`Stockdate/Information/`](Stockdate/Information/)) |
| Phase 2: Implementation Details | 6-11 | In progress |
| Phase 3: Testing and Quality | 12-14 | Pending |

Phase 2 progress:
* **Project structure (week 7):** Done. Layered architecture (presentation, service, persistence) with Gradle, Spring Data JPA, Spring Security, Actuator, JaCoCo, Checkstyle and PMD.
* **Design patterns (week 9):** Done. Observer, Strategy and Factory (see [`Stockdate/README.md`](Stockdate/README.md#design-patterns)).
* **Database schema (week 11):** Pending. Normalized tables for users, categories and stock movements.

---

## Technical Stack & Current Setup
* **Backend:** Java 17 with the **Spring Boot** framework.
* **Frontend:** HTML, CSS, and JavaScript (REST API + client-side rendering).
* **Database:** SQLite, accessed through Spring Data JPA.
* **Build tool:** Gradle (Wrapper included).
* **Quality:** JUnit 5, Mockito, JaCoCo, Checkstyle and PMD.
* **Development Environment (IDE):** IntelliJ IDEA.
* **Deployment:** Executable JAR with `start.bat` / `start.sh`. Docker is optional (ADR-03).
* **Version Control & Management:** Git and GitHub Projects.

---

## Repository Structure
* [`Stockdate/`](Stockdate/): Application source code. See its [`README.md`](Stockdate/README.md) to run, build and test it.
* [`Esquemas_Phase1/`](Esquemas_Phase1/): PlantUML diagrams from Phase 1 (class, component and sequence diagrams).
