package com.example.String_Analysis.controller;

import com.example.String_Analysis.model.AnalyzedString;
import com.example.String_Analysis.service.StringService;
import com.example.String_Analysis.util.NaturalLanguageParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/strings")
public class StringController {

    private final StringService service;

    public StringController(StringService service) { this.service = service; }

    // 1. Create/Analyze String
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        if (body == null || !body.containsKey("value")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing 'value' field"));
        }

        Object rawValue = body.get("value");

        // 422: invalid data type for "value" (must be string)
        if (!(rawValue instanceof String)) {
            return ResponseEntity.unprocessableEntity().body(Map.of("message", "'value' must be a string"));
        }

        String value = (String) rawValue;
        try {
            AnalyzedString entry = service.create(value);
            Map<String, Object> response = toResponse(entry);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException ex) {
            // 409 Conflict: already exists
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "String already exists"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    // 2. Get Specific String by original value
    @GetMapping("/{stringValue}")
    public ResponseEntity<?> getByValue(@PathVariable("stringValue") String stringValue) {
        AnalyzedString entry = service.getByValue(stringValue);
        if (entry == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "String not found"));
        }
        return ResponseEntity.ok(toResponse(entry));
    }

    // 3. Get All Strings with Filtering
    @GetMapping
    public ResponseEntity<?> listAll(
            @RequestParam(required = false) Boolean is_palindrome,
            @RequestParam(required = false) Integer min_length,
            @RequestParam(required = false) Integer max_length,
            @RequestParam(required = false) Integer word_count,
            @RequestParam(required = false) String contains_character
    ) {
        // Basic validation: contains_character must be single char if provided
        if (contains_character != null && contains_character.length() != 1) {
            return ResponseEntity.badRequest().body(Map.of("message", "contains_character must be a single character"));
        }

        List<AnalyzedString> filtered = service.filter(is_palindrome, min_length, max_length, word_count, contains_character);
        Map<String, Object> resp = new HashMap<>();
        resp.put("data", filtered.stream().map(this::toResponse).toList());
        resp.put("count", filtered.size());
        Map<String, Object> filters = new HashMap<>();
        filters.put("is_palindrome", is_palindrome);
        filters.put("min_length", min_length);
        filters.put("max_length", max_length);
        filters.put("word_count", word_count);
        filters.put("contains_character", contains_character);
        resp.put("filters_applied", filters);
        return ResponseEntity.ok(resp);
    }

    // 4. Natural Language Filtering
    @GetMapping("/filter-by-natural-language")
    public ResponseEntity<?> nlFilter(@RequestParam String query) {
        try {
            NaturalLanguageParser.ParseResult parsed = NaturalLanguageParser.parse(query);

            // Basic conflict detection: if both min_length and max_length set and inconsistent
            Integer minLen = (Integer) parsed.getParsedFilters().get("min_length");
            Integer maxLen = (Integer) parsed.getParsedFilters().get("max_length");
            if (minLen != null && maxLen != null && minLen > maxLen) {
                return ResponseEntity.unprocessableEntity().body(Map.of("message", "Parsed filters conflict: min_length > max_length"));
            }

            @SuppressWarnings("unchecked")
            List<AnalyzedString> result = service.filter(
                    (Boolean) parsed.getParsedFilters().get("is_palindrome"),
                    (Integer) parsed.getParsedFilters().get("min_length"),
                    (Integer) parsed.getParsedFilters().get("max_length"),
                    (Integer) parsed.getParsedFilters().get("word_count"),
                    (String) parsed.getParsedFilters().get("contains_character")
            );
            Map<String, Object> resp = new HashMap<>();
            resp.put("data", result.stream().map(this::toResponse).toList());
            resp.put("count", result.size());
            Map<String, Object> interpreted = new HashMap<>();
            interpreted.put("original", parsed.getOriginal());
            interpreted.put("parsed_filters", parsed.getParsedFilters());
            resp.put("interpreted_query", interpreted);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }

    // 5. Delete String
    @DeleteMapping("/{stringValue}")
    public ResponseEntity<?> delete(@PathVariable String stringValue) {
        AnalyzedString entry = service.getByValue(stringValue);
        if (entry == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "String does not exist in the system"));
        }
        service.deleteByValue(stringValue);
        return ResponseEntity.noContent().build();
    }

    // Utility: convert AnalyzedString to response map with created_at in ISO format
    private Map<String, Object> toResponse(AnalyzedString entry) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", entry.getId());
        m.put("value", entry.getValue());
        m.put("properties", Map.of(
                "length", entry.getProperties().getLength(),
                "is_palindrome", entry.getProperties().isIs_palindrome(),
                "unique_characters", entry.getProperties().getUnique_characters(),
                "word_count", entry.getProperties().getWord_count(),
                "sha256_hash", entry.getProperties().getSha256_hash(),
                "character_frequency_map", entry.getProperties().getCharacter_frequency_map()
        ));
        m.put("created_at", DateTimeFormatter.ISO_INSTANT.format(entry.getCreatedAt()));
        return m;
    }
}
