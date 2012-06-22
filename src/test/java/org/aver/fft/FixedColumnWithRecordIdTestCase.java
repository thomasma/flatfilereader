package org.aver.fft;

import junit.framework.TestCase;

public class FixedColumnWithRecordIdTestCase extends TestCase {
	private Transformer spec = null;

	public void setUp() {
		spec = TransformerFactory.getTransformer(FixedColBean.class);
	}

	public void testFixedColumnRecord() {
		String line = "Mathew_Thomas4111111111111111022008 12.8922210212005";
		Transformer spec = TransformerFactory
				.getTransformer(FixedColBean.class);

		FixedColBean cardDetails = (FixedColBean) spec.loadRecord(line);
		System.out.println(cardDetails.getNameOnCard());
		System.out.println(cardDetails.getCardNumber());
		System.out.println(cardDetails.getTransactionDate());
		System.out.println(cardDetails.getAmount());

		assertTrue("Mathew_Thomas".equals(cardDetails.getNameOnCard()));

	}

	public void testInvalidNumberOfColumns() {
		String line = "Mathew_Thomas4111111111111111022008 12.89";
		try {
			spec.loadRecord(line);
		} catch (TransformerParseException ex) {
			return;
		}
		fail("Expected " + TransformerParseException.class.getName()
				+ " due to invalid number of columns.");
	}

	public void testDataTypeMismatchForDouble() {
		// lets change amount 12.89 to 12A.89 -its expected as a double in the
		// bean - currently the tool makes it 0 in such cases
		// TODO: maybe we need to throw an exception ??

		String line = "Mathew_Thomas4111111111111111022008 12A.8922210212005";
		FixedColBean cardDetails = (FixedColBean) spec.loadRecord(line);
		System.out.println(cardDetails.getAmount());
		assertTrue(cardDetails.getAmount() == 0);
	}

	public void testDataTypeMismatchOnDate() {
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

}
