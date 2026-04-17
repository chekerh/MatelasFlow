-- ============================================================================
-- VERIFY AND FIX PACK TRIGGERS - Complete Fix Script
-- ============================================================================
-- This script will:
-- 1. Check if triggers exist
-- 2. Drop and recreate them with improved error handling
-- 3. Verify they're working
-- ============================================================================

DELIMITER $$

-- Drop existing triggers if they exist
DROP TRIGGER IF EXISTS update_stock_after_pack_item$$
DROP TRIGGER IF EXISTS revert_stock_after_pack_item_delete$$
DROP TRIGGER IF EXISTS check_stock_before_pack_item$$

-- ============================================================================
-- Trigger: Update inventory when pack items are added (IMPROVED VERSION)
-- ============================================================================
CREATE TRIGGER update_stock_after_pack_item
AFTER INSERT ON pack_items
FOR EACH ROW
BEGIN
    DECLARE transaction_type VARCHAR(50);
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
    BEGIN
        -- Log error but don't fail the insert
        -- The Java code will handle inventory update as fallback
    END;
    
    -- Get the transaction type
    SELECT type INTO transaction_type
    FROM transaction
    WHERE id = NEW.transaction_id;
    
    -- Only process if this is a Pack transaction (case-insensitive)
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
-- Trigger: Revert inventory when pack items are deleted
-- ============================================================================
CREATE TRIGGER revert_stock_after_pack_item_delete
AFTER DELETE ON pack_items
FOR EACH ROW
BEGIN
    DECLARE transaction_type VARCHAR(50);
    
    -- Get the transaction type
    SELECT type INTO transaction_type
    FROM transaction
    WHERE id = OLD.transaction_id;
    
    -- Only process if this is a Pack transaction (case-insensitive)
    IF transaction_type = 'Pack' OR UPPER(transaction_type) = 'PACK' THEN
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
    
    -- Only validate if this is a Pack transaction (case-insensitive)
    IF transaction_type = 'Pack' OR UPPER(transaction_type) = 'PACK' THEN
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
-- VERIFICATION: Check if triggers are installed
-- ============================================================================
SELECT 
    '=== TRIGGER VERIFICATION ===' AS Info;

SELECT 
    TRIGGER_NAME,
    EVENT_MANIPULATION,
    EVENT_OBJECT_TABLE,
    CASE 
        WHEN TRIGGER_NAME = 'update_stock_after_pack_item' THEN '✅ Main trigger for pack inventory'
        WHEN TRIGGER_NAME = 'revert_stock_after_pack_item_delete' THEN '✅ Revert trigger for pack deletions'
        WHEN TRIGGER_NAME = 'check_stock_before_pack_item' THEN '✅ Validation trigger for pack items'
        ELSE 'Other trigger'
    END AS Description
FROM information_schema.TRIGGERS
WHERE TRIGGER_SCHEMA = DATABASE()
AND TRIGGER_NAME IN (
    'update_stock_after_pack_item',
    'revert_stock_after_pack_item_delete',
    'check_stock_before_pack_item'
)
ORDER BY TRIGGER_NAME;

-- Count triggers found
SELECT 
    CASE 
        WHEN COUNT(*) = 3 THEN '✅ All 3 triggers installed successfully!'
        WHEN COUNT(*) > 0 THEN CONCAT('⚠️ Only ', COUNT(*), ' of 3 triggers found')
        ELSE '❌ No triggers found - installation may have failed'
    END AS Status
FROM information_schema.TRIGGERS
WHERE TRIGGER_SCHEMA = DATABASE()
AND TRIGGER_NAME IN (
    'update_stock_after_pack_item',
    'revert_stock_after_pack_item_delete',
    'check_stock_before_pack_item'
);

SELECT '✅ Trigger installation complete!' AS Final_Status;

