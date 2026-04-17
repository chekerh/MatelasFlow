-- ============================================================================
-- MatelasPro - Complete Database Schema for phpMyAdmin (Simple Version)
-- Database: warehouse_db
-- Version: 2.0
-- Compatible with MySQL/phpMyAdmin
-- Instructions: Copy and paste this entire file into phpMyAdmin SQL tab
-- ============================================================================

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS warehouse_db 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Use the database
USE warehouse_db;

-- ============================================================================
-- TABLE 1: users (Utilisateurs)
-- ============================================================================

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('admin', 'employé') NOT NULL DEFAULT 'employé',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default admin user (password: admin123)
INSERT INTO users (username, password_hash, role) 
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMkIjefe.JQGfewQ92s/dUV4xJNVEWNVZJxlApm', 'admin')
ON DUPLICATE KEY UPDATE username=username;

-- ============================================================================
-- TABLE 2: mattress (Matelas)
-- ============================================================================

CREATE TABLE IF NOT EXISTS mattress (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(100) NOT NULL,
    size VARCHAR(50) NOT NULL,
    reference VARCHAR(100) DEFAULT NULL,
    quantity INT NOT NULL DEFAULT 0,
    initial_stock INT NOT NULL DEFAULT 0 COMMENT 'Stock initial reçu',
    quantity_sold INT NOT NULL DEFAULT 0 COMMENT 'Quantité totale vendue',
    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT 'Prix d''achat unitaire',
    prix DECIMAL(10,2) NOT NULL COMMENT 'Prix de vente',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Ordre d''affichage personnalisé',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_type (type),
    INDEX idx_size (size),
    INDEX idx_quantity (quantity),
    INDEX idx_type_size (type, size),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Update initial_stock for existing records (if any)
UPDATE mattress SET initial_stock = quantity WHERE initial_stock = 0 AND quantity > 0;

-- Insert sample mattresses
INSERT INTO mattress (type, size, reference, quantity, initial_stock, quantity_sold, unit_price, prix, sort_order) VALUES
('Mousse', '90x190', 'SuperMousse', 50, 50, 0, 1200.00, 1500.00, 1),
('Ressort', '140x190', 'SuperMousse', 30, 30, 0, 2000.00, 2500.00, 2),
('Latex', '160x200', 'SuperMousse', 20, 20, 0, 3000.00, 3500.00, 3),
('Mousse', '120x190', 'SuperMousse', 40, 40, 0, 1500.00, 1800.00, 4),
('Ressort', '180x200', 'SuperMousse', 15, 15, 0, 3200.00, 4000.00, 5)
ON DUPLICATE KEY UPDATE id=id;

-- ============================================================================
-- TABLE 3: store_owner (Propriétaires de magasin)
-- ============================================================================

CREATE TABLE IF NOT EXISTS store_owner (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact VARCHAR(100) DEFAULT NULL,
    address TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample store owners
INSERT INTO store_owner (name, contact, address) VALUES
('Magasin Central', '0123456789', 'Rue Principale, Centre-ville'),
('Boutique Nord', '0123456790', 'Avenue du Nord, Zone Industrielle'),
('Dépôt Sud', '0123456791', 'Boulevard Sud, Quartier Commercial')
ON DUPLICATE KEY UPDATE id=id;

-- ============================================================================
-- TABLE 4: transaction (Transactions)
-- ============================================================================

CREATE TABLE IF NOT EXISTS transaction (
    id INT AUTO_INCREMENT PRIMARY KEY,
    date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    mattress_id INT NOT NULL,
    quantity INT NOT NULL,
    type ENUM('Vente', 'Transfert', 'Prêt', 'retour', 'Réception') NOT NULL,
    store_owner_id INT DEFAULT NULL,
    user_id INT NOT NULL,
    prix DECIMAL(10,2) NOT NULL,
    expected_return_date DATE DEFAULT NULL COMMENT 'Date de retour prévue pour les prêts',
    notes TEXT DEFAULT NULL,
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Ordre d''affichage personnalisé',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_date (date),
    INDEX idx_type (type),
    INDEX idx_mattress_id (mattress_id),
    INDEX idx_store_owner_id (store_owner_id),
    INDEX idx_user_id (user_id),
    INDEX idx_date_type (date, type),
    INDEX idx_sort_order (sort_order),
    FOREIGN KEY (mattress_id) REFERENCES mattress(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (store_owner_id) REFERENCES store_owner(id) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Update sort_order for existing records (if any)
UPDATE transaction SET sort_order = id WHERE sort_order = 0 OR sort_order IS NULL;

-- ============================================================================
-- TABLE 5: activity_log (Journal d'activité)
-- ============================================================================

CREATE TABLE IF NOT EXISTS activity_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    description TEXT,
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50) DEFAULT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_activity_type (activity_type),
    INDEX idx_timestamp (timestamp),
    INDEX idx_user_timestamp (user_id, timestamp),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- VIEWS (Optimized queries)
-- ============================================================================

-- Drop view if exists, then create
DROP VIEW IF EXISTS transaction_details;

CREATE VIEW transaction_details AS
SELECT 
    t.id,
    t.date,
    t.type,
    m.type AS mattress_type,
    m.size AS mattress_size,
    m.reference AS mattress_reference,
    t.quantity,
    t.prix,
    t.prix * t.quantity AS total,
    s.name AS store_owner_name,
    u.username AS user_name,
    t.expected_return_date,
    t.notes,
    t.sort_order
FROM transaction t
JOIN mattress m ON t.mattress_id = m.id
LEFT JOIN store_owner s ON t.store_owner_id = s.id
JOIN users u ON t.user_id = u.id;

-- Drop view if exists, then create
DROP VIEW IF EXISTS inventory_summary;

CREATE VIEW inventory_summary AS
SELECT 
    m.id,
    m.type,
    m.size,
    m.reference,
    m.quantity AS current_stock,
    m.initial_stock,
    m.quantity_sold,
    m.unit_price,
    m.prix AS sale_price,
    m.quantity * m.prix AS total_value,
    COALESCE(SUM(CASE WHEN t.type = 'Vente' THEN t.quantity ELSE 0 END), 0) AS total_sold_transactions,
    COALESCE(SUM(CASE WHEN t.type = 'Prêt' THEN t.quantity ELSE 0 END), 0) AS total_lent,
    m.updated_at AS last_updated
FROM mattress m
LEFT JOIN transaction t ON m.id = t.mattress_id
GROUP BY m.id, m.type, m.size, m.reference, m.quantity, m.initial_stock, m.quantity_sold, m.unit_price, m.prix, m.updated_at;

-- ============================================================================
-- STORED PROCEDURES (Performance optimization)
-- ============================================================================

DELIMITER $$

-- Procedure: Get low stock items
DROP PROCEDURE IF EXISTS GetLowStockItems$$
CREATE PROCEDURE GetLowStockItems(IN threshold INT)
BEGIN
    SELECT 
        id,
        type,
        size,
        reference,
        quantity,
        initial_stock,
        quantity_sold,
        prix
    FROM mattress
    WHERE quantity <= threshold
    ORDER BY quantity ASC;
END$$

-- Procedure: Get transactions by date range
DROP PROCEDURE IF EXISTS GetTransactionsByDateRange$$
CREATE PROCEDURE GetTransactionsByDateRange(
    IN start_date DATE,
    IN end_date DATE
)
BEGIN
    SELECT * FROM transaction_details
    WHERE DATE(date) BETWEEN start_date AND end_date
    ORDER BY sort_order ASC, date DESC;
END$$

-- Procedure: Get user activity
DROP PROCEDURE IF EXISTS GetUserActivity$$
CREATE PROCEDURE GetUserActivity(
    IN p_user_id INT,
    IN days_back INT
)
BEGIN
    SELECT 
        activity_type,
        description,
        timestamp,
        ip_address
    FROM activity_log
    WHERE user_id = p_user_id
    AND timestamp >= DATE_SUB(NOW(), INTERVAL days_back DAY)
    ORDER BY timestamp DESC;
END$$

-- Procedure: Get mattress sales statistics
DROP PROCEDURE IF EXISTS GetMattressSalesStats$$
CREATE PROCEDURE GetMattressSalesStats(IN p_mattress_id INT)
BEGIN
    SELECT 
        m.id,
        m.type,
        m.size,
        m.reference,
        m.initial_stock,
        m.quantity AS current_stock,
        m.quantity_sold,
        COALESCE(SUM(CASE WHEN t.type = 'Vente' THEN t.quantity * t.prix ELSE 0 END), 0) AS total_revenue,
        COALESCE(SUM(CASE WHEN t.type = 'Vente' THEN t.quantity ELSE 0 END), 0) AS total_sold_count
    FROM mattress m
    LEFT JOIN transaction t ON m.id = t.mattress_id AND t.type = 'Vente'
    WHERE m.id = p_mattress_id
    GROUP BY m.id, m.type, m.size, m.reference, m.initial_stock, m.quantity, m.quantity_sold;
END$$

DELIMITER ;

-- ============================================================================
-- TRIGGERS (Data integrity and automation)
-- ============================================================================

DELIMITER $$

-- Drop existing triggers if they exist
DROP TRIGGER IF EXISTS update_stock_after_transaction$$
DROP TRIGGER IF EXISTS check_stock_before_transaction$$
DROP TRIGGER IF EXISTS log_user_login$$

-- Trigger: Update mattress quantity after transaction
-- Note: Pack transactions are handled by a separate trigger on pack_items
CREATE TRIGGER update_stock_after_transaction
AFTER INSERT ON transaction
FOR EACH ROW
BEGIN
    IF NEW.type = 'Vente' OR NEW.type = 'Transfert' OR NEW.type = 'Prêt' THEN
        UPDATE mattress 
        SET quantity = quantity - NEW.quantity
        WHERE id = NEW.mattress_id;
        
        IF NEW.type = 'Vente' THEN
            UPDATE mattress 
            SET quantity_sold = quantity_sold + NEW.quantity
            WHERE id = NEW.mattress_id;
        END IF;
    ELSEIF NEW.type = 'retour' OR NEW.type = 'Réception' THEN
        UPDATE mattress 
        SET quantity = quantity + NEW.quantity
        WHERE id = NEW.mattress_id;
        
        IF NEW.type = 'Réception' THEN
            UPDATE mattress 
            SET initial_stock = initial_stock + NEW.quantity
            WHERE id = NEW.mattress_id;
        END IF;
    -- Pack transactions are handled by update_stock_after_pack_item trigger
    END IF;
END$$

-- Trigger: Update inventory when pack items are added (for Pack transactions)
DROP TRIGGER IF EXISTS update_stock_after_pack_item$$
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

-- Trigger: Revert inventory when pack items are deleted (for Pack transactions)
DROP TRIGGER IF EXISTS revert_stock_after_pack_item_delete$$
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

-- Trigger: Prevent negative stock
-- Note: Pack transactions are validated by check_stock_before_pack_item trigger
CREATE TRIGGER check_stock_before_transaction
BEFORE INSERT ON transaction
FOR EACH ROW
BEGIN
    DECLARE current_stock INT;
    
    IF NEW.type IN ('Vente', 'Transfert', 'Prêt') THEN
        SELECT quantity INTO current_stock
        FROM mattress
        WHERE id = NEW.mattress_id;
        
        IF current_stock < NEW.quantity THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Stock insuffisant pour cette transaction';
        END IF;
    -- Pack transactions are validated by check_stock_before_pack_item trigger
    END IF;
END$$

-- Trigger: Prevent negative stock for pack items
DROP TRIGGER IF EXISTS check_stock_before_pack_item$$
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

-- Trigger: Log user last login
CREATE TRIGGER log_user_login
AFTER UPDATE ON users
FOR EACH ROW
BEGIN
    IF NEW.last_login != OLD.last_login OR OLD.last_login IS NULL THEN
        INSERT INTO activity_log (user_id, activity_type, description)
        VALUES (NEW.id, 'LOGIN', CONCAT('User logged in: ', NEW.username));
    END IF;
END$$

DELIMITER ;

-- ============================================================================
-- VERIFICATION
-- ============================================================================

SELECT 
    '✅ Database schema created/updated successfully!' AS Status,
    'warehouse_db' AS Database_Name,
    COUNT(*) AS Total_Tables
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'warehouse_db';

