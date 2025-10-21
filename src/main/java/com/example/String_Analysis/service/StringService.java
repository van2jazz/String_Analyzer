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

//    private AnalyzedString.Properties analyze(String value, String sha) {
//        int length = value.length();
//
//        // is_palindrome: case-insensitive, compare full string as-is (including spaces and punctuation)
//        String lower = value.toLowerCase(Locale.ROOT);
//        boolean isPalindrome = new StringBuilder(lower).reverse().toString().equals(lower);
//
//        // unique characters: distinct code units (characters)
////        Set<Integer> uniqueChars = value.chars().boxed().collect(Collectors.toSet());
//        Set<Integer> uniqueChars = value.toLowerCase(Locale.ROOT)
//                .chars()
//                .boxed()
//                .collect(Collectors.toSet());
//
//        int uniqueCount = uniqueChars.size();
//
//
//        // word count: number of whitespace-separated tokens (trim, split on \\s+)
//        String trimmed = value.trim();
//        int wordCount = trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
//
//        // character frequency map: map each character to its count (characters as String)
//        Map<String, Integer> freq = new LinkedHashMap<>();
//        value.chars().forEachOrdered(cp -> {
//            String ch = new String(Character.toChars(cp));
//            freq.put(ch, freq.getOrDefault(ch, 0) + 1);
//        });
//
//        return new AnalyzedString.Properties(length, isPalindrome, uniqueCount, wordCount, sha, freq);
//    }

    private AnalyzedString.Properties analyze(String value, String sha) {
        int length = value.length();

        // is_palindrome: case-insensitive
        String lower = value.toLowerCase(Locale.ROOT);
        boolean isPalindrome = new StringBuilder(lower).reverse().toString().equals(lower);

        // ✅ unique characters: distinct letters, case-insensitive
        Set<Integer> uniqueChars = lower.chars().boxed().collect(Collectors.toSet());
        int uniqueCount = uniqueChars.size();

        // word count
        String trimmed = value.trim();
        int wordCount = trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;

        // character frequency map
        Map<String, Integer> freq = new LinkedHashMap<>();
        value.chars().forEachOrdered(cp -> {
            String ch = new String(Character.toChars(cp));
            freq.put(ch, freq.getOrDefault(ch, 0) + 1);
        });

        return new AnalyzedString.Properties(length, isPalindrome, uniqueCount, wordCount, sha, freq);
    }


    // Filtering: support params described in spec
    public List<AnalyzedString> filter(Boolean isPalindrome, Integer minLength, Integer maxLength,
                                       Integer wordCount, String containsCharacter) {
        return storeById.values().stream()
                .filter(e -> {
                    AnalyzedString.Properties p = e.getProperties();
                    if (isPalindrome != null && p.isIs_palindrome() != isPalindrome) return false;
                    if (minLength != null && p.getLength() < minLength) return false;
                    if (maxLength != null && p.getLength() > maxLength) return false;
                    if (wordCount != null && p.getWord_count() != wordCount) return false;
                    if (containsCharacter != null) {
                        Map<String, Integer> freq = p.getCharacter_frequency_map();
                        if (!freq.containsKey(containsCharacter)) return false;
                    }
                    return true;
                })
                .sorted(Comparator.comparing(AnalyzedString::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
}
