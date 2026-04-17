-- ============================================================================
-- PACK FEATURE MIGRATION SQL FOR phpMyAdmin
-- ============================================================================
-- Run this SQL in phpMyAdmin to add pack functionality to your database
-- This adds the pack_items table and 'Pack' transaction type
-- ============================================================================

-- Step 1: Create pack_items table
-- This table stores individual items within a pack transaction
CREATE TABLE IF NOT EXISTS pack_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id INT NOT NULL,
    mattress_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_mattress_id (mattress_id),
    FOREIGN KEY (transaction_id) REFERENCES transaction(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (mattress_id) REFERENCES mattress(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 2: Add 'Pack' to transaction type ENUM
-- This allows transactions to have type 'Pack'
ALTER TABLE transaction 
MODIFY COLUMN type ENUM('Vente', 'Transfert', 'Prêt', 'retour', 'Réception', 'Pack') NOT NULL;

-- ============================================================================
-- Verification queries (optional - run these to verify the changes)
-- ============================================================================

-- Check if pack_items table exists
SELECT TABLE_NAME 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'pack_items';

-- Check if 'Pack' is in transaction type enum
SELECT COLUMN_TYPE 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'transaction' 
AND COLUMN_NAME = 'type';

-- ============================================================================
-- END OF MIGRATION
-- ============================================================================

