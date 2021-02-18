package jp.co.zynas.work_schedule.sp.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DataHelper {

    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
    private static final String PREF_USER_ID = "PREF_USER_ID";
    private static final String PREF_USER_PASSWORD = "PREF_USER_PASSWORD";
    private static final String PREF_STAFF_CD = "PREF_STAFF_CD";
    private static final String PREF_STAFF_NAME = "PREF_STAFF_NAME";
    private static final String PREF_LOGGED_IN = "PREF_LOGGED_IN";
    private static final String PREFERENCES_PASS_KEY = "joyfull-pass1234";
    private static final String PREFERENCES_FILE_NAME = "preference";

    public static final int LOGGED_IN = 1;
    public static final int NOTLOGGED_IN = 0;

    private static SharedPreferences getAppSharedPreferences(Context context) {
        return context.getSharedPreferences(
                PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
    }

    public synchronized static String getUUID(Context context) {
        SharedPreferences sharedPrefs = getAppSharedPreferences(context);

        String uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
        if (uniqueID == null) {
            uniqueID = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(PREF_UNIQUE_ID, uniqueID);
            editor.commit();
        }

        return uniqueID;
    }

    public synchronized static void saveAccountInfo(Context context, String loginid, String password, String staffCd, String staffName) {
        setUserPasswordLogin(context, password);
        setUserLogin(context, loginid, staffCd, staffName);
    }

    public synchronized static void setUserPasswordLogin(Context context, String password) {
        try {
            // 設定データファイルを読み込み
            SharedPreferences sharedPrefs = getAppSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPrefs.edit();

            // パスワードは暗号化
            SecretKeySpec keySpec = new SecretKeySpec(PREFERENCES_PASS_KEY.getBytes(), "AES"); // キーファイル生成
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(password.getBytes()); // byte配列を暗号化
            String up = Base64.encodeToString(encrypted, Base64.DEFAULT); // Stringにエンコード

            // 入力されたログインIDとログインパスワード
            editor.putString(PREF_USER_PASSWORD, up);

            // 保存
            editor.apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static void setUserLogin(Context context, String userId, String staffCd, String staffName) {
        SharedPreferences sharedPrefs = getAppSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(PREF_USER_ID, userId);
        editor.putString(PREF_STAFF_CD, staffCd);
        editor.putString(PREF_STAFF_NAME, staffName);
        editor.apply();
    }

    public synchronized static String getUserLogin(Context context) {
        SharedPreferences sharedPrefs = getAppSharedPreferences(context);
        return sharedPrefs.getString(PREF_USER_ID, null);
    }

    public synchronized static String getPassword(Context context) {
        SharedPreferences sharedPrefs = getAppSharedPreferences(context);
        String encryptPass = sharedPrefs.getString(PREF_USER_PASSWORD, null);
        if (!TextUtils.isEmpty(encryptPass))
            try {
                final SecretKeySpec key = new SecretKeySpec(PREFERENCES_PASS_KEY.getBytes(), "AES");

                byte[] decodedCipherText = Base64.decode(encryptPass, Base64.NO_WRAP);

                byte[] decryptedBytes = decrypt(key, decodedCipherText);

                String message = new String(decryptedBytes);

                return message;
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        return null;
    }

    public static byte[] decrypt(final SecretKeySpec key, final byte[] decodedCipherText)
            throws GeneralSecurityException {
        final Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(decodedCipherText);
        return decryptedBytes;
    }

    public synchronized static String getStaffCd(Context context) {
        SharedPreferences sharedPrefs = getAppSharedPreferences(context);
        return sharedPrefs.getString(PREF_STAFF_CD, null);
    }

    public synchronized static String getStaffName(Context context) {
        SharedPreferences sharedPrefs = getAppSharedPreferences(context);
        Log.d("debug", "getStaffName=" + sharedPrefs.getString(PREF_STAFF_NAME, null));
        return sharedPrefs.getString(PREF_STAFF_NAME, null);
    }

    public synchronized static String getFireBaseToken() {
        return FirebaseInstanceId.getInstance().getToken();
    }

    public synchronized static String getModelName() {
        return Build.MODEL;
    }

    public synchronized static void setLoggedIn(Context context, int loggedIn) {
        SharedPreferences sharedPrefs = getAppSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(PREF_LOGGED_IN, loggedIn);
        editor.apply();
    }

    public synchronized static Boolean isLoggedIn(Context context) {
        SharedPreferences sharedPrefs = getAppSharedPreferences(context);
        return sharedPrefs.getInt(PREF_LOGGED_IN, NOTLOGGED_IN) == LOGGED_IN;
    }

    public static void clearUserLoggedIn(Context context) {
        setLoggedIn(context, NOTLOGGED_IN);
        saveAccountInfo(context, null, "", "", "");
    }
}
