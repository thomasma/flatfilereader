package org.aver.fft;

import junit.framework.TestCase;

/**
 * Delimited records test case.
 * 
 * @author Mathew Thomas
 */
public class DelimitedRecordWithRecordListenerTestCase extends TestCase {
    static int recCount = 0;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        recCount = 0;
    }

    public void testDelimitedColumnRead() {
        Transformer spec = TransformerFactory
                .getTransformer(DelimitedBean.class);
        spec.parseFlatFile(DelimitedRecordWithRecordListenerTestCase.class
                .getResourceAsStream("multi-record-delcol-file.txt"),
                new Listener());
        // assertions are in the listener class below
    }

    public void testAbortMultiRecordFileRead() {
        Transformer spec = TransformerFactory
                .getTransformer(DelimitedBean.class);
        spec.parseFlatFile(FixedColumnWithRecordListenerTestCase.class
                .getResourceAsStream("abortread-multi-record-delcol-file.txt"),
                new AbortListener());
    }

    class Listener implements RecordListener {
        public boolean foundRecord(Object o) {
            recCount++;
            DelimitedBean bean = (DelimitedBean) o;
            System.out.println(bean.getNameOnCard());
            // there are 2 records in the file and the assertions check for data
            // in each
            if (recCount == 1) {
                assertTrue("Mathew_Thomas".equals(bean.getNameOnCard()));
            } else {
                assertTrue("fname_lname".equals(bean.getNameOnCard()));
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
            System.out.println(bean.getNameOnCard());
            // there are 2 records in the file and the assertions check for data
            // in each
            assertTrue("Mathew_Thomas".equals(bean.getNameOnCard()));
            return false;
        }

        public boolean unresolvableRecord(String rec) {
            return true;
        }
    }
}
