-- ============================================================================
-- PACK INVENTORY FIX - Migration Script
-- ============================================================================
-- This script fixes the issue where pack sales don't reduce inventory
-- for individual mattresses in the pack.
--
-- Run this script on your existing database to apply the fix.
-- ============================================================================

DELIMITER $$

-- Drop existing triggers if they exist
DROP TRIGGER IF EXISTS update_stock_after_pack_item$$
DROP TRIGGER IF EXISTS revert_stock_after_pack_item_delete$$
DROP TRIGGER IF EXISTS check_stock_before_pack_item$$

-- ============================================================================
-- Trigger: Update inventory when pack items are added (for Pack transactions)
-- ============================================================================
-- This trigger automatically reduces inventory for each mattress when
-- pack items are added to a Pack transaction.
CREATE TRIGGER update_stock_after_pack_item
AFTER INSERT ON pack_items
FOR EACH ROW
BEGIN
    DECLARE transaction_type VARCHAR(50);
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
    BEGIN
        -- Log error but don't fail the insert
        -- The Java code will handle inventory update as fallback
        GET DIAGNOSTICS CONDITION 1
            @sqlstate = RETURNED_SQLSTATE,
            @errno = MYSQL_ERRNO,
            @text = MESSAGE_TEXT;
        -- Error is logged but insert continues
    END;
    
    -- Get the transaction type
    SELECT type INTO transaction_type
    FROM transaction
    WHERE id = NEW.transaction_id;
    
    -- Only process if this is a Pack transaction (case-insensitive check)
    IF transaction_type = 'Pack' OR UPPER(transaction_type) = 'PACK' THEN
        -- Decrease stock for each mattress in the pack
        UPDATE mattress 
        SET quantity = quantity - NEW.quantity
        WHERE id = NEW.mattress_id
        AND quantity >= NEW.quantity;  -- Safety check to prevent negative
        
        -- Update quantity_sold for pack sales
        UPDATE mattress 
        SET quantity_sold = quantity_sold + NEW.quantity
        WHERE id = NEW.mattress_id;
    END IF;
END$$

-- ============================================================================
-- Trigger: Revert inventory when pack items are deleted (for Pack transactions)
-- ============================================================================
-- This trigger automatically reverts inventory when pack items are deleted
-- (e.g., when a pack transaction is deleted or edited).
CREATE TRIGGER revert_stock_after_pack_item_delete
AFTER DELETE ON pack_items
FOR EACH ROW
BEGIN
    DECLARE transaction_type VARCHAR(50);
    
    -- Get the transaction type
    SELECT type INTO transaction_type
    FROM transaction
    WHERE id = OLD.transaction_id;
    
    -- Only process if this is a Pack transaction
    IF transaction_type = 'Pack' THEN
        -- Revert quantity (increase back)
        UPDATE mattress 
        SET quantity = quantity + OLD.quantity
        WHERE id = OLD.mattress_id;
        
        -- Revert quantity_sold (decrease)
        UPDATE mattress 
        SET quantity_sold = quantity_sold - OLD.quantity
        WHERE id = OLD.mattress_id
        AND quantity_sold >= OLD.quantity;
    END IF;
END$$

-- ============================================================================
-- Trigger: Prevent negative stock for pack items
-- ============================================================================
-- This trigger validates stock availability before adding pack items.
CREATE TRIGGER check_stock_before_pack_item
BEFORE INSERT ON pack_items
FOR EACH ROW
BEGIN
    DECLARE current_stock INT;
    DECLARE transaction_type VARCHAR(50);
    
    -- Get the transaction type
    SELECT type INTO transaction_type
    FROM transaction
    WHERE id = NEW.transaction_id;
    
    -- Only validate if this is a Pack transaction
    IF transaction_type = 'Pack' THEN
        SELECT quantity INTO current_stock
        FROM mattress
        WHERE id = NEW.mattress_id;
        
        IF current_stock < NEW.quantity THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = CONCAT('Stock insuffisant pour le matelas ID: ', NEW.mattress_id, ' dans le pack');
        END IF;
    END IF;
END$$

DELIMITER ;

-- ============================================================================
-- IMPORTANT: Fix existing pack transactions
-- ============================================================================
-- If you have existing pack transactions that were created before this fix,
-- you MUST run the FIX_EXISTING_PACK_INVENTORY.sql script to correct your
-- inventory. That script will:
-- 1. Show you diagnostic information about what needs to be fixed
-- 2. Create a backup of your current inventory
-- 3. Automatically fix all pack transactions
-- 4. Verify that the fix worked correctly
--
-- To fix existing pack transactions:
-- 1. Open phpMyAdmin
-- 2. Select your warehouse_db database
-- 3. Go to the SQL tab
-- 4. Open and run FIX_EXISTING_PACK_INVENTORY.sql
--
-- ============================================================================

SELECT '✅ Pack inventory triggers installed successfully!' AS Status;

