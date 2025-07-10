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
import java.util.Optional;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory
            .getLogger(FlatFileTransformer.class);

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
    private final Map<String, Record> recordMap = new HashMap<>();

    private boolean skipFirstLine;

    private final Class<?> clazz;

    /**
     * Initialize the transformer.
     * 
     * @param clazz the target class for transformation
     * @throws TransformerException if the class is invalid
     */
    public FlatFileTransformer(final Class<?> clazz) {
        // Security: Validate input class
        if (clazz == null) {
            throw new TransformerException("Target class cannot be null");
        }
        
        // Security: Ensure class has proper annotations
        if (!clazz.isAnnotationPresent(Transform.class)) {
            throw new TransformerException("Class must be annotated with @Transform: " + clazz.getName());
        }
        
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
    /**
     * Parses an InputStream and sends records to the listener.
     * 
     * @param stream the input stream to parse
     * @param listener the listener to receive parsed records
     * @throws TransformerException if input validation fails or parsing errors occur
     */
    @Override
    public void parseFlatFile(final InputStream stream,
            final RecordListener listener) {
        // Security: Validate inputs
        if (stream == null) {
            throw new TransformerException("InputStream cannot be null");
        }
        
        if (listener == null) {
            throw new TransformerException("RecordListener cannot be null");
        }

        try {
            parse(new BufferedReader(new InputStreamReader(stream)), listener);
        } catch (Exception e) {
            throw new TransformerException("Error parsing input stream: " + e.getMessage(), e);
        }
    }

    /**
     * Parses a file and sends records to the listener.
     * 
     * @param file the file to parse
     * @param listener the listener to receive parsed records
     * @throws TransformerException if input validation fails or file access errors occur
     */
    @Override
    public void parseFlatFile(final File file, final RecordListener listener) {
        // Security: Comprehensive file validation
        if (file == null) {
            throw new TransformerException("File cannot be null");
        }
        
        if (listener == null) {
            throw new TransformerException("RecordListener cannot be null");
        }
        
        if (!file.exists()) {
            throw new TransformerException("File does not exist: " + file.getAbsolutePath());
        }
        
        if (!file.isFile()) {
            throw new TransformerException("Path is not a regular file: " + file.getAbsolutePath());
        }
        
        if (!file.canRead()) {
            throw new TransformerException("File is not readable: " + file.getAbsolutePath());
        }
        
        // Security: Prevent reading of extremely large files (DoS protection)
        if (file.length() > 500_000_000) { // 500MB limit
            throw new TransformerException("File too large (max 500MB): " + file.length() + " bytes");
        }
        
        try {
            parse(new BufferedReader(new FileReader(file)), listener);
        } catch (FileNotFoundException e) {
            throw new TransformerException("File not found: " + file.getAbsolutePath(), e);
        } catch (SecurityException e) {
            throw new TransformerException("Security error accessing file: " + file.getAbsolutePath(), e);
        } catch (Exception e) {
            throw new TransformerException("Error parsing file: " + file.getAbsolutePath() + " - " + e.getMessage(), e);
        }
    }

    /**
     * Loads a record from a line of text into a Java object.
     * 
     * @param line the input line to parse
     * @return the parsed object
     * @throws TransformerException if parsing fails
     * @throws TransformerParseException if the line format is invalid
     */
    @Override
    public Object loadRecord(final String line) {
        // Security: Comprehensive input validation
        if (line == null) {
            throw new TransformerException("Input line cannot be null");
        }
        
        // Security: Prevent DoS attacks with extremely long lines
        if (line.length() > 100000) { // 100KB limit
            throw new TransformerException("Input line too long (max 100,000 characters): " + line.length());
        }
        
        // Security: Validate class state
        if (clazz == null) {
            throw new TransformerException("Target class is not initialized");
        }
        
        Object dest = beanCreator.createBean(clazz.getName());

        Map<String, Object> map = new HashMap<>();
        TokenList tokens = new TokenList(line, this, clazz);
        Optional<Record> optionalRec = getRecord(clazz.getName());
        
        if (optionalRec.isEmpty()) {
            throw new TransformerException("No record format found for class: " + clazz.getName());
        }
        
        Record rec = optionalRec.get();
        for (int i : rec.indexes()) {
            Column col = rec.getColumnAt(i);
            if (col.skip()) {
                continue;
            }

            switch (col.type()) {
                case String dateType when dateType.equals(Date.class.getName()) -> {
                    SimpleDateFormat formatter = new SimpleDateFormat(col.dateFormat());
                    try {
                        map.put(col.name(), formatter.parse((String) tokens
                                .get(col.getIndex())));
                    } catch (ParseException e) {
                        throw new TransformerParseException(e);
                    }
                }
                default -> {
                    String coldata = (String) tokens.get(col.getIndex());
                    map.put(col.name(), coldata);
                }
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
                                annot.format(), annot.skip())
                                .withFixedPositions(annot.start(), annot.end());
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
        Optional.ofNullable(listener)
                .orElseThrow(() -> new TransformerException("Expecting non-null instance of "
                        + RecordListener.class.getName()));

        try {
            String line = null;
            boolean continueReading = true;
            long lineCount = 0;
            long maxLines = 1_000_000; // Security: Limit max lines to prevent DoS
            
            while ((line = reader.readLine()) != null && lineCount < maxLines) {
                lineCount++;

                if (skipFirstLine && lineCount == 1) {
                    continue;
                }

                // check if we need to stop reading
                if (!continueReading) {
                    LOGGER.info("Aborted reading of file at line# " + lineCount);
                    break;
                }

                // Security: Additional line length validation
                if (line.length() > 50000) { // 50KB per line limit
                    LOGGER.warn("Skipping line {} due to excessive length: {} characters", lineCount, line.length());
                    continueReading = listener.unresolvableRecord(line);
                    continue;
                }

                // ok read on
                try {
                    Object o = loadRecord(line);
                    continueReading = listener.foundRecord(o);
                } catch (TransformerException e) {
                    LOGGER.debug("Failed to parse line {}: {}", lineCount, e.getMessage());
                    continueReading = listener.unresolvableRecord(line);
                } catch (OutOfMemoryError e) {
                    // Security: Handle memory exhaustion gracefully
                    throw new TransformerException("Out of memory while processing line " + lineCount + 
                            ". Consider processing smaller files or increasing heap size.", e);
                }
            }
            
            // Security: Warn if max lines exceeded
            if (lineCount >= maxLines) {
                LOGGER.warn("File processing stopped at {} lines (maximum allowed). " + 
                           "Remaining lines were not processed.", maxLines);
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
    public Optional<Record> getRecord(final String string) {
        return Optional.ofNullable(recordMap.get(string));
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
        Optional.ofNullable(beanFactory)
                .filter(bf -> !StringUtils.isEmpty(bf))
                .orElseThrow(() -> new TransformerException("""
                        An implementation of the interface %s must be provided."""
                        .formatted(BeanFactory.class.getName())));

        // Security: Validate bean factory class name to prevent RCE attacks
        if (isBlacklistedBeanFactory(beanFactory)) {
            throw new TransformerException("Bean factory class not allowed for security reasons: " + beanFactory);
        }

        try {
            final Class<?> beanFactoryClazz = Class.forName(beanFactory);
            
            // Security: Ensure class is safe to instantiate
            if (!isSafeBeanFactory(beanFactoryClazz)) {
                throw new TransformerException("Bean factory class not allowed for security reasons: " + beanFactory);
            }
            
            if (!BeanFactory.class.isAssignableFrom(beanFactoryClazz)) {
                throw new TransformerException("""
                        %s does not implement %s"""
                        .formatted(beanFactory, BeanFactory.class.getName()));
            }
            beanCreator = (BeanFactory) beanFactoryClazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | 
                 NoSuchMethodException | java.lang.reflect.InvocationTargetException e) {
            throw new TransformerException(e);
        }
    }
    
    /**
     * Security: Check if bean factory class name is blacklisted
     */
    private boolean isBlacklistedBeanFactory(String className) {
        // Block dangerous classes that could be used for RCE
        String[] blacklistedPrefixes = {
            "java.lang.Runtime",
            "java.lang.ProcessBuilder",
            "java.lang.System",
            "java.lang.Class",
            "java.lang.Thread",
            "java.security.",
            "java.net.",
            "java.io.File",
            "java.io.FileInputStream",
            "java.io.FileOutputStream",
            "java.nio.file.",
            "javax.script.",
            "sun.",
            "com.sun.",
            "jdk.internal.",
            "org.springframework.context.",
            "org.apache.commons.beanutils.BeanUtils"
        };
        
        String lowerClassName = className.toLowerCase();
        for (String prefix : blacklistedPrefixes) {
            if (lowerClassName.startsWith(prefix.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Security: Check if bean factory class is safe to instantiate
     */
    private boolean isSafeBeanFactory(Class<?> clazz) {
        // Additional runtime checks
        if (clazz.isPrimitive() || clazz.isArray()) {
            return false;
        }
        
        // Check if class has dangerous interfaces or superclasses
        try {
            if (java.lang.Runtime.class.isAssignableFrom(clazz) ||
                java.lang.ProcessBuilder.class.isAssignableFrom(clazz) ||
                java.lang.ClassLoader.class.isAssignableFrom(clazz) ||
                java.lang.Thread.class.isAssignableFrom(clazz)) {
                return false;
            }
        } catch (Exception e) {
            // If we can't check safely, assume it's dangerous
            return false;
        }
        
        return true;
    }
}
