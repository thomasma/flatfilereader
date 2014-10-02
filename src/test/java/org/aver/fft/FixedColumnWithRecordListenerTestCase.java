package org.aver.fft;

import junit.framework.TestCase;

/**
 * Fixed length records test case.
 * 
 * @author Mathew Thomas
 */
public class FixedColumnWithRecordListenerTestCase extends TestCase {
    static int recCount = 0;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        recCount = 0;
    }

    public void testFixedColumnRead() {
        Transformer spec = TransformerFactory
                .getTransformer(FixedColBean.class);
        spec.parseFlatFile(FixedColumnWithRecordListenerTestCase.class
                .getResourceAsStream("multi-record-fixedcol-file.txt"),
                new Listener());
    }

    public void testAbortMultiRecordFileRead() {
        Transformer spec = TransformerFactory
                .getTransformer(FixedColBean.class);
        spec.parseFlatFile(
                FixedColumnWithRecordListenerTestCase.class
                        .getResourceAsStream("abortread-multi-record-fixedcol-file.txt"),
                new AbortListener());
    }

    class Listener implements RecordListener {
        public boolean foundRecord(Object o) {
            recCount++;
            FixedColBean bean = (FixedColBean) o;
            System.out.println(bean.getNameOnCard());
            // there are 2 records in the file and the assertions check for data
            // in each
            if (recCount == 1) {
                assertTrue("Matttt_Thomas".equals(bean.getNameOnCard()));
            } else {
                assertTrue("Maaaaa_Thomas".equals(bean.getNameOnCard()));
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
            System.out.println(bean.getNameOnCard());
            // there are 2 records in the file and the assertions check for data
            // in each
            assertTrue("Matttt_Thomas".equals(bean.getNameOnCard()));
            return false;
        }

        public boolean unresolvableRecord(String rec) {
            return true;
        }
    }
}
