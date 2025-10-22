package com.example.String_Analysis.service;

import com.example.String_Analysis.model.AnalyzedString;
import com.example.String_Analysis.util.HashUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class StringService {

    // Store by sha256 hash
    private final Map<String, AnalyzedString> storeById = new ConcurrentHashMap<>();
    // Map exact original value -> id (sha)
    private final Map<String, String> idByValue = new ConcurrentHashMap<>();

    public AnalyzedString create(String value) {
        if (value == null) throw new IllegalArgumentException("value cannot be null");
        String sha = HashUtils.sha256Hex(value);
        if (storeById.containsKey(sha)) {
            throw new IllegalStateException("exists");
        }
        AnalyzedString.Properties props = analyze(value, sha);
        AnalyzedString entry = new AnalyzedString(sha, value, props, Instant.now());
        storeById.put(sha, entry);
        idByValue.put(value, sha);
        return entry;
    }

    public AnalyzedString getByValue(String value) {
        String sha = idByValue.get(value);
        if (sha == null) return null;
        return storeById.get(sha);
    }

    public Optional<AnalyzedString> getById(String id) {
        return Optional.ofNullable(storeById.get(id));
    }

    public List<AnalyzedString> listAll() {
        return new ArrayList<>(storeById.values());
    }

    public void deleteByValue(String value) {
        String sha = idByValue.remove(value);
        if (sha != null) storeById.remove(sha);
    }

    private AnalyzedString.Properties analyze(String value, String sha) {
        int length = value.length();

        // Normalize to lowercase for case-insensitive analysis
        String lower = value.toLowerCase(Locale.ROOT);

        // is_palindrome: case-insensitive, compare full string as-is (including spaces and punctuation)
        boolean isPalindrome = new StringBuilder(lower).reverse().toString().equals(lower);

        // unique characters: distinct code points (case-insensitive because we used lower)
        Set<Integer> uniqueChars = lower.chars().boxed().collect(Collectors.toSet());
        int uniqueCount = uniqueChars.size();

        // word count: number of whitespace-separated tokens (trim, split on \\s+)
        String trimmed = value.trim();
        int wordCount = trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;

        // character frequency map: build using lowercase characters as keys (strings)
        Map<String, Integer> freq = new LinkedHashMap<>();
        lower.chars().forEachOrdered(cp -> {
            String ch = new String(Character.toChars(cp));
            freq.put(ch, freq.getOrDefault(ch, 0) + 1);
        });

        return new AnalyzedString.Properties(length, isPalindrome, uniqueCount, wordCount, sha, freq);
    }

    // Filtering: support params described in spec
    public List<AnalyzedString> filter(Boolean isPalindrome, Integer minLength, Integer maxLength,
                                       Integer wordCount, String containsCharacter) {
        // Normalize containsCharacter to lowercase if provided
        String containsCharLower = containsCharacter == null ? null : containsCharacter.toLowerCase(Locale.ROOT);

        return storeById.values().stream()
                .filter(e -> {
                    AnalyzedString.Properties p = e.getProperties();
                    if (isPalindrome != null && p.isIs_palindrome() != isPalindrome) return false;
                    if (minLength != null && p.getLength() < minLength) return false;
                    if (maxLength != null && p.getLength() > maxLength) return false;
                    if (wordCount != null && p.getWord_count() != wordCount) return false;
                    if (containsCharLower != null) {
                        Map<String, Integer> freq = p.getCharacter_frequency_map();
                        if (!freq.containsKey(containsCharLower)) return false;
                    }
                    return true;
                })
                .sorted(Comparator.comparing(AnalyzedString::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
}
