package jp.co.zynas.work_schedule.sp.android;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;



/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoginTask.OnLoginListener {
    private static final String TAG = "LoginActivity";
    private static final String URL = "URL";


    public static Intent newIntent(Context context, String url) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(URL, url);
        return intent;
    }

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private LoginTask mAuthTask = null;


    // UI references.
    private AutoCompleteTextView mLoginidView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.app_name));

        setContentView(R.layout.activity_login);
        // Set up the login form.
        mLoginidView = (AutoCompleteTextView) findViewById(R.id.loginid);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (!ConnectionHelper.isNetworkConnected(this)) {
            showError(getString(R.string.message_connection));
            return;
        }
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mLoginidView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String loginid = mLoginidView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid loginid and password.
        if (TextUtils.isEmpty(loginid)) {
            mLoginidView.setError(getString(R.string.error_field_required));
            focusView = mLoginidView;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new LoginTask(this.getApplicationContext(), this);
            mAuthTask.execute(loginid, password, DataHelper.getUUID(this.getApplicationContext()), DataHelper.getModelName(), DataHelper.getFireBaseToken());
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    private void showError(String message) {
        String errorMessage = getErrorMessage(message);

        AlertDialog errorDialog = new AlertDialog.Builder(this)
                .setMessage(errorMessage)
                .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();
        errorDialog.show();
    }


    private String getErrorMessage(String message) {
        switch (message) {
            case LoginTask.TOKEN_ERROR:
                return getString(R.string.message_token_error);
            case LoginTask.LOGIN_ERROR:
                return getString(R.string.message_login_error);
            case LoginTask.REGISTPUSH_ERROR:
                return getString(R.string.message_regist_push_error);
            case LoginTask.REDIRECT_ERROR:
                return getString(R.string.message_redirect_error);
            case LoginTask.INFO_ERROR:
                return getString(R.string.message_info_error);
            default:
                return message;
        }
    }

    private String getURL() {
        return getIntent().getStringExtra(URL);
    }

    @Override
    public void onLoginSuccess() {

        Intent intent = JoyShiftActivity.newIntent(this, getURL());
        startActivity(intent);
        mAuthTask = null;
        //showProgress(false);
        finish();
    }

    @Override
    public void onLoginFailure(String message) {
        mAuthTask = null;
        showProgress(false);
        showError(message);
    }
}
