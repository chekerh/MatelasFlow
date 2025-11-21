# 🔐 License Activation System - Guide

## Overview

MatelasPro now includes a license activation system to prevent unauthorized use. The application will prompt for a license key on first launch.

## How It Works

1. **First Launch**: Application checks for a valid license key
2. **No License Found**: Shows activation dialog requiring a valid license key
3. **License Valid**: Application proceeds to login screen
4. **License Stored**: Encrypted and saved to `~/.matelaspro/license.dat`

## License Key Format

License keys follow the format: **XXXX-XXXX-XXXX-XXXX-XXXX**
- 5 groups of 4 alphanumeric characters (A-Z, 0-9)
- Separated by hyphens
- Example: `A1B2-C3D4-E5F6-G7H8-I9J0`

## Security Features

✅ **Encrypted Storage**: License keys are encrypted using AES encryption
✅ **Checksum Validation**: Each license key has a built-in checksum to prevent tampering
✅ **File Permissions**: License file is protected (owner-only read/write on Unix systems)
✅ **Format Validation**: Strict format checking prevents invalid keys
✅ **Master Key**: Hidden master key used for validation (change in `LicenseManager.java`)

## Generating License Keys

### For Testing/Development

Use the `generateLicenseKey()` method in `LicenseManager`:

```java
String testKey = LicenseManager.generateLicenseKey();
System.out.println("Test License: " + testKey);
```

### For Production

**IMPORTANT**: Generate license keys server-side using your own algorithm and send them to customers. Never distribute the master key or key generation code with the application.

### Recommended Production Approach

1. **Create a License Server**: 
   - Generate keys server-side
   - Store customer info (name, email, etc.) linked to each key
   - Track activation dates and hardware IDs (optional)

2. **License Key Format**:
   - Include customer identifier in the key
   - Add expiration date encoding (optional)
   - Include checksum for validation

3. **Activation Process**:
   - Customer purchases → Receives license key
   - Customer enters key in application
   - Application validates key format and checksum
   - For enhanced security: send hardware fingerprint to server for activation

## Customizing the Master Key

**CHANGE THIS BEFORE DISTRIBUTION!**

In `src/main/java/com/warehouse/security/LicenseManager.java`:

```java
// Line 31: Change this to your own secret key
private static final String MASTER_KEY = "YOUR-SECRET-KEY-HERE-2024";
```

**Important**: 
- Use a long, random string
- Keep it secret - never commit to version control
- Use the same master key for generating and validating keys

## File Locations

- **License File**: `~/.matelaspro/license.dat` (user's home directory)
- **Config Directory**: `~/.matelaspro/`

## Removing License (For Testing)

To test the activation dialog again:

1. Delete the license file: `~/.matelaspro/license.dat`
2. Or use: `LicenseManager.removeLicense()`

## Troubleshooting

### "Invalid license key format"
- Check that the key matches the format: `XXXX-XXXX-XXXX-XXXX-XXXX`
- Ensure all characters are uppercase
- Remove any spaces

### "License checksum validation failed"
- The key is not valid (not generated with the correct master key)
- The key has been tampered with

### "Failed to save license key"
- Check file permissions on `~/.matelaspro/`
- Ensure write access to user's home directory

## Implementation Details

### License Validation Process

1. **Format Check**: Validates the format (XXXX-XXXX-XXXX-XXXX-XXXX)
2. **Checksum Calculation**: 
   - Extracts checksum (last 4 characters)
   - Calculates SHA-256 hash of (key part + master key)
   - Compares last 4 characters of hash with checksum
3. **Encryption**: If valid, encrypts and saves to disk

### Security Considerations

⚠️ **Current Implementation**: Uses client-side validation only. For stronger protection:

1. **Online Activation**: Require internet connection for first activation
2. **Hardware Binding**: Bind license to specific machine (CPU ID, MAC address)
3. **Expiration Dates**: Encode expiration dates in the license key
4. **Server Validation**: Validate keys against a server database
5. **Obfuscation**: Obfuscate the Java code to make reverse engineering harder

## Code Locations

- **License Manager**: `src/main/java/com/warehouse/security/LicenseManager.java`
- **Activation Controller**: `src/main/java/com/warehouse/controller/LicenseActivationController.java`
- **Activation View**: `src/main/resources/fxml/LicenseActivationView.fxml`
- **App Integration**: `src/main/java/com/warehouse/App.java` (lines 28-40)

## Schema Verification

✅ **COMPLETE_SCHEMA.sql is ready for phpMyAdmin**
- All tables, triggers, procedures, and views are properly defined
- MySQL syntax is correct
- All DELIMITER statements are properly closed
- Can be safely pasted into phpMyAdmin

---

**⚠️ IMPORTANT FOR PRODUCTION**: 
1. Change the `MASTER_KEY` constant
2. Implement server-side license generation
3. Consider adding hardware binding and online activation
4. Obfuscate your code before distribution

