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

package org.aver.fft;

import java.io.File;
import java.io.InputStream;

/**
 * Transforms a text string to a java object. Supports two modes of operation.
 * In one mode you can pass a line to the transformer and get back a java object
 * with the initialized data. In the second approach you can pass in the entire
 * file and expect to receive notifications for each line in the file (with
 * either the java object or the line itself if the transformed could not map it
 * to a java class).
 * 
 * @author Mathew Thomas
 */
public interface Transformer {
	enum ColumnSeparator {
		CHARACTER, FIXLENGTH
	};

	/**
	 * Loads the record into an instance of the specified type. In the case that
	 * the record format cannot be located a null is returned.
	 * 
	 * @param line
	 * @throws TransformerException
	 *             if error occurs while parsing the line
	 */
	public Object loadRecord(String line);

	/**
	 * Parses the file and for every record matched, notifies the listener with
	 * the fully loaded bean. If record could not be matched then notify the
	 * listener with the record line so that it may do any custom processing.
	 * 
	 * @param file
	 *            file to parse
	 * @param listener
	 *            listener to notify found and notfound events
	 */
	public void parseFlatFile(File file, RecordListener listener);

	/**
	 * Parses the file and for every record matched, notifies the listener with
	 * the fully loaded bean. If record could not be matched then notify the
	 * listener with the record line so that it may do any custom processing.
	 * 
	 * @param stream
	 *            Input stream with file contents to parse
	 * @param listener
	 *            listener to notify found and notfound events
	 */
	public void parseFlatFile(InputStream stream, RecordListener listener);
}