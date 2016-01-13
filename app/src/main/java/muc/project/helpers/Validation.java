package muc.project.helpers;

import android.util.Patterns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by peterus on 21.10.2015.
 */
public class Validation {

    public static boolean isNotBlank(String str){
        return str != null && str.length() > 0;
    }

    public static boolean isPositiveInteger(String str){
        if (str != null && str.matches("^\\d+$")){
            int num = Integer.parseInt(str);
            return num > 0;
        }
        return false;
    }

    public static boolean isPositiveIntegerBetween(String str, int low, int high){
        if (isPositiveInteger(str)){
            int num = Integer.parseInt(str);
            return num >= low && num <= high;
        }
        return false;
    }

    public static boolean isCorrectLength(String str, int low, int high){
        return str != null && str.length() >= low && str.length() <= high;
    }

    public static boolean isEmailAddress(String str){
        return str != null && str.matches(Patterns.EMAIL_ADDRESS.toString());
    }

    public static boolean isMacValid(String mac) {
        Pattern p = Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
        Matcher m = p.matcher(mac);
        return m.find();
    }
}
