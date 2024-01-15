package xatal.petlove.reports;

public @interface ReportableList {
	String headerName();

	String getValueFrom();

	boolean isDate() default false;
}
