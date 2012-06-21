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

/**
 * Metadata about specific column.
 * 
 * @author Mathew Thomas
 */
final class Column {
	/** name of column */
	private String name;

	/** data type of column */
	private String type;

	/** is the column a required one */
	private boolean required;

	/** name of column */
	private String dateFormat;

	/** column position in record (starting from 1) */
	private int index;

	/** should this column be skipped while parsing */
	private boolean skip;

	/** Start column position for value. Only relevant for fixed column lengths. */
	private int startColumn;

	/** End column position for value. Only relevant for fixed column lengths. */
	private int endColumn;

	/**
	 * Initializes column.
	 * 
	 * @param name
	 * @param type
	 * @param required
	 * @param index
	 * @param format
	 * @param skip
	 */
	public Column(String name, String type, boolean required, int index,
			String format, boolean skip) {
		setName(name);
		this.type = type;
		this.required = required;
		this.index = index;
		this.dateFormat = format;
		this.skip = skip;
	}

	public int getEndColumn() {
		return endColumn;
	}

	public void setEndColumn(int end) {
		endColumn = end;
	}

	public int getStartColumn() {
		return startColumn;
	}

	public void setStartColumn(int start) {
		startColumn = start;
	}

	public String getName() {
		return name;
	}

	public void setName(String nm) {
		name = nm;
		if (Character.isUpperCase(name.charAt(0))) {
			name = Introspector.decapitalize(name);
		}
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getIndex() {
		return Integer.valueOf(index);
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public boolean isSkip() {
		return skip;
	}
}
