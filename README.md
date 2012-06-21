User Guide - Flat File Reader
=============================
If your goal is to parse a simple flat file (either character separated columns or fixed length columns) then this library will be of interest to you. This is also ideal for large files where you want control of converting the records line by line and definitely do not want the framework to load the entire file into a collection in memory.

The parser supports two ways of parsing a file. In the first approach you are responsible for reading the file and providing each line that needs to be transformed to the reader. The second approach is SAX-like, in that you register a listener and the transformer will notify your listener as it binds a record to a POJO and also when it could not bind a record. The second approach is also stax-like, such that you have control to stop the parsing at any time.

We will run through the first approach and at the end I will tell you what changes (minor) are to be made to switch to the SAX-line parsing approach.

Let’s start by creating a java class that represents our record (assume our record has comma character separated columns).


Approach#1 - DELIMITED COLUMN - You parse the file and each line to the parser.
-------------------------------------------------------------------------------

import org.aver.fft.annotations.Column;
import org.aver.fft.annotations.Transform;

@Transform (spaceEscapeCharacter=",")
public class DelimitedBean
{
    .....

    @Column(position = 1, required = true)
    public int getRecordId()
    {
        return recordId;
    }

    @Column(position = 2, required = true)
    public String getNameOnCard()
    {
        return nameOnCard;
    }


    @Column(position = 3, required = true)
    public String getCardNumber()
    {
        return cardNumber;
    }

    @Column(position = 4, required = true)
    public int getExpMonth()
    {
        return expMonth;
    }


    @Column(position = 5, required = true)
    public int getExpYear()
    {
        return expYear;
    }

    @Column(position = 6, required = true)
    public double getAmount()
    {
        return amount;
    }

    @Column(position = 7, required = true)
    public String getCardSecurityCode()
    {
        return cardSecurityCode;
    }

    @Column(position = 8, required = true, format = "MMddyyyy")
    public Date getTransactionDate()
    {
        return transactionDate;
    }
    
    ... other methods here ...
}

  
By default the parser is setup to parse character delimited columns. The optional attribute spaceEscapeCharacter indicates the character used to represent spaces within column data. The parser can replace that with space before loading it into your java object. By default the column separator is space for character. You can change that using columnSeparator.

That’s enough on defining the file format. Now here is how to actually read it.

Transformer spec = TransformerFactory.getTransformer(DelimitedBean.class);
String line = "Mathew_Thomas 4111111111111111 02 2008 12.89 222 10212005";
DelimitedBean bean = (DelimitedBean) spec.loadRecord(line);


You get a transformer instance as shown above. Pass it the class that represent your Java bean that uses annotations to map the various columns. Now you have a fully loaded bean from which to read your data. That’s all. 


Approach#1 - FIXED COLUMN: You parse the file and each line to the parser.
--------------------------------------------------------------------------
Now lets see how you define the same for a fixed column record format. The parsing code above stays the same. The difference is in how you annotate your result bean class.

import org.aver.fft.annotations.Column;
import org.aver.fft.annotations.Transform;

@Transform(columnSeparatorType = Transformer.ColumnSeparator.FIXLENGTH)
public class FixedColBean
{
    @Column(position= 1, start = 1, end = 2, required = true)
    public int getRecordId()
    {
        return recordId;
    }

    @Column(position= 2, start = 3, end = 15, required = true)
    public String getNameOnCard()
    {
        return nameOnCard;
    }

    @Column(position= 3, start = 16, end = 31, required = true)
    public String getCardNumber()
    {
        return cardNumber;
    }

    @Column(position= 4, start = 32, end = 33, required = true)
    public int getExpMonth()
    {
        return expMonth;
    }

    @Column(position= 5, start = 34, end = 37, required = true)
    public int getExpYear()
    {
        return expYear;
    }

    @Column(position= 6, start = 38, end = 43, required = true)
    public double getAmount()
    {
        return amount;
    }

    @Column(position= 7, start = 44, end = 46, required = true)
    public String getCardSecurityCode()
    {
        return cardSecurityCode;
    }

    @Column(position= 8, start = 47, end = 54, required = true, format = "MMddyyyy")
    public Date getTransactionDate()
    {
        return transactionDate;
    }

    … other methods here …
}

The parsing logic stays the same. Just give it the correct line of data.



Approach#2 - SAX/STAX line parsing using event listener.
--------------------------------------------------------
Now I will show you the SAX-like parsing approach.
package org.aver.fft;

import java.io.File;

import junit.framework.TestCase;

public class DelimitedFullFileReaderTestCase extends TestCase
{
    public void testFullFileReader()
    {
        Transformer spec = TransformerFactory.getTransformer(DelimitedBean.class);
        spec.parseFlatFile(new File("c:/multi-record-delcol-file.txt"), new Listener());
    }

    class Listener implements RecordListener
    {
        public boolean foundRecord(Object o)
        {
            // make sure this is your bean though
            DelimitedBean bean = (DelimitedBean) o;
            System.out.println(bean.getNameOnCard());
            return true;
        }

        public boolean unresolvableRecord(String rec)
        {
            // TODO Auto-generated method stub
            return true;
        }
    }
}

For the RecordListener implementation you pass true to indicate to the parser that you want to continue parsing. To stop parsing return a false.

Its important that you either let the parser parse the entire file or return a false to indicate you want to stop parsing. This allows the parser to close the handle to the file stream.

