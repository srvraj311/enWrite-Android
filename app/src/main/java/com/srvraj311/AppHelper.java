package com.srvraj311;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;
public class AppHelper {
    static SharedPreferences prefs;

    public static AppHelper getInstance() {
        return new AppHelper();
    }

    public static String getDevicedetails() {
        return "VERSION.RELEASE : " + Build.VERSION.RELEASE
                + "\nVERSION.INCREMENTAL : " + Build.VERSION.INCREMENTAL
                + "\nVERSION.SDK.NUMBER : " + Build.VERSION.SDK_INT
                + "\nBOARD : " + Build.BOARD
                + "\nBOOTLOADER : " + Build.BOOTLOADER
                + "\nBRAND : " + Build.BRAND
                + "\nDISPLAY : " + Build.DISPLAY
                + "\nHARDWARE : " + Build.HARDWARE
                + "\nHOST : " + Build.HOST
                + "\nMANUFACTURER : " + Build.MANUFACTURER
                + "\nMODEL : " + Build.MODEL
                + "\nPRODUCT : " + Build.PRODUCT
                + "\nSERIAL : " + Build.SERIAL
                + "\nTAGS : " + Build.TAGS
                + "\nTIME : " + Build.TIME
                + "\nTYPE : " + Build.TYPE
                + "\nUSER : " + Build.USER;
    }


    /**
     * this method checks the internet connectivity of the application
     */
    public static boolean isOnline(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    /**
     * This method auto Capatalizer the name of the user
     */

    public static String capatalizeName(String name) {
        try {
            if (!TextUtils.isEmpty(name)) {
                name = name.trim();
                StringBuilder sb = new StringBuilder();
                String[] arr = name.split(" ");
                for (int i = 0; i < arr.length; i++) {
                    sb.append(arr[i].substring(0, 1).toUpperCase());
                    sb.append(arr[i].substring(1));
                    if (arr.length >= 2) {
                        sb.append(" ");
                    }
                }
                return sb.toString();
            }
            return name;
        } catch (Exception e) {
            return name;
        }
    }

    public static Object getProperty(String key, Context context) throws IOException {
        Properties properties = new Properties();
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open("config.properties");
        properties.load(inputStream);
        return properties.getProperty(key);
    }

    public static Object getProperty(String fileName, String key, Context context) throws IOException {
        Properties properties = new Properties();
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(fileName);
        properties.load(inputStream);
        return properties.getProperty(key);
    }

    public static String prepareDateFormat(String formattedDate) {
        if (TextUtils.isEmpty(formattedDate)) {
            return "N/A";
        }
        if (formattedDate.equals("N/A")) {
            return formattedDate;
        }
        DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
        fromFormat.setLenient(false);
        SimpleDateFormat toFormat = new SimpleDateFormat("MMM dd, yyyy ");
        toFormat.setLenient(false);

        Date date = null;
        try {

            date = fromFormat.parse(formattedDate);

        } catch (ParseException e) {
            date = new Date();
        }
        formattedDate = toFormat.format(date);
        return formattedDate;
    }

    public static String prepareDateFormatWithoutYear(String formattedDate) {
        if (TextUtils.isEmpty(formattedDate)) {
            return "N/A";
        }
        DateFormat fromFormat = new SimpleDateFormat("MM-dd");
        fromFormat.setLenient(false);
        SimpleDateFormat toFormat = new SimpleDateFormat("MMM dd");
        toFormat.setLenient(false);

        Date date = null;
        try {

            date = fromFormat.parse(formattedDate);

        } catch (ParseException e) {
            date = new Date();
        }
        formattedDate = toFormat.format(date);
        return formattedDate;
    }

    public static Date getDateFormat(String formattedDate) {

        if (TextUtils.isEmpty(formattedDate)) {
            return null;
        }
        DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
        fromFormat.setLenient(false);
        SimpleDateFormat toFormat = new SimpleDateFormat("MMM dd, yyyy ");
        toFormat.setLenient(false);
        Date date = null;
        try {
            date = fromFormat.parse(formattedDate);
            return date;

        } catch (ParseException e) {
            return null;
        }
    }

    public static String covertTimeToText(String prefix, String suffix, String dataDate) {
        String convTime = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date pasTime = dateFormat.parse(dataDate);

            Date time = new Date();
            String now = dateFormat.format(time);
            Date date = dateFormat.parse(now);

            long dateDiff = date.getTime() - pasTime.getTime();

            long second = TimeUnit.MILLISECONDS.toSeconds(dateDiff);
            long minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff);
            long hour = TimeUnit.MILLISECONDS.toHours(dateDiff);
            long day = TimeUnit.MILLISECONDS.toDays(dateDiff);

            if (second < 60) {
                if (second == 1)
                    convTime = prefix.concat(" ").concat(String.valueOf(second)).concat(" Second ").concat(suffix);
                else
                    convTime = prefix.concat(" ").concat(String.valueOf(second)).concat(" Seconds ").concat(suffix);
            } else if (minute < 60) {
                if (minute == 1)
                    convTime = prefix.concat(" ").concat(String.valueOf(minute)).concat(" Minute ").concat(suffix);
                else
                    convTime = prefix.concat(" ").concat(String.valueOf(minute)).concat(" Minutes ").concat(suffix);
            } else if (hour < 24) {
                if (hour == 1)
                    convTime = prefix.concat(" ").concat(String.valueOf(hour)).concat(" Hour ").concat(suffix);
                else
                    convTime = prefix.concat(" ").concat(String.valueOf(hour)).concat(" Hours ").concat(suffix);
            } else if (day >= 7) {
                if (day > 360) {
                    if ((day / 360) == 1)
                        convTime = prefix.concat(" ").concat(String.valueOf(day / 360)).concat(" Year ").concat(suffix);
                    else
                        convTime = prefix.concat(" ").concat(String.valueOf(day / 360)).concat(" Years ").concat(suffix);
                } else if (day > 30) {
                    if ((day / 30) == 1)
                        convTime = prefix.concat(" ").concat(String.valueOf(day / 30)).concat(" Month ").concat(suffix);
                    else
                        convTime = prefix.concat(" ").concat(String.valueOf(day / 30)).concat(" Months ").concat(suffix);
                } else {
                    if ((day / 7) == 1)
                        convTime = prefix.concat(" ").concat(String.valueOf(day / 7)).concat(" Week ").concat(suffix);
                    else
                        convTime = prefix.concat(" ").concat(String.valueOf(day / 7)).concat(" Weeks ").concat(suffix);
                }
            } else if (day < 7) {
                if (day == 1)
                    convTime = prefix.concat(" ").concat(String.valueOf(day)).concat(" Day ").concat(suffix);
                else
                    convTime = prefix.concat(" ").concat(String.valueOf(day)).concat(" Days ").concat(suffix);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("ConvTimeError: ", e.getMessage());
        }
        return convTime;
    }


    public static String prepareTimeFormat(String formattedTime) {
        Date date;
        if (formattedTime != null && formattedTime.length() != 0) {
            try {
                DateFormat fromFormat = new SimpleDateFormat("hh:mm:ss", new Locale("en-us"));
                date = fromFormat.parse(formattedTime);

                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", new Locale("en-us"));
                formattedTime = sdf.format(date);
                return formattedTime;
            } catch (ParseException e) {
                return "N/A";
            }
        } else {
            return "N/A";
        }
    }

    public static String convertIntoDoubleDecimal(String s) {
        NumberFormat formatter = new DecimalFormat("0.00");
        return formatter.format(Double.valueOf(s));
    }

    public static String checkForNull(String text) {
        try {
            if (text.equalsIgnoreCase("null")) {
                return "N/A";
            } else if (text == null) {
                return "N/A";
            } else if (text.equalsIgnoreCase("")) {
                return "N/A";
            } else if (TextUtils.isEmpty(text)) {
                return "N/A";
            } else {
                return text;
            }
        } catch (Exception e) {
            return text;
        }
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static long getTimeStampFromDate(String date) {

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date parsedDate = dateFormat.parse(date);
            Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
            return timestamp.getTime();
        } catch (Exception e) { //this generic but you can control another types of exception
            // look the origin of excption
            return System.currentTimeMillis();
        }
    }

    public static long getCurrentTimeStamp() {
        return System.currentTimeMillis();
    }


    public Object getValueFromCache(String key, Class<?> type, Context context) {
        try {
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (type == String.class) {
                return prefs.getString(key, "");
            }
            if (type == Boolean.class) {
                return prefs.getBoolean(key, false);
            }
            if (type == Integer.class) {
                return prefs.getInt(key, 0);
            }
            if (type == Float.class) {
                return prefs.getFloat(key, 0.0f);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }

    public void setValueInCache(String key, Object value, Class<?> type, Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        try {
            if (type == String.class) {
                editor.putString(key, (String) value);
            }
            if (type == Boolean.class) {
                editor.putBoolean(key, (boolean) value);
            }
            if (type == Integer.class) {
                editor.putInt(key, (Integer) value);
            }
            if (type == Float.class) {
                editor.putFloat(key, (Float) value);
            }
        } catch (Exception e) {
            editor.putString(key, value + "");
        }
        editor.apply();
        editor.commit();
    }


    public void callPhone(Context context, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        context.startActivity(intent);
    }

    public void callPhoneOutside(Context context, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        context.startActivity(intent);
    }
}