package com.warehouse.integration;

import com.warehouse.model.Mattress;
import com.warehouse.model.Transaction;
import java.time.LocalDateTime;
import java.util.*;

public class EcommerceIntegration {
    
    public enum Platform {
        SHOPIFY("Shopify", "shopify"),
        WOOCOMMERCE("WooCommerce", "woocommerce"),
        PRESTASHOP("PrestaShop", "prestashop"),
        MAGENTO("Magento", "magento");
        
        private final String displayName;
        private final String apiEndpoint;
        
        Platform(String displayName, String apiEndpoint) {
            this.displayName = displayName;
            this.apiEndpoint = apiEndpoint;
        }
        
        public String getDisplayName() { return displayName; }
        public String getApiEndpoint() { return apiEndpoint; }
    }
    
    /**
     * Synchronise les produits avec une plateforme e-commerce
     */
    public static boolean syncProducts(Platform platform, List<Mattress> mattresses) {
        try {
            System.out.println("Synchronisation avec " + platform.getDisplayName() + "...");
            
            for (Mattress mattress : mattresses) {
                // Simuler l'envoi à l'API
                String productData = createProductData(mattress);
                sendToEcommerce(platform, productData);
            }
            
            System.out.println("Synchronisation terminée avec succès");
            return true;
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la synchronisation: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Synchronise les commandes depuis une plateforme e-commerce
     */
    public static List<EcommerceOrder> syncOrders(Platform platform) {
        List<EcommerceOrder> orders = new ArrayList<>();
        
        try {
            System.out.println("Récupération des commandes depuis " + platform.getDisplayName() + "...");
            
            // Simuler la récupération d'ordres
            orders.add(new EcommerceOrder(
                "ORD-001",
                "Matelas King Premium",
                2,
                1200.0,
                LocalDateTime.now(),
                "En attente"
            ));
            
            orders.add(new EcommerceOrder(
                "ORD-002", 
                "Matelas Queen Standard",
                1,
                800.0,
                LocalDateTime.now().minusHours(2),
                "Livré"
            ));
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des commandes: " + e.getMessage());
        }
        
        return orders;
    }
    
    /**
     * Met à jour le stock sur la plateforme e-commerce
     */
    public static boolean updateStock(Platform platform, Mattress mattress) {
        try {
            String stockData = createStockData(mattress);
            sendToEcommerce(platform, stockData);
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour du stock: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Crée les données produit pour l'e-commerce
     */
    private static String createProductData(Mattress mattress) {
        return String.format(
            "{\"name\":\"%s\",\"price\":%.2f,\"stock\":%d,\"description\":\"%s - %s\"}",
            mattress.getType(),
            mattress.getPrix(),
            mattress.getQuantity(),
            mattress.getBrand(),
            mattress.getSize()
        );
    }
    
    /**
     * Crée les données de stock pour l'e-commerce
     */
    private static String createStockData(Mattress mattress) {
        return String.format(
            "{\"product_id\":\"%d\",\"stock\":%d,\"price\":%.2f}",
            mattress.getId(),
            mattress.getQuantity(),
            mattress.getPrix()
        );
    }
    
    /**
     * Envoie les données à la plateforme e-commerce
     */
    private static void sendToEcommerce(Platform platform, String data) {
        // Simulation d'envoi API
        System.out.println("Envoi vers " + platform.getDisplayName() + ": " + data);
    }
    
    /**
     * Vérifie la connectivité avec la plateforme
     */
    public static boolean testConnection(Platform platform) {
        try {
            // Simulation de test de connexion
            System.out.println("Test de connexion avec " + platform.getDisplayName() + "...");
            Thread.sleep(1000); // Simuler un délai réseau
            return true;
        } catch (Exception e) {
            System.err.println("Erreur de connexion avec " + platform.getDisplayName() + ": " + e.getMessage());
            return false;
        }
    }
    
    public static class EcommerceOrder {
        private String orderId;
        private String productName;
        private int quantity;
        private double totalPrice;
        private LocalDateTime orderDate;
        private String status;
        
        public EcommerceOrder(String orderId, String productName, int quantity, 
                            double totalPrice, LocalDateTime orderDate, String status) {
            this.orderId = orderId;
            this.productName = productName;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
            this.orderDate = orderDate;
            this.status = status;
        }
        
        // Getters
        public String getOrderId() { return orderId; }
        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
        public double getTotalPrice() { return totalPrice; }
        public LocalDateTime getOrderDate() { return orderDate; }
        public String getStatus() { return status; }
    }
} 