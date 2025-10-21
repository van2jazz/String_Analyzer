package com.example.String_Analysis.model;

import java.time.Instant;
import java.util.Map;

public class AnalyzedString {
    private final String id; // sha256 hash
    private final String value;
    private final Properties properties;
    private final Instant createdAt;

    public AnalyzedString(String id, String value, Properties properties, Instant createdAt) {
        this.id = id;
        this.value = value;
        this.properties = properties;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getValue() { return value; }
    public Properties getProperties() { return properties; }
    public Instant getCreatedAt() { return createdAt; }

    public static class Properties {
        private final int length;
        private final boolean isPalindrome;
        private final int uniqueCharacters;
        private final int wordCount;
        private final String sha256Hash;
        private final Map<String, Integer> characterFrequencyMap;

        public Properties(int length, boolean isPalindrome, int uniqueCharacters, int wordCount,
                          String sha256Hash, Map<String, Integer> characterFrequencyMap) {
            this.length = length;
            this.isPalindrome = isPalindrome;
            this.uniqueCharacters = uniqueCharacters;
            this.wordCount = wordCount;
            this.sha256Hash = sha256Hash;
            this.characterFrequencyMap = characterFrequencyMap;
        }

        public int getLength() { return length; }
        public boolean isIs_palindrome() { return isPalindrome; }
        public int getUnique_characters() { return uniqueCharacters; }
        public int getWord_count() { return wordCount; }
        public String getSha256_hash() { return sha256Hash; }
        public Map<String, Integer> getCharacter_frequency_map() { return characterFrequencyMap; }
    }
}
