package org.aver.fft;

import java.util.Date;

import org.aver.fft.annotations.Column;
import org.aver.fft.annotations.Transform;

/**
 * This is a bean that is to be filled in from a semi-colon delimited record
 * format.
 * 
 * @author Mathew Thomas
 */
@Transform(columnSeparator = ";")
public class CommaDelimitedBean {
    private String nameOnCard;

    private int expMonth;

    private int expYear;

    private String cardSecurityCode;

    private String cardNumber;

    private double amount;

    private Date transactionDate;

    @Column(position = 1, required = true)
    public String getNameOnCard() {
        return nameOnCard;
    }

    public void setNameOnCard(String nameOnCard) {
        this.nameOnCard = nameOnCard;
    }

    @Column(position = 2, required = true)
    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    @Column(position = 3, required = true)
    public int getExpMonth() {
        return expMonth;
    }

    public void setExpMonth(int expMonth) {
        this.expMonth = expMonth;
    }

    @Column(position = 4, required = true)
    public int getExpYear() {
        return expYear;
    }

    public void setExpYear(int expYear) {
        this.expYear = expYear;
    }

    @Column(position = 5, required = true)
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Column(position = 6, required = true)
    public String getCardSecurityCode() {
        return cardSecurityCode;
    }

    public void setCardSecurityCode(String cardSecurityCode) {
        this.cardSecurityCode = cardSecurityCode;
    }

    @Column(position = 7, required = true, format = "MMddyyyy")
    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }
}
