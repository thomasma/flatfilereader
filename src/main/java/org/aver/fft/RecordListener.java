package org.aver.fft;

/**
 * Register an implementation of this listener to get notifications on record
 * lines as they are read from the file.
 * 
 * @author Mathew Thomas
 */
public interface RecordListener {
	/**
	 * Notifies the listener that a record line matched a java object in the
	 * transformer specification and passes in the initialized java object.
	 * 
	 * @param o
	 *            java object representing the record
	 * @return return true to continue reading more records, or a false to abort
	 *         parsing of rest of file.
	 */
	public boolean foundRecord(Object o);

	/**
	 * Notifies the listener that the transformer could not load this record.
	 * Passes in the unparsed line.
	 * 
	 * @param rec
	 *            unparsed record line from file
	 * @return return true to continue reading more records, or a false to abort
	 *         parsing of rest of file.
	 */
	public boolean unresolvableRecord(String rec);
}
