package one.skean.booksalesinfo.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CalendarTool {
	public static String parseCalendarToString(Calendar calendar) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.format(calendar.getTime()) ;
	}
}
