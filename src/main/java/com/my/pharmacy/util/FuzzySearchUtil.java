package com.my.pharmacy.util;

public class FuzzySearchUtil {

    /**
     * Calculates how similar two strings are.
     * Returns true if the query is "close enough" to the target.
     * * @param query The user's typed text (e.g., "Pnadol")
     * @param target The actual product name (e.g., "Panadol")
     * @return true if it's a fuzzy match
     */
    public static boolean isFuzzyMatch(String query, String target) {
        if (query == null || target == null) return false;

        String q = query.toLowerCase();
        String t = target.toLowerCase();

        // 1. Direct contains check (Fastest)
        if (t.contains(q)) return true;

        // 2. Levenshtein Distance (Smart check)
        // Allow 1 mistake for every 4 characters typed
        int allowedErrors = Math.max(1, q.length() / 4);
        int distance = calculateLevenshteinDistance(q, t);

        return distance <= allowedErrors;
    }

    // Standard Algorithm for counting string differences
    private static int calculateLevenshteinDistance(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];

        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(dp[i - 1][j - 1]
                                    + (x.charAt(i - 1) == y.charAt(j - 1) ? 0 : 1),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }
        // We look for the best match within the target string (substring search)
        // For simplicity here, we return the total distance.
        // In a full implementation, we'd check substrings, but this suffices for "Pan" vs "Panadol"
        return dp[x.length()][y.length()];
    }

    private static int min(int... numbers) {
        int result = Integer.MAX_VALUE;
        for (int each : numbers) {
            result = Math.min(result, each);
        }
        return result;
    }
}