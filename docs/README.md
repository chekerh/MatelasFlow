# Warehouse Mattress Management System

A professional JavaFX application for managing warehouse inventory, transactions, store owners, users, and business statistics. Built with best practices for reliability, security, and usability.

## Features
- **User Authentication**: Secure login for admin and employees (passwords hashed with BCrypt)
- **Inventory Management**: Add, edit, delete, and view mattresses (type, size, brand, quantity)
- **Transactions**: Record sales, lends, and returns; link to store owners; set expected return dates
- **Store Owners Management**: Add, edit, delete, and view store owners
- **User Management (Admin only)**: Add, edit, delete, and view users (admin/employee roles)
- **Statistics & Reports**: View stock levels, sales, returns, lends, and filterable transaction history
- **PDF Daily Reports**: Generate professional PDF reports of daily transactions

## Prerequisites
- **Java 11+**
- **Maven**
- **MySQL** (use XAMPP or your preferred local server)
- **IntelliJ IDEA** (recommended)
- **iText PDF library** (add to Maven dependencies)

## Database Setup
1. Start MySQL (e.g., via XAMPP)
2. Run the provided `mysql_schema.sql` in your MySQL client to create the database and tables
3. Default DB credentials are set for local XAMPP (user: `root`, password: empty). Update `DBUtil.java` if needed.

## Build & Run
1. Open the project in IntelliJ IDEA
2. Ensure Maven dependencies are downloaded (`Reload All Maven Projects`)
3. Add iText to your `pom.xml`:
   ```xml
   <dependency>
     <groupId>com.itextpdf</groupId>
     <artifactId>itextpdf</artifactId>
     <version>5.5.13.2</version>
   </dependency>
   ```
4. Build the project (`Build > Build Project`)
5. Run `App.java` (main class)

## Usage Guide
- **Login**: Use admin credentials (set up in DB) or add users via User Management
- **Inventory**: Manage mattresses (CRUD)
- **Transactions**: Record sales, lends (with expected return), and returns; generate daily PDF reports
- **Store Owners**: Manage store owner info
- **Users**: Admin can manage all users and roles
- **Statistics**: View and filter key business metrics

## Testing
- **Manual Testing**: 
  - Log in as admin and employee, test all CRUD operations
  - Add, edit, delete mattresses, store owners, and users
  - Record all transaction types and verify statistics update
  - Generate PDF reports and verify file output
- **Database**: Check that all changes are reflected in MySQL
- **UI**: Ensure all navigation and dialogs work as expected

## Best Practices
- Only one admin account is supported; multiple employees can be added
- Passwords are always hashed
- All user input is validated
- All business logic is separated into controllers and DAOs
- PDF reports are generated in the project root

## Troubleshooting
- If you see DB connection errors, check your MySQL server and credentials
- If PDF generation fails, ensure iText is in your dependencies
- For UI issues, check FXML file paths and controller bindings

## Contact
For support or questions, contact the developer or open an issue in your project tracker. 