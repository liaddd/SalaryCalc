package com.liad.salarycalc.fragments;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
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
import com.liad.salarycalc.activities.MainActivity;
import com.liad.salarycalc.entities.ShiftItem;
import com.liad.salarycalc.managers.AnalyticsManager;
import com.liad.salarycalc.managers.FirebaseManager;
import com.liad.salarycalc.managers.SharedPreferencesManager;
import com.liad.salarycalc.utills.DateFormatter;
import com.liad.salarycalc.utills.Validator;

import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class InsertManuallyFragment extends Fragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private FirebaseUser firebaseUser;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private int currentDatePicker = 1, hourlySalary;
    private TextInputEditText enterET, exitET, hourlySalaryET;
    private TextInputLayout enterTIL, exitTIL, hourlySalaryTIL;
    private ProgressBar progressBar;
    private boolean editMode;
    private String shiftID;
    private Long startShift, endShit;
    private DateFormatter dateFormatter;


    public static InsertManuallyFragment newInstance() {
        return new InsertManuallyFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AnalyticsManager.getInstance(getContext()).sendData(AnalyticsManager.AnalyticsEvents.ENTER_INSERT_MANUALLY_FRAGMENT, null);
        initFirebaseUser();

        View view = inflater.inflate(R.layout.insert_manually_fragment, container, false);
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        return view;
    }

    private void initFirebaseUser() {
        Context context = getContext();
        if (context != null) {
            firebaseUser = FirebaseManager.getInstance().getFirebaseUser();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getViewAndListeners(view);
        Bundle bundle = getArguments();
        if (bundle != null) {
            editMode = bundle.getBoolean(Constants.EDIT_MODE);
            shiftID = bundle.getString(Constants.SHIFT_ID, null);
            startShift = bundle.getLong(Constants.START_SHIFT);
            endShit = bundle.getLong(Constants.END_SHIFT);
            hourlySalary = bundle.getInt(Constants.USER_HOURLY_SALARY);
            if (editMode) changeMode(view);
        }
    }

    private void getViewAndListeners(View view) {

        Context context = getContext();

        initAdView(view);
        enterTIL = view.findViewById(R.id.insert_manually_enter_text_input_layout);
        enterET = view.findViewById(R.id.insert_manually_enter_edit_text);
        enterET.setOnClickListener(this);

        exitTIL = view.findViewById(R.id.insert_manually_exit_text_input_layout);
        exitET = view.findViewById(R.id.insert_manually_exit_edit_text);
        exitET.setOnClickListener(this);

        hourlySalaryTIL = view.findViewById(R.id.insert_manually_hourly_salary_text_input_layout);
        hourlySalaryET = view.findViewById(R.id.insert_manually_hourly_salary_edit_text);

        SharedPreferences pref = SharedPreferencesManager.getInstance(context).getPref();
        String savedSalary = pref.getString(Constants.USER_HOURLY_SALARY, null);
        hourlySalary = savedSalary != null ? Integer.parseInt(savedSalary) : hourlySalary;
        if (hourlySalary > 0) hourlySalaryET.post(new Runnable() {
            @Override
            public void run() {
                hourlySalaryET.setText(String.valueOf(hourlySalary));
            }
        });


        dateFormatter = DateFormatter.getInstance();
        Calendar calendar = Calendar.getInstance();

        if (context != null) {
            datePickerDialog = new DatePickerDialog(
                    context, this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            timePickerDialog = new TimePickerDialog(context, this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.HOUR_OF_DAY), true);
        }
        progressBar = view.findViewById(R.id.insert_manually_progress_bar);
        Button saveBtn = view.findViewById(R.id.insert_manually_fragment_save_btn);
        saveBtn.setOnClickListener(this);

    }

    private void initAdView(View view) {
        AdView adView = view.findViewById(R.id.banner_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void changeMode(View view) {
        if (editMode) {
            TextView titleTV = view.findViewById(R.id.insert_manually_title_text_view);
            titleTV.setTextColor(getResources().getColor(R.color.colorAccent));
            titleTV.setText(getString(R.string.update_shift_details));
            enterET.setText(dateFormatter.getStrFromTimeStamp(startShift));
            exitET.setText(dateFormatter.getStrFromTimeStamp(endShit));
            hourlySalaryET.post(new Runnable() {
                @Override
                public void run() {
                    hourlySalaryET.setText(String.valueOf(hourlySalary));
                }
            });
        }
    }

    private boolean validateData(Context context) {
        return Validator.validateTextInputET(context, enterTIL, exitTIL, hourlySalaryTIL);
    }

    @Override
    public void onClick(View v) {
        Context context = getContext();
        if (context != null) {
            if (v.getId() == R.id.insert_manually_fragment_save_btn) {
                MainActivity activity = (MainActivity) context;
                activity.closeKeyboard();
                progressBar.setVisibility(View.VISIBLE);
                if (validateData(context)) {
                    long entryDateAndHourTS = dateFormatter.getTimeStampFromStr(enterET.getText().toString());
                    long exitDateAndHourTS = dateFormatter.getTimeStampFromStr(exitET.getText().toString());
                    if (entryDateAndHourTS == 0 | exitDateAndHourTS == 0) {
                        Toast.makeText(context, getString(R.string.date_or_time_missing_error), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        return;
                    }
                    int month = dateFormatter.getMonth(entryDateAndHourTS);
                    int year = dateFormatter.getYear(entryDateAndHourTS);
                    double hourSalary = Double.parseDouble(hourlySalaryET.getText().toString());
                    ShiftItem shiftItem = new ShiftItem(entryDateAndHourTS / 1000, exitDateAndHourTS / 1000, hourSalary, month, year);
                    sendData(shiftItem);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            } else {
                currentDatePicker = Integer.parseInt(v.getTag().toString());
                datePickerDialog.show();
            }
        }
    }

    private void sendData(final ShiftItem shift) {
        final Context context = getContext();

        if (context != null) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child(Constants.USERS_TABLE).child(firebaseUser.getUid()).child(Constants.SHIFTS);

            if (shiftID != null) dbRef.child(shiftID);
            if (editMode && shiftID != null) {
                dbRef.child(shiftID).setValue(shift, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        progressBar.setVisibility(View.GONE);
                        if (databaseError == null) {
                            AnalyticsManager.getInstance(context).sendData(AnalyticsManager.AnalyticsEvents.SAVED_EDIT_SHIFT, null);
                            Toast.makeText(context, getString(R.string.data_saved_successfully), Toast.LENGTH_SHORT).show();
                            FragmentManager fragmentManager = getFragmentManager();
                            if (fragmentManager != null) fragmentManager.popBackStack();
                        }
                    }
                });
            } else {
                dbRef.push().setValue(shift, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        progressBar.setVisibility(View.GONE);
                        if (databaseError != null) {
                            Toast.makeText(context, getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
                        } else {
                            FragmentManager fragmentManager = getFragmentManager();
                            if (fragmentManager != null) {
                                AnalyticsManager.getInstance(getContext()).sendData(AnalyticsManager.AnalyticsEvents.SAVED_MANUALLY, null);
                                AnalyticsManager.getInstance(getContext()).sendData(AnalyticsManager.AnalyticsEvents.CHANGE_HOUR_SALARY, null);
                                Toast.makeText(context, getString(R.string.data_saved_successfully), Toast.LENGTH_SHORT).show();
                                fragmentManager.popBackStack();
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        StringBuilder selectedDate = new StringBuilder().append(dayOfMonth).append("/").append((month + 1)).append("/").append(year);

        if (currentDatePicker == 1) {
            enterET.setText(selectedDate);
        } else {
            exitET.setText(selectedDate);
        }
        timePickerDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String formattedMinute = String.valueOf(minute < 10 ? "0" + minute : minute);
        StringBuilder selectedTime = new StringBuilder().append(hourOfDay < 10 ? "0" + hourOfDay : hourOfDay).append(":").append(formattedMinute);
        if (currentDatePicker == 1) {
            enterET.setText(String.format(Locale.getDefault(), "%s, %s", enterET.getText(), selectedTime));
        } else {
            exitET.setText(String.format(Locale.getDefault(), "%s, %s", exitET.getText(), selectedTime));
        }
    }

}
