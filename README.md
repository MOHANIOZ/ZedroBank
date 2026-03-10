# ZedroBank - Java Fullstack Banking Application

ZedroBank is a robust backend banking system built using **Java**, **Spring Boot**, and **MySQL**. This project handles core banking functionalities like account creation, secure transfers, and transaction history tracking.

## Project Overview
The application follows a Clean Architecture (N-tier) approach:
- **Controller Layer:** Handles REST API endpoints.
- **Service Layer:** Manages business logic and transaction processing.
- **Repository Layer:** Interacts with the database using Spring Data JPA.
- **Security:** Integrated with Spring Security for protected resource access.

## Key Features
- **Account Management:** Create, view, and delete user accounts.
- **Financial Operations:** Secure deposit, withdrawal, and peer-to-peer transfers.
- **Transaction History:** Real-time tracking of all account activities with pagination and sorting.
- **Secure Access:** Configured security filters to allow public access to core APIs while maintaining internal safety.

## API Documentation & Testing
You can explore and test the live APIs using the **Swagger UI** interface. It provides a visual representation of all available endpoints and allows you to execute requests directly from your browser.

**Live Swagger Documentation:**
[https://zedrobank.onrender.com/swagger-ui/index.html](https://zedrobank.onrender.com/swagger-ui/index.html)

### How to Test:
1. Open the Swagger link above.
2. Expand any controller (e.g., `account-controller`).
3. Click on **"Try it out"**.
4. Enter the required parameters or JSON body.
5. Click **"Execute"** to see the live response from the Render-hosted server.

## Tech Stack
- **Backend:** Java 17, Spring Boot 3.x, Spring Security, Spring Data JPA
- **Database:** MySQL (Hosted on Aiven/Render)
- **Deployment:** Render (Dockerized environment)
