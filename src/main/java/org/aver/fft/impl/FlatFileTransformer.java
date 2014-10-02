/*
 *  Copyright 2005 AverConsulting Inc.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.aver.fft.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aver.fft.BeanFactory;
import org.aver.fft.DefaultBeanCreator;
import org.aver.fft.RecordListener;
import org.aver.fft.Transformer;
import org.aver.fft.TransformerException;
import org.aver.fft.TransformerParseException;
import org.aver.fft.annotations.Transform;

/**
 * Implements the {@link Transformer} interface.
 * 
 * @author Mathew Thomas
 */
public final class FlatFileTransformer implements Transformer {
    /** Log messages into this. */
    private static final Log LOGGER = LogFactory
            .getLog(FlatFileTransformer.class);

    /** Default column separator (for delimited columns) */
    private final static String DEFAULT_COLUMN_SEPARATOR_CHARACTER = " ";

    /** Default column separator is CHARACTER (and not fixed length columns). */
    private ColumnSeparator columnSeparatorType = ColumnSeparator.CHARACTER;

    /** Default column separator (for delimited columns) */
    private String columnSeparator = DEFAULT_COLUMN_SEPARATOR_CHARACTER;

    /**
     * Column number that is the identifier for the record. An identifier is
     * used to uniquely map a record to a certain record type. This is needed
     * when we {@link RecordListener} is used.
     */
    private int idColumnIndex = 1;

    /**
     * An external reference to a factory that is responsible for creating the
     * beans into which the data is read into. By default
     * {@link DefaultBeanCreator} is used which simply creates a new instance
     * using the default constructor.
     */
    private BeanFactory beanCreator;

    /** Bean class name to {@link Record} instance. */
    private Map<String, Record> recordMap = new HashMap<String, Record>();

    private boolean skipFirstLine;

    private Class<Transform> clazz;

    /**
     * Initialize the transformer.
     * 
     * @param classes
     */
    public FlatFileTransformer(final Class<Transform> clazz) {
        this.clazz = clazz;
        loadClasses();
    }

    /**
     * Loads the FFT annotated classes and loads them into an internal cache for
     * fast reference later.
     * 
     * @param classes
     */
    private void loadClasses() {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Reading record metadata from annotations in class "
                    + clazz.getClass().getName());
        }
        Transform classAnnotation = (Transform) clazz
                .getAnnotation(Transform.class);
        setBeanCreator(classAnnotation.beanCreator());
        setColumnSeparator(classAnnotation.columnSeparator());
        setColumnSeparatorType(classAnnotation.columnSeparatorType());
        setSkipFirstLine(classAnnotation.skipFirstLine());

        // parse column mappings for this record
        parseRecordMappingDetails();

    }

    /***/
    public void parseFlatFile(final InputStream stream,
            final RecordListener listener) {
        if (stream == null) {
            throw new TransformerException(
                    "Cannot read file. Invalid InputStream.");
        }

        parse(new BufferedReader(new InputStreamReader(stream)), listener);
    }

    /***/
    public void parseFlatFile(final File file, final RecordListener listener) {
        if (file == null || !file.exists() || !file.canRead()
                || file.isDirectory()) {
            throw new TransformerException("Cannot read file " + file);
        }
        try {
            parse(new BufferedReader(new FileReader(file)), listener);
        } catch (FileNotFoundException e) {
            throw new TransformerException(e);
        }
    }

    /***/
    public Object loadRecord(final String line) {
        Object dest = beanCreator.createBean(clazz.getName());

        Map<String, Object> map = new HashMap<String, Object>();
        TokenList tokens = new TokenList(line, this, clazz);
        Record rec = recordMap.get(clazz.getName());
        for (int i : rec.indexes()) {
            Column col = rec.getColumnAt(i);
            if (col.isSkip()) {
                continue;
            }

            if (col.getType().equals(Date.class.getName())) {
                SimpleDateFormat formatter = new SimpleDateFormat(
                        col.getDateFormat());
                try {
                    map.put(col.getName(), formatter.parse((String) tokens
                            .get(col.getIndex())));
                } catch (ParseException e) {
                    throw new TransformerParseException(e);
                }
            } else {
                String coldata = (String) tokens.get(col.getIndex());
                map.put(col.getName(), coldata);
            }
        }

        // use BeanUtils to load data into bean
        try {
            BeanUtils.populate(dest, map);
        } catch (IllegalAccessException e) {
            throw new TransformerParseException(e);
        } catch (InvocationTargetException e) {
            throw new TransformerParseException(e);
        }

        return dest;
    }

    /**
     * Reads the specified class and loads into (if not already loaded) a map
     * containing the bean class name to a {@link Record} instance. The Record
     * instance will contain metadata about the record format.
     * 
     * @param destClazz
     */
    private void parseRecordMappingDetails() {
        if (!recordMap.containsKey(clazz.getName())) {
            synchronized (recordMap) {
                Record rec = new Record(clazz.getName());
                recordMap.put(rec.getName(), rec);
                for (Method m : clazz.getMethods()) {
                    if (m.isAnnotationPresent(org.aver.fft.annotations.Column.class)) {
                        String colname = m.getName();
                        if (m.getName().startsWith("get")
                                || m.getName().startsWith("set")) {
                            colname = colname.substring(3);
                        }
                        colname = Character.toLowerCase(colname.charAt(0))
                                + colname.substring(1);
                        org.aver.fft.annotations.Column annot = m
                                .getAnnotation(org.aver.fft.annotations.Column.class);
                        // String name, String type, boolean required, int
                        // index, String format, boolean skip
                        Column col = new Column(colname, m.getReturnType()
                                .getName(), annot.required(), annot.position(),
                                annot.format(), annot.skip());
                        col.setStartColumn(annot.start());
                        col.setEndColumn(annot.end());
                        rec.addColumn(col);
                    }
                }
            }
        }
    }

    /**
     * Parse the file contents and register the listener to which we send
     * individual records read from the file. File contents will be read until
     * either the complete file is read or if the {@link RecordListener} returns
     * a <code>false</code> requesting the framework to stop reading the rest of
     * the file.
     * 
     * @param reader
     *            file contents
     * @param listener
     *            listener waiting for records
     */
    private void parse(final BufferedReader reader,
            final RecordListener listener) {
        if (listener == null) {
            throw new TransformerException("Expecting non-null instance of "
                    + RecordListener.class.getName());
        }

        try {
            String line = null;
            boolean continueReading = true;
            long lineCount = 0;
            while ((line = reader.readLine()) != null) {
                lineCount++;

                if (skipFirstLine && lineCount == 1) {
                    continue;
                }

                // check if we need to stop reading
                if (!continueReading) {
                    LOGGER.info("Aborted reading of file at line# " + lineCount);
                    break;
                }

                // ok read on
                Object o = loadRecord(line);
                if (o != null) {
                    continueReading = listener.foundRecord(o);
                } else {
                    continueReading = listener.unresolvableRecord(line);
                }
            }
        } catch (IOException e) {
            throw new TransformerException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    LOGGER.error("Exception while closing the file reader.", ex);
                }
            }
        }
    }

    // ----------------------------------------------------------------------
    // Setters/Getters.
    // ----------------------------------------------------------------------
    public Record getRecord(final String string) {
        return (Record) recordMap.get(string);
    }

    public ColumnSeparator getColumnSeparatorType() {
        return columnSeparatorType;
    }

    public void setColumnSeparatorType(final ColumnSeparator columnSeparatorType) {
        this.columnSeparatorType = columnSeparatorType;
    }

    public void setBeanCreator(BeanFactory beanCreator) {
        this.beanCreator = beanCreator;
    }

    public int getIdColumnIndex() {
        return idColumnIndex;
    }

    public BeanFactory getBeanCreator() {
        return beanCreator;
    }

    public String getColumnSeparator() {
        return columnSeparator;
    }

    public void setColumnSeparator(final String separator) {
        this.columnSeparator = separator;

        if (columnSeparator == null || columnSeparator.trim().length() == 0) {
            columnSeparator = DEFAULT_COLUMN_SEPARATOR_CHARACTER;
        }

    }

    public boolean isSkipFirstLine() {
        return skipFirstLine;
    }

    public void setSkipFirstLine(boolean skipFirstLine) {
        this.skipFirstLine = skipFirstLine;
    }

    public int getRecordIdentifierColumn() {
        return idColumnIndex;
    }

    public void setRecordIdentifierColumn(int idcol) {
        idColumnIndex = idcol;
        if (idColumnIndex < 0) {
            idColumnIndex = 0;
        }
        idColumnIndex--;
    }

    /**
     * Sets the factory that constructs the beans into which record data is to
     * be loaded.
     * 
     * @param value
     */
    public void setBeanCreator(final String beanFactory) {
        if (StringUtils.isEmpty(beanFactory)) {
            throw new TransformerException(
                    "An implementation of the interface "
                            + BeanFactory.class.getName()
                            + " must be provided.");
        }

        try {
            final Class beanFactoryClazz = Class.forName(beanFactory);
            if (!BeanFactory.class.isAssignableFrom(beanFactoryClazz)) {
                throw new TransformerException(beanFactory
                        + " does not implement " + BeanFactory.class.getName());
            }
            beanCreator = (BeanFactory) beanFactoryClazz.newInstance();
        } catch (ClassNotFoundException e) {
            throw new TransformerException(e);
        } catch (InstantiationException e) {
            throw new TransformerException(e);
        } catch (IllegalAccessException e) {
            throw new TransformerException(e);
        }
    }
}
