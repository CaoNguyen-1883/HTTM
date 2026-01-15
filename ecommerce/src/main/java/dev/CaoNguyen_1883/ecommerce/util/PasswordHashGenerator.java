package dev.CaoNguyen_1883.ecommerce.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "User123";

        // Generate 3 different hashes to verify they all work
        System.out.println("Password: " + password);
        System.out.println("\nBCrypt hashes (any of these should work):");
        for (int i = 1; i <= 3; i++) {
            String hash = encoder.encode(password);
            System.out.println(i + ". " + hash);

            // Verify it matches
            boolean matches = encoder.matches(password, hash);
            System.out.println("   Verification: " + (matches ? "✓ VALID" : "✗ INVALID"));
        }

        // Test the hash from database
        String dbHash = "$2a$10$7H4GSR3CCPWcZb9QAtWHMeWSZKx6693GP9B3AiZV4BNjTpvkMWle6";
        System.out.println("\nDatabase hash verification:");
        System.out.println(dbHash);
        boolean dbMatches = encoder.matches(password, dbHash);
        System.out.println("Matches 'User123': " + (dbMatches ? "✓ VALID" : "✗ INVALID"));
    }
}
