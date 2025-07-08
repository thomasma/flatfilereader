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

import java.beans.Introspector;
import java.util.Optional;

/**
 * Metadata about specific column.
 * 
 * @author Mathew Thomas
 */
record Column(
    String name,
    String type,
    boolean required,
    String dateFormat,
    int index,
    boolean skip,
    int startColumn,
    int endColumn
) {
    
    /**
     * Initializes column with processed name.
     * 
     * @param name column name
     * @param type data type of column
     * @param required is the column required
     * @param index column position in record (starting from 1)
     * @param dateFormat date format string
     * @param skip should this column be skipped while parsing
     */
    public Column(String name, String type, boolean required, int index,
            String dateFormat, boolean skip) {
        this(processName(name), type, required, dateFormat, index, skip, 0, 0);
    }
    
    /**
     * Creates column with start and end positions for fixed-width columns.
     */
    public Column withFixedPositions(int startColumn, int endColumn) {
        return new Column(name, type, required, dateFormat, index, skip, startColumn, endColumn);
    }
    
    /**
     * Returns boxed Integer for compatibility with existing code.
     */
    public Integer getIndex() {
        return index;
    }
    
    /**
     * Processes name to ensure proper capitalization.
     */
    private static String processName(String name) {
        return Optional.ofNullable(name)
                .filter(n -> !n.isEmpty())
                .filter(n -> Character.isUpperCase(n.charAt(0)))
                .map(Introspector::decapitalize)
                .orElse(name);
    }
}
