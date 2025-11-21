package com.warehouse.util;

import com.warehouse.security.LicenseManager;

/**
 * Utility class to generate a valid license key for testing
 * Run this main method to get a valid license key
 */
public class GenerateLicenseKey {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("MatelasPro - License Key Generator");
        System.out.println("========================================");
        System.out.println();
        
        // Generate a valid license key
        String licenseKey = LicenseManager.generateLicenseKey();
        
        if (licenseKey != null) {
            System.out.println("✅ Valid License Key Generated:");
            System.out.println();
            System.out.println("   " + licenseKey);
            System.out.println();
            System.out.println("========================================");
            System.out.println("Copy this key and paste it in the activation dialog");
            System.out.println("========================================");
        } else {
            System.err.println("❌ Failed to generate license key");
        }
    }
}

