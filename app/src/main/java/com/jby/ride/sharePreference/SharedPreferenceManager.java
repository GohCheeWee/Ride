package com.jby.ride.sharePreference;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by wypan on 2/24/2017.
 */

public class SharedPreferenceManager {


    private static String LanguageId = "language_id";
    private static String UserID = "uid";
    private static String UserName = "userName";
    private static String DeviceToken = "deviceToken";
    private static String MatchRideID = "matchRideID";
    private static String UserProfilePic = "userProfilePic";

    private static SharedPreferences getSharedPreferences(Context context) {
        String SharedPreferenceFileName = "MJTaiwanUserLoginSessionDetail";
        return context.getSharedPreferences(SharedPreferenceFileName, Context.MODE_PRIVATE);
    }

    public static void clear(Context context){
        getSharedPreferences(context).edit().clear().apply();
    }

    /*
    *       User Shared Preference
    * */

    public static String getLanguageId(Context context) {
        return getSharedPreferences(context).getString(LanguageId, "default");
    }

    public static void setLanguageId(Context context, String languageId) {
        getSharedPreferences(context).edit().putString(LanguageId, languageId).apply();
    }

    public static String getUserID(Context context) {
        return getSharedPreferences(context).getString(UserID, "default");
    }

    public static void setUserID(Context context, String userID) {
        getSharedPreferences(context).edit().putString(UserID, userID).apply();
    }

    public static String getUserName(Context context) {
        return getSharedPreferences(context).getString(UserName, "default");
    }

    public static void setUserName(Context context, String userID) {
        getSharedPreferences(context).edit().putString(UserName, userID).apply();
    }

    public static String getDeviceToken(Context context) {
        return getSharedPreferences(context).getString(DeviceToken, "default");
    }

    public static void setDeviceToken(Context context, String deviceToken) {
        getSharedPreferences(context).edit().putString(DeviceToken, deviceToken).apply();
    }

    public static String getMatchRideID(Context context) {
        return getSharedPreferences(context).getString(MatchRideID, "default");
    }

    public static void setMatchRideID(Context context, String matchRideID) {
        getSharedPreferences(context).edit().putString(MatchRideID, matchRideID).apply();
    }

    public static String getUserProfilePic(Context context) {
        return getSharedPreferences(context).getString(UserProfilePic, "default");
    }

    public static void setUserProfilePic(Context context, String userProfilePic) {
        getSharedPreferences(context).edit().putString(UserProfilePic, userProfilePic).apply();
    }

}
