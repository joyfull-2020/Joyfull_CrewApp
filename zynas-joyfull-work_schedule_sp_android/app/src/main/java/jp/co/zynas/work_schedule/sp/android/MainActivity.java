package jp.co.zynas.work_schedule.sp.android;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String PUSH_NOTIFICATION_ACTION = "FCM";
    public static final String ARG_CONTENTS = "contents";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (haveStoragePermission())
            goOther();
        else {
            requestStoragePermission();
        }
    }

    private void goOther() {
        String URL = getURL();

        if (isLoggedIn()) { // JoyShiftActivity に遷移
            Intent intent = JoyShiftActivity.newIntent(this, URL);
            startActivity(intent);
        } else { // LoginActivity に遷移
            Intent intent = LoginActivity.newIntent(this, URL);
            startActivity(intent);
        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        goOther();
    }

    private String getURL() {
        if (getIntent().hasExtra(ARG_CONTENTS)) {
            return getIntent().getStringExtra(ARG_CONTENTS);
        }
        return BuildConfig.URL_API_BASE + BuildConfig.URL_API_CONTEXT_ROOT + "/weekly_shift";
    }

    public Boolean isLoggedIn() {
        return DataHelper.isLoggedIn(this);
    }


    public boolean haveStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.e("Permission error", "You have permission");
                return true;
            } else
                return false;
        } else {
            Log.e("Permission error", "You already have the permission");
            return true;
        }
    }

    public boolean requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        return false;
    }
}
