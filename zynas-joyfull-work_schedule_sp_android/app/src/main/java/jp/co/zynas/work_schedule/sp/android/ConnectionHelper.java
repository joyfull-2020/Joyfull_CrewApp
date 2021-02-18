package jp.co.zynas.work_schedule.sp.android;

import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.webkit.CookieManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class ConnectionHelper {

    private static final String COOKIES_HEADER = "Set-Cookie";

    private static final String TAG = "ConnectionHelper";
    private static final int READ_TIMEOUT = 10000;
    private static final int CONNECTION_TIMEOUT = 20000;


    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public static boolean registDevice(String session, String uId, String staffCode, String modelName, String pushToken) {

        String urlSt = BuildConfig.URL_API_BASE + BuildConfig.URL_API_CONTEXT_ROOT + "/com/regist_device";
        HttpURLConnection con = null;
        Boolean result = false;
        String csrfToken = getCsrfToken(session);
        try {
            // URL設定
            URL url = new URL(urlSt);
            // HttpURLConnection
            con = (HttpURLConnection) url.openConnection();
            // request POST
            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.setDoOutput(true);

            con.setReadTimeout(READ_TIMEOUT);
            con.setConnectTimeout(CONNECTION_TIMEOUT);
            if(TextUtils.isEmpty(session)){
                CookieManager cookieManager = CookieManager.getInstance();
                String preCookie = cookieManager.getCookie(BuildConfig.URL_API_BASE + BuildConfig.URL_API_CONTEXT_ROOT + "/");
                con.setRequestProperty("Cookie", preCookie);
            }
            else {
                con.setRequestProperty("Cookie", session);
            }

            con.setRequestProperty("x-csrf-token", csrfToken);

            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Accept", "application/json");


            JSONObject jsonParam = new JSONObject();
            jsonParam.put("uUID", uId);
            jsonParam.put("staffCode", staffCode);
            jsonParam.put("modelName", modelName);
            jsonParam.put("pushToken", pushToken);

            OutputStream out;
            BufferedWriter writer;

            out = con.getOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.write(jsonParam.toString());
            writer.flush();
            Log.d(TAG, "flush");
            // 接続
            con.connect();

            int status = con.getResponseCode();
            Log.i(TAG, "registDevice: " + status);
            if (status == HttpURLConnection.HTTP_OK) {
                result = true;
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            result = false;
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return result;
    }

    /**
     * パラメータ文字列を組み立てます。
     *
     * @param params
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String getQuery(ContentValues params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, Object> entry : params.valueSet()) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
        }

        Log.d("debug", "params=" + result.toString());

        return result.toString();
    }


    public static boolean redirect(String newUrl, List<String> cookieHeader) {
        // URL設定
        Boolean result = false;
        HttpURLConnection con = null;
        try {
            URL url = new URL(BuildConfig.URL_API_BASE + newUrl);
            con = (HttpURLConnection) url.openConnection();
            String cookie = null;
            for (String _cookie : cookieHeader) {
                Log.d(TAG, "redirect cookie=" + _cookie);
                String[] tokens = TextUtils.split(_cookie, "=");
                if (tokens[0].equals("JSESSIONID")) {
                    cookie = _cookie;
                    con.setRequestProperty("Cookie", cookie);
                }
            }
            // 接続
            con.connect();

            int status = con.getResponseCode();

            Log.d(TAG, "redirect status=" + status);

            if (status == HttpURLConnection.HTTP_OK) {
                // 認証OK
                result = true;
                // セッションIDをcookieに保存
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.setAcceptCookie(true);
                cookieManager.removeAllCookies(null);
                cookieManager.setCookie(BuildConfig.URL_API_BASE + BuildConfig.URL_API_CONTEXT_ROOT + "/", cookie);
                cookieManager.flush();

            } else {
                result = false;
            }
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }

        return result;
    }

    public static Pair<String, List<String>> login(String csrfToken, String loginid, String password) {

        String urlSt = BuildConfig.URL_API_BASE + BuildConfig.URL_API_CONTEXT_ROOT + "/j_spring_security_check";

        HttpURLConnection con = null;

        try {
            // URL設定
            URL url = new URL(urlSt);
            // HttpURLConnection
            con = (HttpURLConnection) url.openConnection();
            // request POST
            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.setDoOutput(true);

            con.setReadTimeout(READ_TIMEOUT);
            con.setConnectTimeout(CONNECTION_TIMEOUT);

            con.setInstanceFollowRedirects(false);

            CookieManager cookieManager = CookieManager.getInstance();
            String preCookie = cookieManager.getCookie(BuildConfig.URL_API_BASE + BuildConfig.URL_API_CONTEXT_ROOT + "/");
            Log.d(TAG, "get preCookie LOGIN=" + preCookie);
            Log.d(TAG, "get token=" + csrfToken);
            con.setRequestProperty("Cookie", preCookie);

            ContentValues params = new ContentValues();
            params.put("_csrf", csrfToken);
            params.put("j_username", loginid);
            params.put("j_password", password);

            // POSTデータ送信処理
            OutputStream out = null;
            BufferedWriter writer = null;
            try {
                out = con.getOutputStream();
                writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(getQuery(params));
                writer.flush();
                Log.d(TAG, "flush");

                // 接続
                con.connect();

            } catch (IOException e) {
                // POST送信エラー
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    writer.close();
                }
                if (out != null) {
                    out.close();
                }
            }

            int status = con.getResponseCode();
            Log.d(TAG, "status=" + status);

            if (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER) {

                // リダイレクト

                String newUrlString = con.getHeaderField("Location");

                Log.d(TAG, "redirect url=" + newUrlString);

                List<String> cookieHeader = con.getHeaderFields().get(COOKIES_HEADER);

                return new Pair<>(newUrlString, cookieHeader);

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return null;
    }

    public static String getCsrfToken(String session) {

        String urlSt = BuildConfig.URL_API_BASE + BuildConfig.URL_API_CONTEXT_ROOT + "/csrf";

        HttpURLConnection con = null;
        String csrfToken = null;

        try {
            // URL設定
            URL url = new URL(urlSt);
            // HttpURLConnection
            con = (HttpURLConnection) url.openConnection();
            // request POST
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.setDoOutput(false);

            con.setReadTimeout(READ_TIMEOUT);
            con.setConnectTimeout(CONNECTION_TIMEOUT);
            if(!TextUtils.isEmpty(session)) {
                con.setRequestProperty("Cookie", session);
            }

            // GETデータ送信処理
            try {
                // 接続
                con.connect();

            } catch (IOException e) {
                // GET送信エラー
                e.printStackTrace();
                return null;
            }

            final int status = con.getResponseCode();
            Log.d(TAG, "status=" + status);

            if (status == HttpURLConnection.HTTP_OK) {
                // レスポンスを受け取る処理
                InputStream stream = con.getInputStream();
                StringBuffer sb = new StringBuffer();
                String line = "";
                BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                try {
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                csrfToken = sb.toString();
                Log.d(TAG, "csrfToken=" + csrfToken);


                List<String> cookieHeader = con.getHeaderFields().get(COOKIES_HEADER);

                Log.d(TAG, "con.getHeaderFields()=" + con.getHeaderFields());

                CookieManager cookieManager = CookieManager.getInstance();
                // セッションIDをcookieに保存
                cookieManager.setAcceptCookie(true);
                cookieManager.removeAllCookies(null);
                if (cookieHeader != null) {
                    for (String cookie : cookieHeader) {
                        Log.d(TAG, "cookie=" + cookie);
                        String[] tokens = TextUtils.split(cookie, "=");
                        if (tokens[0].equals("JSESSIONID")) {
                            cookieManager.setCookie(BuildConfig.URL_API_BASE + BuildConfig.URL_API_CONTEXT_ROOT + "/", cookie);
                        }
                    }
                }
                cookieManager.flush();

            } else {
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }

        return csrfToken;
    }

    public static LoginInfo getLoginInfo() {

        String urlSt = BuildConfig.URL_API_BASE + BuildConfig.URL_API_CONTEXT_ROOT + "/com/login_info_json";

        HttpURLConnection con = null;
        String loginInfoJson = null;

        try {
            // URL設定
            URL url = new URL(urlSt);
            // HttpURLConnection
            con = (HttpURLConnection) url.openConnection();
            // request POST
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.setDoOutput(false);

            con.setReadTimeout(READ_TIMEOUT);
            con.setConnectTimeout(CONNECTION_TIMEOUT);

            CookieManager cookieManager = CookieManager.getInstance();
            String cookie = cookieManager.getCookie(BuildConfig.URL_API_BASE + BuildConfig.URL_API_CONTEXT_ROOT + "/");
            con.setRequestProperty("Cookie", cookie);

            // GETデータ送信処理
            try {
                // 接続
                con.connect();

            } catch (IOException e) {
                // GET送信エラー
                e.printStackTrace();
                return null;
            }

            final int status = con.getResponseCode();
            Log.d(TAG, "status=" + status);

            if (status == HttpURLConnection.HTTP_OK) {
                // レスポンスを受け取る処理
                InputStream stream = con.getInputStream();
                StringBuffer sb = new StringBuffer();
                String line = "";
                BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                try {
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                loginInfoJson = sb.toString();
                Log.d(TAG, "loginInfoJson=" + loginInfoJson);

            } else {
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }

        LoginInfo loginInfo = new LoginInfo();
        try {
            JSONObject json = new JSONObject(loginInfoJson);

            String staffCd = json.getString("staffCd");
            String staffName = json.getString("staffName");

            Log.d(TAG, "staffCd=" + staffCd + " staffName=" + staffName);

            loginInfo.setStaffCd(staffCd);
            loginInfo.setStaffName(staffName);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return loginInfo;
    }

    public static void downloadFile(String urlString, String session, String storePath, String fileName, IDownloadListener listener) {
        File file = new File(storePath);
        if (!file.exists()) {
            file.mkdirs();
        }

        File downloadFile = new File(storePath + File.separator + fileName);
        if (downloadFile.exists()) {
            downloadFile.delete();
        }

        BufferedInputStream input = null;
        FileOutputStream output = null;
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Cookie", session);
            connection.connect();
            int length = connection.getContentLength();
            if (listener != null)
                listener.onDownloadLength(length);

            input = new BufferedInputStream(connection.getInputStream());
            output = new FileOutputStream(storePath + File.separator + fileName);

            byte[] data = new byte[8192];
            long total = 0;
            int count;
            while (true) {
                count = input.read(data);
                if (count == -1) {
                    break;
                }
                total += count;
                output.write(data, 0, count);

                if (listener != null)
                    listener.onDownloadProgress(total);
            }
            output.flush();
            if (listener != null)
                listener.onDownloadSuccess(storePath + File.separator + fileName);

        } catch (Exception ex) {
            ex.printStackTrace();
            if (listener != null)
                listener.onDownloadFailure(ex);
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

    public interface IDownloadListener {
        void onDownloadLength(long length);

        void onDownloadProgress(long progress);

        void onDownloadSuccess(String filePath);

        void onDownloadFailure(Exception exception);
    }
}
