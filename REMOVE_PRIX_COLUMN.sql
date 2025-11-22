-- ============================================================================
-- Migration Script: Remove prix (sale price) column from mattress table
-- Purpose: Price is now set per transaction, not per mattress (varies by client/time)
-- Date: 2025-11-21
-- ============================================================================

USE warehouse_db;

-- Check if prix column exists before attempting to remove it
SET @col_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'warehouse_db' 
    AND TABLE_NAME = 'mattress' 
    AND COLUMN_NAME = 'prix'
);

-- Remove prix column if it exists
SET @sql = IF(@col_exists > 0,
    'ALTER TABLE mattress DROP COLUMN prix',
    'SELECT "Column prix does not exist in mattress table - no action needed" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Drop and recreate inventory_summary view without prix column
DROP VIEW IF EXISTS inventory_summary;

CREATE OR REPLACE VIEW inventory_summary AS
SELECT 
    m.id,
    m.type,
    m.size,
    m.reference,
    m.quantity AS current_stock,
    m.initial_stock,
    m.quantity_sold,
    m.unit_price,
    m.quantity * m.unit_price AS total_cost,
    COALESCE(SUM(CASE WHEN t.type = 'Vente' THEN t.quantity ELSE 0 END), 0) AS total_sold_transactions,
    COALESCE(SUM(CASE WHEN t.type = 'Prêt' THEN t.quantity ELSE 0 END), 0) AS total_lent,
    COALESCE(AVG(CASE WHEN t.type = 'Vente' THEN t.prix ELSE NULL END), 0) AS avg_sale_price,
    m.updated_at AS last_updated
FROM mattress m
LEFT JOIN transaction t ON m.id = t.mattress_id
GROUP BY m.id, m.type, m.size, m.reference, m.quantity, m.initial_stock, m.quantity_sold, m.unit_price, m.updated_at;

SELECT 'Migration completed: prix column removed from mattress table' AS status;
SELECT 'Note: Price is now set per transaction in the transaction table' AS note;

