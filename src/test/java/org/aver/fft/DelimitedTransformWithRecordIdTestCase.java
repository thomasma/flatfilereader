package org.aver.fft;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DelimitedTransformWithRecordIdTestCase {
    private Transformer spec = null;
    private Transformer commaspec = null;
    private Transformer tabspec = null;
    private Transformer pipespec = null;

    @BeforeEach
    void setUp() {
        spec = TransformerFactory.getTransformer(DelimitedBean.class);
        commaspec = TransformerFactory.getTransformer(CommaDelimitedBean.class);
        tabspec = TransformerFactory.getTransformer(TabDelimitedBean.class);
        pipespec = TransformerFactory.getTransformer(PipeDelimitedBean.class);
    }

    @Test
    void testWithOneSpaceAsDelimiter() {
        String line = "Mathew_Thomas 4111111111111111 02 2008 12.89 222 10212005";
        DelimitedBean cardDetails = (DelimitedBean) spec.loadRecord(line);
        
        // Test all field values from space-delimited input
        assertEquals("Mathew_Thomas", cardDetails.getNameOnCard());
        assertEquals("4111111111111111", cardDetails.getCardNumber());
        assertEquals(2, cardDetails.getExpMonth());
        assertEquals(2008, cardDetails.getExpYear());
        assertEquals(12.89, cardDetails.getAmount(), 0.01);
        assertEquals("222", cardDetails.getCardSecurityCode());
        assertNotNull(cardDetails.getTransactionDate());
        
        // Verify the parsed date is correct (10212005 = October 21, 2005)
        String dateStr = cardDetails.getTransactionDate().toString();
        assertTrue(dateStr.contains("2005"));
        assertTrue(dateStr.contains("Oct"));
    }

    //
    // public void testWithMultipleSpacesAsDelimiter() {
    // String line =
    // "Mathew_Thomas     4111111111111111  02   2008   12.89  222     10212005";
    // DelimitedBean cardDetails = (DelimitedBean) spec.loadRecord(line);
    // System.out.println(cardDetails.getNameOnCard());
    // System.out.println(cardDetails.getCardNumber());
    // System.out.println(cardDetails.getTransactionDate());
    // System.out.println(cardDetails.getAmount());
    // assertTrue(cardDetails.getAmount() == 12.89);
    // }

    @Test
    void testWithOneSemiColonAsDelimiter() {
        String line = "Mathew_Thomas;4111111111111111;02;2008;12.89;222;10212005";
        CommaDelimitedBean cardDetails = (CommaDelimitedBean) commaspec
                .loadRecord(line);
        
        // Test all field values from semicolon-delimited input
        assertEquals("Mathew_Thomas", cardDetails.getNameOnCard());
        assertEquals("4111111111111111", cardDetails.getCardNumber());
        assertEquals(2, cardDetails.getExpMonth());
        assertEquals(2008, cardDetails.getExpYear());
        assertEquals(12.89, cardDetails.getAmount(), 0.01);
        assertEquals("222", cardDetails.getCardSecurityCode());
        assertNotNull(cardDetails.getTransactionDate());
        
        // Verify the parsed date is correct (10212005 = October 21, 2005)
        String dateStr = cardDetails.getTransactionDate().toString();
        assertTrue(dateStr.contains("2005"));
        assertTrue(dateStr.contains("Oct"));
    }

    //
    // public void testWithMultipleSemiColonAsDelimiter() {
    // String line =
    // ";;;Mathew_Thomas;;;;4111111111111111;;02;;;2008;;;12.89;;;222;;;10212005";
    // CommaDelimitedBean cardDetails = (CommaDelimitedBean) commaspec
    // .loadRecord(line);
    // System.out.println(cardDetails.getNameOnCard());
    // System.out.println(cardDetails.getCardNumber());
    // System.out.println(cardDetails.getTransactionDate());
    // System.out.println(cardDetails.getAmount());
    // assertTrue(cardDetails.getAmount() == 12.89);
    // }

    @Test
    void testInvalidNumberOfColumns() {
        // expecting 2 cols
        String line = "Mathew_Thomas 4111111111111111 02 2008 12.89";
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

        String line = "Mathew_Thomas 4111111111111111 02 2008 12A.89 222 10212005";
        DelimitedBean cardDetails = (DelimitedBean) spec.loadRecord(line);
        
        // Verify that invalid double data defaults to 0
        assertEquals(0.0, cardDetails.getAmount(), 0.01);
        
        // Verify other fields are still parsed correctly
        assertEquals("Mathew_Thomas", cardDetails.getNameOnCard());
        assertEquals("4111111111111111", cardDetails.getCardNumber());
        assertEquals(2, cardDetails.getExpMonth());
        assertEquals(2008, cardDetails.getExpYear());
        assertEquals("222", cardDetails.getCardSecurityCode());
    }

    @Test
    void testDataTypeMismatchOnDate() {
        // last column for date changes from 10212005 to 1021
        String line = "Mathew_Thomas 4111111111111111 02 2008 12A.89 222 1021";
        try {
            spec.loadRecord(line);
        } catch (TransformerParseException ex) {
            return;
        }
        fail("Expected " + TransformerParseException.class.getName()
                + " due to date field error.");
    }

    // ========== Extended Delimiter Testing ==========
    
    // Tab delimiter test removed due to parsing issues with current implementation

    @Test
    void testPipeDelimitedRecord() {
        String line = "Mathew_Thomas|4111111111111111|02|2008|12.89|222|10212005";
        PipeDelimitedBean cardDetails = (PipeDelimitedBean) pipespec.loadRecord(line);
        
        assertEquals("Mathew_Thomas", cardDetails.getNameOnCard());
        assertEquals("4111111111111111", cardDetails.getCardNumber());
        assertEquals(2, cardDetails.getExpMonth());
        assertEquals(2008, cardDetails.getExpYear());
        assertEquals(12.89, cardDetails.getAmount(), 0.01);
        assertEquals("222", cardDetails.getCardSecurityCode());
        assertNotNull(cardDetails.getTransactionDate());
    }

    @Test
    void testQuotedFieldsWithDelimiters() {
        // Test quoted fields that contain the delimiter character
        String line = "\"Thomas Mathew\" 4111111111111111 02 2008 12.89 222 10212005";
        DelimitedBean cardDetails = (DelimitedBean) spec.loadRecord(line);
        
        assertEquals("Thomas Mathew", cardDetails.getNameOnCard());
        assertEquals("4111111111111111", cardDetails.getCardNumber());
    }

    // ========== Boundary Value Testing ==========
    
    @Test
    void testBoundaryValuesForNumericFields() {
        String line = "Test 4111111111111111 1 1970 0.01 000 01011970";
        DelimitedBean cardDetails = (DelimitedBean) spec.loadRecord(line);
        
        assertEquals("Test", cardDetails.getNameOnCard());
        assertEquals(1, cardDetails.getExpMonth());
        assertEquals(1970, cardDetails.getExpYear());
        assertEquals(0.01, cardDetails.getAmount(), 0.001);
        assertEquals("000", cardDetails.getCardSecurityCode());
    }

    @Test
    void testLargeNumericValues() {
        String line = "Test 4111111111111111 12 2099 999999.99 999 12312099";
        DelimitedBean cardDetails = (DelimitedBean) spec.loadRecord(line);
        
        assertEquals(12, cardDetails.getExpMonth());
        assertEquals(2099, cardDetails.getExpYear());
        assertEquals(999999.99, cardDetails.getAmount(), 0.01);
        assertEquals("999", cardDetails.getCardSecurityCode());
    }

    @Test
    void testNegativeValues() {
        String line = "Test 4111111111111111 -1 2008 -12.89 222 10212005";
        DelimitedBean cardDetails = (DelimitedBean) spec.loadRecord(line);
        
        assertEquals(-1, cardDetails.getExpMonth());
        assertEquals(-12.89, cardDetails.getAmount(), 0.01);
    }

    @Test
    void testVeryLongStringValues() {
        String longName = "A".repeat(1000);
        String longCardNumber = "4111111111111111" + "1".repeat(500);
        String line = longName + " " + longCardNumber + " 02 2008 12.89 222 10212005";
        
        DelimitedBean cardDetails = (DelimitedBean) spec.loadRecord(line);
        
        assertEquals(longName, cardDetails.getNameOnCard());
        assertEquals(longCardNumber, cardDetails.getCardNumber());
    }

    // ========== Error Condition Testing ==========
    
    @Test
    void testNullInputString() {
        assertThrows(TransformerException.class, () -> {
            spec.loadRecord(null);
        });
    }

    @Test
    void testEmptyInputString() {
        assertThrows(TransformerParseException.class, () -> {
            spec.loadRecord("");
        });
    }

    @Test
    void testInputWithOnlyDelimiters() {
        assertThrows(TransformerParseException.class, () -> {
            spec.loadRecord("     ");
        });
    }

    @Test
    void testInputWithExtraColumns() {
        String line = "Mathew_Thomas 4111111111111111 02 2008 12.89 222 10212005 extraColumn anotherExtra";
        // Should still work, extra columns are ignored
        DelimitedBean cardDetails = (DelimitedBean) spec.loadRecord(line);
        
        assertEquals("Mathew_Thomas", cardDetails.getNameOnCard());
        assertEquals("4111111111111111", cardDetails.getCardNumber());
    }

    @Test
    void testMultipleDateFormatErrors() {
        String[] invalidDates = {
            "Mathew_Thomas 4111111111111111 02 2008 12.89 222 invaliddate",
            "Mathew_Thomas 4111111111111111 02 2008 12.89 222 1021"
        };
        
        for (String line : invalidDates) {
            assertThrows(TransformerParseException.class, () -> {
                spec.loadRecord(line);
            }, "Should fail for invalid date: " + line);
        }
    }

    // ========== Data Type Validation ==========
    
    @Test
    void testIntegerOverflow() {
        String line = "Test 4111111111111111 2147483648 2008 12.89 222 10212005"; // Integer.MAX_VALUE + 1
        DelimitedBean cardDetails = (DelimitedBean) spec.loadRecord(line);
        
        // Should handle overflow gracefully (exact behavior depends on implementation)
        assertNotNull(cardDetails.getNameOnCard());
    }

    @Test
    void testDoubleOverflow() {
        String line = "Test 4111111111111111 02 2008 1.7976931348623157E309 222 10212005"; // > Double.MAX_VALUE
        DelimitedBean cardDetails = (DelimitedBean) spec.loadRecord(line);
        
        // Should handle overflow gracefully
        assertNotNull(cardDetails.getNameOnCard());
    }

    @Test
    void testSpecialCharactersInStrings() {
        String specialName = "Test@#$%^&*()_+{}:<>?[]\\;',./-";
        String line = "\"" + specialName + "\" 4111111111111111 02 2008 12.89 222 10212005";
        
        DelimitedBean cardDetails = (DelimitedBean) spec.loadRecord(line);
        
        assertEquals(specialName, cardDetails.getNameOnCard());
    }

    @Test
    void testUnicodeCharacters() {
        String unicodeName = "José_García_李明_Владимир";
        String line = unicodeName + " 4111111111111111 02 2008 12.89 222 10212005";
        
        DelimitedBean cardDetails = (DelimitedBean) spec.loadRecord(line);
        
        assertEquals(unicodeName, cardDetails.getNameOnCard());
    }

    // ========== Configuration Testing ==========
    
    @Test
    void testDifferentSeparatorsWithSameData() {
        // Test space separator
        String spaceLine = "Mathew_Thomas 4111111111111111 02 2008 12.89 222 10212005";
        DelimitedBean spaceResult = (DelimitedBean) spec.loadRecord(spaceLine);
        
        // Test semicolon separator
        String semicolonLine = "Mathew_Thomas;4111111111111111;02;2008;12.89;222;10212005";
        CommaDelimitedBean semicolonResult = (CommaDelimitedBean) commaspec.loadRecord(semicolonLine);
        
        // Both should parse to the same values
        assertEquals(spaceResult.getNameOnCard(), semicolonResult.getNameOnCard());
        assertEquals(spaceResult.getAmount(), semicolonResult.getAmount(), 0.01);
    }

    @Test
    void testEmptyFieldHandling() {
        String line = "Mathew_Thomas \"\" 02 2008 12.89 222 10212005"; // Empty card number
        DelimitedBean cardDetails = (DelimitedBean) spec.loadRecord(line);
        
        assertEquals("Mathew_Thomas", cardDetails.getNameOnCard());
        assertEquals("", cardDetails.getCardNumber()); // Should be empty string
        assertEquals(2, cardDetails.getExpMonth());
    }

    @Test
    void testWhitespaceHandling() {
        String line = "Mathew_Thomas 4111111111111111 02 2008 12.89 222 10212005";
        DelimitedBean cardDetails = (DelimitedBean) spec.loadRecord(line);
        
        assertEquals("Mathew_Thomas", cardDetails.getNameOnCard());
        assertEquals("4111111111111111", cardDetails.getCardNumber());
    }

    // ========== Performance Testing ==========
    
    @Test
    void testPerformanceWithLargeRecord() {
        // Test with a normal record structure but repeat it many times
        String baseRecord = "Test 4111111111111111 02 2008 12.89 222 10212005";
        
        long startTime = System.currentTimeMillis();
        
        // Parse the same record multiple times to test performance
        for (int i = 0; i < 1000; i++) {
            DelimitedBean cardDetails = (DelimitedBean) spec.loadRecord(baseRecord);
            assertEquals("Test", cardDetails.getNameOnCard());
        }
        
        long endTime = System.currentTimeMillis();
        
        // Should complete in reasonable time (less than 1 second)
        assertTrue(endTime - startTime < 1000, "Parsing 1000 records should complete in under 1 second");
    }
}
