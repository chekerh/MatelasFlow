# Fix Java Version Issue

## Problem
The application was compiled with Java 21 but is being run with Java 8, causing:
```
UnsupportedClassVersionError: class file version 65.0 (requires Java 21), but Java Runtime only recognizes up to 52.0 (Java 8)
```

**Root Cause**: The JavaFX Maven plugin is spawning a Java process that uses Java 8, even though Maven itself uses Java 24.

## Solution - Configure IntelliJ IDEA

### Step 1: Set Project SDK
1. Go to **File** → **Project Structure** → **Project**
2. Set **SDK** to **corretto-24.0.2** (or Java 21+)
3. Click **OK**

### Step 2: Configure Maven Runner
1. Go to **File** → **Settings** (or **IntelliJ IDEA** → **Preferences** on Mac)
2. Navigate to **Build, Execution, Deployment** → **Build Tools** → **Maven** → **Runner**
3. Set **JRE** to **"Use Project JDK"** (this ensures Maven uses Java 24)
4. **IMPORTANT**: Also check **"Delegate IDE build/run actions to Maven"** if available
5. Click **OK**

### Step 3: Set JAVA_HOME Environment Variable (CRITICAL)
The JavaFX plugin spawns a separate Java process that uses JAVA_HOME. **This is the most important step!**

**Option A - Set in Maven Run Configuration (RECOMMENDED):**
1. In IntelliJ, go to **Run** → **Edit Configurations...**
2. Find your Maven run configuration for `javafx:run` (or create a new one)
3. If it doesn't exist, create a new Maven configuration:
   - Click **+** → **Maven**
   - Name: `javafx:run`
   - Working directory: your project root
   - Command line: `org.openjfx:javafx-maven-plugin:0.0.8:run`
4. In the configuration, find **Environment variables** section
5. Add/Edit: `JAVA_HOME=C:\Users\aymou\.jdks\corretto-24.0.2`
   - **Important**: This should be the JDK root folder, NOT the `bin` folder
   - Make sure there are no quotes around the path
6. Click **OK**

**Option B - Set System-Wide (Alternative):**
1. Open Windows System Properties → **Environment Variables**
2. Under **User variables** or **System variables**, find or create `JAVA_HOME`
3. Set value to: `C:\Users\aymou\.jdks\corretto-24.0.2`
   - **Important**: Point to the JDK root, NOT the `bin` folder
4. Also ensure `C:\Users\aymou\.jdks\corretto-24.0.2\bin` is in your PATH (should be first)
5. Restart IntelliJ IDEA after changing system environment variables

### Step 4: Clean and Rebuild
1. In IntelliJ: **Build** → **Rebuild Project**
2. Or from Maven tool window: Right-click project → **Reload Project**, then **Clean**, then **Install**

## What Was Fixed in pom.xml
- ✅ Added explicit Java 21 configuration to maven-compiler-plugin
- ✅ Added Java version properties
- ✅ Created Maven toolchain configuration
- ✅ Cleaned target directory (force fresh rebuild)
- ✅ Configured all plugins to use Java 21

## Verify It's Working
After configuration, when you run the application, check the console output:
- You should see Java 24/21 in the path, not Java 8
- The error about "class file version 65.0" should be gone

If the issue persists, the JavaFX plugin might be using a different Java. Check:
- `echo %JAVA_HOME%` in command prompt
- Make sure Java 8 is not in your PATH before Java 21/24

