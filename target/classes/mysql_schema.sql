-- Création de la base de données
CREATE DATABASE IF NOT EXISTS warehouse_mattress;
USE warehouse_mattress;

-- Table des utilisateurs (admin/employé)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('admin', 'employé') NOT NULL
);

-- Table des matelas
CREATE TABLE IF NOT EXISTS mattress (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(100) NOT NULL,
    size VARCHAR(50) NOT NULL,
    brand VARCHAR(100),
    quantity INT NOT NULL DEFAULT 0,
    prix DECIMAL(10,2) NOT NULL -- Prix fixe du matelas
);

-- Table des propriétaires de magasin
CREATE TABLE IF NOT EXISTS store_owner (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact VARCHAR(100)
);

-- Table des transactions
CREATE TABLE IF NOT EXISTS transaction (
    id INT AUTO_INCREMENT PRIMARY KEY,
    date DATETIME NOT NULL,
    mattress_id INT NOT NULL,
    quantity INT NOT NULL,
    type ENUM('Vente', 'Transfert', 'Prêt') NOT NULL,
    store_owner_id INT,
    user_id INT NOT NULL,
    prix DECIMAL(10,2) NOT NULL,
    expected_return_date DATE,
    notes TEXT,
    FOREIGN KEY (mattress_id) REFERENCES mattress(id),
    FOREIGN KEY (store_owner_id) REFERENCES store_owner(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
); 