package jp.co.zynas.work_schedule.sp.android;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.webkit.CookieManager;

import java.util.List;

public class LoginTask extends AsyncTask<String, Void, String> {

    public static final String SUCCESS = "SUCCESS";
    public static final String TOKEN_ERROR = "TOKEN_ERROR";
    public static final String LOGIN_ERROR = "LOGIN_ERROR";
    public static final String INFO_ERROR = "INFO_ERROR";
    public static final String REDIRECT_ERROR = "REDIRECT_ERROR";
    public static final String REGISTPUSH_ERROR = "REGISTPUSH_ERROR";

    private final OnLoginListener loginListener;
    private final Context context;

    public LoginTask(Context context, OnLoginListener loginListener) {
        this.context = context;
        this.loginListener = loginListener;
    }

    @Override
    protected String doInBackground(String... strings) {
        String userId = strings[0];
        String password = strings[1];
        String uId = strings[2];
        String modelName = strings[3];
        String pushToken = strings[4];
        String token = ConnectionHelper.getCsrfToken("");
        if (TextUtils.isEmpty(token)) {
            return TOKEN_ERROR;
        }

        Pair<String, List<String>> loginResponse = ConnectionHelper.login(token, userId, password);
        if (loginResponse == null || loginResponse.first == null || loginResponse.second == null) {
            return LOGIN_ERROR;
        }

        String session = null;
        for (String _cookie : loginResponse.second) {
            String[] tokens = TextUtils.split(_cookie, "=");
            if (tokens[0].equals("JSESSIONID")) {
                session = _cookie;
            }
        }

        Boolean redirectResult = ConnectionHelper.redirect(loginResponse.first, loginResponse.second);
        if (!redirectResult) {
            return REDIRECT_ERROR;
        }
        CookieManager cookieManager = CookieManager.getInstance();
        String redirectCookie = cookieManager.getCookie(BuildConfig.URL_API_BASE + BuildConfig.URL_API_CONTEXT_ROOT + "/");

        LoginInfo loginInfo = ConnectionHelper.getLoginInfo();
        if(loginInfo==null){
            return INFO_ERROR;
        }

        String staffCd = loginInfo.getStaffCd();
        String staffName = loginInfo.getStaffName();
        Boolean registPush = ConnectionHelper.registDevice(session, uId, staffCd, modelName, pushToken);
        if (!registPush) {
            return REGISTPUSH_ERROR;
        }

        cookieManager.setAcceptCookie(true);
        cookieManager.removeAllCookies(null);
        cookieManager.setCookie(BuildConfig.URL_API_BASE + BuildConfig.URL_API_CONTEXT_ROOT + "/", redirectCookie);
        cookieManager.flush();

        DataHelper.saveAccountInfo(this.context, userId, password, staffCd, staffName);
        DataHelper.setLoggedIn(this.context, DataHelper.LOGGED_IN);

        return SUCCESS;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (loginListener != null) {
            if (s.equalsIgnoreCase(SUCCESS)) {
                loginListener.onLoginSuccess();
            } else {
                loginListener.onLoginFailure(s);
            }
        }
    }

    public interface OnLoginListener {
        void onLoginSuccess();

        void onLoginFailure(String message);
    }
}
