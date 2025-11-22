# How to Import Schema into phpMyAdmin

## Quick Guide

### Step 1: Open phpMyAdmin
1. Log into your phpMyAdmin interface (usually at `http://localhost/phpmyadmin`)
2. You should see the phpMyAdmin dashboard

### Step 2: Import the Schema

**Option A: Using SQL Tab (Recommended)**
1. Click on the **SQL** tab at the top of phpMyAdmin
2. Open the file `SCHEMA_PHPMYADMIN_SIMPLE.sql` in a text editor
3. **Copy the entire contents** of the file
4. **Paste it into the SQL text area** in phpMyAdmin
5. Click the **Go** button at the bottom
6. Wait for the execution to complete

**Option B: Using Import Tab**
1. Click on the **Import** tab at the top of phpMyAdmin
2. Click **Choose File** button
3. Select the file `SCHEMA_PHPMYADMIN_SIMPLE.sql`
4. Make sure **SQL** format is selected
5. Click **Go** at the bottom
6. Wait for the import to complete

### Step 3: Verify Import
1. Look at the left sidebar - you should see a database called `warehouse_db`
2. Click on `warehouse_db` to expand it
3. You should see 5 tables:
   - `users`
   - `mattress`
   - `store_owner`
   - `transaction`
   - `activity_log`

### Step 4: Check Sample Data
1. Click on the `users` table
2. Click the **Browse** tab
3. You should see the default admin user
4. Click on the `mattress` table and browse to see sample mattresses

## Troubleshooting

### If you get errors:

**Error: "Database already exists"**
- This is OK - the schema will use the existing database
- You can ignore this or manually drop the database first if you want a fresh start

**Error: "Table already exists"**
- Some tables might already exist
- The `CREATE TABLE IF NOT EXISTS` should prevent this, but if it occurs:
  1. You can manually drop existing tables first
  2. Or skip the table creation statements and only run the INSERT statements

**Error: "Unknown collation" or "Unknown charset"**
- Your MySQL version might not support `utf8mb4`
- Replace `utf8mb4` with `utf8` in the schema file
- Replace `utf8mb4_unicode_ci` with `utf8_general_ci`

**Error with DELIMITER statements**
- If you get errors with stored procedures or triggers:
  1. Try running the tables and views first (everything before DELIMITER $$)
  2. Then run the stored procedures section
  3. Finally run the triggers section

**Error with FOREIGN KEY**
- If foreign key constraints fail:
  1. Make sure all tables are created first
  2. Make sure sample data is inserted before creating foreign keys
  3. Or temporarily remove foreign key constraints and add them later

### Partial Import

If some parts fail:
1. **Tables and Views**: Should work fine - copy everything up to "STORED PROCEDURES"
2. **Stored Procedures**: Copy the DELIMITER $$ section through DELIMITER ;
3. **Triggers**: Copy the second DELIMITER $$ section through DELIMITER ;

## Files Available

- **SCHEMA_PHPMYADMIN_SIMPLE.sql** - Recommended version, simpler and easier to use
- **SCHEMA_PHPMYADMIN.sql** - Full version with advanced index checks
- **COMPLETE_SCHEMA.sql** - Original schema (may have compatibility issues)

## Database Credentials

After import, the default admin user is:
- **Username**: `admin`
- **Password**: `admin123` (BCrypt hash in database)

⚠️ **Important**: Change the default password in production!

## Next Steps

1. Verify all tables are created
2. Check sample data is inserted
3. Test the views work (transaction_details, inventory_summary)
4. Configure your Java application to connect to this database
5. Update database connection settings in your application

