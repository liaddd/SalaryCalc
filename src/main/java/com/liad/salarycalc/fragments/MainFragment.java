package com.liad.salarycalc.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.liad.salarycalc.Constants;
import com.liad.salarycalc.R;
import com.liad.salarycalc.activities.BaseActivity;
import com.liad.salarycalc.adapters.MainCardsAdapter;
import com.liad.salarycalc.entities.MainCardItem;
import com.liad.salarycalc.entities.ShiftItem;
import com.liad.salarycalc.managers.AnalyticsManager;
import com.liad.salarycalc.managers.FirebaseManager;
import com.liad.salarycalc.managers.SharedPreferencesManager;
import com.liad.salarycalc.utills.DateFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

public class MainFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = MainFragment.class.getSimpleName();
    private View view;
    private Button startBtn, stopBtn;
    private List<MainCardItem> cards;
    private RelativeLayout relativeLayout;
    private int visibleItem, lastVisibleItem;
    private Handler handler;
    private long startTime = 0, millis, startTimeConst = -1, pausedTime;
    private boolean isRunning;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String userHourlySalary;
    private Calendar calendar;
    private DatabaseReference dbRef;
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            isRunning = true;
            updateUi();
            handler.postDelayed(this, 1000);
        }
    };

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AnalyticsManager.getInstance(getContext()).sendData(AnalyticsManager.AnalyticsEvents.MAIN_FRAGMENT, null);
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_main, container, false);
            initViews(view);
            initCardItems();
            initRecyclerAndAdapter(view);
            initFirebase();
        } else {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initFirebase() {
        FirebaseUser firebaseUser = FirebaseManager.getInstance().getFirebaseUser();
        dbRef = FirebaseDatabase.getInstance().getReference().child(Constants.USERS_TABLE).child(firebaseUser.getUid()).child(Constants.SHIFTS);
    }

    private void initCardItems() {
        cards = new ArrayList<>();
        cards.add(new MainCardItem(getString(R.string.data), getString(R.string.monthly_data_summary), R.drawable.pink_and_orange_gradient_background));
        cards.add(new MainCardItem(getString(R.string.insert_manually), getString(R.string.insert_data_manually), R.drawable.blue_and_purple_gradient_background));
        cards.add(new MainCardItem(getString(R.string.settings), getString(R.string.insert_hours_and_salary), R.drawable.green_and_blue_gradient_background));
    }

    private void initViews(View view) {
        final Context context = getContext();
        if (context != null) {
            pref = SharedPreferencesManager.getInstance(context).getPref();

            userHourlySalary = pref.getString(Constants.USER_HOURLY_SALARY, "");

            handler = new Handler();
            startBtn = view.findViewById(R.id.main_activity_start_calc_btn);
            startBtn.setOnClickListener(this);

            stopBtn = view.findViewById(R.id.main_activity_stop_calc_btn);
            stopBtn.setOnClickListener(this);

            relativeLayout = view.findViewById(R.id.main_activity_container);

            calendar = Calendar.getInstance();

            handleTimerState();
        }
    }

    private void handleTimerState() {
        pausedTime = pref.getLong(Constants.SHIFT_PAUSED_TIME, -1);
        if (pausedTime != -1) {
            showPausedTime();
            return;
        }
        startTimeConst = pref.getLong(Constants.START_SHIFT, -1);
        if (startTimeConst != -1) startTimer();
    }

    private void initRecyclerAndAdapter(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.main_activity_recycler_view);
        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true);
        recyclerView.setLayoutManager(linearLayoutManager);
        final MainCardsAdapter adapter = new MainCardsAdapter(getActivity(), cards);
        recyclerView.setAdapter(adapter);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                visibleItem = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (visibleItem != -1/* && visibleItem != lastVisibleItem*/) {
                    relativeLayout.setBackgroundResource(cards.get(visibleItem).getBackground());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        Context context = getContext();
        switch (v.getId()) {
            case R.id.main_activity_start_calc_btn:
                if (pref.getString(Constants.USER_HOURLY_SALARY, null) == null) {
                    if (context != null) {
                        AnalyticsManager.getInstance(getContext()).sendData(AnalyticsManager.AnalyticsEvents.TAP_ON_CLOCK_WITHOUT_SETTINGS, null);
                        ((BaseActivity) context).showDialog(context, getString(R.string.we_ran_into_problem), getString(R.string.missing_salary_settings), false);
                    }
                } else {
                    AnalyticsManager.getInstance(getContext()).sendData(AnalyticsManager.AnalyticsEvents.TAP_ON_CLOCK_WITH_SETTINGS, null);
                    startTimer();
                }
                break;
            case R.id.main_activity_stop_calc_btn:
                AnalyticsManager.getInstance(getContext()).sendData(AnalyticsManager.AnalyticsEvents.FINISH_COUNTING_CLOCK, null);
                stopTimer();
                break;
        }
    }

    private void startTimer() {
        stopBtn.setVisibility(View.VISIBLE);
        if (pausedTime != -1) { // there is paused time saved.
            startTime = pausedTime / 1000;
            deletePauseTimeFromPref();
        } else if (startTimeConst == -1) { // there isn't start time saved
            saveStartTimeInPref(); // save start time in pref only if no start time is saved
        } else {
            startTime = (System.currentTimeMillis() - startTimeConst) / 1000;
        }
        if (!isRunning) handler.post(timerRunnable);
        else pauseTimer();
    }

    private void updateUi() {
        if (pausedTime != -1) deletePauseTimeFromPref();
        millis = ++startTime * 1000;
        String text = DateFormatter.getInstance().getHourFromMillis(millis);
        startBtn.setText(text);
    }

    private void showPausedTime() {
        stopBtn.setVisibility(View.VISIBLE);
        String text = DateFormatter.getInstance().getHourFromMillis(pausedTime);
        startBtn.setText(text);
    }

    private void saveStartTimeInPref() {
        startTimeConst = System.currentTimeMillis();
        editor = pref.edit();
        editor.putLong(Constants.START_SHIFT, startTimeConst);
        editor.apply();
    }

    private void deleteTimeFromPrefs() {
        startTimeConst = -1;
        editor = pref.edit();
        editor.remove(Constants.START_SHIFT);
        editor.apply();
    }

    private void savePauseTimeInPref() {
        pausedTime = millis;
        editor = pref.edit();
        editor.putLong(Constants.SHIFT_PAUSED_TIME, pausedTime);
        editor.apply();
    }

    private void deletePauseTimeFromPref() {
        pausedTime = -1;
        editor = pref.edit();
        editor.remove(Constants.SHIFT_PAUSED_TIME);
        editor.apply();
    }

    private void stopTimer() {
        handler.removeCallbacks(timerRunnable);
        userHourlySalary = pref.getString(Constants.USER_HOURLY_SALARY, userHourlySalary); // get updated hour salary value if changed

        ShiftItem shift = new ShiftItem(startTimeConst / 1000, (startTimeConst + millis) / 1000, Double.parseDouble(userHourlySalary), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));

        saveShiftInDatabase(shift);
        deleteTimeFromPrefs();
        deletePauseTimeFromPref();

        isRunning = false;
        stopBtn.setVisibility(View.GONE);
        String[] startBtnText = startBtn.getText().toString().split(":");

        String totalText = getString(R.string.total);
        String grossText = getString(R.string.gross);

        SpannableString coloredAmountTitle = new SpannableString(totalText + String.format("\n%s:%s:%s", startBtnText[0], startBtnText[1], startBtnText[2]));
        final SpannableString coloredSalaryTitle = new SpannableString(grossText + String.format("\n%s", shift.getTotalGross()));

        coloredAmountTitle.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.start_button_stopped_titles)), 0, totalText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        coloredSalaryTitle.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.start_button_stopped_titles)), 0, grossText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView textView = new TextView(getContext());
        textView.setText(coloredAmountTitle);
        textView.append("\n");
        textView.append(coloredSalaryTitle);

        startBtn.setText(textView.getText());

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startBtn.setText(getString(R.string.start_shift));
            }
        }, 1500);

        millis = 0;
        startTime = 0;

    }

    private void saveShiftInDatabase(ShiftItem shift) {
        dbRef.push().setValue(shift, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Toast.makeText(getContext(), getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), getString(R.string.data_saved_successfully), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void pauseTimer() {
        savePauseTimeInPref();
        handler.removeCallbacks(timerRunnable);
        isRunning = false;
    }

}
