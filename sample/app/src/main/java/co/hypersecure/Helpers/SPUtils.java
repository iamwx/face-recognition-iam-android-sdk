package co.hypersecure.Helpers;

import android.content.Context;
import android.content.SharedPreferences;

import co.hypersecure.HyperSecureApp;

/**
 * Created by Awanish Raj on 04/01/16.
 */
public class SPUtils {

    public static final String spSilver = "SilverSP";
    private static SharedPreferences sp = null;
    private static SharedPreferences.Editor editor = null;

    private static SharedPreferences getSP() {
        if (sp == null) {
            init(HyperSecureApp.getInstance());
        }
        return sp;
    }

    private static SharedPreferences.Editor getEditor() {
        if (editor == null) {
            editor = getSP().edit();
        }
        return editor;
    }

    public static void init(Context context) {
        if (context != null) {
            sp = context.getSharedPreferences(spSilver, Context.MODE_PRIVATE);
            editor = sp.edit();
        }
    }

    public static void saveUserDetails(String name, String number) {
        getEditor().putString("userName", name);
        getEditor().putString("userNumber", number);
        getEditor().commit();
    }

    public static void saveUserLogin(String token) {
        getEditor().putString("token", token);
        getEditor().putBoolean("userLogged", true);
        getEditor().commit();
    }

    public static boolean isUserRegistered() {
        return !getSP().getString("token", "nulltoken").equals("nulltoken");
    }

    public static void setUserLoggedIn() {
        getEditor().putBoolean("userLogged", true);
        getEditor().commit();
    }
    public static boolean isUserLoggedIn() {
        return getSP().getBoolean("userLogged", false);
    }

    public static String getToken() {
        return getSP().getString("token", "nan");
    }

    public static String getNumber() {
        return getSP().getString("userNumber", "nan");
    }

    public static void saveGCMToken(String token) {
        getEditor().putString("gcmToken", token);
        getEditor().commit();
    }

    public static String getGCMToken() {
        return getSP().getString("gcmToken", "nan");
    }

    public static String getName() {
        return getSP().getString("userName", "nan");
    }

    public static boolean isFaceRegistered() {
        return getSP().getBoolean("isFaceRegistered", false);
    }

    public static void setFaceRegistered() {
        getEditor().putBoolean("isFaceRegistered", true);
        getEditor().commit();
    }
}
