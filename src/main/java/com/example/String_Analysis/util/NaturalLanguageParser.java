package com.example.String_Analysis.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight heuristic parser for the small set of example queries we must support.
 * Returns parsed filters map suitable to apply to stored entries.
 */
public class NaturalLanguageParser {

    public static class ParseResult {
        private final Map<String, Object> parsedFilters = new HashMap<>();
        private final String original;

        public ParseResult(String original) { this.original = original; }

        public Map<String, Object> getParsedFilters() { return parsedFilters; }
        public String getOriginal() { return original; }
    }

    public static ParseResult parse(String query) throws IllegalArgumentException {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query empty");
        }
        String q = query.trim().toLowerCase(Locale.ROOT);
        ParseResult res = new ParseResult(query);

        // Examples to support
        // "all single word palindromic strings" -> word_count=1, is_palindrome=true
        if (q.contains("single word") || q.matches(".*\\b1\\b.*single.*word.*")) {
            res.getParsedFilters().put("word_count", 1);
        }
        if (q.contains("palindrom") || q.contains("palindromic")) {
            res.getParsedFilters().put("is_palindrome", true);
        }

        // "strings longer than 10 characters" -> min_length=11
        Pattern longerThan = Pattern.compile("longer than (\\d+) characters?");
        Matcher m = longerThan.matcher(q);
        if (m.find()) {
            int n = Integer.parseInt(m.group(1));
            res.getParsedFilters().put("min_length", n + 1);
        }

        // "strings containing the letter z" or "strings containing z"
        Pattern containsChar = Pattern.compile("contain(?:ing|s)? (the )?(letter )?(['\"])?([a-zA-Z])\\3?");
        m = containsChar.matcher(q);
        if (m.find()) {
            String c = m.group(4);
            res.getParsedFilters().put("contains_character", c.toLowerCase());
        } else {
            // fallback: "containing z" w/o word "letter"
            Pattern containsSingle = Pattern.compile("containing ([a-zA-Z])\\b");
            m = containsSingle.matcher(q);
            if (m.find()) {
                res.getParsedFilters().put("contains_character", m.group(1).toLowerCase());
            }
        }

        // "that contain the first vowel" heuristic -> contains_character = a (first vowel in english)
        if (q.contains("first vowel")) {
            res.getParsedFilters().put("contains_character", "a"); // heuristic
            if (q.contains("palindrom")) {
                res.getParsedFilters().put("is_palindrome", true);
            }
        }

        if (res.getParsedFilters().isEmpty()) {
            throw new IllegalArgumentException("Unable to parse natural language query");
        }
        return res;
    }
}
