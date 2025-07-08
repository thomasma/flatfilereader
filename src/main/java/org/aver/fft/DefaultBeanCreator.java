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
    @Override
    public Object createBean(String className) {
        // Security: Validate class name to prevent RCE attacks
        if (className == null || className.trim().isEmpty()) {
            throw new TransformerException("Class name cannot be null or empty");
        }
        
        // Security: Prevent dangerous class loading
        if (isBlacklistedClass(className)) {
            throw new TransformerException("Class name not allowed for security reasons: " + className);
        }
        
        try {
            Class<?> clazz = Class.forName(className);
            
            // Security: Ensure class is safe to instantiate
            if (!isSafeClass(clazz)) {
                throw new TransformerException("Class not allowed for security reasons: " + className);
            }
            
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | 
                 NoSuchMethodException | java.lang.reflect.InvocationTargetException e) {
            throw new TransformerException(e);
        }
    }
    
    /**
     * Security: Check if class name is blacklisted
     */
    private boolean isBlacklistedClass(String className) {
        // Block dangerous classes that could be used for RCE
        String[] blacklistedPrefixes = {
            "java.lang.Runtime",
            "java.lang.ProcessBuilder",
            "java.lang.System",
            "java.lang.Class",
            "java.lang.Thread",
            "java.security.",
            "java.net.",
            "java.io.File",
            "java.io.FileInputStream",
            "java.io.FileOutputStream",
            "java.nio.file.",
            "javax.script.",
            "sun.",
            "com.sun.",
            "jdk.internal.",
            "org.springframework.context.",
            "org.apache.commons.beanutils.BeanUtils"
        };
        
        String lowerClassName = className.toLowerCase();
        for (String prefix : blacklistedPrefixes) {
            if (lowerClassName.startsWith(prefix.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Security: Check if class is safe to instantiate
     */
    private boolean isSafeClass(Class<?> clazz) {
        // Additional runtime checks
        if (clazz.isPrimitive() || clazz.isArray()) {
            return false;
        }
        
        // Check if class has dangerous interfaces or superclasses
        if (isAssignableFromDangerousClass(clazz)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Security: Check if class extends/implements dangerous classes
     */
    private boolean isAssignableFromDangerousClass(Class<?> clazz) {
        try {
            // Check for dangerous interfaces/superclasses
            if (java.lang.Runtime.class.isAssignableFrom(clazz) ||
                java.lang.ProcessBuilder.class.isAssignableFrom(clazz) ||
                java.lang.ClassLoader.class.isAssignableFrom(clazz) ||
                java.lang.Thread.class.isAssignableFrom(clazz)) {
                return true;
            }
        } catch (Exception e) {
            // If we can't check safely, assume it's dangerous
            return true;
        }
        
        return false;
    }

}
