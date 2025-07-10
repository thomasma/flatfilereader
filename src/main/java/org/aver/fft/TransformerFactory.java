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

import org.aver.fft.impl.FlatFileTransformer;

/**
 * Factory class used to initialize and retrieve an instance of the
 * <code>Transformer</code>.
 * 
 * @author Mathew Thomas
 */
public class TransformerFactory {
    /**
     * Private constructor to force usage via the factory method.
     */
    private TransformerFactory() {
        // no implementation
    }

    /**
     * Returns an initialized <code>Transformer</code> instance.
     * 
     * @param clazz
     *            class annotated with @Transform annotation
     * @return transformer instance
     */
    public static Transformer getTransformer(final Class<?> clazz) {
        // Type-safe approach: validate that the class is compatible
        if (clazz == null) {
            throw new TransformerException("Class cannot be null");
        }
        
        // Create a type-safe transformer with proper validation
        return new FlatFileTransformer(clazz);
    }
}
