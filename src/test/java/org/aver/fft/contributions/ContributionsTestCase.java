package org.aver.fft.contributions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.aver.fft.RecordListener;
import org.aver.fft.Transformer;
import org.aver.fft.TransformerFactory;

/**
 * Delimited records test case.
 * 
 * @author Mathew Thomas
 */
public class ContributionsTestCase {
    static int recCount = 0;

    @BeforeEach
    void setUp() {
        recCount = 0;
    }

    @Test
    void testRestFirstRecordOnly() {
        Transformer spec = TransformerFactory
                .getTransformer(Contribution.class);
        spec.parseFlatFile(
                ContributionsTestCase.class.getResourceAsStream("P.csv"),
                new RecordListener() {
                    public boolean foundRecord(Object o) {
                        recCount++;
                        Contribution bean = (Contribution) o;
                        
                        // Verify first record data
                        assertEquals("C00410118", bean.getCmteId());
                        assertEquals("P20002978", bean.getCandId());
                        assertEquals("AL", bean.getContbrSt());
                        assertEquals("366010290", bean.getContbrZip());
                        assertEquals("SA17A", bean.getFormTp());
                        assertEquals("736166", bean.getFileNum());
                        assertEquals(250.0, bean.getContbReceiptAmt(), 0.01);
                        
                        // Additional field validations
                        assertNotNull(bean.getContbrNm());
                        assertNotNull(bean.getContbrCity());
                        assertNotNull(bean.getContbReceiptDt());

                        // abort after first record
                        return false;
                    }

                    public boolean unresolvableRecord(String rec) {
                        fail("Not expecting this call. test not setup for this.");
                        return true;
                    }
                });
        
        // Verify that only one record was processed
        assertEquals(1, recCount, "Should have processed only 1 record before abort");
    }

    @Test
    void testReadAllRecords() {
        Transformer spec = TransformerFactory
                .getTransformer(Contribution.class);
        recCount = 0;
        spec.parseFlatFile(
                ContributionsTestCase.class.getResourceAsStream("P.csv"),
                new RecordListener() {
                    public boolean foundRecord(Object o) {
                        recCount++;
                        Contribution bean = (Contribution) o;
                        
                        // Verify basic field structure for all records
                        // Some fields might be null/empty in the CSV data, so just check for non-null objects
                        assertNotNull(bean);

                        // continue processing
                        return true;
                    }

                    public boolean unresolvableRecord(String rec) {
                        fail("Not expecting this call. test not setup for this.");
                        return true;
                    }
                });
        
        // Verify that all 221 records were processed
        assertEquals(221, recCount, "Should have processed all 221 records");
    }

}
