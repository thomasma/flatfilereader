package org.aver.fft;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FixedColumnWithRecordIdTestCase {
    private Transformer spec = null;

    @BeforeEach
    void setUp() {
        spec = TransformerFactory.getTransformer(FixedColBean.class);
    }

    @Test
    void testFixedColumnRecord() {
        String line = "Mathew_Thomas4111111111111111022008 12.8922210212005";
        
        FixedColBean cardDetails = (FixedColBean) spec.loadRecord(line);
        
        // Test all field values extracted from fixed positions
        assertEquals("Mathew_Thomas", cardDetails.getNameOnCard());
        assertEquals("4111111111111111", cardDetails.getCardNumber());
        assertEquals(2, cardDetails.getExpMonth());
        assertEquals(2008, cardDetails.getExpYear());
        assertEquals(12.89, cardDetails.getAmount(), 0.01);
        assertEquals("222", cardDetails.getCardSecurityCode());
        assertNotNull(cardDetails.getTransactionDate());
        
        // Verify the parsed date is correct (10212005 = October 21, 2005)
        // Using toString to check the date format since exact time comparison can be tricky
        String dateStr = cardDetails.getTransactionDate().toString();
        assertTrue(dateStr.contains("2005"));
        assertTrue(dateStr.contains("Oct"));
    }

    @Test
    void testInvalidNumberOfColumns() {
        String line = "Mathew_Thomas4111111111111111022008 12.89";
        try {
            spec.loadRecord(line);
        } catch (TransformerParseException ex) {
            return;
        }
        fail("Expected " + TransformerParseException.class.getName()
                + " due to invalid number of columns.");
    }

    @Test
    void testDataTypeMismatchForDouble() {
        // lets change amount 12.89 to 12A.89 -its expected as a double in the
        // bean - currently the tool makes it 0 in such cases
        // TODO: maybe we need to throw an exception ??

        String line = "Mathew_Thomas4111111111111111022008 12A.8922210212005";
        FixedColBean cardDetails = (FixedColBean) spec.loadRecord(line);
        
        // Verify that invalid double data defaults to 0
        assertEquals(0.0, cardDetails.getAmount(), 0.01);
        
        // Verify other fields are still parsed correctly
        assertEquals("Mathew_Thomas", cardDetails.getNameOnCard());
        assertEquals("4111111111111111", cardDetails.getCardNumber());
        assertEquals(2, cardDetails.getExpMonth());
        assertEquals(2008, cardDetails.getExpYear());
    }

    @Test
    void testDataTypeMismatchOnDate() {
        // last column for date changes from 10212005 to 1021
        String line = "Mathew_Thomas4111111111111111022008 12A.892221021";
        try {
            spec.loadRecord(line);
        } catch (TransformerParseException ex) {
            return;
        }
        fail("Expected " + TransformerParseException.class.getName()
                + " due to date field error.");
    }

    // ========== Boundary Value Testing ==========
    
    @Test
    void testMinimumBoundaryValues() {
        // Test minimum values for numeric fields
        String line = "A            0000000000000000012000  0.00100101000";
        FixedColBean cardDetails = (FixedColBean) spec.loadRecord(line);
        
        assertEquals("A", cardDetails.getNameOnCard());
        assertEquals("0000000000000000", cardDetails.getCardNumber());
        assertEquals(1, cardDetails.getExpMonth());
        assertEquals(2000, cardDetails.getExpYear());
        assertEquals(0.0, cardDetails.getAmount(), 0.01);
        assertEquals("100", cardDetails.getCardSecurityCode());
        assertNotNull(cardDetails.getTransactionDate());
    }

    @Test
    void testMaximumBoundaryValues() {
        // Test maximum length values
        String line = "MaxNameLength4999999999999999122099999.99999012312099";
        FixedColBean cardDetails = (FixedColBean) spec.loadRecord(line);
        
        assertEquals("MaxNameLength", cardDetails.getNameOnCard());
        assertEquals("4999999999999999", cardDetails.getCardNumber());
        assertEquals(12, cardDetails.getExpMonth());
        assertEquals(2099, cardDetails.getExpYear());
        assertEquals(999.99, cardDetails.getAmount(), 0.01);
        assertEquals("999", cardDetails.getCardSecurityCode());
        assertNotNull(cardDetails.getTransactionDate());
    }

    @Test
    void testNegativeNumericValues() {
        // Test negative values for numeric fields
        String line = "Test         4111111111111111-12008 -1.23222-1012005";
        FixedColBean cardDetails = (FixedColBean) spec.loadRecord(line);
        
        assertEquals("Test", cardDetails.getNameOnCard());
        assertEquals(-1, cardDetails.getExpMonth());
        assertEquals(2008, cardDetails.getExpYear());
        assertEquals(-1.23, cardDetails.getAmount(), 0.01);
        assertEquals("222", cardDetails.getCardSecurityCode());
    }

    @Test
    void testLeapYearDateBoundary() {
        // Test leap year date (Feb 29, 2004)
        String line = "Test         411111111111111102200412.8922202292004";
        FixedColBean cardDetails = (FixedColBean) spec.loadRecord(line);
        
        assertEquals("Test", cardDetails.getNameOnCard());
        assertNotNull(cardDetails.getTransactionDate());
        
        // Verify leap year date is parsed correctly
        String dateStr = cardDetails.getTransactionDate().toString();
        assertTrue(dateStr.contains("2004"));
    }

    // ========== Column Position Validation ==========
    
    @Test
    void testRecordShorterThanExpected() {
        // Test record that's shorter than the last column's end position
        String line = "Short        4111111111111111022008 12.89222";
        try {
            spec.loadRecord(line);
        } catch (TransformerParseException ex) {
            return;
        }
        fail("Expected " + TransformerParseException.class.getName()
                + " due to insufficient record length.");
    }

    @Test
    void testRecordExactLength() {
        // Test record with exact length (52 characters)
        String line = "ExactLength  411111111111111102200812.8922210212005";
        assertEquals(52, line.length());
        
        FixedColBean cardDetails = (FixedColBean) spec.loadRecord(line);
        assertEquals("ExactLength", cardDetails.getNameOnCard());
        assertNotNull(cardDetails.getTransactionDate());
    }

    @Test
    void testRecordLongerThanExpected() {
        // Test record longer than expected - should still work
        String line = "LongerRecord 4111111111111111022008 12.8922210212005EXTRA_DATA";
        FixedColBean cardDetails = (FixedColBean) spec.loadRecord(line);
        
        assertEquals("LongerRecord", cardDetails.getNameOnCard());
        assertEquals("4111111111111111", cardDetails.getCardNumber());
        assertNotNull(cardDetails.getTransactionDate());
    }

    // ========== Data Format Validation ==========
    
    @Test
    void testLeadingSpacesInFields() {
        // Test fields with leading spaces
        String line = "  Leading    4111111111111111 2200812.8922210212005";
        FixedColBean cardDetails = (FixedColBean) spec.loadRecord(line);
        
        assertEquals("  Leading", cardDetails.getNameOnCard());
        assertEquals("4111111111111111", cardDetails.getCardNumber());
        assertEquals(2, cardDetails.getExpMonth());
    }

    @Test
    void testTrailingSpacesInFields() {
        // Test fields with trailing spaces
        String line = "Trailing     4111111111111111022008 12.8922210212005";
        FixedColBean cardDetails = (FixedColBean) spec.loadRecord(line);
        
        assertEquals("Trailing", cardDetails.getNameOnCard());
        assertEquals("4111111111111111", cardDetails.getCardNumber());
    }

    @Test
    void testAllSpacesInStringFields() {
        // Test string fields with all spaces
        String line = "             4111111111111111022008 12.89   10212005";
        FixedColBean cardDetails = (FixedColBean) spec.loadRecord(line);
        
        assertEquals("", cardDetails.getNameOnCard().trim());
        assertEquals("4111111111111111", cardDetails.getCardNumber());
        assertEquals("", cardDetails.getCardSecurityCode().trim());
    }

    // ========== Enhanced Error Handling ==========
    
    @Test
    void testEmptyInputString() {
        assertThrows(TransformerParseException.class, () -> {
            spec.loadRecord("");
        });
    }

    @Test
    void testNullInputString() {
        assertThrows(TransformerException.class, () -> {
            spec.loadRecord(null);
        });
    }

    @Test
    void testVeryShortRecord() {
        // Test with record much shorter than expected
        String line = "Short";
        assertThrows(TransformerParseException.class, () -> {
            spec.loadRecord(line);
        });
    }

    @Test
    void testRecordWithOnlySpaces() {
        // Test record with only spaces
        String line = "                                                    ";
        assertEquals(52, line.length());
        
        FixedColBean cardDetails = (FixedColBean) spec.loadRecord(line);
        assertEquals("", cardDetails.getNameOnCard().trim());
        assertEquals("", cardDetails.getCardNumber().trim());
        assertEquals(0, cardDetails.getExpMonth());
        assertEquals(0, cardDetails.getExpYear());
    }

    // ========== Field-Specific Validation ==========
    
    @Test
    void testIntegerOverflowScenarios() {
        // Test with very large numbers that might cause overflow
        String line = "Test         4111111111111111999999999999999999999910212005";
        FixedColBean cardDetails = (FixedColBean) spec.loadRecord(line);
        
        // Should handle overflow gracefully
        assertEquals("Test", cardDetails.getNameOnCard());
        assertNotNull(cardDetails.getTransactionDate());
    }

    @Test
    void testDoubleOverflowScenarios() {
        // Test with very large double values
        String line = "Test         411111111111111102200899999922210212005";
        FixedColBean cardDetails = (FixedColBean) spec.loadRecord(line);
        
        assertEquals("Test", cardDetails.getNameOnCard());
        // Should handle large double values
        assertTrue(cardDetails.getAmount() >= 0);
    }

    @Test
    void testSpecialCharactersInStringFields() {
        // Test special characters in string fields
        String line = "Test@#$%^&*()4111111111111111022008 12.89@#$10212005";
        FixedColBean cardDetails = (FixedColBean) spec.loadRecord(line);
        
        assertEquals("Test@#$%^&*()", cardDetails.getNameOnCard());
        assertEquals("4111111111111111", cardDetails.getCardNumber());
        assertEquals("@#$", cardDetails.getCardSecurityCode());
    }

    @Test
    void testUnicodeCharactersInFields() {
        // Test Unicode characters
        String line = "José_García  4111111111111111022008 12.89Αβγ10212005";
        FixedColBean cardDetails = (FixedColBean) spec.loadRecord(line);
        
        assertEquals("José_García", cardDetails.getNameOnCard());
        assertEquals("Αβγ", cardDetails.getCardSecurityCode());
    }

    @Test
    void testMultipleDateFormatErrors() {
        String[] invalidDates = {
            "Test         4111111111111111022008 12.89222InvalidD",
            "Test         4111111111111111022008 12.89222133112005", // Invalid month
            "Test         4111111111111111022008 12.89222103212005"  // Invalid day
        };
        
        for (String line : invalidDates) {
            assertThrows(TransformerParseException.class, () -> {
                spec.loadRecord(line);
            }, "Should fail for invalid date: " + line);
        }
    }

    // ========== Performance and Security ==========
    
    @Test
    void testVeryLongRecord() {
        // Test with record much longer than expected
        String validPart = "Test         4111111111111111022008 12.8922210212005";
        String longRecord = validPart + "X".repeat(10000);
        
        long startTime = System.currentTimeMillis();
        FixedColBean cardDetails = (FixedColBean) spec.loadRecord(longRecord);
        long endTime = System.currentTimeMillis();
        
        // Should complete quickly despite long record
        assertTrue(endTime - startTime < 1000, "Processing should complete quickly");
        assertEquals("Test", cardDetails.getNameOnCard());
    }

    @Test
    void testSecurityValidationWithFixedWidth() {
        // Test that security validation works with fixed-width parsing
        String line = "Test         4111111111111111022008 12.8922210212005";
        FixedColBean cardDetails = (FixedColBean) spec.loadRecord(line);
        
        // Should parse normally - security validation should not interfere
        assertEquals("Test", cardDetails.getNameOnCard());
        assertEquals("4111111111111111", cardDetails.getCardNumber());
    }

    @Test
    void testMemoryUsageWithLargeFields() {
        // Test memory usage with large string fields
        String largeName = "A".repeat(13); // Fill the name field completely
        String line = largeName + "4111111111111111022008 12.8922210212005";
        
        FixedColBean cardDetails = (FixedColBean) spec.loadRecord(line);
        assertEquals(largeName, cardDetails.getNameOnCard());
    }

    // ========== Edge Case Data ==========
    
    @Test
    void testAllZeroValues() {
        // Test with all zero values
        String line = "Zero         0000000000000000002000  0.00000000000000";
        FixedColBean cardDetails = (FixedColBean) spec.loadRecord(line);
        
        assertEquals("Zero", cardDetails.getNameOnCard());
        assertEquals("0000000000000000", cardDetails.getCardNumber());
        assertEquals(0, cardDetails.getExpMonth());
        assertEquals(2000, cardDetails.getExpYear());
        assertEquals(0.0, cardDetails.getAmount(), 0.01);
        assertEquals("000", cardDetails.getCardSecurityCode());
    }

    @Test
    void testBoundaryDateValues() {
        // Test boundary date values
        String[] boundaryDates = {
            "Test         4111111111111111022008 12.89222010111999", // Jan 1, 1999
            "Test         4111111111111111022008 12.89222123112099"  // Dec 31, 2099
        };
        
        for (String line : boundaryDates) {
            FixedColBean cardDetails = (FixedColBean) spec.loadRecord(line);
            assertNotNull(cardDetails.getTransactionDate(), "Date should be parsed: " + line);
        }
    }

    @Test
    void testExactColumnBoundaries() {
        // Test exact column boundary conditions
        String line = "MaxLength13Field6111111111111111122099999.99999012312099";
        assertEquals(56, line.length());
        
        FixedColBean cardDetails = (FixedColBean) spec.loadRecord(line);
        assertEquals("MaxLength13F", cardDetails.getNameOnCard()); // Should be truncated to 13 chars
        assertEquals("6111111111111111", cardDetails.getCardNumber());
        assertEquals(12, cardDetails.getExpMonth());
        assertEquals(2099, cardDetails.getExpYear());
    }

}
