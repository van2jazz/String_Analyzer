package com.example.String_Analysis.stringAnalyzer;

import com.example.String_Analysis.model.AnalyzedString;
import com.example.String_Analysis.service.StringService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StringServiceTests {

    @Test
    public void analyzeAndStore() {
        StringService s = new StringService();
        AnalyzedString a = s.create("Level");
        assertNotNull(a.getId());
        assertEquals("Level", a.getValue());
        assertEquals(5, a.getProperties().getLength());
        assertTrue(a.getProperties().isIs_palindrome()); // case-insensitive -> "level"
        assertEquals(3, a.getProperties().getUnique_characters()); // L e v l ? depends on case; this implementation counts chars as-is
    }

    @Test
    public void duplicateThrows() {
        StringService s = new StringService();
        s.create("abc");
        try {
            s.create("abc");
            fail("Expected duplicate");
        } catch (IllegalStateException e) {
            // ok
        }
    }
}
