# 🔐 Security Credentials & Important Files

## ⚠️ IMPORTANT: DO NOT COMMIT THESE TO GITHUB

This file contains sensitive information. Keep it private or delete it after noting the credentials.

---

## 📍 License Key Location

**File Path:** `~/.matelaspro/license.dat`

- **Full Path:** `/Users/[username]/.matelaspro/license.dat` (macOS/Linux) or `C:\Users\[username]\.matelaspro\license.dat` (Windows)
- **Purpose:** Stores the encrypted license key for application activation
- **Status:** ✅ Already added to `.gitignore` - will NOT be pushed to GitHub
- **Note:** This file is created automatically after activation and stored in the user's home directory

---

## 🔑 Master Password for Creating Accounts

**Password:** `MATELASPRO-ADMIN`

- **Location:** Defined in `src/main/java/com/warehouse/controller/LoginController.java` (line 29)
- **Purpose:** Required to access the "Créer un compte" (Create Account) dialog
- **When to use:** Click "Créer un compte" on login screen → Enter this master password → Create new user account
- **⚠️ RECOMMENDATION:** Change this password before production deployment

**To Change:**
1. Open `src/main/java/com/warehouse/controller/LoginController.java`
2. Find line 29: `private static final String MASTER_SIGNUP_PASSWORD = "MATELASPRO-ADMIN";`
3. Change to your desired password
4. Rebuild the application

---

## 👤 Default Admin Account

**Username:** `admin`  
**Password:** `admin123`

- **Location:** Created in database schema (`COMPLETE_SCHEMA.sql`)
- **Purpose:** Default administrator account for initial setup
- **⚠️ IMPORTANT:** Change this password immediately after first login!
- **How to change:** Login as admin → Go to User Management → Edit admin user → Change password

---

## 🔐 License Master Key

**Key:** `STE-HABIBA-MATELAS-PRO-2024`

- **Location:** `src/main/java/com/warehouse/security/LicenseManager.java` (line 33)
- **Purpose:** Used to generate and validate license keys
- **⚠️ CRITICAL:** Change this BEFORE distribution to prevent unauthorized key generation
- **Status:** ⚠️ Currently in source code - should be moved to config file or environment variable for production

---

## 📁 Files Already in .gitignore

The following sensitive files are protected from being committed:

- ✅ `license.dat` (license keys)
- ✅ `*.dat`, `*.license` (license file patterns)
- ✅ `config.properties` (database credentials)
- ✅ `firebase-key.json` (Firebase credentials)
- ✅ `*.key`, `*.pem` (private keys)
- ✅ `login_preferences.properties` (saved usernames)
- ✅ All files in `~/.matelaspro/` directory (user data)

---

## 🔒 Security Checklist for Production

Before distributing your application:

- [ ] Change `MASTER_SIGNUP_PASSWORD` in `LoginController.java`
- [ ] Change `MASTER_KEY` in `LicenseManager.java`
- [ ] Change default admin password after first login
- [ ] Remove or secure this file (`SECURITY_CREDENTIALS.md`)
- [ ] Consider moving sensitive constants to encrypted config files
- [ ] Implement proper key rotation for license keys
- [ ] Add environment-specific configuration management

---

## 📝 Quick Reference

| Item | Value | Location |
|------|-------|----------|
| License File | `~/.matelaspro/license.dat` | User home directory |
| Master Signup Password | `MATELASPRO-ADMIN` | LoginController.java:29 |
| Default Admin Username | `admin` | Database schema |
| Default Admin Password | `admin123` | Database schema |
| License Master Key | `STE-HABIBA-MATELAS-PRO-2024` | LicenseManager.java:33 |

---

**Last Updated:** $(date)  
**⚠️ Keep this file secure and do not commit to version control!**

