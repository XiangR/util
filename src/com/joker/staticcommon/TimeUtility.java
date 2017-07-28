package com.joker.staticcommon;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
}
