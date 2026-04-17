-- ============================================================================
-- CHECK PACK TRIGGERS - Diagnostic Script
-- ============================================================================
-- This script helps you verify if the pack inventory triggers are installed
-- and working correctly.
-- ============================================================================

-- Check if triggers exist
SELECT 
    '=== CHECKING TRIGGERS ===' AS Info;

SELECT 
    TRIGGER_NAME,
    EVENT_MANIPULATION,
    EVENT_OBJECT_TABLE,
    ACTION_STATEMENT
FROM information_schema.TRIGGERS
WHERE TRIGGER_SCHEMA = DATABASE()
AND TRIGGER_NAME IN (
    'update_stock_after_pack_item',
    'revert_stock_after_pack_item_delete',
    'check_stock_before_pack_item'
)
ORDER BY TRIGGER_NAME;

-- Check recent pack transactions
SELECT 
    '=== RECENT PACK TRANSACTIONS ===' AS Info;

SELECT 
    t.id,
    t.date,
    t.type,
    t.quantity,
    COUNT(pi.id) AS pack_item_count
FROM transaction t
LEFT JOIN pack_items pi ON pi.transaction_id = t.id
WHERE t.type = 'Pack' OR t.type LIKE '%Pack%' OR t.type LIKE '%pack%'
GROUP BY t.id, t.date, t.type, t.quantity
ORDER BY t.date DESC
LIMIT 10;

-- Check pack items for recent pack transactions
SELECT 
    '=== PACK ITEMS FOR RECENT TRANSACTIONS ===' AS Info;

SELECT 
    t.id AS transaction_id,
    t.type AS transaction_type,
    pi.mattress_id,
    m.size,
    m.reference,
    pi.quantity AS pack_quantity,
    m.quantity AS current_mattress_stock,
    m.quantity_sold AS current_quantity_sold
FROM transaction t
JOIN pack_items pi ON pi.transaction_id = t.id
JOIN mattress m ON m.id = pi.mattress_id
WHERE t.type = 'Pack' OR t.type LIKE '%Pack%' OR t.type LIKE '%pack%'
ORDER BY t.date DESC, pi.mattress_id
LIMIT 20;

-- Check if there's a mismatch (pack items exist but inventory wasn't reduced)
SELECT 
    '=== POTENTIAL ISSUES: PACK ITEMS WHERE INVENTORY WAS NOT REDUCED ===' AS Info;

SELECT 
    t.id AS transaction_id,
    t.date,
    t.type,
    pi.mattress_id,
    m.size,
    m.reference,
    pi.quantity AS should_have_reduced,
    m.quantity AS current_stock,
    -- If current stock + pack quantity = what it should be, then inventory wasn't reduced
    CASE 
        WHEN (m.quantity + pi.quantity) <= m.initial_stock THEN '✅ Inventory was reduced'
        ELSE '⚠️ Inventory may not have been reduced'
    END AS status
FROM transaction t
JOIN pack_items pi ON pi.transaction_id = t.id
JOIN mattress m ON m.id = pi.mattress_id
WHERE (t.type = 'Pack' OR t.type LIKE '%Pack%' OR t.type LIKE '%pack%')
AND t.date >= DATE_SUB(NOW(), INTERVAL 7 DAY)  -- Check last 7 days
ORDER BY t.date DESC;

-- Show all distinct transaction types (to see how Pack is stored)
SELECT 
    '=== ALL TRANSACTION TYPES IN DATABASE ===' AS Info;

SELECT DISTINCT type, COUNT(*) AS count
FROM transaction
GROUP BY type
ORDER BY type;

