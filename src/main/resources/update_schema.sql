-- Script de mise à jour pour ajouter le type 'retour' aux transactions
USE warehouse ;

-- Renommer la colonne 'brand' en 'reference' si elle existe encore
ALTER TABLE mattress CHANGE COLUMN brand reference VARCHAR(100) NULL;

-- Ajouter la colonne unit_price si elle n'existe pas
ALTER TABLE mattress ADD COLUMN IF NOT EXISTS unit_price DECIMAL(10,2) NOT NULL DEFAULT 0 AFTER quantity;

-- Modifier la table transaction pour inclure 'retour' et 'Réception' dans l'ENUM
ALTER TABLE transaction MODIFY COLUMN type ENUM('Vente', 'Transfert', 'Prêt', 'retour', 'Réception') NOT NULL;

-- Ajouter la colonne expected_return_date si elle n'existe pas
ALTER TABLE transaction ADD COLUMN IF NOT EXISTS expected_return_date DATE AFTER prix; 

-- Colonnes d'ordre manuel
ALTER TABLE mattress ADD COLUMN IF NOT EXISTS sort_order INT NOT NULL DEFAULT 0;
ALTER TABLE transaction ADD COLUMN IF NOT EXISTS sort_order INT NOT NULL DEFAULT 0;

UPDATE mattress SET sort_order = id WHERE sort_order = 0;
UPDATE transaction SET sort_order = id WHERE sort_order = 0;