package ru.badr.vkfeed.utils;

import java.text.SimpleDateFormat;

/**
 * User: ABadretdinov
 * Date: 29.05.14
 * Time: 14:49
 */
public class DateUtils{

	protected static final String DATETIME_FORMAT = "HH:mm dd.MM.yyyy";

	public static SimpleDateFormat getDateTimeFormat() {
		return new SimpleDateFormat(DATETIME_FORMAT);
	}
}
