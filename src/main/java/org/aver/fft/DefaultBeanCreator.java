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
 * Default implementation of the BeanFactory interface. The bean class must have
 * a default constructor for this to work.
 * 
 * @author Mathew Thomas
 */
public class DefaultBeanCreator implements BeanFactory {
    /**
     * Creates an instance of the destination bean using its default
     * constructor.
     * 
     * @param name
     *            of the destination bean
     */
    public Object createBean(String className) {
        try {
            return Class.forName(className).newInstance();
        } catch (InstantiationException e) {
            throw new TransformerException(e);
        } catch (IllegalAccessException e) {
            throw new TransformerException(e);
        } catch (ClassNotFoundException e) {
            throw new TransformerException(e);
        }
    }

}
