package org.aver.fft;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Fixed length records test case.
 * 
 * @author Mathew Thomas
 */
public class FixedColumnWithRecordListenerTestCase {
    static int recCount = 0;

    @BeforeEach
    void setUp() {
        recCount = 0;
    }

    @Test
    void testFixedColumnRead() {
        Transformer spec = TransformerFactory
                .getTransformer(FixedColBean.class);
        spec.parseFlatFile(FixedColumnWithRecordListenerTestCase.class
                .getResourceAsStream("multi-record-fixedcol-file.txt"),
                new Listener());
        
        // Verify that both records were processed
        assertEquals(2, recCount, "Should have processed 2 records");
    }

    @Test
    void testAbortMultiRecordFileRead() {
        Transformer spec = TransformerFactory
                .getTransformer(FixedColBean.class);
        spec.parseFlatFile(
                FixedColumnWithRecordListenerTestCase.class
                        .getResourceAsStream("abortread-multi-record-fixedcol-file.txt"),
                new AbortListener());
        
        // Verify that processing was aborted after first record
        assertEquals(1, recCount, "Should have processed only 1 record before abort");
    }

    class Listener implements RecordListener {
        public boolean foundRecord(Object o) {
            recCount++;
            FixedColBean bean = (FixedColBean) o;
            
            // Verify record data based on sequence
            if (recCount == 1) {
                assertEquals("Matttt_Thomas", bean.getNameOnCard());
                assertNotNull(bean.getCardNumber());
                assertNotNull(bean.getTransactionDate());
                assertTrue(bean.getAmount() > 0);
            } else if (recCount == 2) {
                assertEquals("Maaaaa_Thomas", bean.getNameOnCard());
                assertNotNull(bean.getCardNumber());
                assertNotNull(bean.getTransactionDate());
                assertTrue(bean.getAmount() > 0);
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
            FixedColBean bean = (FixedColBean) o;
            
            // Verify first record data and abort processing
            assertEquals("Matttt_Thomas", bean.getNameOnCard());
            assertNotNull(bean.getCardNumber());
            assertNotNull(bean.getTransactionDate());
            assertTrue(bean.getAmount() > 0);
            
            return false; // Abort processing after first record
        }

        public boolean unresolvableRecord(String rec) {
            return true;
        }
    }
}
