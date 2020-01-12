package com.liad.salarycalc.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.liad.salarycalc.Constants;
import com.liad.salarycalc.R;
import com.liad.salarycalc.entities.User;
import com.liad.salarycalc.managers.AnalyticsManager;
import com.liad.salarycalc.managers.FirebaseManager;
import com.liad.salarycalc.utills.Validator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LoginActivity extends BaseActivity implements View.OnTouchListener, View.OnClickListener {

    private final String TAG = getClass().getSimpleName();
    private FirebaseAuth mAuth;
    private EditText usernameET, passET, passVerifyET;
    private Button loginButton;
    private CallbackManager callbackManager;
    private LoginButton facebookLoginButton;
    private DatabaseReference dbRef;
    private TextView loginOrSignInTV;
    private ProgressBar progressBar;
    private LinearLayout passVerificationContainer;
    private int loginOrSignState = STATE.LOG_IN;

    @interface STATE {
        int LOG_IN = 1;
        int REGISTER = 2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AnalyticsManager.getInstance(this).sendData(AnalyticsManager.AnalyticsEvents.ENTER_LOGIN_ACTIVITY, null);

        // Initialize Firebase Auth
        initFirebaseAuthAndDatabase();
        // initialize view and set listeners
        initViewAndListeners();
    }

    private void initFirebaseAuthAndDatabase() {
        mAuth = FirebaseManager.getInstance().getFirebaseAuth();
        dbRef = FirebaseDatabase.getInstance().getReference();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViewAndListeners() {

        TextView forgotPassTV = findViewById(R.id.login_activity_forgot_pass_text_view);
        forgotPassTV.setOnClickListener(this);
        forgotPassTV.setOnTouchListener(this);

        loginOrSignInTV = findViewById(R.id.login_activity_login_or_sign_up_text_view);
        loginOrSignInTV.setOnClickListener(this);
        loginOrSignInTV.setOnTouchListener(this);

        usernameET = findViewById(R.id.login_activity_user_name_edit_text);

        passET = findViewById(R.id.login_activity_pass_edit_text);
        passVerifyET = findViewById(R.id.login_activity_pass_verification_edit_text);

        loginButton = findViewById(R.id.login_activity_log_in_button);
        loginButton.setOnClickListener(this);

        callbackManager = CallbackManager.Factory.create();

        facebookLoginButton = findViewById(R.id.facebook_login_button);
        facebookLoginButton.setOnClickListener(this);

        Button fbLoginBtn = findViewById(R.id.login_activity_facebook_login_btn);
        fbLoginBtn.setOnClickListener(this);

        passVerificationContainer = findViewById(R.id.login_activity_pass_verification_container);

        progressBar = findViewById(R.id.login_activity_progress_bar);

    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                AnalyticsManager.getInstance(LoginActivity.this).sendData(AnalyticsManager.AnalyticsEvents.REGISTER_WITH_FACEBOOK, null);
                                addUserToDB(user);
                            } else
                                Toast.makeText(LoginActivity.this, getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addUserToDB(final FirebaseUser user) {
        updateUser(user);
        User userInstance = User.getInstance();
        dbRef.child(Constants.USERS_TABLE).child(user.getUid()).child(Constants.FULL_NAME).setValue(userInstance.getFullName());
        dbRef.child(Constants.USERS_TABLE).child(user.getUid()).child(Constants.EMAIL).setValue(userInstance.getEmail());
        changeActivity(this, MainActivity.class, true);
    }

    private void loginWithFacebook() {

        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                AnalyticsManager.getInstance(LoginActivity.this).sendData(AnalyticsManager.AnalyticsEvents.CANCELED_FACEBOOK_LOGIN, null);
            }

            @Override
            public void onError(FacebookException exception) {
                AnalyticsManager.getInstance(LoginActivity.this).sendData(AnalyticsManager.AnalyticsEvents.FAILED_TO_LOGIN, null);
                Toast.makeText(LoginActivity.this, getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUser(FirebaseUser currentUser) {
        User user = User.getInstance();
        user.setUserID(currentUser.getUid());
        user.setEmail(currentUser.getEmail() == null ? "" : currentUser.getEmail());
        user.setFullName(currentUser.getDisplayName());
        user.setPhotoURL(currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : null);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                v.setAlpha((float) 0.5);
                break;
            case MotionEvent.ACTION_UP:
                v.setAlpha(1);
                break;

        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_activity_log_in_button:
                if (validateEmailAndPassword()) {
                    if (loginOrSignState == STATE.LOG_IN) {
                        signInWithEmailAndPassword();
                    } else {
                        createUserWithEmailAndPassword();
                    }
                }
                break;
            case R.id.login_activity_facebook_login_btn:
                facebookLoginButton.performClick();
                break;
            case R.id.facebook_login_button:
                loginWithFacebook();
                break;
            case R.id.login_activity_login_or_sign_up_text_view:
                handleStateChanged();
                break;
            case R.id.login_activity_forgot_pass_text_view:
                AnalyticsManager.getInstance(LoginActivity.this).sendData(AnalyticsManager.AnalyticsEvents.RESET_PASSWORD_CLICKED, null);
                forgotPassDialog();
                break;
        }
    }

    private boolean validateEmailAndPassword() {
        if (loginOrSignState == STATE.REGISTER) {
            if (Validator.validatePassword(this, passET) && Validator.validatePassword(this, passVerifyET)) {
                if (!Validator.isPasswordEqual(passET, passVerifyET)) {
                    Toast.makeText(this, getString(R.string.passwords_arent_equal), Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else {
                return false;
            }
        }
        return Validator.validateEmail(this, usernameET) && Validator.validatePassword(this, passET);
    }

    private void forgotPassDialog() {
        final EditText resetPassEmailET = new EditText(this);
        resetPassEmailET.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        resetPassEmailET.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle(getString(R.string.reset_password))
                .setMessage(getString(R.string.insert_email_address))
                .setView(resetPassEmailET)
                .setPositiveButton(getString(R.string.reset_pass_now), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showProgress(true);
                        if (Validator.validateEmail(LoginActivity.this, resetPassEmailET)) {
                            mAuth.sendPasswordResetEmail(resetPassEmailET.getText().toString().trim())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            showProgress(false);
                                            if (task.isSuccessful()) {
                                                AnalyticsManager.getInstance(LoginActivity.this).sendData(AnalyticsManager.AnalyticsEvents.RESET_PASSWORD_SENT, null);
                                                Toast.makeText(LoginActivity.this, getString(R.string.email_sent), Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(LoginActivity.this, getString(R.string.email_wasnt_found), Toast.LENGTH_SHORT).show();
                                                forgotPassDialog();
                                            }
                                        }
                                    });

                        }
                    }

                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void handleStateChanged() {
        String loginOrSignInCurrentText = loginOrSignInTV.getText().toString();
        loginOrSignState = loginOrSignInCurrentText.equals(getString(R.string.login)) ? STATE.LOG_IN : STATE.REGISTER;
        passVerificationContainer.setVisibility(loginOrSignState == STATE.REGISTER ? View.VISIBLE : View.GONE);
        loginOrSignInTV.setText(getString(loginOrSignState == STATE.REGISTER ? R.string.login : R.string.sign_in));
        loginButton.setText(getString(loginOrSignState == STATE.REGISTER ? R.string.sign_in : R.string.login_btn));
    }

    private void signInWithEmailAndPassword() {
        showProgress(true);
        mAuth.signInWithEmailAndPassword(usernameET.getText().toString().trim(), passET.getText().toString().trim())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showProgress(false);
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                AnalyticsManager.getInstance(LoginActivity.this).sendData(AnalyticsManager.AnalyticsEvents.LOGGED_IN_WITH_CREDS, null);
                                updateUser(user);
                                changeActivity(LoginActivity.this, MainActivity.class, true);
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, getString(R.string.auth_failed_error), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }

    private void createUserWithEmailAndPassword() {
        showProgress(true);
        mAuth.createUserWithEmailAndPassword(usernameET.getText().toString().trim(), passET.getText().toString().trim())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showProgress(false);
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                AnalyticsManager.getInstance(LoginActivity.this).sendData(AnalyticsManager.AnalyticsEvents.REGISTER_WITH_CREDS, null);
                                updateUser(user);
                                changeActivity(LoginActivity.this, MainActivity.class, true);
                            } else {
                                Toast.makeText(LoginActivity.this, getString(R.string.error_occurred), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            usernameET.setError(task.getException() != null ? task.getException().getMessage() : getString(R.string.error_occurred));
                            usernameET.requestFocus();
                        }
                    }
                });

    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
