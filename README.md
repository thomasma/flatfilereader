User Guide - Flat File Reader
=============================
This project now targets **Java 21**. Ensure a Java 21 JDK is available when building or running the library.
If your goal is to parse a simple flat file (with either character delimited columns or fixed length columns) then this library will be of interest to you. This is also ideal for large files where you want control of parsing the records line by line and definitely do not want the framework to load the entire file into a collection in memory.

The framework supports two ways of parsing a file. 
* In the first approach *you* are responsible for reading the file and providing each line that needs to be transformed to the reader. The framework then returns you a POJO.
* The second approach is SAX-like, in that you register a listener and the framework will notify your listener as it binds each record in the file to a POJO and also when it could not bind a record. The second approach is also stax-like, such that you have control to stop the parsing at any time.

We will run through the first approach and at the end I will tell you what changes (minor) are to be made to switch to the SAX/STAX-style parsing approach.

Let’s start by creating a java class that represents our record (assume our record has comma character separated columns).


Approach#1 - DELIMITED COLUMN - You parse the file and provide each line to the parser.
-------------------------------------------------------------------------------

    import org.aver.fft.annotations.Column;
    import org.aver.fft.annotations.Transform;

    @Transform(columnSeparator=",")
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

  
By default the parser is setup to parse character delimited columns. We used the attribute columnSeparator to switch the delimiter to comma. Here is the code snippet to convert a line to a POJO. Remember in this approach you are responsible for parsing the file and reading each line.

    Transformer spec = TransformerFactory.getTransformer(DelimitedBean.class);
    String line = "Mathew_Thomas 4111111111111111 02 2008 12.89 222 10212005";
    DelimitedBean bean = (DelimitedBean) spec.loadRecord(line);


Approach#1 - FIXED COLUMN: You parse the file and provide each line to the parser.
--------------------------------------------------------------------------
Now lets see how you define the same for a fixed column record format. The parsing code above stays the same. The difference is in how you annotate your result bean class.

    import org.aver.fft.annotations.Column;
    import org.aver.fft.annotations.Transform;

    @Transform(columnSeparatorType = Transformer.ColumnSeparator.FIXLENGTH)
    public class FixedColBean
    {
        ... same code as before ...
    }


Approach#2 - SAX/STAX line parsing using event listener.
--------------------------------------------------------
Now I will show you the SAX-like parsing approach. No changes to the annotations on the java beans.

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

                // return true to continue parsing and false to stop parsing
                return true;
            }

            public boolean unresolvableRecord(String rec)
            {
                // TODO Auto-generated method stub
                return true;
            }
        }
    }

In the RecordListener you must decide whether or not you want to contine or stop the parsing by return true or false respectively pass.


