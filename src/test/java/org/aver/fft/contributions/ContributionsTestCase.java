package org.aver.fft.contributions;

import junit.framework.TestCase;

import org.aver.fft.RecordListener;
import org.aver.fft.Transformer;
import org.aver.fft.TransformerFactory;

/**
 * Delimited records test case.
 * 
 * @author Mathew Thomas
 */
public class ContributionsTestCase extends TestCase {
	static int recCount = 0;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		recCount = 0;
	}

	public void testRestFirstRecordOnly() {
		Transformer spec = TransformerFactory
				.getTransformer(Contribution.class);
		spec.parseFlatFile(
				ContributionsTestCase.class.getResourceAsStream("P.csv"),
				new RecordListener() {
					public boolean foundRecord(Object o) {
						recCount++;
						Contribution bean = (Contribution) o;
						System.out.println(recCount + "=>" + bean.toString());
						assertTrue("C00410118".equals(bean.getCmteId()));
						assertTrue("P20002978".equals(bean.getCandId()));
						assertTrue("AL".equals(bean.getContbrSt()));
						assertTrue("366010290".equals(bean.getContbrZip()));
						assertTrue("SA17A".equals(bean.getFormTp()));
						assertTrue("736166".equals(bean.getFileNum()));
						assertTrue(250 == bean.getContbReceiptAmt());

						// abort
						return false;
					}

					public boolean unresolvableRecord(String rec) {
						// nothing in here for now
						fail("Not expecting this call. test not setup for this.");
						return true;
					}
				});
		// assertions are in the listener class below
	}

	public void testReadAllRecords() {
		Transformer spec = TransformerFactory
				.getTransformer(Contribution.class);
		recCount = 0;
		spec.parseFlatFile(
				ContributionsTestCase.class.getResourceAsStream("P.csv"),
				new RecordListener() {
					public boolean foundRecord(Object o) {
						recCount++;
						Contribution bean = (Contribution) o;
						System.out.println(recCount + "=>" + bean.toString());

						// continue
						return true;
					}

					public boolean unresolvableRecord(String rec) {
						// nothing in here for now
						fail("Not expecting this call. test not setup for this.");
						return true;
					}
				});
		assertTrue(recCount == 221);
	}

}
