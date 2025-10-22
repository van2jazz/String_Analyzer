package com.example.String_Analysis.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NaturalLanguageParser {

    public static class ParseResult {
        private final String original;
        private final Map<String, Object> parsedFilters;

        public ParseResult(String original, Map<String, Object> parsedFilters) {
            this.original = original;
            this.parsedFilters = parsedFilters;
        }

        public String getOriginal() {
            return original;
        }

        public Map<String, Object> getParsedFilters() {
            return parsedFilters;
        }
    }

    public static ParseResult parse(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query cannot be empty");
        }

        query = query.toLowerCase().trim();
        Map<String, Object> filters = new HashMap<>();

        // --- Rule 1: Palindromic strings ---
        if (query.contains("palindromic")) {
            filters.put("is_palindrome", true);
        } else if (query.contains("non-palindromic")) {
            filters.put("is_palindrome", false);
        }

        // --- Rule 2: Single word / multi word ---
        if (query.contains("single word")) {
            filters.put("word_count", 1);
        } else if (query.contains("two word")) {
            filters.put("word_count", 2);
        } else if (query.contains("three word")) {
            filters.put("word_count", 3);
        }

        // --- Rule 3: Length-based filters ---
        Matcher longerThan = Pattern.compile("longer than (\\d+)").matcher(query);
        Matcher shorterThan = Pattern.compile("shorter than (\\d+)").matcher(query);
        if (longerThan.find()) {
            int value = Integer.parseInt(longerThan.group(1));
            filters.put("min_length", value + 1);
        } else if (shorterThan.find()) {
            int value = Integer.parseInt(shorterThan.group(1));
            filters.put("max_length", value - 1);
        }

        // --- Rule 4: Contains letter or vowel ---
        Matcher letterMatcher = Pattern.compile("letter ([a-z])").matcher(query);
        if (letterMatcher.find()) {
            filters.put("contains_character", letterMatcher.group(1));
        } else if (query.contains("first vowel")) {
            filters.put("contains_character", "a"); // heuristic
        }

        // --- Check for conflicts (e.g., contradictory filters) ---
        Integer minLen = (Integer) filters.get("min_length");
        Integer maxLen = (Integer) filters.get("max_length");
        if (minLen != null && maxLen != null && minLen > maxLen) {
            throw new IllegalStateException("Parsed filters conflict: min_length > max_length");
        }

        // --- No recognizable filters ---
        if (filters.isEmpty()) {
            throw new IllegalArgumentException("Unable to parse natural language query");
        }

        return new ParseResult(query, filters);
    }
}
