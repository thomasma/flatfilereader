package org.aver.fft.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Column {
	boolean required() default false;

	String format() default "";

	int position();

	boolean skip() default false;

	int start() default 0;

	int end() default 0;
}
