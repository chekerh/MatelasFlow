-- Script de mise à jour pour ajouter le type 'retour' aux transactions
USE warehouse_mattress;

-- Modifier la table transaction pour inclure 'retour' et 'Réception' dans l'ENUM
ALTER TABLE transaction MODIFY COLUMN type ENUM('Vente', 'Transfert', 'Prêt', 'retour', 'Réception') NOT NULL;

-- Ajouter la colonne expected_return_date si elle n'existe pas
ALTER TABLE transaction ADD COLUMN IF NOT EXISTS expected_return_date DATE AFTER prix; 