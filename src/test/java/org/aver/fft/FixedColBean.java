package org.aver.fft;

import java.util.Date;

import org.aver.fft.annotations.Column;
import org.aver.fft.annotations.Transform;

/**
 * This is a bean that is to be filled in from a fixed column record format.
 * 
 * @author Mathew Thomas
 */

@Transform(columnSeparatorType = Transformer.ColumnSeparator.FIXLENGTH)
public class FixedColBean {
	private String nameOnCard;

	private int expMonth;

	private int expYear;

	private String cardSecurityCode;

	private String cardNumber;

	private double amount;

	private Date transactionDate;

	// "Mathew_Thomas4111111111111111022008 12.8922210212005"

	@Column(position = 1, start = 1, end = 13, required = true)
	public String getNameOnCard() {
		return nameOnCard;
	}

	public void setNameOnCard(String nameOnCard) {
		this.nameOnCard = nameOnCard;
	}

	@Column(position = 2, start = 14, end = 29, required = true)
	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	@Column(position = 3, start = 30, end = 31, required = true)
	public int getExpMonth() {
		return expMonth;
	}

	public void setExpMonth(int expMonth) {
		this.expMonth = expMonth;
	}

	@Column(position = 4, start = 32, end = 35, required = true)
	public int getExpYear() {
		return expYear;
	}

	public void setExpYear(int expYear) {
		this.expYear = expYear;
	}

	@Column(position = 5, start = 36, end = 41, required = true)
	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	@Column(position = 6, start = 42, end = 44, required = true)
	public String getCardSecurityCode() {
		return cardSecurityCode;
	}

	public void setCardSecurityCode(String cardSecurityCode) {
		this.cardSecurityCode = cardSecurityCode;
	}

	@Column(position = 7, start = 45, end = 52, required = true, format = "MMddyyyy")
	public Date getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}
}
