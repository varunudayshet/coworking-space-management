# Coworking Space Management System

**Java/Kotlin (JDBC + Compose Desktop UI) Project**

## 1. Project Overview

- This project is a comprehensive Coworking Space Management System implemented in Kotlin using JDBC for database operations and Compose Desktop for the graphical user interface.  
- It manages members, workspaces, meeting rooms, bookings, services, billing and invoicing with full business logic and reporting.

## 2. Features

### 2.1 Core Functionalities

#### Member Management  
- Register new members with assigned membership plans.  
- View and search members.  
- Update member status (active/inactive).

#### Booking Management  
- Book workspaces with date/time and conflict checking.  
- Book meeting rooms with price/hour calculation and availability checks.  
- View upcoming bookings and booking patterns.

#### Access Control  
- Assign access cards to members.  
- Log member entry/exit events.  
- Generate daily and location-based access statistics.

#### Service and Amenities  
- Record and manage purchase of services/amenities.  
- Track and update stock of stocked items per location.  
- Generate reports for popular amenities and low stock alerts.

#### Billing and Invoicing  
- Generate invoices for members.  
- Update invoice payment status.  
- View overdue invoices with alerts.  
- Revenue and membership tier profitability analysis.

#### Dashboard and Reporting  
- Overview of today’s access logs, upcoming bookings, revenue, member retention, and stock alerts.  
- Peak usage hours and analytics.

## 3. Technologies Used

- Kotlin — Language for backend and UI.  
- JDBC — For SQL database connectivity and operations.  
- Compose Desktop — For creating reactive desktop UI forms and interactions.  
- MySQL — Relational database server storing project data.  
- Gradle — Build automation and dependency management.

## 4. Project Structure

|-- src
| |-- main
| |-- kotlin
| |-- dao # JDBC Data Access Objects per entity
| |-- models # Data classes representing entities
| |-- service # Business logic layer integrating DAOs
| |-- database # DB connection manager
| |-- app # Compose Desktop UI & main entry point (main.kt)
|-- build.gradle.kts # Gradle build script with Compose setup and dependencies
|-- settings.gradle.kts # Gradle project settings and plugin management


## 5. Setup Instructions

### Database (MySQL)

- Install MySQL Server on your machine if not present.  
- Create database **coworking_space**:

CREATE DATABASE coworking_space;
USE coworking_space;

- Run the provided **schema.sql** script to create tables and constraints.  
- (Optional) Insert sample/master data as needed (membership plans, locations, etc).

### Project Build & Run

- Clone the repository:

git clone <Your-GitHub-Repo-URL>
cd co-working_space_management

text

- Ensure you have JDK 17+ installed and Gradle wrapper is set.  
- Build the project:

./gradlew clean build

text

- Run the Compose Desktop application via Gradle:

./gradlew run

text

- Or run the packaged JAR after building/distribution creation.

## 6. How to Use the Application

- On launch, the main window shows the dashboard and test buttons.  
- Use **Test Database Connection** to verify connectivity.  
- Test data queries/buttons to fetch dashboard, members, bookings, workspaces, services, invoices.  
- Use the **Add Member** form to register new members.  
- Use **Book Workspace** or **Book Meeting Room** forms to make bookings.  
- Use **Purchase Service/Amenity** form to record service usage.  
- Use **Pay Invoice** form to mark invoices as paid.  
- Scroll vertically to access all forms and buttons.  
- Clear test results anytime with the **Clear Results** button.  
- Follow on-screen validation and messages for feedback.

## 7. Architecture Overview

- **UI Layer:** Compose Desktop interactive forms and buttons.  
- **Service Layer:** Kotlin classes handling business logic and coordinating DAOs.  
- **DAO Layer:** JDBC-based data access classes for CRUD on MySQL tables.  
- **Database:** MySQL relational DB with normalized tables, constraints, and indexes.

## 8. Key Design Highlights

- Separation of concerns ensures UI only calls services, which internally call DAOs.  
- Service layer enforces business rules (booking conflicts, stock updates, etc).  
- Comprehensive data model aligning with SQL schema.  
- Exception handling and user feedback integrated.  
- Scrollable UI supports ease of navigation through multiple forms.

## 9. Troubleshooting

- Ensure MySQL server is running and credentials match those in **DatabaseManager.kt**.  
- For JAR issues, run with:

java -jar co-working_space_management.jar

text
- Verify Gradle build completes without errors.  
- On database errors, check network, user permissions, and schema setup.  
- JVM version compatibility should be JDK 17 or above.

Thank you for reviewing this project!  
It demonstrates a practical, full-stack Java/Kotlin JDBC application with modern Kotlin UI capabilities and real-world business logic.
