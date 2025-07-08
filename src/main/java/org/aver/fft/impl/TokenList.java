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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;

import org.aver.fft.Transformer;
import org.aver.fft.TransformerException;
import org.aver.fft.TransformerParseException;

/**
 * A custom LinkedList which represents the record as a token of string values.
 * Will take into consideration if the records are character separated or fixed
 * position columns.
 * 
 * @author Mathew Thomas
 */
@SuppressWarnings("serial")
class TokenList extends LinkedList<String> {
    private String src;

    private FlatFileTransformer transformer;

    private Class destClazz;

    /**
     * Tokenize the record given the transformer.
     * 
     * @param src
     * @param transformer
     * @param clazz
     */
    public TokenList(String src, FlatFileTransformer transformer, Class clazz) {
        this.src = src;
        this.transformer = transformer;
        destClazz = clazz;

        // apply sane defaults
        if (transformer == null) {
            throw new TransformerException("Invalid file spec.");
        }

        if (src == null) {
            throw new TransformerException(
                    "Source string to parse cannot be null.");
        }

        // parse string into tokens as per file spec
        parse();
    }

    private void parseTokens(String src, char delim) {
        StringBuilder colVal = new StringBuilder();
        boolean quotedValue = false;
        for (int i = 0; i < src.length(); i++) {
            if (src.charAt(i) == '"' && quotedValue) {
                add(colVal.toString());
                colVal = new StringBuilder();
                quotedValue = false;
                i++;
                continue;
            } else if (src.charAt(i) == '"') {
                quotedValue = true;
                continue;
            }

            if ((!quotedValue && src.charAt(i) != delim) || (quotedValue)
                    || (src.charAt(i) != delim)
                    || (quotedValue && src.charAt(i) == delim)) {
                colVal.append(src.charAt(i));
                continue;
            }

            add(colVal.toString());
            colVal = new StringBuilder();
            quotedValue = false;
        }

        // last column is not added in the loop above so adding it explicitly
        // here
        add(colVal.toString());
    }

    private void parse() {
        switch (transformer.getColumnSeparatorType()) {
            case CHARACTER -> parseTokens(src, transformer.getColumnSeparator().charAt(0));
            case FIXLENGTH -> {
                Optional<Record> optionalRec = transformer.getRecord(destClazz.getName());
                if (optionalRec.isEmpty()) {
                    throw new TransformerException("No record format found for class: " + destClazz.getName());
                }
                
                Record rec = optionalRec.get();
                for (int i : rec.indexes()) {
                    Column col = rec.getColumnAt(i);
                    
                    // Security: Validate column positions to prevent DoS attacks
                    if (!isValidColumnPosition(col, src.length())) {
                        throw new TransformerParseException("Invalid column position - startColumn: " + 
                                col.startColumn() + ", endColumn: " + col.endColumn() + 
                                ", source length: " + src.length());
                    }
                    
                    try {
                        add(src.substring(col.startColumn() - 1,
                                col.endColumn()));
                    } catch (IndexOutOfBoundsException ex) {
                        throw new TransformerParseException(ex);
                    }
                }
            }
            default -> throw new TransformerException("""
                    Invalid column separator type. Only supports enums in %s"""
                    .formatted(Transformer.ColumnSeparator.class.getName()));
        }
    }

    public String getRecordIdentifierValue() {
        return (String) get(transformer.getRecordIdentifierColumn());
    }
    
    /**
     * Security: Validate column positions to prevent DoS attacks
     */
    private boolean isValidColumnPosition(Column col, int sourceLength) {
        if (col == null) {
            return false;
        }
        
        int startColumn = col.startColumn();
        int endColumn = col.endColumn();
        
        // Validate start column
        if (startColumn < 1) {
            return false;
        }
        
        // Validate end column
        if (endColumn < startColumn) {
            return false;
        }
        
        // Validate against source length
        if (startColumn - 1 >= sourceLength) {
            return false;
        }
        
        if (endColumn > sourceLength) {
            return false;
        }
        
        // Prevent extremely large column ranges that could cause DoS
        if (endColumn - startColumn > 10000) {
            return false;
        }
        
        return true;
    }

    public Iterator<String> iterator() {
        return new CustomIterator();
    }

    private Iterator<String> getBaseIterator() {
        return super.iterator();
    }

    public String get(int pos) {
        // Security: Validate position to prevent DoS attacks
        if (pos < 1) {
            throw new TransformerParseException("Invalid position: " + pos + ". Position must be >= 1");
        }
        
        if (pos > size()) {
            throw new TransformerParseException("Invalid position: " + pos + ". Maximum position is: " + size());
        }
        
        try {
            return super.get(pos - 1);
        } catch (IndexOutOfBoundsException ex) {
            throw new TransformerParseException(ex);
        }
    }

    private class CustomIterator implements TokenIterator {
        private Iterator<String> iter = getBaseIterator();

        public void remove() {
            iter.remove();
        }

        public boolean hasNext() {
            return iter.hasNext();
        }

        public String next() {
            return iter.next();
        }

        public int getInt() {
            return Integer.parseInt(next());
        }

        public long getLong() {
            return Long.parseLong(next());
        }

        public String getString() {
            return next();
        }
    }
}
