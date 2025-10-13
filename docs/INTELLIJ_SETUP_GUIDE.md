# 🚀 IntelliJ IDEA Setup Guide - MatelasPro

## 🔴 CURRENT PROBLEM

When running from IntelliJ IDEA, you're getting:
```
Unknown database 'warehouse_db'
```

**Why?** 
1. ❌ Database doesn't exist
2. ❌ Running from IntelliJ bypasses the XAMPP auto-start script

---

## ✅ SOLUTION - 3 STEPS

### **STEP 1: Start XAMPP Manually** ⚠️ **DO THIS FIRST!**

When running from IntelliJ, you must **manually start XAMPP** before running the app.

**Option A: Using XAMPP Control Panel** (Recommended)
1. Open **XAMPP Control Panel**
2. Click **Start** next to **Apache**
3. Click **Start** next to **MySQL**
4. Wait until both show **green** status

**Option B: Using Command Line**
```batch
# Run these in Command Prompt (as Administrator):
C:\xampp\apache_start.bat
C:\xampp\mysql_start.bat
```

**✅ Verify Services Running**:
```batch
# Check if services are running:
tasklist | findstr "httpd.exe mysqld.exe"

# Should show:
# httpd.exe
# mysqld.exe
```

---

### **STEP 2: Create Database** ⚠️ **ONE-TIME SETUP**

**Option A: Using the Batch Script** (Easiest)

1. Navigate to project folder:
   ```
   C:\Users\Dell\Documents\matress\
   ```

2. **Right-click** on `create-database.bat`

3. Select **"Run as administrator"**

4. The script will:
   - ✅ Check if MySQL is running
   - ✅ Create `warehouse_db` database
   - ✅ Set UTF-8 encoding

**Option B: Manual Creation via phpMyAdmin**

1. Make sure XAMPP MySQL is running

2. Open browser: http://localhost/phpmyadmin/

3. Click **"New"** in left sidebar

4. Create database:
   - **Name**: `warehouse_db`
   - **Collation**: `utf8mb4_general_ci`

5. Click **"Create"**

**Option C: Using MySQL Command Line**

```sql
# Open Command Prompt and run:
C:\xampp\mysql\bin\mysql.exe -u root -e "CREATE DATABASE warehouse_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
```

---

### **STEP 3: Import Schema & Create Admin User**

Once database is created, you need to populate it with tables.

**A. Create Tables**

If you have a schema file:
1. Open phpMyAdmin
2. Select `warehouse_db` database
3. Click **Import** tab
4. Choose your `.sql` file
5. Click **Go**

**B. Create Admin User**

Open phpMyAdmin SQL tab and run:

```sql
-- Make sure you're using warehouse_db
USE warehouse_db;

-- Create users table (if not exists)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);

-- Insert admin user
-- Password is: admin123
INSERT INTO users (username, password_hash, role) 
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMkIjefe.JQGfewQ92s/dUV4xJNVEWNVZJxlApm', 'admin')
ON DUPLICATE KEY UPDATE username=username;
```

**Default Credentials**:
- **Username**: `admin`
- **Password**: `admin123`

---

## 🔧 INTELLIJ CONFIGURATION

### **Option 1: Run from IntelliJ** (Current Method)

**Before each run**:
1. ✅ Start XAMPP Control Panel
2. ✅ Start Apache & MySQL
3. ✅ Then run app from IntelliJ

**To Run**:
- Click the **green play button** in IntelliJ
- Or: Run → Run 'App'
- Or: `Shift+F10`

---

### **Option 2: Create IntelliJ Run Configuration with XAMPP Check**

1. **Go to**: Run → Edit Configurations

2. **Click**: + → Application

3. **Configure**:
   - **Name**: `MatelasPro (with XAMPP check)`
   - **Main class**: `com.warehouse.App`
   - **VM options**: (leave blank for JavaFX 17)
   - **Working directory**: `$ProjectFileDir$`
   - **Environment variables**: (none needed)

4. **Before launch section**:
   - Click **+** → Run External Tool
   - Click **+** to add new tool
   - Configure:
     - **Name**: `Check XAMPP`
     - **Program**: `powershell`
     - **Arguments**: 
       ```
       -Command "if (-not (Get-Process mysqld -ErrorAction SilentlyContinue)) { Write-Host 'ERROR: MySQL not running! Start XAMPP first.'; exit 1 }"
       ```
     - **Working directory**: `$ProjectFileDir$`

5. Click **OK** to save

Now when you run, it will check if MySQL is running first!

---

### **Option 3: Auto-Start XAMPP Before Run** (Advanced)

Create a PowerShell script to auto-start XAMPP:

**File**: `start-xampp.ps1` (in project root)

```powershell
# Check and start XAMPP services
$xamppPath = "C:\xampp"

Write-Host "Checking XAMPP services..."

# Check MySQL
$mysql = Get-Process mysqld -ErrorAction SilentlyContinue
if (-not $mysql) {
    Write-Host "Starting MySQL..."
    Start-Process "$xamppPath\mysql_start.bat" -WindowStyle Hidden
    Start-Sleep -Seconds 3
}

# Check Apache
$apache = Get-Process httpd -ErrorAction SilentlyContinue
if (-not $apache) {
    Write-Host "Starting Apache..."
    Start-Process "$xamppPath\apache_start.bat" -WindowStyle Hidden
    Start-Sleep -Seconds 3
}

Write-Host "XAMPP services ready!"
```

Then in IntelliJ:
1. Run → Edit Configurations
2. Before launch → + → Run External Tool
3. Add:
   - **Program**: `powershell`
   - **Arguments**: `-ExecutionPolicy Bypass -File "$ProjectFileDir$/start-xampp.ps1"`

---

## 📋 COMPLETE SETUP CHECKLIST

### **One-Time Setup** (Do Once)

- [ ] Install Java JDK 17
- [ ] Install XAMPP
- [ ] Start XAMPP services (Apache + MySQL)
- [ ] Create database `warehouse_db`
- [ ] Import schema / create tables
- [ ] Create admin user
- [ ] Compile project: `mvn clean package`

### **Before Each Run from IntelliJ**

- [ ] XAMPP Control Panel open
- [ ] Apache showing **green** (started)
- [ ] MySQL showing **green** (started)
- [ ] Then click Run in IntelliJ

---

## 🐛 TROUBLESHOOTING

### **Error: "Unknown database 'warehouse_db'"**

**Cause**: Database doesn't exist

**Fix**:
1. Start XAMPP MySQL
2. Run `create-database.bat` (as admin)
3. Or create manually in phpMyAdmin

---

### **Error: "Can't connect to MySQL server"**

**Cause**: MySQL not running

**Fix**:
1. Open XAMPP Control Panel
2. Click Start on MySQL
3. Wait for green status
4. Try again

---

### **Error: Port 3306 already in use**

**Cause**: Another MySQL instance running

**Fix**:
```batch
# Stop other MySQL:
net stop mysql

# Or find and kill process:
tasklist | findstr mysqld
taskkill /IM mysqld.exe /F

# Then start XAMPP MySQL
```

---

### **Error: Port 80 already in use**

**Cause**: Another web server (IIS, Skype, etc.)

**Fix Option 1** - Change Apache port:
1. Edit: `C:\xampp\apache\conf\httpd.conf`
2. Change: `Listen 80` → `Listen 8080`
3. Restart Apache
4. Access: http://localhost:8080/

**Fix Option 2** - Stop other services:
```batch
# Stop IIS:
net stop was /y

# Stop Skype web sharing:
# Settings → Advanced → Connections → Uncheck port 80
```

---

### **CSS Warnings (Can Ignore)**

You're seeing these warnings:
```
WARNING: CSS Error parsing... Expected 'gaussian'...
```

**Impact**: None - these are just warnings  
**Cause**: CSS syntax JavaFX doesn't fully support  
**Action**: Can be ignored safely

---

### **Background Image Error (Can Ignore)**

```
Background image not set: Cannot invoke "javafx.scene.Scene.getRoot()"
```

**Impact**: None - background still works  
**Cause**: Scene not ready during initialize()  
**Action**: Can be ignored - it's caught by try-catch

---

## 🎯 QUICK START (After Setup)

### **Every Time You Want to Run**:

1. **Start XAMPP** (if not already running):
   - Open XAMPP Control Panel
   - Start Apache
   - Start MySQL

2. **Run from IntelliJ**:
   - Click green play button
   - Or press `Shift+F10`

3. **Login**:
   - Username: `admin`
   - Password: `admin123`

---

## 🚀 ALTERNATIVE: Use Launcher Script

Instead of running from IntelliJ, you can use the launcher:

1. **Compile once**:
   ```
   mvn clean package
   ```

2. **Use launcher** (auto-starts XAMPP):
   - Double-click: `MatelasPro-Launcher.vbs`
   - Or desktop shortcut

**Benefits**:
- ✅ Auto-starts XAMPP
- ✅ No need to manually start services
- ✅ Simpler for end users

---

## 📊 COMPARISON

| Method | XAMPP Auto-Start | Easy to Debug | Best For |
|--------|------------------|---------------|----------|
| **IntelliJ Run** | ❌ Manual | ✅ Yes | Development |
| **Launcher Script** | ✅ Auto | ❌ No | Production/Testing |
| **JAR Direct** | ❌ Manual | ❌ No | Quick test |

---

## 💡 RECOMMENDATION

**For Development** (Current):
1. Keep XAMPP Control Panel open
2. Start services manually
3. Run from IntelliJ for debugging

**For Testing/Client**:
1. Compile: `mvn clean package`
2. Use: `MatelasPro-Launcher.vbs`
3. XAMPP starts automatically

---

## ✅ VERIFICATION STEPS

After setup, verify everything works:

### **1. Check Database Exists**
```sql
# In phpMyAdmin or MySQL command line:
SHOW DATABASES LIKE 'warehouse_db';
# Should return: warehouse_db
```

### **2. Check Tables Exist**
```sql
USE warehouse_db;
SHOW TABLES;
# Should show: users, mattress, transactions, store_owners, etc.
```

### **3. Check Admin User**
```sql
SELECT username, role FROM users WHERE username='admin';
# Should return: admin | admin
```

### **4. Test Connection**
Run this in IntelliJ and check console:
```java
// In any main method or test:
try {
    Connection conn = DBUtil.getConnection();
    System.out.println("✅ Database connection successful!");
    conn.close();
} catch (SQLException e) {
    System.out.println("❌ Database connection failed: " + e.getMessage());
}
```

---

## 📝 SUMMARY

### **To Fix Current Error**:

1. ✅ Start XAMPP (Apache + MySQL)
2. ✅ Run `create-database.bat` (creates warehouse_db)
3. ✅ Import schema / create admin user
4. ✅ Run app from IntelliJ

### **Remember**:
- 🔴 **Always start XAMPP before running from IntelliJ**
- 🔴 **Database setup is one-time only**
- ✅ **After setup, just start XAMPP and run**

---

*IntelliJ Setup Guide*  
*Last Updated: October 12, 2025*  
*For MatelasPro v1.0*
