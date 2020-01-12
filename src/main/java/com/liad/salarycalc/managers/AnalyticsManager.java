package com.liad.salarycalc.managers;

import android.content.Context;

import com.google.firebase.auth.FirebaseUser;
import com.liad.salarycalc.Config;
import com.liad.salarycalc.R;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import androidx.annotation.StringDef;

import static com.liad.salarycalc.managers.AnalyticsManager.AnalyticsEvents.*;
import static com.liad.salarycalc.managers.AnalyticsManager.AnalyticsProperty.*;

public class AnalyticsManager {

    private static AnalyticsManager instance;
    private MixpanelAPI mixPanel;
    private static final String DEFAULT_USER_TYPE = "register";

    @StringDef({ENTER_LOGIN_ACTIVITY, REGISTER_WITH_CREDS, REGISTER_WITH_FACEBOOK, CANCELED_FACEBOOK_LOGIN,
            LOGGED_IN_WITH_CREDS, RESET_PASSWORD_CLICKED, RESET_PASSWORD_SENT, FAILED_TO_LOGIN, MAIN_FRAGMENT, TAP_ON_CLOCK_WITHOUT_SETTINGS,
            TAP_ON_CLOCK_WITH_SETTINGS, FINISH_COUNTING_CLOCK, ENTER_INSERT_MANUALLY_FRAGMENT, SAVED_MANUALLY, CHANGE_HOUR_SALARY,
            ENTER_DATA_FRAGMENT, OPEN_EDIT_SHIFT, SAVED_EDIT_SHIFT, DELETE_SHIFT, CHANGED_DATE, ENTER_INSERT_MANUALLY_DATA_FRAGMENT, ENTER_SETTINGS_FRAGMENT, SAVED_SETTINGS_DATA
    })
    public @interface AnalyticsEvents {
        /* Register */
        String ENTER_LOGIN_ACTIVITY = "Login Activity Initialized";
        String REGISTER_WITH_CREDS = "Registered With Creds";
        String REGISTER_WITH_FACEBOOK = "Register With Facebook";
        String CANCELED_FACEBOOK_LOGIN = "Canceled Facebook Login";
        String LOGGED_IN_WITH_CREDS = "Logged In With Creds";
        String RESET_PASSWORD_CLICKED = "Reset Password Clicked";
        String RESET_PASSWORD_SENT = "Reset Password Sent";
        String FAILED_TO_LOGIN = "Failed To Login";

        /* Main fragment */
        String MAIN_FRAGMENT = "Main Fragment Initialized";
        String TAP_ON_CLOCK_WITHOUT_SETTINGS = "Tap On Clock No Settings";
        String TAP_ON_CLOCK_WITH_SETTINGS = "Tap On Clock With Settings";
        String FINISH_COUNTING_CLOCK = "Finish Counting Clock";

        /* insert manually fragment */
        String ENTER_INSERT_MANUALLY_FRAGMENT = "Insert Manually Fragment Initialized";
        String SAVED_MANUALLY = "Saved Manually";
        String CHANGE_HOUR_SALARY = "Change Hour Salary";

        /* data fragment */
        String ENTER_DATA_FRAGMENT = "Data Fragment Initialized";
        String OPEN_EDIT_SHIFT = "Open Edit Shift";
        String SAVED_EDIT_SHIFT = "Saved Edited Shift";
        String DELETE_SHIFT = "Deleted Shift";
        String CHANGED_DATE = "Changed Date";
        String ENTER_INSERT_MANUALLY_DATA_FRAGMENT = "Open Insert Manually from Data Fragment";

        /* settings fragment */
        String ENTER_SETTINGS_FRAGMENT = "Settings Fragment Initialized";
        String SAVED_SETTINGS_DATA = "Saved Settings data";
    }

    @StringDef({USER_TYPE, USER_ID, SCREEN_VIEW, EMAIL, TIME, AMOUNT, SHIFT_ID})
    public @interface AnalyticsProperty {
        String USER_TYPE = "User Type";
        String USER_ID = "User ID";
        String SCREEN_VIEW = "Screen View";
        String EMAIL = "email";
        String TIME = "time";
        String AMOUNT = "amount";
        String SHIFT_ID = "shift ID";
    }

    public static synchronized AnalyticsManager getInstance(Context context) {
        if (instance == null) {
            instance = new AnalyticsManager(context);
        }
        return instance;
    }

    private AnalyticsManager(Context context) {
        mixPanel = MixpanelAPI.getInstance(context, context.getString(Config.getInstance().STAGE.equals(Config.STAGES.prod) ? R.string.mixpanel_prod_token : R.string.mixpanel_dev_token));
        mixPanel.registerSuperProperties(getDefaultProperties());
    }

    private JSONObject getDefaultProperties() {
        JSONObject props = new JSONObject();
        try {
            FirebaseUser firebaseUser = FirebaseManager.getInstance().getFirebaseUser();
            props.put(USER_ID, firebaseUser != null ? firebaseUser.getUid() : "guest");
            props.put(USER_TYPE, DEFAULT_USER_TYPE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return props;
    }

    public MixpanelAPI getMixPanel() {
        return mixPanel;
    }


    public void sendData(@AnalyticsEvents String event, Map<String, String> params) {
        if (event != null) {
            if (params != null) {
                mixPanel.track(event, (JSONObject) params);
            } else {
                mixPanel.track(event);
            }
        }
    }
}
