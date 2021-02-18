package jp.co.zynas.work_schedule.sp.android;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class JoyShiftActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ConnectionHelper.IDownloadListener {
    private static final String DOWNLOAD_DESTINATION_BASE = "download";
    private static final String URL = "URL";
    private static final int REQUEST_DPF = 450;
    private static final int MAX_TRY_LOGIN = 1;
    private int tryLoginCount = 0;
    WebView myWebView;
    private LoginTask mAuthTask = null;


    public static Intent newIntent(Context context, String url) {
        Intent intent = new Intent(context, JoyShiftActivity.class);
        intent.putExtra(URL, url);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.app_joy_shift));

        setContentView(R.layout.activity_joy_shift);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View naviHeader = navigationView.getHeaderView(0);
        TextView staffNameText = (TextView) naviHeader.findViewById(R.id.staffName);
        staffNameText.setText(DataHelper.getStaffName(getApplicationContext()));


        String url = getURL();

        //CookieManager cookieManager = CookieManager.getInstance();
        //String cookie = cookieManager.getCookie(BuildConfig.URL_API_BASE + BuildConfig.URL_API_CONTEXT_ROOT + "/");
        //Log.d("debug","webView cookie=" + cookie);

        myWebView = (WebView) findViewById(R.id.webView);

        WebSettings settings = myWebView.getSettings();

        settings.setAppCacheEnabled(true);
        // JavaScript有効化
        settings.setJavaScriptEnabled(true);
        // レスポンシブ対応化
        //settings.setLoadWithOverviewMode(true);
        //settings.setUseWideViewPort(true);
        //settings.setBuiltInZoomControls(true);

        //myWebView.setInitialScale(1);
        // スクロールバーWebView包含
        //myWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        myWebView.setWebViewClient(new JoyShiftClient());

        myWebView.loadUrl(url);

    }

    private String getURL() {
        return getIntent().getStringExtra(URL);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private String getSession() {

        CookieManager cookieManager = CookieManager.getInstance();
        String cookie = cookieManager.getCookie(BuildConfig.URL_API_BASE + BuildConfig.URL_API_CONTEXT_ROOT + "/");
        return cookie;

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_shift_app) {
            Intent intent = JoyShiftActivity.newIntent(this, BuildConfig.URL_API_BASE + BuildConfig.URL_API_CONTEXT_ROOT + "/hope_work");
            startActivity(intent);

        } else if (id == R.id.nav_shift) {
            Intent intent = JoyShiftActivity.newIntent(this, BuildConfig.URL_API_BASE + BuildConfig.URL_API_CONTEXT_ROOT + "/weekly_shift");
            startActivity(intent);

        } else if (id == R.id.nav_ws_actual) {
            Intent intent = JoyShiftActivity.newIntent(this, BuildConfig.URL_API_BASE + BuildConfig.URL_API_CONTEXT_ROOT + "/service_record");
            startActivity(intent);

        } else if (id == R.id.nav_logout) {
            doLogout(BuildConfig.URL_API_BASE + BuildConfig.URL_API_CONTEXT_ROOT + "/weekly_shift");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void doLogout(String url) {
        logout();
        Intent intent = LoginActivity.newIntent(this, url);
        startActivity(intent);
        finish();
    }

    private void downloadPDF(String url) {
        String storage = getStorageDownloadDirectoryPath();
        int lastIndex = url.lastIndexOf("/");
        String fileName = url.substring(lastIndex);
        ConnectionHelper.downloadFile(url, getSession(), storage, fileName, this);
    }

    private String getStorageDownloadDirectoryPath() {
        String dirPath = getDataStorageDirectory();
        return dirPath + File.separator + DOWNLOAD_DESTINATION_BASE + File.separator;
    }

    private File getStorage() {
        return this.getExternalFilesDir(Environment.MEDIA_MOUNTED);
    }

    private String getDataStorageDirectory() {
        return getStorage().getAbsolutePath();
    }

    public void showError(String message) {
        if (!ConnectionHelper.isNetworkConnected(this)) {
            AlertDialog errorDialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.message_connection)
                    .setCancelable(false)
                    .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            myWebView.loadUrl(getURL());
                        }
                    }).create();
            errorDialog.show();
        } else {
            if (mAuthTask == null) {
                if (tryLoginCount < MAX_TRY_LOGIN) {
                    tryLoginCount++;
                    tryToLogin(message);
                } else {
                    showAlert(message);
                }

            }

        }

    }

    private void showAlert(String message) {
        AlertDialog errorDialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        doLogout(getURL());
                        dialogInterface.dismiss();
                    }
                }).create();
        errorDialog.show();
    }

    private void tryToLogin(final String messageError) {
        final ProgressDialog dialog = new ProgressDialog(this);
        mAuthTask = new LoginTask(this.getApplicationContext(), new LoginTask.OnLoginListener() {

            @Override
            public void onLoginSuccess() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                tryLoginCount = 0;
                myWebView.loadUrl(getURL());
                mAuthTask = null;
            }

            @Override
            public void onLoginFailure(String message) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                showAlert(messageError);
                mAuthTask = null;

            }
        });
        String id = DataHelper.getUserLogin(this);
        String password = DataHelper.getPassword(this);
        if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(password)) {
            mAuthTask.execute(id, password, DataHelper.getUUID(this.getApplicationContext()), DataHelper.getModelName(), DataHelper.getFireBaseToken());
            dialog.show();
        } else {
            showAlert(messageError);
        }
    }

    public void logout() {
        DataHelper.clearUserLoggedIn(this);
    }


    class JoyShiftClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Nullable
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.endsWith(".pdf")) {
                downloadPDFByDownloadManager(url);
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            if (url.endsWith(".pdf")) {
                this.shouldOverrideUrlLoading(view, url);
            }
        }

        @Override
        public void onReceivedLoginRequest(WebView view, String realm, @Nullable String account, String args) {
            super.onReceivedLoginRequest(view, realm, account, args);
            Log.d("debug", "onReceivedLoginRequest " + realm + " " + account + " " + args);
            showError(getString(R.string.message_session_error));
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            Log.d("debug", "onReceivedError " + request.getRequestHeaders() + " " + error.getErrorCode());
            showError(getString(R.string.message_session_error));
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
            Log.d("debug", "onReceivedHttpError " + request.getRequestHeaders() + " " + errorResponse.getResponseHeaders());
            showError(getString(R.string.message_session_error));
        }

        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            super.onReceivedHttpAuthRequest(view, handler, host, realm);
            Log.d("debug", "onReceivedHttpAuthRequest " + host + " " + realm);
            showError(getString(R.string.message_session_error));
        }
    }

    private void downloadPDFByDownloadManager(String url) {
        if (!haveStoragePermission()) {
            return;
        }

        String[] values = url.split("/");
        String filename = values[values.length - 1];

        Log.d("debug", "webView download filename=" + filename);

        try {
            filename = URLDecoder.decode(filename, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.d("debug", "webView throw UnsupportedEncodingException");
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        CookieManager cookieManager = CookieManager.getInstance();
        String cookie = cookieManager.getCookie(BuildConfig.URL_API_BASE + BuildConfig.URL_API_CONTEXT_ROOT + "/");
        Log.d("debug", "in webView get cookie=" + cookie);
        request.allowScanningByMediaScanner();
        request.addRequestHeader("Cookie", cookie);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setVisibleInDownloadsUi(true);
        request.setTitle(filename);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        dm.enqueue(request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("SCROLLX", myWebView.getScrollX());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final int scrollX = savedInstanceState.getInt("SCROLLX", 0);
        if (scrollX != 0) {
            myWebView.post(new Runnable() {
                @Override
                public void run() {
                    myWebView.setScrollX(scrollX);
                }
            });
        }
    }

    @Override
    public void onDownloadLength(long length) {
        Log.i("AAAAA onDownloadLength", String.valueOf(length));
    }

    @Override
    public void onDownloadProgress(long progress) {
        Log.i("AAAAA onDowProgress", String.valueOf(progress));
    }

    @Override
    public void onDownloadSuccess(String filePath) {
        startActivityForResult(PDFViewerActivity.newIntent(this, filePath, ""), REQUEST_DPF);
    }

    @Override
    public void onDownloadFailure(Exception exception) {
        showDownloadError(exception.getMessage());
    }

    private void showDownloadError(final String message) {
        myWebView.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog errorDialog = new AlertDialog.Builder(JoyShiftActivity.this)
                        .setMessage(message)
                        .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create();
                errorDialog.show();
            }
        });
    }

    public boolean haveStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.e("Permission error", "You have permission");
                return true;
            } else {
                Log.e("Permission error", "You have asked for permission");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            Log.e("Permission error", "You already have the permission");
            return true;
        }
    }

}
