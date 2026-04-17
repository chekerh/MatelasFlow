-- ============================================================================
-- QUICK FIX PACK INVENTORY - Simple One-Step Fix
-- ============================================================================
-- This script quickly fixes inventory for all existing pack transactions.
-- Use this if you just want to fix it without seeing diagnostics.
--
-- WARNING: This will modify your inventory. Make sure you have a backup!
-- ============================================================================

-- Create backup
DROP TABLE IF EXISTS mattress_inventory_backup_before_pack_fix;
CREATE TABLE mattress_inventory_backup_before_pack_fix AS
SELECT * FROM mattress;

-- Fix inventory: Reduce quantity for all mattresses in pack transactions
UPDATE mattress m
JOIN (
    SELECT 
        pi.mattress_id,
        SUM(pi.quantity) AS total_quantity_to_reduce
    FROM transaction t
    JOIN pack_items pi ON pi.transaction_id = t.id
    WHERE t.type = 'Pack'
    GROUP BY pi.mattress_id
) AS pack_summary ON pack_summary.mattress_id = m.id
SET m.quantity = m.quantity - pack_summary.total_quantity_to_reduce;

-- Fix quantity_sold: Add pack quantities to quantity_sold
UPDATE mattress m
JOIN (
    SELECT 
        pi.mattress_id,
        SUM(pi.quantity) AS total_quantity_sold
    FROM transaction t
    JOIN pack_items pi ON pi.transaction_id = t.id
    WHERE t.type = 'Pack'
    GROUP BY pi.mattress_id
) AS pack_summary ON pack_summary.mattress_id = m.id
SET m.quantity_sold = m.quantity_sold + pack_summary.total_quantity_sold;

SELECT '✅ Pack inventory fixed successfully!' AS Status;

