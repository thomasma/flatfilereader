package org.aver.fft;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Delimited records test case.
 * 
 * @author Mathew Thomas
 */
public class DelimitedRecordWithRecordListenerTestCase {
    static int recCount = 0;

    @BeforeEach
    void setUp() {
        recCount = 0;
    }

    @Test
    void testDelimitedColumnRead() {
        Transformer spec = TransformerFactory
                .getTransformer(DelimitedBean.class);
        spec.parseFlatFile(DelimitedRecordWithRecordListenerTestCase.class
                .getResourceAsStream("multi-record-delcol-file.txt"),
                new Listener());
        
        // Verify that both records were processed
        assertEquals(2, recCount, "Should have processed 2 records");
    }

    @Test
    void testAbortMultiRecordFileRead() {
        Transformer spec = TransformerFactory
                .getTransformer(DelimitedBean.class);
        spec.parseFlatFile(FixedColumnWithRecordListenerTestCase.class
                .getResourceAsStream("abortread-multi-record-delcol-file.txt"),
                new AbortListener());
        
        // Verify that processing was aborted after first record
        assertEquals(1, recCount, "Should have processed only 1 record before abort");
    }

    @Test
    void testMalformedRecordHandling() {
        Transformer spec = TransformerFactory
                .getTransformer(DelimitedBean.class);
        MalformedRecordListener listener = new MalformedRecordListener();
        spec.parseFlatFile(DelimitedRecordWithRecordListenerTestCase.class
                .getResourceAsStream("malformed-delcol-file.txt"),
                listener);
        
        // Should have processed 1 valid record and 1 unresolvable record
        assertEquals(1, listener.validRecords, "Should have processed 1 valid record");
        assertEquals(1, listener.unresolvableRecords, "Should have 1 unresolvable record");
    }

    @Test
    void testInvalidDateFormatHandling() {
        Transformer spec = TransformerFactory
                .getTransformer(DelimitedBean.class);
        InvalidDateListener listener = new InvalidDateListener();
        spec.parseFlatFile(DelimitedRecordWithRecordListenerTestCase.class
                .getResourceAsStream("invalid-date-delcol-file.txt"),
                listener);
        
        // Should have 1 unresolvable record due to date parsing error
        assertEquals(0, listener.validRecords, "Should have no valid records");
        assertEquals(1, listener.unresolvableRecords, "Should have 1 unresolvable record due to date error");
    }

    @Test
    void testEmptyFileHandling() {
        Transformer spec = TransformerFactory
                .getTransformer(DelimitedBean.class);
        EmptyFileListener listener = new EmptyFileListener();
        spec.parseFlatFile(DelimitedRecordWithRecordListenerTestCase.class
                .getResourceAsStream("empty-delcol-file.txt"),
                listener);
        
        // Should have processed no records
        assertEquals(0, listener.recordCount, "Should have processed no records from empty file");
    }

    @Test
    void testSingleRecordFile() {
        Transformer spec = TransformerFactory
                .getTransformer(DelimitedBean.class);
        SingleRecordListener listener = new SingleRecordListener();
        spec.parseFlatFile(DelimitedRecordWithRecordListenerTestCase.class
                .getResourceAsStream("single-record-delcol-file.txt"),
                listener);
        
        // Should have processed exactly 1 record
        assertEquals(1, listener.recordCount, "Should have processed exactly 1 record");
    }

    @Test
    void testNullListenerHandling() {
        Transformer spec = TransformerFactory
                .getTransformer(DelimitedBean.class);
        
        // Should throw TransformerException for null listener
        assertThrows(TransformerException.class, () -> {
            spec.parseFlatFile(DelimitedRecordWithRecordListenerTestCase.class
                    .getResourceAsStream("multi-record-delcol-file.txt"),
                    null);
        }, "Should throw TransformerException for null listener");
    }

    class Listener implements RecordListener {
        public boolean foundRecord(Object o) {
            recCount++;
            DelimitedBean bean = (DelimitedBean) o;
            
            // Verify record data based on sequence
            if (recCount == 1) {
                assertEquals("Mathew_Thomas", bean.getNameOnCard());
                assertNotNull(bean.getCardNumber());
                assertNotNull(bean.getTransactionDate());
            } else if (recCount == 2) {
                assertEquals("fname_lname", bean.getNameOnCard());
                assertNotNull(bean.getCardNumber());
                assertNotNull(bean.getTransactionDate());
            }
            return true;
        }

        public boolean unresolvableRecord(String rec) {
            // nothing in here for now
            fail("Not expecting this call. test not setup for this.");
            return true;
        }
    }

    class AbortListener implements RecordListener {
        public boolean foundRecord(Object o) {
            recCount++;
            DelimitedBean bean = (DelimitedBean) o;
            
            // Verify first record data and abort processing
            assertEquals("Mathew_Thomas", bean.getNameOnCard());
            assertNotNull(bean.getCardNumber());
            assertNotNull(bean.getTransactionDate());
            assertTrue(bean.getAmount() > 0);
            
            return false; // Abort processing after first record
        }

        public boolean unresolvableRecord(String rec) {
            return true;
        }
    }

    class MalformedRecordListener implements RecordListener {
        int validRecords = 0;
        int unresolvableRecords = 0;
        
        public boolean foundRecord(Object o) {
            validRecords++;
            DelimitedBean bean = (DelimitedBean) o;
            assertNotNull(bean.getNameOnCard());
            return true;
        }

        public boolean unresolvableRecord(String rec) {
            unresolvableRecords++;
            assertNotNull(rec);
            assertTrue(rec.length() > 0);
            return true;
        }
    }

    class InvalidDateListener implements RecordListener {
        int validRecords = 0;
        int unresolvableRecords = 0;
        
        public boolean foundRecord(Object o) {
            validRecords++;
            return true;
        }

        public boolean unresolvableRecord(String rec) {
            unresolvableRecords++;
            assertNotNull(rec);
            assertTrue(rec.contains("invalid-date"));
            return true;
        }
    }

    class EmptyFileListener implements RecordListener {
        int recordCount = 0;
        
        public boolean foundRecord(Object o) {
            recordCount++;
            return true;
        }

        public boolean unresolvableRecord(String rec) {
            recordCount++;
            return true;
        }
    }

    class SingleRecordListener implements RecordListener {
        int recordCount = 0;
        
        public boolean foundRecord(Object o) {
            recordCount++;
            DelimitedBean bean = (DelimitedBean) o;
            assertEquals("SingleRecord", bean.getNameOnCard());
            return true;
        }

        public boolean unresolvableRecord(String rec) {
            recordCount++;
            return true;
        }
    }
    
    @Test
    void testEdgeCaseValues() {
        Transformer spec = TransformerFactory
                .getTransformer(DelimitedBean.class);
        EdgeCaseListener listener = new EdgeCaseListener();
        spec.parseFlatFile(DelimitedRecordWithRecordListenerTestCase.class
                .getResourceAsStream("edge-case-delcol-file.txt"),
                listener);
        
        assertEquals(1, listener.recordCount, "Should have processed 1 record with edge case values");
    }

    @Test
    void testCommaDelimitedRecords() {
        Transformer spec = TransformerFactory
                .getTransformer(CsvBean.class);
        CsvListener listener = new CsvListener();
        spec.parseFlatFile(DelimitedRecordWithRecordListenerTestCase.class
                .getResourceAsStream("comma-delimited-file.txt"),
                listener);
        
        assertEquals(2, listener.recordCount, "Should have processed 2 comma-delimited records");
    }

    @Test
    void testFileVsInputStream() {
        // Test with File input
        Transformer spec = TransformerFactory
                .getTransformer(DelimitedBean.class);
        FileInputListener fileListener = new FileInputListener();
        
        java.io.File testFile = new java.io.File(DelimitedRecordWithRecordListenerTestCase.class
                .getResource("single-record-delcol-file.txt").getFile());
        spec.parseFlatFile(testFile, fileListener);
        
        // Test with InputStream input
        StreamInputListener streamListener = new StreamInputListener();
        spec.parseFlatFile(DelimitedRecordWithRecordListenerTestCase.class
                .getResourceAsStream("single-record-delcol-file.txt"),
                streamListener);
        
        // Both should process the same number of records
        assertEquals(fileListener.recordCount, streamListener.recordCount, 
                "File and InputStream should process same number of records");
    }

    class EdgeCaseListener implements RecordListener {
        int recordCount = 0;
        
        public boolean foundRecord(Object o) {
            recordCount++;
            DelimitedBean bean = (DelimitedBean) o;
            
            // Test edge cases - empty strings, zero values, etc.
            assertNotNull(bean.getNameOnCard());
            assertEquals(0, bean.getAmount(), "Amount should be 0 for edge case test");
            assertEquals(0, bean.getExpMonth(), "ExpMonth should be 0 for edge case test");
            return true;
        }

        public boolean unresolvableRecord(String rec) {
            fail("Not expecting unresolvable record for edge case test");
            return true;
        }
    }

    class CsvListener implements RecordListener {
        int recordCount = 0;
        
        public boolean foundRecord(Object o) {
            recordCount++;
            CsvBean bean = (CsvBean) o;
            assertNotNull(bean.getName());
            assertNotNull(bean.getEmail());
            assertTrue(bean.getAge() > 0);
            return true;
        }

        public boolean unresolvableRecord(String rec) {
            fail("Not expecting unresolvable record for comma delimited test");
            return true;
        }
    }

    class FileInputListener implements RecordListener {
        int recordCount = 0;
        
        public boolean foundRecord(Object o) {
            recordCount++;
            return true;
        }

        public boolean unresolvableRecord(String rec) {
            recordCount++;
            return true;
        }
    }

    class StreamInputListener implements RecordListener {
        int recordCount = 0;
        
        public boolean foundRecord(Object o) {
            recordCount++;
            return true;
        }

        public boolean unresolvableRecord(String rec) {
            recordCount++;
            return true;
        }
    }
}
