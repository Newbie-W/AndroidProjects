package com.demo.demo.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/*
* 时间相关处理
* 1.毫秒转秒
* 2.十进制转16进制
* 3.当前时间往后推一天（返回长整型）
* 4.长整型时间->常见的时间格式
* 5.常见的时间格式->长整型时间
* */
public class TimeUtil {
    // 两种时间格式
    public static String DATE_FORMAT = "yyyy-MM-dd";
    public static String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);     //当前只处理"yyyy-MM-dd HH:mm:ss"格式相关转换的时间
    private static String TAG = "demo.TimeUtil";

    public static void test() {
        Date nowDate = new Date();
        long nowTime = nowDate.getTime();   //单位为ms的长整型
        String timeWithFormat = longToString(nowTime);

        Log.d(TAG, "second:"+msToSecond(nowTime)+"\nhex: "+decimalToHex(nowTime)+"\ntomorrowTime: "+tomorrowTime(nowTime)+"\ntimeWithFormat:"+timeWithFormat);
        try {
            Log.d(TAG, "\nlongIntTime:"+stringToLong(timeWithFormat));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static long msToSecond(long time) {
        return time / 1000;
    }

    public static String decimalToHex(long time) {
        return Long.toHexString(time).toUpperCase();
    }

    public static long tomorrowTime(long time) {
        // 也可使用Calendar类计算明日时间，此处采用直接计算的方式
        long intervalTime = 24 * 60 * 60 * 1000L; //一天间隔（单位ms）
        return time+intervalTime;
    }

    public static long stringToLong(String time) throws ParseException {
        return dateFormat.parse(time).getTime();
    }

    public static String longToString(Long time) {
        return dateFormat.format(time);
    }
}
