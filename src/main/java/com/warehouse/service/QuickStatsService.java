package com.warehouse.service;

import com.warehouse.model.Mattress;
import com.warehouse.model.MattressDAO;
import com.warehouse.model.Transaction;
import com.warehouse.model.TransactionDAO;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Service for calculating and updating quick statistics widgets
 */
public class QuickStatsService {
    private static final Logger logger = LoggerFactory.getLogger(QuickStatsService.class);
    
    /**
     * Data class for quick statistics
     */
    public static class QuickStats {
        public int lowStock;
        public long pendingReturns;
        public double todayRevenue;
    }
    
    /**
     * Calculates quick statistics asynchronously
     */
    public static CompletableFuture<QuickStats> calculateStats() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Mattress> mattresses = MattressDAO.getAllMattresses();
                List<Transaction> transactions = TransactionDAO.getAllTransactions();
                
                QuickStats stats = new QuickStats();
                
                // Count low stock items (≤ 5 units)
                stats.lowStock = (int) mattresses.stream()
                    .filter(m -> m.getQuantity() <= 5)
                    .count();
                
                // Count pending returns (loans with return date today or past)
                stats.pendingReturns = transactions.stream()
                    .filter(t -> {
                        String type = t.getType();
                        if (type == null) return false;
                        String lower = type.toLowerCase(Locale.ROOT);
                        return lower.startsWith("prêt") || lower.startsWith("pret");
                    })
                    .filter(t -> t.getExpectedReturnDate() != null && 
                                 t.getExpectedReturnDate().isBefore(LocalDate.now().plusDays(1)))
                    .count();
                
                // Calculate today's revenue
                stats.todayRevenue = transactions.stream()
                    .filter(t -> t.getType() != null && 
                                t.getType().toLowerCase(Locale.ROOT).startsWith("vente"))
                    .filter(t -> t.getDate() != null && 
                                t.getDate().toLocalDate().equals(LocalDate.now()))
                    .mapToDouble(t -> t.getPrix() * t.getQuantity())
                    .sum();
                
                logger.debug("Quick stats calculated: lowStock={}, pendingReturns={}, todayRevenue={}", 
                    stats.lowStock, stats.pendingReturns, stats.todayRevenue);
                
                return stats;
            } catch (Exception e) {
                logger.error("Error calculating quick stats: {}", e.getMessage(), e);
                // Return empty stats on error
                return new QuickStats();
            }
        });
    }
    
    /**
     * Updates the UI labels with calculated statistics
     */
    public static void updateQuickStatsUI(QuickStats stats, 
                                         HBox quickActionsBar,
                                         Label lowStockValueLabel,
                                         Label lowStockSubLabel,
                                         Label pendingReturnsValueLabel,
                                         Label pendingReturnsSubLabel,
                                         Label todayRevenueValueLabel) {
        if (quickActionsBar == null) {
            return;
        }
        
        Platform.runLater(() -> {
            if (lowStockValueLabel != null) {
                lowStockValueLabel.setText(String.valueOf(stats.lowStock));
            }
            if (lowStockSubLabel != null) {
                lowStockSubLabel.setText(stats.lowStock == 1 ? "article ≤ 5 unités" : "articles ≤ 5 unités");
            }
            if (pendingReturnsValueLabel != null) {
                pendingReturnsValueLabel.setText(String.valueOf(stats.pendingReturns));
            }
            if (pendingReturnsSubLabel != null) {
                pendingReturnsSubLabel.setText(stats.pendingReturns == 0 ? "tout est revenu" : "prêts à suivre");
            }
            if (todayRevenueValueLabel != null) {
                todayRevenueValueLabel.setText(String.format(Locale.ROOT, "%.2f DT", stats.todayRevenue));
            }
            
            quickActionsBar.setManaged(true);
            quickActionsBar.setVisible(true);
        });
    }
    
    /**
     * Refreshes quick stats and updates UI
     */
    public static void refreshAndUpdate(HBox quickActionsBar,
                                       Label lowStockValueLabel,
                                       Label lowStockSubLabel,
                                       Label pendingReturnsValueLabel,
                                       Label pendingReturnsSubLabel,
                                       Label todayRevenueValueLabel) {
        if (quickActionsBar == null) {
            return;
        }
        
        // Hide while loading
        quickActionsBar.setManaged(false);
        quickActionsBar.setVisible(false);
        
        // Calculate and update
        calculateStats().thenAccept(stats -> 
            updateQuickStatsUI(stats, quickActionsBar, lowStockValueLabel, 
                lowStockSubLabel, pendingReturnsValueLabel, pendingReturnsSubLabel, todayRevenueValueLabel)
        ).exceptionally(ex -> {
            logger.error("Error refreshing quick stats: {}", ex.getMessage(), ex);
            Platform.runLater(() -> {
                if (quickActionsBar != null) {
                    quickActionsBar.setManaged(false);
                    quickActionsBar.setVisible(false);
                }
            });
            return null;
        });
    }
}

