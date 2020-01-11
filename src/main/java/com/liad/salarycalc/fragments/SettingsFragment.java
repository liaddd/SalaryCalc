package com.liad.salarycalc.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.liad.salarycalc.Constants;
import com.liad.salarycalc.R;
import com.liad.salarycalc.activities.BaseActivity;
import com.liad.salarycalc.activities.MainActivity;
import com.liad.salarycalc.entities.ShiftDetails;
import com.liad.salarycalc.managers.AnalyticsManager;
import com.liad.salarycalc.managers.FirebaseManager;
import com.liad.salarycalc.managers.SharedPreferencesManager;
import com.liad.salarycalc.utills.Validator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class SettingsFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {

    private TextInputLayout hourlySalaryTIL, rideSalaryTIL;
    private TextInputEditText hourlyET, rideSalaryET;
    private RadioGroup radioGroup;
    private String hourlySalary, rideSalary;
    private ProgressBar progressBar;
    private DatabaseReference dbRef;
    private int rideType = 1;
    private SharedPreferences pref;
    private FirebaseUser firebaseUser;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AnalyticsManager.getInstance(getContext()).sendData(AnalyticsManager.AnalyticsEvents.ENTER_SETTINGS_FRAGMENT, null);
        return inflater.inflate(R.layout.settings_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViewAndListeners(view);
    }

    private void findViewAndListeners(View view) {

        final Context context = getContext();
        initAdView(view);
        if (context != null) {
            pref = SharedPreferencesManager.getInstance(context).getPref();
            view.findViewById(R.id.settings_fragment_fragment_save_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendData();
                }
            });
        }

        firebaseUser = FirebaseManager.getInstance().getFirebaseUser();
        dbRef = FirebaseDatabase.getInstance().getReference();

        progressBar = view.findViewById(R.id.settings_fragment_progress_bar);

        radioGroup = view.findViewById(R.id.settings_fragment_radio_group);
        radioGroup.setOnCheckedChangeListener(this);

        hourlySalaryTIL = view.findViewById(R.id.settings_fragment_salary_amount_text_input_layout);
        hourlyET = view.findViewById(R.id.settings_fragment_salary_amount_edit_text);

        rideSalaryTIL = view.findViewById(R.id.settings_fragment_ride_text_input_layout);
        rideSalaryET = view.findViewById(R.id.settings_fragment_ride_edit_text);

        if (pref != null) {
            populateFields();
        }
    }

    private void initAdView(View view) {
        AdView adView = view.findViewById(R.id.banner_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void populateFields() {
        hourlySalary = pref.getString(Constants.USER_HOURLY_SALARY, "");
        rideSalary = pref.getString(Constants.USER_RIDE_SALARY, "");
        rideType = pref.getInt(Constants.USER_RIDE_TYPE, rideType);

        hourlyET.setText(hourlySalary);
        rideSalaryET.setText(rideSalary);
        radioGroup.check(rideType == 1 ? R.id.monthly : R.id.daily);
    }

    private void sendData() {
        final Context context = getContext();
        if (validateData(context)) {
            if (context != null) {
                progressBar.setVisibility(View.VISIBLE);

                final ShiftDetails shiftDetails = new ShiftDetails(hourlySalary, rideSalary, rideType);
                dbRef.child(Constants.USERS_TABLE).child(firebaseUser.getUid()).child(Constants.WORK_DATA).setValue(shiftDetails, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        progressBar.setVisibility(View.GONE);
                        if (databaseError != null) {
                            Toast.makeText(context, getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
                        } else {
                            AnalyticsManager.getInstance(getContext()).sendData(AnalyticsManager.AnalyticsEvents.SAVED_SETTINGS_DATA, null);
                            ((BaseActivity) context).showDialog(context, getString(R.string.data_saved_successfully), null, false);
                            saveSettingsInPref();
                            MainActivity activity = (MainActivity) context;
                            activity.closeKeyboard();
                        }
                    }
                });
            }
        }
    }

    private void saveSettingsInPref() {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Constants.USER_HOURLY_SALARY, hourlyET.getText().toString().trim());
        editor.putString(Constants.USER_RIDE_SALARY, rideSalaryET.getText().toString().trim());
        editor.putInt(Constants.USER_RIDE_TYPE, rideType);
        editor.apply();
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) getFragmentManager().popBackStack();
    }

    private boolean validateData(Context context) {
        return Validator.validateSettingsInputs(context, hourlySalaryTIL, rideSalaryTIL);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        RadioButton selectedButton = group.findViewById(checkedId);
        rideType = selectedButton.getText().equals(getString(R.string.daily)) ? 2 : 1;
    }
}
