# Performance App

This project is a **Performance Appraisal Management System** built using **Spring Boot**. The application calculates employee performance metrics, including adjustments based on the bell curve approach, and allows for efficient data management of employees and category standards.

## Features
- Upload employee and category data.
- Fetch, update, or delete employee records.
- Manage category standards with validation.
- Generate performance suggestions.
- Integrated with H2 database for persistence.

---

## Prerequisites

1. **Java JDK**:
    - Ensure Java JDK 17 or later is installed.
    - Check the installed version:
      ```bash
      java -version
      ```

2. **Maven**:
    - Ensure Apache Maven is installed and added to your PATH.
    - Verify Maven installation:
      ```bash
      mvn -version
      ```

3. **Git (Optional)**:
    - Used for cloning the project repository.
    - Verify Git installation:
      ```bash
      git --version
      ```

---

## How to Clone the Repository
1. Open a terminal.
2. Clone the repository:
   ```bash
   git clone <repository-url>
   ```
3. Navigate to the project directory:
   ```bash
   cd performance-app
   ```

---

## How to Run the Application

### Using IntelliJ IDEA
1. Open IntelliJ IDEA.
2. Import the project:
    - Select **File > Open** and choose the project directory.
    - Wait for IntelliJ to download dependencies and index the project.
3. Run the application:
    - Open `PerformanceAppApplication.java`.
    - Right-click on the file and select **Run 'PerformanceAppApplication'**.

### Using Maven Command Line
1. Open a terminal in the project root directory.
2. Clean and build the project:
   ```bash
   mvn clean install
   ```
3. Run the Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```

---

## Application Endpoints

### Base URL
The application runs at:
```
http://localhost:9090
```

### API Endpoints

#### Employee Management
- **Upload Employees**:
    - **POST** `/performance/upload-employees`
    - Request Body: List of employees (JSON).

- **Get All Employees**:
    - **GET** `/performance/employees`

- **Get Employee by ID**:
    - **GET** `/performance/employees/{id}`

- **Update Employee by ID**:
    - **PUT** `/performance/employees/{id}`

- **Delete Employee by ID**:
    - **DELETE** `/performance/employees/{id}`

- **Delete All Employees**:
    - **DELETE** `/performance/employees/all`

#### Category Management
- **Upload Categories**:
    - **POST** `/performance/upload-category-standard`

- **Get All Categories**:
    - **GET** `/performance/categories`

- **Update Category Standards**:
    - **PUT** `/performance/categories/update`

#### Performance Calculations
- **Calculate Bell Curve**:
    - **GET** `/performance/calculate-curve`

- **Generate Suggestions**:
    - **GET** `/performance/suggest-changes`

---

## H2 Database Console

Access the in-memory database:

1. Go to:
   ```
   http://localhost:9090/h2-console
   ```

2. Use the following credentials:
    - **JDBC URL**: `jdbc:h2:mem:testdb`
    - **Username**: `sa`
    - **Password**: (leave blank)

---

## Running Tests

### Unit Tests

Run all unit tests using Maven:

```bash
mvn test
```

### Specific Test Class
Run a specific test class:
```bash
mvn -Dtest=PerformanceServiceTest test
```

---

## Testing APIs with Postman

You can test the application’s endpoints using Postman. Follow these steps:

1. **Import Postman Collection**:
    - Open Postman.
    - Click on **Import** and upload the provided `PerformanceAppAPI.postman_collection.json` file.
    - The collection will appear in the sidebar.

2. **Set Up Environment**:
    - Go to **Environments** in Postman.
    - Create a new environment with the variable `base_url` set to `http://localhost:9090`.

3. **Run Endpoints**:
    - Expand the imported collection.
    - Select any endpoint, modify request parameters if needed, and click **Send**.
    - Use the **Collection Runner** in Postman to execute multiple API requests at once and validate responses.

---

## Troubleshooting

1. **Maven Build Issues**:
    - Ensure `MAVEN_OPTS` is configured for sufficient heap space:
      ```bash
      export MAVEN_OPTS="-Xmx1024m -Xms512m"
      ```

2. **Port Already in Use**:
    - Stop any other application running on port `9090` or change the port in `application.properties`:
      ```properties
      server.port=9091
      ```

3. **Database Connection Errors**:
    - Ensure that the H2 database console is reachable, and the application has been started properly.

---

## Additional Resources
- **Spring Boot Reference**: [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- **Maven Documentation**: [Maven Documentation](https://maven.apache.org/)

