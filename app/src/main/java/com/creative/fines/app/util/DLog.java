package com.creative.fines.app.util;

import android.util.Log;

public final class DLog {
	private static boolean DEBUG = true;

	public static int v(String tag, String msg) {
		if (DEBUG == false) {
			return 0;
		}
		return Log.v(tag, msg);
	}

	public static int v(String tag, String msg, Throwable tr) {
		if (DEBUG == false) {
			return 0;
		}
		return Log.v(tag, msg, tr);		
	}

	public static int e(String tag, String msg) {
		if (DEBUG == false) {
			return 0;
		}
		return Log.e(tag, msg);
	}

	public static int e(String tag, String msg, Throwable tr) {
		if (DEBUG == false) {
			return 0;
		}
		return Log.e(tag, msg, tr);		
	}	
	
	public static int w(String tag, String msg) {
		if (DEBUG == false) {
			return 0;
		}
		return Log.w(tag, msg);
	}

	public static int w(String tag, String msg, Throwable tr) {
		if (DEBUG == false) {
			return 0;
		}
		return Log.w(tag, msg, tr);		
	}	

	public static int i(String tag, String msg) {
		if (DEBUG == false) {
			return 0;
		}
		return Log.i(tag, msg);
	}

	public static int i(String tag, String msg, Throwable tr) {
		if (DEBUG == false) {
			return 0;
		}
		return Log.i(tag, msg, tr);		
	}	

	public static int d(String tag, String msg) {
		if (DEBUG == false) {
			return 0;
		}
		return Log.d(tag, msg);
	}

	public static int d(String tag, String msg, Throwable tr) {
		if (DEBUG == false) {
			return 0;
		}
		return Log.d(tag, msg, tr);		
	}

	//각종 메소드
	public static String now_date(){
		java.util.Calendar cal = java.util.Calendar.getInstance();
		//현재 년도, 월, 일
		int year = cal.get ( cal.YEAR );
		String now = year + "";
		int month = cal.get ( cal.MONTH ) + 1 ;
		if(month<10){
			now+= "0"+month;
		}else{
			now+= month;
		}
		int date = cal.get ( cal.DATE ) ;
		if(date<10){
			now+= "0"+date;
		}else{
			now+= date;
		}
		return now;
	}

}
