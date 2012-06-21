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

	public void testDelimitedColumnRead() {
		Transformer spec = TransformerFactory
				.getTransformer(Contribution.class);
		spec.parseFlatFile(
				ContributionsTestCase.class.getResourceAsStream("P00000001-ALL.csv"),
				new Listener());
		// assertions are in the listener class below
	}

	class Listener implements RecordListener {
		public boolean foundRecord(Object o) {
			recCount++;
			Contribution bean = (Contribution) o;
			System.out.println(recCount + "=>" + bean.toString());
			return false;
		}

		public boolean unresolvableRecord(String rec) {
			// nothing in here for now
			fail("Not expecting this call. test not setup for this.");
			return true;
		}
	}

}
