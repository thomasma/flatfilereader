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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.aver.fft.TransformerException;

/**
 * Represents a record in a flat file.
 * 
 * @author Mathew Thomas
 */
final class Record {
	private String name;

	private Map<Integer, Column> columnMap = new HashMap<Integer, Column>();

	public Record(String name) {
		// sanity checks
		if (StringUtils.isEmpty(name)) {
			throw new TransformerException("Record name must be specified");
		}

		// copy parameters
		this.name = name;
	}

	public String getName() {
		return name;
	}

	void addColumn(Column col) {
		columnMap.put(col.getIndex() - 1, col);
	}

	public Integer[] indexes() {
		Integer[] keyWeights = (Integer[]) columnMap.keySet().toArray(
				new Integer[columnMap.size()]);
		Arrays.sort(keyWeights);
		return keyWeights;
	}

	public Column getColumnAt(int index) {
		return columnMap.get(index);
	}
}
