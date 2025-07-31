package com.warehouse.util;

import com.warehouse.model.Mattress;
import com.warehouse.model.StoreOwner;
import com.warehouse.model.Transaction;
import java.time.LocalDate;
import java.util.List;

public class TransactionValidator {
    
    /**
     * Valide une transaction selon son type
     */
    public static ValidationResult validateTransaction(String type, Mattress mattress, int quantity, 
                                                   StoreOwner storeOwner, LocalDate expectedReturnDate, 
                                                   String destination, StoreOwner returnFrom) {
        
        ValidationResult result = new ValidationResult();
        
        // Validation de base
        if (mattress == null) {
            result.addError("Un matelas doit être sélectionné.");
            return result;
        }
        
        if (quantity <= 0) {
            result.addError("La quantité doit être positive.");
            return result;
        }
        
        // Validation des quantités maximales
        if (quantity > 1000) {
            result.addError("La quantité ne peut pas dépasser 1000 unités.");
            return result;
        }
        
        // Validation selon le type
        switch (type) {
            case "Vente":
                return validateSale(mattress, quantity, result);
                
            case "Prêt":
                return validateLending(mattress, quantity, storeOwner, expectedReturnDate, result);
                
            case "Transfert":
                return validateTransfer(mattress, quantity, destination, result);
                
            case "retour":
                return validateReturn(mattress, quantity, returnFrom, result);
                
            case "Réception":
                return validateReception(mattress, quantity, result);
                
            default:
                result.addError("Type de transaction invalide: " + type);
                return result;
        }
    }
    
    private static ValidationResult validateSale(Mattress mattress, int quantity, ValidationResult result) {
        if (mattress.getQuantity() < quantity) {
            result.addError("Stock insuffisant. Disponible: " + mattress.getQuantity());
        }
        return result;
    }
    
    private static ValidationResult validateLending(Mattress mattress, int quantity, StoreOwner storeOwner, 
                                                  LocalDate expectedReturnDate, ValidationResult result) {
        if (storeOwner == null) {
            result.addError("Un propriétaire est obligatoire pour un prêt.");
        }
        
        if (expectedReturnDate == null) {
            result.addError("Une date de retour est obligatoire pour un prêt.");
        } else if (expectedReturnDate.isBefore(LocalDate.now())) {
            result.addError("La date de retour ne peut pas être dans le passé.");
        }
        
        if (mattress.getQuantity() < quantity) {
            result.addError("Stock insuffisant. Disponible: " + mattress.getQuantity());
        }
        
        return result;
    }
    
    private static ValidationResult validateTransfer(Mattress mattress, int quantity, String destination, 
                                                   ValidationResult result) {
        if (destination == null || destination.trim().isEmpty()) {
            result.addError("Une destination est obligatoire pour un transfert.");
        }
        
        if (mattress.getQuantity() < quantity) {
            result.addError("Stock insuffisant. Disponible: " + mattress.getQuantity());
        }
        
        return result;
    }
    
    private static ValidationResult validateReturn(Mattress mattress, int quantity, StoreOwner returnFrom, 
                                                 ValidationResult result) {
        if (returnFrom == null) {
            result.addError("Un propriétaire est obligatoire pour un retour.");
        }
        
        return result;
    }
    
    private static ValidationResult validateReception(Mattress mattress, int quantity, ValidationResult result) {
        if (quantity <= 0) {
            result.addError("La quantité reçue doit être positive.");
        }
        
        return result;
    }
    
    /**
     * Détermine si une transaction affecte le stock
     */
    public static boolean affectsStock(String type) {
        return "Vente".equals(type) || "Prêt".equals(type) || "Transfert".equals(type) || "retour".equals(type) || "Réception".equals(type);
    }
    
    /**
     * Détermine si une transaction diminue le stock
     */
    public static boolean decreasesStock(String type) {
        return "Vente".equals(type) || "Prêt".equals(type) || "Transfert".equals(type);
    }
    
    /**
     * Détermine si une transaction augmente le stock
     */
    public static boolean increasesStock(String type) {
        return "retour".equals(type) || "Réception".equals(type);
    }
    
    /**
     * Génère les notes automatiques selon le type de transaction
     */
    public static String generateNotes(String type, String destination, StoreOwner returnFrom) {
        switch (type) {
            case "Transfert":
                return "Transfert vers: " + destination;
            case "retour":
                return "Retour de: " + (returnFrom != null ? returnFrom.getName() : "Inconnu");
            case "Réception":
                return "Réception de nouveaux matelas";
            default:
                return "";
        }
    }
    
    public static class ValidationResult {
        private boolean valid = true;
        private StringBuilder errors = new StringBuilder();
        
        public void addError(String error) {
            valid = false;
            if (errors.length() > 0) {
                errors.append("\n");
            }
            errors.append(error);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrors() {
            return errors.toString();
        }
    }
} 