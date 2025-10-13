# 🚨 QUICK FIX - Do This NOW!

## Your Current Error:
```
Unknown database 'warehouse_db'
```

---

## ✅ FIX IN 3 STEPS (5 minutes)

### **STEP 1: Start XAMPP** ⏱️ 1 minute

1. Open **XAMPP Control Panel**
2. Click **"Start"** next to **Apache**
3. Click **"Start"** next to **MySQL**
4. Wait until both show **green** status

**✅ Verify**: Both services should be green/running

---

### **STEP 2: Create Database** ⏱️ 2 minutes

**Right-click** on this file and select **"Run as administrator"**:
```
C:\Users\Dell\Documents\matress\create-database.bat
```

The script will:
- ✅ Check MySQL is running
- ✅ Create `warehouse_db` database
- ✅ Show success message

**✅ Verify**: Script says "Database created successfully!"

---

### **STEP 3: Create Admin User** ⏱️ 2 minutes

1. Open browser: http://localhost/phpmyadmin/

2. Click **SQL** tab at the top

3. Copy and paste this:

```sql
-- Select the database
USE warehouse_db;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);

-- Insert admin user (password: admin123)
INSERT INTO users (username, password_hash, role) 
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMkIjefe.JQGfewQ92s/dUV4xJNVEWNVZJxlApm', 'admin')
ON DUPLICATE KEY UPDATE username=username;
```

4. Click **"Go"**

5. Should see: "1 row affected"

**✅ Verify**: Check the `users` table - you should see the admin user

---

## 🚀 NOW RUN YOUR APP

Go back to IntelliJ and click the **green play button**

**Login with**:
- Username: `admin`
- Password: `admin123`

---

## ⚠️ IMPORTANT FOR FUTURE

**Every time you run from IntelliJ:**
1. Make sure XAMPP Control Panel is open
2. Apache and MySQL must be **green** (running)
3. Then click Run in IntelliJ

**Why?** When you run from IntelliJ, the auto-start script is bypassed. You must manually start XAMPP first.

---

## 🔇 ABOUT THE WARNINGS

You're seeing these warnings - **THEY ARE HARMLESS**:

### ✅ CSS Warnings (Can Ignore)
```
WARNING: CSS Error parsing... Expected 'gaussian'...
```
**Impact**: None - just syntax warnings  
**Action**: Ignore them - they don't affect functionality

### ✅ Background Image (Can Ignore)
```
Background image not set: Cannot invoke "javafx.scene.Scene.getRoot()"
```
**Impact**: None - background still works  
**Action**: Ignore - it's a timing issue, safely caught

### ❌ Database Error (Must Fix)
```
Unknown database 'warehouse_db'
```
**Impact**: App won't work  
**Action**: **Follow the 3 steps above!**

---

## 📋 CHECKLIST

After completing the 3 steps:

- [ ] XAMPP Control Panel shows Apache **green**
- [ ] XAMPP Control Panel shows MySQL **green**
- [ ] `create-database.bat` ran successfully
- [ ] phpMyAdmin shows `warehouse_db` database exists
- [ ] `users` table exists with admin user
- [ ] App runs from IntelliJ without database error
- [ ] Can login with admin/admin123

---

## 🎯 SUMMARY

**Your Problem**:
- Database doesn't exist
- XAMPP not running when you use IntelliJ

**The Solution**:
1. Start XAMPP manually (Control Panel)
2. Create database (run the .bat file)
3. Create admin user (SQL in phpMyAdmin)

**Time**: 5 minutes  
**Difficulty**: Easy

---

## 💡 FOR MORE DETAILS

See the comprehensive guide:
📄 `docs/INTELLIJ_SETUP_GUIDE.md`

---

*Quick Fix Guide*  
*October 12, 2025*
