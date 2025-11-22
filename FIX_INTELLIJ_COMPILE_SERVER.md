# Fix IntelliJ Compile Server Java Path Issue

## Problem
IntelliJ's compile server cannot find the Java executable:
```
Cannot run program "C:\Users\aymou\.jdks\openjdk-25.0.1\bin\java.exe": CreateProcess error=2
```

## Solution Steps

### Step 1: Verify JDK Path in IntelliJ
1. Go to **File** → **Project Structure** → **Project**
2. Verify the **SDK** is set to **openjdk-25** (Java 25)
3. If not, click the dropdown and select **openjdk-25** from the list
4. If it's not in the list:
   - Click **Edit** next to SDK
   - Click **+** → **Add SDK** → **Download JDK...**
   - Or **+** → **Add SDK** → **JDK**
   - Browse to: `C:\Users\aymou\.jdks\openjdk-25.0.1`
   - Click **OK**
5. Verify **Language level** is set to **24** (or match your Java version)
6. Click **OK**

### Step 2: Invalidate IntelliJ Caches
1. Go to **File** → **Invalidate Caches...**
2. Check all options (especially "Clear file system cache and Local History")
3. Click **Invalidate and Restart**
4. Wait for IntelliJ to restart

### Step 3: Reload Maven Project
1. Open the **Maven** tool window (View → Tool Windows → Maven)
2. Click the **Reload All Maven Projects** button (circular arrow icon)
3. Wait for Maven to reload

### Step 4: Clean and Rebuild
1. Go to **Build** → **Clean Project**
2. Wait for clean to complete
3. Go to **Build** → **Rebuild Project**
4. Wait for rebuild to complete

### Step 5: Check Maven Runner Configuration
1. Go to **File** → **Settings** → **Build, Execution, Deployment** → **Build Tools** → **Maven** → **Runner**
2. Ensure **JRE** is set to **"Use Project JDK"** (should be Java 25)
3. Click **OK**

### Step 6: Set JAVA_HOME for Maven Run Configuration
1. Go to **Run** → **Edit Configurations...**
2. Find or create your Maven configuration for `javafx:run`
3. In **Environment variables**, add:
   ```
   JAVA_HOME=C:\Users\aymou\.jdks\openjdk-25.0.1
   ```
   (No quotes, no trailing slash)
4. Click **OK**

## If Issue Persists

If the compile server still can't find Java, try:

1. **Check File Permissions**: Make sure you have read/execute permissions on the JDK folder
2. **Check PATH**: Verify `C:\Users\aymou\.jdks\openjdk-25.0.1\bin` is in your system PATH
3. **Restart IntelliJ**: Close IntelliJ completely and reopen it
4. **Check Antivirus**: Make sure antivirus isn't blocking access to java.exe

## Verify It's Working
After these steps, try building the project:
- **Build** → **Build Project**
- Check if the compile server error is gone

