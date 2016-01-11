package muc.project.helpers;

import android.util.Patterns;

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
}
