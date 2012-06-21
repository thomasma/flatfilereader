package org.aver.fft.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.aver.fft.Transformer;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Transform {
	Transformer.ColumnSeparator columnSeparatorType() default Transformer.ColumnSeparator.CHARACTER;

	String columnSeparator() default " ";

	String beanCreator() default "org.aver.fft.DefaultBeanCreator";

	String spaceEscapeCharacter() default "";

	int recordIdStartColumn() default 1;

	int recordIdEndColumn() default 1;
	
	boolean skipFirstLine() default false;

}