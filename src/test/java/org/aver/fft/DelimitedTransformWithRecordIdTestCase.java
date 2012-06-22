package org.aver.fft;

import junit.framework.TestCase;

public class DelimitedTransformWithRecordIdTestCase extends TestCase {
	private Transformer spec = null;

	private Transformer commaspec = null;

	public void setUp() {
		spec = TransformerFactory.getTransformer(DelimitedBean.class);
		commaspec = TransformerFactory.getTransformer(CommaDelimitedBean.class);
	}

	public void testWithOneSpaceAsDelimiter() {
		String line = "Mathew_Thomas 4111111111111111 02 2008 12.89 222 10212005";
		DelimitedBean cardDetails = (DelimitedBean) spec.loadRecord(line);
		System.out.println(cardDetails.getNameOnCard());
		System.out.println(cardDetails.getCardNumber());
		System.out.println(cardDetails.getTransactionDate());
		System.out.println(cardDetails.getAmount());
		assertTrue(cardDetails.getAmount() == 12.89);
	}
//
//	public void testWithMultipleSpacesAsDelimiter() {
//		String line = "Mathew_Thomas     4111111111111111  02   2008   12.89  222     10212005";
//		DelimitedBean cardDetails = (DelimitedBean) spec.loadRecord(line);
//		System.out.println(cardDetails.getNameOnCard());
//		System.out.println(cardDetails.getCardNumber());
//		System.out.println(cardDetails.getTransactionDate());
//		System.out.println(cardDetails.getAmount());
//		assertTrue(cardDetails.getAmount() == 12.89);
//	}

	public void testWithOneSemiColonAsDelimiter() {
		String line = "Mathew_Thomas;4111111111111111;02;2008;12.89;222;10212005";
		CommaDelimitedBean cardDetails = (CommaDelimitedBean) commaspec
				.loadRecord(line);
		System.out.println(cardDetails.getNameOnCard());
		System.out.println(cardDetails.getCardNumber());
		System.out.println(cardDetails.getTransactionDate());
		System.out.println(cardDetails.getAmount());
		assertTrue(cardDetails.getAmount() == 12.89);
	}
//
//	public void testWithMultipleSemiColonAsDelimiter() {
//		String line = ";;;Mathew_Thomas;;;;4111111111111111;;02;;;2008;;;12.89;;;222;;;10212005";
//		CommaDelimitedBean cardDetails = (CommaDelimitedBean) commaspec
//				.loadRecord(line);
//		System.out.println(cardDetails.getNameOnCard());
//		System.out.println(cardDetails.getCardNumber());
//		System.out.println(cardDetails.getTransactionDate());
//		System.out.println(cardDetails.getAmount());
//		assertTrue(cardDetails.getAmount() == 12.89);
//	}

	public void testInvalidNumberOfColumns() {
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

	public void testDataTypeMismatchForDouble() {
		// lets change amount 12.89 to 12A.89 -its expected as a double in the
		// bean - currently the tool makes it 0 in such cases
		// TODO: maybe we need to throw an exception ??

		String line = "Mathew_Thomas 4111111111111111 02 2008 12A.89 222 10212005";
		DelimitedBean cardDetails = (DelimitedBean) spec.loadRecord(line);
		System.out.println(cardDetails.getAmount());
		assertTrue(cardDetails.getAmount() == 0);
	}

	public void testDataTypeMismatchOnDate() {
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
}
