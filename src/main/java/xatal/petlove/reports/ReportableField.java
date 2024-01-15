package xatal.petlove.reports;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReportableField {
	String headerName();

	String getValueFrom() default "";

	boolean isDate() default false;
}
