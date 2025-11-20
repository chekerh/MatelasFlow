package com.warehouse.util;

import com.warehouse.model.Transaction;
import com.warehouse.model.TransactionDAO;
import com.warehouse.model.Mattress;
import com.warehouse.model.MattressDAO;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class PredictiveAnalytics {
    
    public static class SalesPrediction {
        private String mattressName;
        private int predictedSales;
        private double confidence;
        private String recommendation;
        
        public SalesPrediction(String mattressName, int predictedSales, double confidence, String recommendation) {
            this.mattressName = mattressName;
            this.predictedSales = predictedSales;
            this.confidence = confidence;
            this.recommendation = recommendation;
        }
        
        // Getters
        public String getMattressName() { return mattressName; }
        public int getPredictedSales() { return predictedSales; }
        public double getConfidence() { return confidence; }
        public String getRecommendation() { return recommendation; }
    }
    
    public static class StockRecommendation {
        private String mattressName;
        private int currentStock;
        private int recommendedStock;
        private String urgency;
        private String reason;
        
        public StockRecommendation(String mattressName, int currentStock, int recommendedStock, String urgency, String reason) {
            this.mattressName = mattressName;
            this.currentStock = currentStock;
            this.recommendedStock = recommendedStock;
            this.urgency = urgency;
            this.reason = reason;
        }
        
        // Getters
        public String getMattressName() { return mattressName; }
        public int getCurrentStock() { return currentStock; }
        public int getRecommendedStock() { return recommendedStock; }
        public String getUrgency() { return urgency; }
        public String getReason() { return reason; }
    }
    
    /**
     * Prédit les ventes pour les 30 prochains jours
     */
    public static List<SalesPrediction> predictSales() {
        List<SalesPrediction> predictions = new ArrayList<>();
        
        try {
            List<Transaction> allTransactions = TransactionDAO.getAllTransactions();
            List<Mattress> allMattresses = MattressDAO.getAllMattresses();
            
            // Analyser les ventes des 90 derniers jours
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(90);
            
            for (Mattress mattress : allMattresses) {
                // Filtrer les ventes pour ce matelas
                List<Transaction> sales = allTransactions.stream()
                    .filter(t -> t.getMattressId() == mattress.getId())
                    .filter(t -> "Vente".equals(t.getType()))
                    .filter(t -> {
                        LocalDate transactionDate = t.getDate().toLocalDate();
                        return !transactionDate.isBefore(startDate) && !transactionDate.isAfter(endDate);
                    })
                    .collect(Collectors.toList());
                
                if (!sales.isEmpty()) {
                    // Calculer la moyenne des ventes par jour
                    double avgDailySales = sales.size() / 90.0;
                    int predictedSales = (int) Math.round(avgDailySales * 30);
                    
                    // Calculer la confiance basée sur la variance
                    double variance = calculateVariance(sales, avgDailySales);
                    double confidence = Math.max(0.1, Math.min(0.9, 1.0 - (variance / avgDailySales)));
                    
                    String recommendation = generateSalesRecommendation(predictedSales, confidence, mattress.getQuantity());
                    
                    predictions.add(new SalesPrediction(
                        mattress.getType(),
                        predictedSales,
                        confidence,
                        recommendation
                    ));
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la prédiction des ventes: " + e.getMessage());
        }
        
        return predictions;
    }
    
    /**
     * Génère des recommandations de stock
     */
    public static List<StockRecommendation> generateStockRecommendations() {
        List<StockRecommendation> recommendations = new ArrayList<>();
        
        try {
            List<Mattress> mattresses = MattressDAO.getAllMattresses();
            List<SalesPrediction> predictions = predictSales();
            
            for (Mattress mattress : mattresses) {
                // Trouver la prédiction correspondante
                SalesPrediction prediction = predictions.stream()
                    .filter(p -> p.getMattressName().equals(mattress.getType()))
                    .findFirst()
                    .orElse(null);
                
                int currentStock = mattress.getQuantity();
                int recommendedStock = 0;
                String urgency = "Normal";
                String reason = "";
                
                if (prediction != null) {
                    // Recommandation basée sur les ventes prédites + marge de sécurité
                    recommendedStock = (int) (prediction.getPredictedSales() * 1.2); // 20% de marge
                    
                    if (currentStock < recommendedStock * 0.5) {
                        urgency = "Critique";
                        reason = "Stock très faible par rapport aux prévisions";
                    } else if (currentStock < recommendedStock * 0.8) {
                        urgency = "Urgent";
                        reason = "Stock insuffisant pour les prévisions";
                    } else if (currentStock > recommendedStock * 1.5) {
                        urgency = "Surstock";
                        reason = "Stock excessif, risque d'obsolescence";
                    } else {
                        urgency = "Normal";
                        reason = "Stock dans les limites recommandées";
                    }
                } else {
                    // Pas de données de vente, recommandation basique
                    recommendedStock = Math.max(10, currentStock);
                    urgency = "Normal";
                    reason = "Pas de données de vente suffisantes";
                }
                
                recommendations.add(new StockRecommendation(
                    mattress.getType(),
                    currentStock,
                    recommendedStock,
                    urgency,
                    reason
                ));
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération des recommandations: " + e.getMessage());
        }
        
        return recommendations;
    }
    
    /**
     * Analyse les tendances saisonnières
     */
    public static Map<String, Double> analyzeSeasonalTrends() {
        Map<String, Double> trends = new HashMap<>();
        
        try {
            List<Transaction> allTransactions = TransactionDAO.getAllTransactions();
            
            // Analyser par mois
            for (int month = 1; month <= 12; month++) {
                final int currentMonth = month;
                long monthlySales = allTransactions.stream()
                    .filter(t -> "Vente".equals(t.getType()))
                    .filter(t -> t.getDate().getMonthValue() == currentMonth)
                    .count();
                
                trends.put("Mois " + month, (double) monthlySales);
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'analyse des tendances: " + e.getMessage());
        }
        
        return trends;
    }
    
    /**
     * Identifie les produits les plus populaires
     */
    public static List<String> getTopSellingProducts(int limit) {
        List<String> topProducts = new ArrayList<>();
        
        try {
            List<Transaction> allTransactions = TransactionDAO.getAllTransactions();
            
            // Compter les ventes par matelas
            Map<Integer, Long> salesCount = allTransactions.stream()
                .filter(t -> "Vente".equals(t.getType()))
                .collect(Collectors.groupingBy(
                    Transaction::getMattressId,
                    Collectors.counting()
                ));
            
            // Trier par nombre de ventes
            List<Map.Entry<Integer, Long>> sortedSales = salesCount.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
            
            // Convertir en noms de matelas
            for (Map.Entry<Integer, Long> entry : sortedSales) {
                try {
                    Mattress mattress = MattressDAO.getMattressById(entry.getKey());
                    if (mattress != null) {
                        topProducts.add(mattress.getType() + " (" + entry.getValue() + " ventes)");
                    }
                } catch (Exception e) {
                    // Ignorer les erreurs
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'analyse des produits populaires: " + e.getMessage());
        }
        
        return topProducts;
    }
    
    private static double calculateVariance(List<Transaction> sales, double mean) {
        if (sales.isEmpty()) return 0.0;
        
        double variance = sales.stream()
            .mapToDouble(sale -> Math.pow(1 - mean, 2)) // Chaque vente compte pour 1
            .sum();
        
        return variance / sales.size();
    }
    
    private static String generateSalesRecommendation(int predictedSales, double confidence, int currentStock) {
        if (confidence < 0.3) {
            return "Données insuffisantes pour une prédiction fiable";
        } else if (predictedSales > currentStock * 2) {
            return "Augmenter significativement le stock";
        } else if (predictedSales > currentStock) {
            return "Augmenter légèrement le stock";
        } else if (predictedSales < currentStock * 0.5) {
            return "Réduire le stock";
        } else {
            return "Maintenir le stock actuel";
        }
    }
} 