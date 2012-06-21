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
		StringBuffer colVal = new StringBuffer();
		boolean quotedValue = false;
		for (int i = 0; i < src.length(); i++) {
			if (src.charAt(i) == '"' && quotedValue) {
				add(colVal.toString());
				colVal = new StringBuffer();
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
			colVal = new StringBuffer();
			quotedValue = false;
		}

		// last column is not added in the loop above so adding it explicitly here
		add(colVal.toString());
	}

	private void parse() {
		if (Transformer.ColumnSeparator.CHARACTER == transformer
				.getColumnSeparatorType()) {
			parseTokens(src, transformer.getColumnSeparator().charAt(0));
		} else if (Transformer.ColumnSeparator.FIXLENGTH == transformer
				.getColumnSeparatorType()) {
			Record rec = (Record) transformer.getRecord(destClazz.getName());
			for (int i : rec.indexes()) {
				Column col = (Column) rec.getColumnAt(i);
				try {
					add(src.substring(col.getStartColumn() - 1,
							col.getEndColumn()));
				} catch (IndexOutOfBoundsException ex) {
					throw new TransformerParseException(ex);
				}

			}
		} else {
			throw new TransformerException(
					"Invalid column separator type. Only supports enums in "
							+ Transformer.ColumnSeparator.class.getName());
		}
	}

	public String getRecordIdentifierValue() {
		return (String) get(transformer.getRecordIdentifierColumn());
	}

	public Iterator<String> iterator() {
		return new CustomIterator();
	}

	private Iterator getBaseIterator() {
		return super.iterator();
	}

	public String get(int pos) {
		try {
			return super.get(pos - 1);
		} catch (IndexOutOfBoundsException ex) {
			throw new TransformerParseException(ex);
		}
	}

	private class CustomIterator implements TokenIterator {
		private Iterator iter = getBaseIterator();

		public void remove() {
			iter.remove();
		}

		public boolean hasNext() {
			return iter.hasNext();
		}

		public String next() {
			return (String) iter.next();
		}

		public int getInt() {
			return Integer.parseInt((String) next());
		}

		public long getLong() {
			return Long.parseLong((String) next());
		}

		public String getString() {
			return (String) next();
		}
	}
}
