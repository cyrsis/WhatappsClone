package com.sourcecanyon.whatsClone.helpers;

import android.content.Context;
import android.text.format.Time;

import com.sourcecanyon.whatsClone.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Abderrahim El imame on 6/20/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class UtilsTime {

    /**
     * method to convert String to Date
     *
     * @param mDate this is the  parameter for convertStringToDate
     * @return date string
     */
    public static Date convertStringToDate(String mDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        Date date = null;
        try {
            date = sdf.parse(mDate);
        } catch (ParseException ex) {
            AppHelper.LogCat(ex);
        }
        return date;
    }

    /**
     * method to convert Date to String
     *
     * @param context this is the first parameter for convertDateToString
     * @param date    convertDateToString
     * @return date string
     */
    public static String convertDateToString(Context context, Date date) {

        /*
        String dayOfTheWeek = (String) android.text.format.DateFormat.format("EEEE", date);//Thursday
        String stringMonth = (String) android.text.format.DateFormat.format("MMM", date); //Jun
        String year = (String) android.text.format.DateFormat.format("yyyy", date); //2016*/
        //time that will convert
        String intMonth = (String) android.text.format.DateFormat.format("MM", date); //06
        String day = (String) android.text.format.DateFormat.format("dd", date); //29
        int time_dd = Integer.parseInt(day);
        int time_MM = Integer.parseInt(intMonth);

        //Current time
        Calendar now = Calendar.getInstance();
        String nowMonth = (String) android.text.format.DateFormat.format("MM", now); //06
        String nowDay = (String) android.text.format.DateFormat.format("dd", now); //29

        int c_dd = Integer.parseInt(nowDay);
        int c_MM = Integer.parseInt(nowMonth);
        if (time_MM == c_MM) {
            if (time_dd == c_dd)
                return reformatCurrentDate(date, context.getResources().getString(R.string.date_format_today));
            else if (time_dd == c_dd - 1)
                return context.getResources().getString(R.string.date_format_yesterday);
            else
                return reformatCurrentDate(date, context.getResources().getString(R.string.date_format));
        }
        return reformatCurrentDate(date, context.getResources().getString(R.string.date_format));
    }

    /**
     * method to reformat the format of date
     *
     * @param mDate  this is the first parameter for reformatCurrentDate
     * @param format this is the second parameter for reformatCurrentDate
     * @return date string
     */
    private static String reformatCurrentDate(Date mDate, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        String date_to_string = formatter.format(mDate);
        return date_to_string;
    }

    /**
     * method to get current time and reformat
     *
     * @param context this is the first parameter for getCurrentTime
     * @param date    this is the second parameter for getCurrentTime
     * @return date string
     */
    public static String getCurrentTime(Context context, Time date) {
        int time_dd = date.monthDay;
        int time_MM = date.month;
        Time now = new Time();
        now.setToNow();
        int c_dd = now.monthDay;
        int c_MM = now.month;
        if (time_MM == c_MM) {
            if (time_dd == c_dd)
                return date.format(context.getResources().getString(R.string.date_format_today));
            else
                return date.format(context.getResources().getString(R.string.date_format_current_month));
        }
        return date.format(context.getResources().getString(R.string.date_format));
    }

    /**
     * method to get time of file  (video/audio)
     *
     * @param milliSeconds this is the first parameter for getFileTime
     * @param dateFormat   this is the second parameter for getFileTime
     * @return
     */
   /* public static String getFileTime(long milliSeconds, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }*/


    /**
     * Function to convert milliseconds time to
     * Timer Format
     */
    public static String getFileTime(long milliseconds) {
        String TimerString = "";
        String secondsString;

        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        if (hours > 0) {
            TimerString = hours + ":";
        }
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        TimerString = TimerString + minutes + ":" + secondsString;

        return TimerString;
    }

    /**
     * Function to get Progress percentage
     *
     * @param currentDuration this is the first parameter for  getProgressPercentage
     * @param totalDuration   this is the second parameter for  getProgressPercentage
     */
    public static int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage;
        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);
        percentage = (((double) currentSeconds) / totalSeconds) * 100;
        return percentage.intValue();
    }

    /**
     * Function to change progress to timer
     *
     * @param progress      this is the first parameter for  progressToTimer
     * @param totalDuration returns current duration in milliseconds
     */
    public static int progressToTimer(int progress, int totalDuration) {
        int currentDuration ;
        totalDuration = (int) totalDuration / 1000;
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);
        return currentDuration * 1000;
    }
}
