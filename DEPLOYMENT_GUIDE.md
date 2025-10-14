# 🚀 DEPLOYMENT GUIDE - MatelasFlow Desktop Application
**Complete Guide to Convert JavaFX App to Windows Desktop .exe**

---

## 📋 TABLE OF CONTENTS

1. [Prerequisites](#prerequisites)
2. [Database Setup (XAMPP)](#database-setup)
3. [Building the Application](#building-the-application)
4. [Creating Windows .exe](#creating-windows-exe)
5. [Auto-Start XAMPP with Application](#auto-start-xampp)
6. [Installation on Client Computer](#installation-on-client-computer)
7. [Troubleshooting](#troubleshooting)

---

## 🔧 PREREQUISITES

### **What You Need Before Starting:**

1. ✅ **Java Development Kit (JDK) 17 or higher**
   - Download: https://www.oracle.com/java/technologies/downloads/
   - Or use OpenJDK: https://adoptium.net/

2. ✅ **Apache Maven**
   - Download: https://maven.apache.org/download.cgi
   - Add to PATH environment variable

3. ✅ **XAMPP (for MySQL database)**
   - Download: https://www.apachefriends.org/download.html
   - Install in default location: `C:\xampp`

4. ✅ **jpackage Tool** (included with JDK 17+)
   - Verify: `jpackage --version`

5. ✅ **WiX Toolset** (for Windows .exe/.msi installer)
   - Download: https://wixtoolset.org/releases/
   - Version 3.11.2 or higher
   - Add to PATH: `C:\Program Files (x86)\WiX Toolset v3.11\bin`

---

## 💾 DATABASE SETUP (XAMPP)

### **Step 1: Install XAMPP**

1. Download XAMPP from https://www.apachefriends.org/download.html
2. Run installer as Administrator
3. Install to: `C:\xampp` (default location)
4. Select components:
   - ✅ Apache
   - ✅ MySQL
   - ✅ phpMyAdmin
   - ❌ Others (optional)

### **Step 2: Start XAMPP Services**

1. Open XAMPP Control Panel
2. Click "Start" for:
   - Apache
   - MySQL
3. Verify MySQL is running on port 3306

### **Step 3: Create Database**

1. Open browser: http://localhost/phpmyadmin
2. Click "New" to create database
3. Database name: `warehouse_db`
4. Collation: `utf8mb4_general_ci`
5. Click "Create"

### **Step 4: Import Database Schema**

1. Select `warehouse_db` database
2. Click "Import" tab
3. Choose file: `COMPLETE_SCHEMA.sql` (from project root)
4. Click "Go"
5. ✅ Success! You should see all tables and triggers created

### **Step 5: Verify Database**

Run this query in phpMyAdmin SQL tab:
```sql
SHOW TABLES;
```

You should see:
- `activity_log`
- `mattress`
- `store_owner`
- `transaction`
- `users`

---

## 🏗️ BUILDING THE APPLICATION

### **Method 1: Using Maven (Recommended)**

```bash
# Navigate to project directory
cd C:\Users\Dell\Documents\matress

# Clean previous builds
mvn clean

# Build with dependencies
mvn clean package

# This creates: target/matress-1.0-SNAPSHOT.jar
```

### **Method 2: Building Shaded JAR (All-in-One)**

Add this plugin to `pom.xml` (if not already present):

```xml
<build>
    <plugins>
        <!-- Maven Shade Plugin for creating fat JAR -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.4.1</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <transformers>
                            <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                <mainClass>com.warehouse.App</mainClass>
                            </transformer>
                        </transformers>
                        <filters>
                            <filter>
                                <artifact>*:*</artifact>
                                <excludes>
                                    <exclude>META-INF/*.SF</exclude>
                                    <exclude>META-INF/*.DSA</exclude>
                                    <exclude>META-INF/*.RSA</exclude>
                                </excludes>
                            </filter>
                        </filters>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

Then build:
```bash
mvn clean package
```

This creates a single JAR with all dependencies: `target/matress-1.0-SNAPSHOT-shaded.jar`

---

## 🖥️ CREATING WINDOWS .EXE

### **Option A: Using jpackage (Professional, Recommended)**

#### **Step 1: Create jpackage Configuration**

Create file: `build-installer.bat`

```batch
@echo off
echo ================================================
echo Building MatelasFlow Windows Installer
echo ================================================

REM Set variables
set APP_NAME=MatelasFlow
set APP_VERSION=1.0
set VENDOR=SuperMousse
set MAIN_JAR=target\matress-1.0-SNAPSHOT.jar
set MAIN_CLASS=com.warehouse.App
set ICON=src\main\resources\images\icon.ico

echo.
echo Step 1: Building JAR with Maven...
call mvn clean package
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven build failed!
    pause
    exit /b 1
)

echo.
echo Step 2: Creating installer with jpackage...
jpackage ^
    --type exe ^
    --name "%APP_NAME%" ^
    --app-version "%APP_VERSION%" ^
    --vendor "%VENDOR%" ^
    --icon "%ICON%" ^
    --input target ^
    --main-jar matress-1.0-SNAPSHOT.jar ^
    --main-class %MAIN_CLASS% ^
    --dest installer ^
    --win-dir-chooser ^
    --win-menu ^
    --win-shortcut ^
    --win-per-user-install ^
    --java-options "-Xmx512m" ^
    --java-options "-Xms256m"

if %ERRORLEVEL% EQ 0 (
    echo.
    echo ================================================
    echo SUCCESS! Installer created in 'installer' folder
    echo ================================================
    echo.
    echo File: installer\%APP_NAME%-%APP_VERSION%.exe
    echo.
) else (
    echo.
    echo ERROR: jpackage failed!
    pause
    exit /b 1
)

pause
```

#### **Step 2: Create Application Icon**

1. Create or download an `.ico` file (Windows icon)
2. Save as: `src\main\resources\images\icon.ico`
3. Icon size: 256x256 pixels recommended

#### **Step 3: Run the Build Script**

```bash
# Run the batch file
build-installer.bat
```

This creates: `installer/MatelasFlow-1.0.exe`

---

### **Option B: Using Launch4j (Simpler, .exe wrapper)**

#### **Step 1: Download Launch4j**

Download from: https://launch4j.sourceforge.net/

#### **Step 2: Create Launch4j Configuration**

Create file: `launch4j-config.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<launch4jConfig>
  <dontWrapJar>false</dontWrapJar>
  <headerType>gui</headerType>
  <jar>target\matress-1.0-SNAPSHOT.jar</jar>
  <outfile>MatelasFlow.exe</outfile>
  <errTitle>MatelasFlow</errTitle>
  <cmdLine></cmdLine>
  <chdir>.</chdir>
  <priority>normal</priority>
  <downloadUrl>https://adoptium.net/</downloadUrl>
  <supportUrl></supportUrl>
  <stayAlive>false</stayAlive>
  <restartOnCrash>false</restartOnCrash>
  <manifest></manifest>
  <icon>src\main\resources\images\icon.ico</icon>
  <jre>
    <path>%JAVA_HOME%</path>
    <bundledJre64Bit>false</bundledJre64Bit>
    <bundledJreAsFallback>false</bundledJreAsFallback>
    <minVersion>17</minVersion>
    <maxVersion></maxVersion>
    <jdkPreference>preferJre</jdkPreference>
    <runtimeBits>64</runtimeBits>
    <initialHeapSize>256</initialHeapSize>
    <maxHeapSize>512</maxHeapSize>
  </jre>
  <versionInfo>
    <fileVersion>1.0.0.0</fileVersion>
    <txtFileVersion>1.0.0.0</txtFileVersion>
    <fileDescription>MatelasFlow - Warehouse Management System</fileDescription>
    <copyright>SuperMousse 2025</copyright>
    <productVersion>1.0.0.0</productVersion>
    <txtProductVersion>1.0.0.0</txtProductVersion>
    <productName>MatelasFlow</productName>
    <companyName>SuperMousse</companyName>
    <internalName>MatelasFlow</internalName>
    <originalFilename>MatelasFlow.exe</originalFilename>
  </versionInfo>
</launch4jConfig>
```

#### **Step 3: Build with Launch4j**

```bash
# Using Launch4j command line
launch4jc.exe launch4j-config.xml
```

Or use the GUI:
1. Open Launch4j
2. Load `launch4j-config.xml`
3. Click the gear icon (Build wrapper)

This creates: `MatelasFlow.exe`

---

## ⚙️ AUTO-START XAMPP WITH APPLICATION

### **Method 1: Startup Script (Recommended)**

Create file: `start-matelasflow.bat`

```batch
@echo off
title MatelasFlow Startup
echo ================================================
echo Starting MatelasFlow Warehouse Management System
echo ================================================
echo.

REM Check if XAMPP MySQL is already running
echo Checking MySQL status...
netstat -an | find "3306" | find "LISTENING" >nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] MySQL is already running
    goto START_APP
)

REM Start MySQL via XAMPP
echo Starting MySQL (XAMPP)...
if exist "C:\xampp\mysql\bin\mysqld.exe" (
    start "" "C:\xampp\mysql_start.bat"
    echo Waiting for MySQL to start...
    timeout /t 5 /nobreak >nul
    echo [OK] MySQL started
) else (
    echo [ERROR] XAMPP not found at C:\xampp
    echo Please install XAMPP or update the path in this script
    pause
    exit /b 1
)

:START_APP
echo.
echo Starting MatelasFlow application...
echo.
start "" "MatelasFlow.exe"

echo.
echo [SUCCESS] MatelasFlow is starting...
echo.
echo This window will close in 3 seconds...
timeout /t 3 >nul
exit
```

### **Method 2: Create Windows Service (Advanced)**

Create file: `install-mysql-service.bat` (Run as Administrator):

```batch
@echo off
echo Installing MySQL as Windows Service...

REM Stop MySQL if running
net stop MySQL 2>nul

REM Install MySQL service
"C:\xampp\mysql\bin\mysqld.exe" --install MySQL --defaults-file="C:\xampp\mysql\bin\my.ini"

REM Set service to start automatically
sc config MySQL start= auto

REM Start MySQL service
net start MySQL

echo.
echo MySQL installed as Windows service!
echo It will now start automatically with Windows.
pause
```

### **Method 3: Integrated Startup (Best UX)**

Modify your `App.java` to check and start MySQL:

```java
// Add to App.java main method
public static void main(String[] args) {
    // Check MySQL before launching app
    if (!checkMySQLRunning()) {
        System.out.println("MySQL not running. Attempting to start...");
        startMySQL();
        
        // Wait for MySQL to start
        int retries = 10;
        while (retries > 0 && !checkMySQLRunning()) {
            try {
                Thread.sleep(1000);
                retries--;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        if (!checkMySQLRunning()) {
            showMySQLError();
            return;
        }
    }
    
    launch(args);
}

private static boolean checkMySQLRunning() {
    try {
        Connection conn = DBUtil.getConnection();
        conn.close();
        return true;
    } catch (SQLException e) {
        return false;
    }
}

private static void startMySQL() {
    try {
        // Try to start XAMPP MySQL
        String xamppPath = "C:\\xampp\\mysql_start.bat";
        if (new File(xamppPath).exists()) {
            Runtime.getRuntime().exec(xamppPath);
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}

private static void showMySQLError() {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Erreur de connexion");
    alert.setHeaderText("MySQL n'est pas disponible");
    alert.setContentText(
        "L'application ne peut pas se connecter à la base de données.\n\n" +
        "Assurez-vous que XAMPP est installé et que MySQL est démarré.\n\n" +
        "Chemin XAMPP: C:\\xampp"
    );
    alert.showAndWait();
}
```

---

## 📦 INSTALLATION ON CLIENT COMPUTER

### **What to Send to Your Friend:**

Create a deployment package with these files:

```
MatelasFlow-Deployment/
│
├── installer/
│   └── MatelasFlow-1.0.exe          ← Main installer
│
├── database/
│   └── COMPLETE_SCHEMA.sql          ← Database schema
│
├── xampp/
│   └── xampp-windows-installer.exe  ← Download link/installer
│
├── docs/
│   ├── INSTALLATION_GUIDE.md        ← See below
│   └── USER_MANUAL.md               ← How to use the app
│
└── README.txt                        ← Quick start
```

---

## 📖 INSTALLATION_GUIDE.md (For Your Friend)

Create this file for the end user:

```markdown
# MatelasFlow - Installation Guide

## Step-by-Step Installation

### 1️⃣ Install XAMPP (Database Server)

1. Download XAMPP from: https://www.apachefriends.org/download.html
2. Run `xampp-windows-installer.exe` as Administrator
3. Install to: `C:\xampp` (keep default)
4. Select components:
   - ✅ Apache
   - ✅ MySQL
   - ✅ phpMyAdmin
5. Click "Next" until installation completes

### 2️⃣ Setup Database

1. Open XAMPP Control Panel (from Start Menu)
2. Click "Start" next to MySQL
3. Wait until MySQL shows "Running" (green highlight)
4. Click "Admin" next to MySQL
5. This opens phpMyAdmin in your browser
6. Click "New" in the left sidebar
7. Database name: `warehouse_db`
8. Click "Create"
9. Select `warehouse_db` from left sidebar
10. Click "Import" tab
11. Click "Choose File" and select `COMPLETE_SCHEMA.sql`
12. Click "Go" at the bottom
13. ✅ Success! You should see "Import has been successfully finished"

### 3️⃣ Install MatelasFlow Application

1. Run `MatelasFlow-1.0.exe` as Administrator
2. Follow the installation wizard:
   - Choose installation location (default: `C:\Program Files\MatelasFlow`)
   - Create desktop shortcut: ✅ YES
   - Create Start Menu shortcut: ✅ YES
3. Click "Install"
4. Click "Finish"

### 4️⃣ First Launch

1. **IMPORTANT**: Start XAMPP MySQL BEFORE launching MatelasFlow
   - Open XAMPP Control Panel
   - Click "Start" for MySQL
   - Wait for green "Running" status

2. Launch MatelasFlow from desktop shortcut

3. Login with default credentials:
   - Username: `admin`
   - Password: `admin123`

4. ✅ You're in! Change your password immediately:
   - Go to "Gestion des Utilisateurs"
   - Edit admin user
   - Set a strong password

### 5️⃣ Auto-Start MySQL (Optional but Recommended)

To avoid manually starting MySQL every time:

1. Open XAMPP Control Panel
2. Click "Config" button (top right)
3. Check "MySQL" in "Autostart modules"
4. Click "Save"

OR install MySQL as Windows Service:
1. Run `install-mysql-service.bat` as Administrator (if provided)
2. MySQL will now start automatically with Windows

### ⚠️ Troubleshooting

**Problem**: "Cannot connect to database"
- **Solution**: Start XAMPP MySQL first
- Open XAMPP Control Panel → Start MySQL

**Problem**: "Port 3306 already in use"
- **Solution**: Another MySQL instance is running
- Open Task Manager → End any MySQL processes
- Or change port in XAMPP config

**Problem**: "Application won't start"
- **Solution**: Install Java Runtime Environment (JRE) 17+
- Download from: https://adoptium.net/

**Problem**: "phpMyAdmin won't open"
- **Solution**: Start Apache in XAMPP Control Panel
- Then start MySQL
- Try again: http://localhost/phpmyadmin

### 📞 Support

For technical support, contact: [Your Contact Info]
```

---

## 🎯 QUICK START README.txt

Create this simple file:

```text
================================================
   MatelasFlow - Warehouse Management System
================================================

QUICK START:

1. Install XAMPP
   - Run xampp-installer.exe
   - Install to C:\xampp

2. Setup Database
   - Open XAMPP Control Panel
   - Start MySQL
   - Import COMPLETE_SCHEMA.sql via phpMyAdmin

3. Install Application
   - Run MatelasFlow-1.0.exe
   - Follow installation wizard

4. Launch
   - Start XAMPP MySQL first
   - Then launch MatelasFlow from desktop

Default Login:
   Username: admin
   Password: admin123

For detailed instructions, see:
   docs/INSTALLATION_GUIDE.md

================================================
```

---

## 🚨 IMPORTANT NOTES

### **Database Connection Settings**

Your `DBUtil.java` has hardcoded connection:
```java
private static final String URL = "jdbc:mysql://localhost:3306/warehouse_db";
private static final String USER = "root";
private static final String PASSWORD = "";
```

This is CORRECT for standard XAMPP installation (default MySQL root user has no password).

### **Security Recommendations**

After deployment, advise your friend to:

1. **Change MySQL Root Password**:
```sql
SET PASSWORD FOR 'root'@'localhost' = PASSWORD('new_password');
FLUSH PRIVILEGES;
```

Then update `DBUtil.java` and rebuild.

2. **Change Default Admin Password**:
   - First login with admin/admin123
   - Go to User Management
   - Edit admin user
   - Set strong password

3. **Regular Backups**:
   - Backup database regularly via phpMyAdmin
   - Or use the built-in backup feature in the app

### **System Requirements**

Minimum:
- Windows 10 or higher
- 4 GB RAM
- 500 MB disk space
- Java Runtime Environment 17+

Recommended:
- Windows 11
- 8 GB RAM
- 1 GB disk space
- Dedicated MySQL server (for multiple users)

---

## ✅ FINAL CHECKLIST

Before sending to your friend:

- [ ] Build the application: `mvn clean package`
- [ ] Create .exe installer using jpackage or Launch4j
- [ ] Test the installer on a clean Windows machine
- [ ] Include `COMPLETE_SCHEMA.sql` file
- [ ] Include XAMPP installer or download link
- [ ] Write clear installation instructions
- [ ] Include troubleshooting guide
- [ ] Test database import process
- [ ] Verify default login works
- [ ] Create README.txt with quick start
- [ ] Zip everything into one package
- [ ] Upload to GitHub or Google Drive
- [ ] Send download link to friend

---

## 📧 SENDING TO YOUR FRIEND

### **Option 1: GitHub Release**

```bash
# Tag your release
git tag -a v1.0 -m "Initial release"
git push origin v1.0

# Create GitHub Release
# Go to: https://github.com/yourusername/matress/releases
# Click "Create a new release"
# Upload: MatelasFlow-1.0.exe, COMPLETE_SCHEMA.sql, docs
```

### **Option 2: Google Drive / Dropbox**

1. Create folder: `MatelasFlow-v1.0-Deployment`
2. Add all files listed above
3. Zip the folder
4. Upload to Google Drive/Dropbox
5. Share link with your friend

---

## 🎉 SUCCESS!

Your JavaFX application is now a professional Windows desktop application with:
- ✅ Windows .exe installer
- ✅ Desktop shortcuts
- ✅ Start Menu integration
- ✅ Auto-start MySQL capability
- ✅ Complete installation guide
- ✅ User-friendly setup process

**Your friend can now install and use MatelasFlow like any professional Windows application!**
```

---

## 💡 ADDITIONAL TIPS

### **Creating a Portable Version**

If you want a version that doesn't require installation:

```bash
# Build fat JAR
mvn clean package

# Create portable folder
mkdir MatelasFlow-Portable
copy target\matress-1.0-SNAPSHOT.jar MatelasFlow-Portable\
copy start-matelasflow.bat MatelasFlow-Portable\
copy COMPLETE_SCHEMA.sql MatelasFlow-Portable\

# Add portable XAMPP (download XAMPP portable version)
```

### **One-Click Installer Script**

Create `INSTALL_ALL.bat`:
```batch
@echo off
echo Installing MatelasFlow Complete System...
echo.

echo Step 1: Installing XAMPP...
start /wait xampp-installer.exe

echo Step 2: Installing MatelasFlow...
start /wait MatelasFlow-1.0.exe

echo.
echo Installation complete!
echo Please run "Setup Database.bat" next
pause
```
