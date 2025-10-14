-- ============================================================================
-- MatelasPro - Complete Database Schema (Optimized)
-- Database: warehouse_db
-- Version: 1.0
-- Date: October 12, 2025
-- ============================================================================

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
    brand VARCHAR(100) DEFAULT NULL,
    quantity INT NOT NULL DEFAULT 0,
    prix DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_type (type),
    INDEX idx_size (size),
    INDEX idx_quantity (quantity),
    INDEX idx_type_size (type, size)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample mattresses
INSERT INTO mattress (type, size, brand, quantity, prix) VALUES
('Mousse', '90x190', 'SuperMousse', 50, 1500.00),
('Ressort', '140x190', 'SuperMousse', 30, 2500.00),
('Latex', '160x200', 'SuperMousse', 20, 3500.00),
('Mousse', '120x190', 'SuperMousse', 40, 1800.00),
('Ressort', '180x200', 'SuperMousse', 15, 4000.00)
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
    expected_return_date DATE DEFAULT NULL,
    notes TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_date (date),
    INDEX idx_type (type),
    INDEX idx_mattress_id (mattress_id),
    INDEX idx_store_owner_id (store_owner_id),
    INDEX idx_user_id (user_id),
    INDEX idx_date_type (date, type),
    FOREIGN KEY (mattress_id) REFERENCES mattress(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (store_owner_id) REFERENCES store_owner(id) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

-- View: Transaction details with names (instead of IDs)
CREATE OR REPLACE VIEW transaction_details AS
SELECT 
    t.id,
    t.date,
    t.type,
    m.type AS mattress_type,
    m.size AS mattress_size,
    m.brand AS mattress_brand,
    t.quantity,
    t.prix,
    t.prix * t.quantity AS total,
    s.name AS store_owner_name,
    u.username AS user_name,
    t.expected_return_date,
    t.notes
FROM transaction t
JOIN mattress m ON t.mattress_id = m.id
LEFT JOIN store_owner s ON t.store_owner_id = s.id
JOIN users u ON t.user_id = u.id;

-- View: Inventory summary
CREATE OR REPLACE VIEW inventory_summary AS
SELECT 
    m.id,
    m.type,
    m.size,
    m.brand,
    m.quantity AS current_stock,
    m.prix AS unit_price,
    m.quantity * m.prix AS total_value,
    COALESCE(SUM(CASE WHEN t.type = 'Vente' THEN t.quantity ELSE 0 END), 0) AS total_sold,
    COALESCE(SUM(CASE WHEN t.type = 'Prêt' THEN t.quantity ELSE 0 END), 0) AS total_lent,
    m.updated_at AS last_updated
FROM mattress m
LEFT JOIN transaction t ON m.id = t.mattress_id
GROUP BY m.id;

-- ============================================================================
-- STORED PROCEDURES (Performance optimization)
-- ============================================================================

DELIMITER $$

-- Procedure: Get low stock items
CREATE PROCEDURE IF NOT EXISTS GetLowStockItems(IN threshold INT)
BEGIN
    SELECT 
        id,
        type,
        size,
        brand,
        quantity,
        prix
    FROM mattress
    WHERE quantity <= threshold
    ORDER BY quantity ASC;
END$$

-- Procedure: Get transactions by date range
CREATE PROCEDURE IF NOT EXISTS GetTransactionsByDateRange(
    IN start_date DATE,
    IN end_date DATE
)
BEGIN
    SELECT * FROM transaction_details
    WHERE DATE(date) BETWEEN start_date AND end_date
    ORDER BY date DESC;
END$$

-- Procedure: Get user activity
CREATE PROCEDURE IF NOT EXISTS GetUserActivity(
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

DELIMITER ;

-- ============================================================================
-- TRIGGERS (Data integrity and automation)
-- ============================================================================

DELIMITER $$

-- Trigger: Update mattress quantity after transaction
CREATE TRIGGER IF NOT EXISTS update_stock_after_transaction
AFTER INSERT ON transaction
FOR EACH ROW
BEGIN
    IF NEW.type = 'Vente' OR NEW.type = 'Transfert' OR NEW.type = 'Prêt' THEN
        UPDATE mattress 
        SET quantity = quantity - NEW.quantity
        WHERE id = NEW.mattress_id;
    ELSEIF NEW.type = 'retour' OR NEW.type = 'Réception' THEN
        UPDATE mattress 
        SET quantity = quantity + NEW.quantity
        WHERE id = NEW.mattress_id;
    END IF;
END$$

-- Trigger: Prevent negative stock
CREATE TRIGGER IF NOT EXISTS check_stock_before_transaction
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
    END IF;
END$$

-- Trigger: Log user last login
CREATE TRIGGER IF NOT EXISTS log_user_login
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
-- INDEXES (Additional performance optimization)
-- ============================================================================

-- Composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_transaction_date_type_mattress 
ON transaction(date, type, mattress_id);

CREATE INDEX IF NOT EXISTS idx_mattress_type_quantity 
ON mattress(type, quantity);

-- ============================================================================
-- STATISTICS UPDATE (For query optimizer)
-- ============================================================================

ANALYZE TABLE users;
ANALYZE TABLE mattress;
ANALYZE TABLE store_owner;
ANALYZE TABLE transaction;
ANALYZE TABLE activity_log;

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

-- Check all tables exist
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    DATA_LENGTH,
    INDEX_LENGTH
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'warehouse_db'
ORDER BY TABLE_NAME;

-- ============================================================================
-- OPTIMIZATION SETTINGS (Optional - for production)
-- ============================================================================

-- Enable InnoDB performance features
SET GLOBAL innodb_buffer_pool_size = 256M;  -- Adjust based on available RAM
SET GLOBAL innodb_flush_log_at_trx_commit = 2;  -- Better performance, still safe
SET GLOBAL innodb_log_file_size = 64M;

-- ============================================================================
-- COMPLETION MESSAGE
-- ============================================================================

SELECT 
    '✅ Database schema created successfully!' AS Status,
    'warehouse_db' AS Database_Name,
    COUNT(*) AS Total_Tables
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'warehouse_db';

-- ============================================================================
-- END OF SCHEMA
-- ============================================================================
