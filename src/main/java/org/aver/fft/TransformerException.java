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

/**
 * Indicates that an unrecoverable exception occurred while transforming the file
 * to java objects.
 * 
 * @author Mathew Thomas
 */
public class TransformerException extends RuntimeException {
    
    /**
     * Constructs a new transformer exception with the specified detail message.
     * 
     * @param msg the detail message
     */
    public TransformerException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new transformer exception with the specified cause.
     * 
     * @param cause the cause of the exception
     */
    public TransformerException(Exception cause) {
        super(cause);
    }
    
    /**
     * Constructs a new transformer exception with the specified detail message and cause.
     * 
     * @param msg the detail message
     * @param cause the cause of the exception
     */
    public TransformerException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
