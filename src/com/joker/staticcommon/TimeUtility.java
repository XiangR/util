package com.joker.staticcommon;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * 
 * @author xiangR
 * @date 2017年7月28日上午9:47:55
 *
 */
public class TimeUtility {

	/** 年月日时分秒(无下划线) yyyyMMddHHmmss */
	public static final String dtLong = "yyyyMMddHHmmss";

	/** 完整时间 yyyy-MM-dd HH:mm:ss */
	public static final String simple = "yyyy-MM-dd HH:mm:ss";

	/** 完整时间 yyyy-MM-dd HH:mm:ss SSS */
	public static final String simpleMore = "yyyy-MM-dd HH:mm:ss SSS";

	/** 年月日 yyyy-MM-dd */
	public static final String dtShort = "yyyy-MM-dd";

	public static String getDiffTime(Date date) {
		String shortstring = null;
		if (date == null)
			return shortstring;
		long deltime = (System.currentTimeMillis() - date.getTime()) / 1000;
		if (deltime > 365 * 24 * 60 * 60) {
			shortstring = (int) (deltime / (365 * 24 * 60 * 60)) + "年前";
			return shortstring;
		} else if (deltime > 30 * 24 * 60 * 60) {
			shortstring = (int) (deltime / (30 * 24 * 60 * 60)) + "月前";
			return shortstring;
		} else if (deltime > 24 * 60 * 60) {
			shortstring = (int) (deltime / (24 * 60 * 60)) + "天前";
			return shortstring;
		} else if (deltime > 60 * 60) {
			shortstring = (int) (deltime / (60 * 60)) + "小时前";
			return shortstring;
		} else if (deltime > 60) {
			shortstring = (int) (deltime / (60)) + "分钟前";
			return shortstring;
		} else if (deltime > 1) {
			shortstring = deltime + "秒前";
			return shortstring;
		} else {
			shortstring = "1秒前";
			return shortstring;
		}
	}

	public static boolean isFirstTimeOfday(Date date) {
		if (date != null) {
			long deltime = (System.currentTimeMillis() - date.getTime()) / 1000;
			if (deltime > 24 * 60 * 60) {
				return true;
			}
		}
		return false;
	}

	public static boolean islatestMonth(Date date) {
		if (date != null) {
			long deltime = (System.currentTimeMillis() - date.getTime()) / 1000;
			if (deltime < 30 * 24 * 60 * 60) {
				return true;
			}
		}
		return false;
	}

	public static String getWeekFirstDay(int diff) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.WEEK_OF_MONTH, diff);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(cal.getTime());
	}

	public static String getWeekLastDay(int diff) {
		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		cal.add(Calendar.WEEK_OF_MONTH, diff);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		System.out.println(sdf.format(cal.getTime()));
		return sdf.format(cal.getTime());
	}

	public static String getMonthFirstDay(int diff) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, diff);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		System.out.println(sdf.format(cal.getTime()));
		return sdf.format(cal.getTime());
	}

	public static String getMonthLastDay(int diff) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, diff);
		c.set(Calendar.DAY_OF_MONTH, getMaxDay(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		System.out.println(sdf.format(c.getTime()));
		return sdf.format(c.getTime());
	}

	public static String getSomeday(int index) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_MONTH, index);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(cal.getTime());
	}

	public static Date getSomeDate(int index) {
		return getSomeDate(null, index);
	}

	public static Date getSomeDate(Date from, int index) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(from == null ? new Date() : from);
		cal.add(Calendar.DAY_OF_MONTH, index);
		return cal.getTime();
	}

	public static Date getSomeMonthDate(Date from, int index) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(from == null ? new Date() : from);
		cal.add(Calendar.MONTH, index);
		return cal.getTime();
	}

	public static Date getSomeMinute(Date from, int index) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(from == null ? new Date() : from);
		cal.add(Calendar.MINUTE, index);
		return cal.getTime();
	}

	public static Date getDayOfWeek(Date from, int weekDiff, int day) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(from);
		cal.add(Calendar.WEEK_OF_MONTH, weekDiff);
		cal.set(Calendar.DAY_OF_WEEK, day);
		return cal.getTime();
	}

	public static String toString(String format, Date date) {
		if (date != null) {
			SimpleDateFormat fmt = new SimpleDateFormat(format);
			return fmt.format(date);
		}
		return null;
	}

	public static String toString(Date date) {
		return toString(simple, date);
	}

	public static String toStringMore(Date date) {
		return toString(simpleMore, date);
	}

	public static Date toDate(String format, String date) {
		if (!StringUtility.isNullOrEmpty(date)) {
			SimpleDateFormat fmt = new SimpleDateFormat(format);
			try {
				return fmt.parse(date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static int getMaxDay(int year, int month) {
		if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
			return 31;
		}
		if (month == 2) {
			if (year % 400 == 0 || (year % 4 == 0 && year % 100 != 0)) {
				return 29;
			}
			return 28;
		}
		return 30;
	}

	public static int daysBetween(Date dateFrom, Date dateTo) {
		int dayMills = 1000 * 3600 * 24;
		long time1 = dateFrom.getTime() / dayMills;
		long time2 = dateTo.getTime() / dayMills;
		return (int) (time2 - time1);
	}

	public static Date getDayBegin(Date date) {
		int dayMills = 1000 * 3600 * 24;
		long time = date.getTime();
		time = time - time % dayMills - TimeZone.getDefault().getRawOffset();
		return new Date(time);
	}

	/**
	 * 得到两个时间的时间间隔
	 * 
	 * @param dateMin
	 * @param dateMax
	 * @return
	 */
	public static String getDistanceTime(Date dateMin, Date dateMax) {
		StringBuffer result = new StringBuffer();
		long time1 = dateMin.getTime();
		long time2 = dateMax.getTime();
		long diff;
		if (time1 < time2) {
			diff = time2 - time1;
		} else {
			diff = time1 - time2;
		}
		long day = diff / (24 * 60 * 60 * 1000);
		long hour = (diff / (60 * 60 * 1000) - day * 24);
		long min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
		long sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
		if (day > 0) {
			result.append(day + "天");
		}
		if (hour > 0) {
			result.append(hour + "小时");
		}
		if (min > 0) {
			result.append(min + "分钟");
		}
		if (sec > 0) {
			result.append(sec + "秒");
		}
		return result.toString();
	}

	public static Date DateTimeToDate(LocalDateTime localDateTime) {
		ZoneId zone = ZoneId.systemDefault();
		Instant instant = localDateTime == null ? LocalDateTime.now().atZone(zone).toInstant() : localDateTime.atZone(zone).toInstant();
		Date date = Date.from(instant);
		return date;
	}

	public static LocalDateTime DateToDateTime(Date date) {
		ZoneId zone = ZoneId.systemDefault();
		Instant instant = date == null ? new Date().toInstant() : date.toInstant();
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
		return localDateTime;
	}
}
