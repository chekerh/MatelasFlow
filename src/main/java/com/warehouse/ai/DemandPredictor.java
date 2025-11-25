package com.warehouse.ai;

import com.warehouse.model.Transaction;
import com.warehouse.model.TransactionDAO;
import com.warehouse.model.Mattress;
import com.warehouse.model.MattressDAO;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class DemandPredictor {
    
    public static class DemandPrediction {
        private String mattressName;
        private int predictedDemand;
        private double confidence;
        private LocalDate predictionDate;
        private String recommendation;
        private List<String> factors;
        
        public DemandPrediction(String mattressName, int predictedDemand, double confidence, 
                              LocalDate predictionDate, String recommendation, List<String> factors) {
            this.mattressName = mattressName;
            this.predictedDemand = predictedDemand;
            this.confidence = confidence;
            this.predictionDate = predictionDate;
            this.recommendation = recommendation;
            this.factors = factors;
        }
        
        // Getters
        public String getMattressName() { return mattressName; }
        public int getPredictedDemand() { return predictedDemand; }
        public double getConfidence() { return confidence; }
        public LocalDate getPredictionDate() { return predictionDate; }
        public String getRecommendation() { return recommendation; }
        public List<String> getFactors() { return factors; }
    }
    
    /**
     * Prédit la demande pour les 30 prochains jours
     */
    public static List<DemandPrediction> predictDemand() {
        List<DemandPrediction> predictions = new ArrayList<>();
        
        try {
            List<Transaction> allTransactions = TransactionDAO.getAllTransactions();
            List<Mattress> allMattresses = MattressDAO.getAllMattresses();
            
            // Analyser les 6 derniers mois
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusMonths(6);
            
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
                    // Calculer les tendances
                    Map<Integer, Integer> monthlySales = new HashMap<>();
                    for (Transaction sale : sales) {
                        int month = sale.getDate().getMonthValue();
                        monthlySales.put(month, monthlySales.getOrDefault(month, 0) + sale.getQuantity());
                    }
                    
                    // Calculer la moyenne mensuelle
                    double avgMonthlySales = monthlySales.values().stream()
                        .mapToInt(Integer::intValue)
                        .average()
                        .orElse(0.0);
                    
                    // Prédire pour 30 jours (1 mois)
                    int predictedDemand = (int) Math.round(avgMonthlySales);
                    
                    // Calculer la confiance basée sur la variance
                    double variance = calculateVariance(monthlySales.values(), avgMonthlySales);
                    double confidence = Math.max(0.1, Math.min(0.9, 1.0 - (variance / avgMonthlySales)));
                    
                    // Analyser les facteurs
                    List<String> factors = analyzeFactors(sales, mattress);
                    
                    // Générer recommandation
                    String recommendation = generateRecommendation(predictedDemand, confidence, mattress.getQuantity());
                    
                    predictions.add(new DemandPrediction(
                        mattress.getType(),
                        predictedDemand,
                        confidence,
                        LocalDate.now().plusDays(30),
                        recommendation,
                        factors
                    ));
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la prédiction de demande: " + e.getMessage());
        }
        
        return predictions;
    }
    
    /**
     * Détecte les anomalies dans les transactions
     */
    public static List<FraudDetection> detectFraud() {
        List<FraudDetection> frauds = new ArrayList<>();
        
        try {
            List<Transaction> allTransactions = TransactionDAO.getAllTransactions();
            
            // Détecter les ventes à prix très bas
            for (Transaction transaction : allTransactions) {
                if ("Vente".equals(transaction.getType())) {
                    Mattress mattress = MattressDAO.getMattressById(transaction.getMattressId());
                    if (mattress != null) {
                        double priceRatio = transaction.getPrix() / mattress.getUnitPrice();
                        if (priceRatio < 0.3) {
                            frauds.add(new FraudDetection(
                                "Vente à prix suspect",
                                "Transaction ID: " + transaction.getId(),
                                "Prix de vente: " + transaction.getPrix() + "€, Prix d'achat: " + mattress.getUnitPrice() + "€",
                                "HIGH"
                            ));
                        }
                    }
                }
            }
            
            // Détecter les transactions en dehors des heures normales
            for (Transaction transaction : allTransactions) {
                int hour = transaction.getDate().getHour();
                if (hour < 6 || hour > 22) {
                    frauds.add(new FraudDetection(
                        "Transaction en dehors des heures normales",
                        "Transaction ID: " + transaction.getId(),
                        "Heure: " + hour + "h",
                        "MEDIUM"
                    ));
                }
            }
            
            // Détecter les quantités anormales
            for (Transaction transaction : allTransactions) {
                if (transaction.getQuantity() > 100) {
                    frauds.add(new FraudDetection(
                        "Quantité anormalement élevée",
                        "Transaction ID: " + transaction.getId(),
                        "Quantité: " + transaction.getQuantity(),
                        "HIGH"
                    ));
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la détection de fraude: " + e.getMessage());
        }
        
        return frauds;
    }
    
    private static double calculateVariance(Collection<Integer> values, double mean) {
        if (values.isEmpty()) return 0.0;
        
        double variance = values.stream()
            .mapToDouble(value -> Math.pow(value - mean, 2))
            .average()
            .orElse(0.0);
        
        return variance;
    }
    
    private static List<String> analyzeFactors(List<Transaction> sales, Mattress mattress) {
        List<String> factors = new ArrayList<>();
        
        // Analyser la saisonnalité
        Map<Integer, Integer> monthlySales = new HashMap<>();
        for (Transaction sale : sales) {
            int month = sale.getDate().getMonthValue();
            monthlySales.put(month, monthlySales.getOrDefault(month, 0) + sale.getQuantity());
        }
        
        // Trouver le mois le plus actif
        Optional<Map.Entry<Integer, Integer>> peakMonth = monthlySales.entrySet().stream()
            .max(Map.Entry.comparingByValue());
        
        if (peakMonth.isPresent()) {
            factors.add("Pic de vente en mois " + peakMonth.get().getKey());
        }
        
        // Analyser la taille
        if (mattress.getSize().contains("King")) {
            factors.add("Matelas premium - demande saisonnière");
        }
        
        // Analyser la marque
        if (mattress.getReference() != null && mattress.getReference().contains("Premium")) {
            factors.add("Référence premium - clientèle haut de gamme");
        }
        
        return factors;
    }
    
    private static String generateRecommendation(int predictedDemand, double confidence, int currentStock) {
        if (confidence < 0.3) {
            return "Données insuffisantes pour une prédiction fiable";
        } else if (predictedDemand > currentStock * 2) {
            return "Augmenter significativement le stock - Demande élevée prévue";
        } else if (predictedDemand > currentStock) {
            return "Augmenter légèrement le stock";
        } else if (predictedDemand < currentStock * 0.5) {
            return "Réduire le stock - Demande faible";
        } else {
            return "Maintenir le stock actuel";
        }
    }
    
    public static class FraudDetection {
        private String type;
        private String transactionId;
        private String details;
        private String severity;
        
        public FraudDetection(String type, String transactionId, String details, String severity) {
            this.type = type;
            this.transactionId = transactionId;
            this.details = details;
            this.severity = severity;
        }
        
        // Getters
        public String getType() { return type; }
        public String getTransactionId() { return transactionId; }
        public String getDetails() { return details; }
        public String getSeverity() { return severity; }
    }
} 