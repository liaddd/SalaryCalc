package com.liad.salarycalc.utills;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.liad.salarycalc.R;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;

public class Validator {


    /**
     * Validate TextInputLayout's and Handle it directly
     */


    public static boolean validateTextInputET(@NonNull Context context, @NonNull View... views) {
        boolean validate = false;
        for (View view : views) {
            TextInputLayout textInputLayout = null;
            if (view instanceof TextInputLayout) {
                textInputLayout = (TextInputLayout) view;
            }
            if (textInputLayout != null) {
                FrameLayout frameLayout = (FrameLayout) textInputLayout.getChildAt(0);
                if (frameLayout != null) {
                    for (int i = 0; i < frameLayout.getChildCount(); i++) {
                        if (frameLayout.getChildAt(i) instanceof TextInputEditText) {
                            TextInputEditText editText = (TextInputEditText) frameLayout.getChildAt(i);
                            if (editText != null) {
                                if (editText.getText() != null && !editText.getText().toString().isEmpty()) {
                                    validate = true;
                                    textInputLayout.setErrorEnabled(false);
                                } else {
                                    textInputLayout.setError(String.format(Locale.getDefault(), "%s %s", context.getString(R.string.error), editText.getHint()));
                                    editText.requestFocus();
                                    validate = false;
                                }
                            }
                        }
                    }
                }
            }
            if (!validate) return false;
        }
        return validate;
    }

    /**
     * Validate TextInputLayout's of Settings Fragment and Handle it directly
     */

    public static boolean validateSettingsInputs(Context context, View... views) {
        boolean validate = false;
        for (View view : views) {
            if (view instanceof TextInputLayout) {
                TextInputLayout textInputLayout = (TextInputLayout) view;
                FrameLayout frameLayout = (FrameLayout) textInputLayout.getChildAt(0);
                if (frameLayout != null) {
                    for (int i = 0; i < frameLayout.getChildCount(); i++) {
                        if (frameLayout.getChildAt(i) instanceof TextInputEditText) {
                            TextInputEditText editText = (TextInputEditText) frameLayout.getChildAt(i);
                            if (validateEditText(editText)) {
                                if (editText.getText() != null && !editText.getText().toString().startsWith("0") && editText.getText().length() < 9 && Integer.parseInt(editText.getText().toString()) > 0) {
                                    validate = true;
                                } else if (editText.getHint() != null && editText.getHint().equals(context.getString(R.string.hourly_salary))) {
                                    editText.setError(context.getString(R.string.min_max_amount_error));
                                    editText.requestFocus();
                                    validate = false;
                                }
                            } else {
                                textInputLayout.setError(String.format(Locale.getDefault(), "%s %s", context.getString(R.string.error), editText.getHint()));
                                editText.requestFocus();
                                validate = false;
                            }
                        }
                    }
                }
            }
            if (!validate) return false;
        }
        return validate;
    }

    /**
     * Validate EditText
     */

    private static boolean validateEditText(@NonNull EditText editText) {
        return !editText.getText().toString().isEmpty();
    }

    /**
     * Validate Password and Password verification equality
     */

    public static boolean validateEmail(@NonNull Context context, @NonNull EditText email) {

        Matcher matcher = Pattern.compile(context.getString(R.string.email_validation_pattern)).matcher(email.getText().toString());
        if (!email.getText().toString().isEmpty()) {
            if (matcher.matches()) return true;
            else {
                email.setError(context.getString(R.string.email_required));
            }
        } else {
            email.setError(String.format(Locale.getDefault(), "%s %s", context.getString(R.string.error), email.getHint()));
        }
        email.requestFocus();

        return false;
    }

    /**
     * Validate Password and Handle it directly
     */

    public static boolean validatePassword(@NonNull Context context, @NonNull EditText password) {

        int inputLength = password.getText().toString().trim().length();
        if (!password.getText().toString().isEmpty()) {
            if (inputLength >= 6) return true;
            else {
                password.setError(context.getString(R.string.pass_min_length));
            }
        } else {
            password.setError(String.format(Locale.getDefault(), "%s %s", context.getString(R.string.error), password.getHint()));
        }
        password.requestFocus();
        return false;
    }

    /**
     * Validate Password and Password verification equality
     */

    public static boolean isPasswordEqual(EditText firstPass, EditText passVerify) {
        return firstPass != null &&
                !firstPass.getText().toString().isEmpty() &&
                passVerify != null &&
                !passVerify.getText().toString().isEmpty() &&
                firstPass.getText().toString().equals(passVerify.getText().toString());
    }
}
