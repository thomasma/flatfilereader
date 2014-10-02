package org.aver.fft;

/**
 * Indicates that an unrecoverable parsing exception occured while transforming
 * the file to java objects.
 * 
 * @author Mathew Thomas
 */
@SuppressWarnings("serial")
public class TransformerParseException extends TransformerException {
    public TransformerParseException(Exception ex) {
        super(ex);
    }

    public TransformerParseException(String msg) {
        super(msg);
    }
}
