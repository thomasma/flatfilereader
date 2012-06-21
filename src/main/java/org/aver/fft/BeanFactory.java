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
 * An implementation of this factory is responsible for creating beans into
 * which record column values are copied into. By default the transformer uses
 * the default implementation <code>DefaultBeanCreator</code>. The default
 * creator creates an instance using the default constructor of the specified
 * class. If you have a different mechanism for creating beans you can override
 * the default and provide your own instance when specifying the
 * 
 * @Transform tag.
 * 
 * @author Mathew Thomas
 * @see DefaultBeanCreator
 */
public interface BeanFactory {
	Object createBean(String className);
}
