package com.liad.salarycalc.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.liad.salarycalc.Constants;
import com.liad.salarycalc.R;
import com.liad.salarycalc.activities.BaseActivity;
import com.liad.salarycalc.activities.MainActivity;
import com.liad.salarycalc.adapters.DataAdapter;
import com.liad.salarycalc.entities.ShiftItem;
import com.liad.salarycalc.managers.AnalyticsManager;
import com.liad.salarycalc.managers.FirebaseManager;
import com.liad.salarycalc.managers.SharedPreferencesManager;
import com.liad.salarycalc.utills.DateFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DataFragment extends Fragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private DataAdapter adapter;
    private List<ShiftItem> shiftItems;
    private DatabaseReference dbRef;
    private ProgressBar progressBar;
    private TextView totalGrossTV, emptyStateTV, mainHeaderDateTV;
    private RelativeLayout titleContainer;
    private Calendar calendar;
    private DateFormatter dateFormatter;
    private ShiftItem shiftItem;
    private int year, month;
    private View view;
    private Button emptyStateBtn;
    private SharedPreferences pref;


    public static DataFragment newInstance() {
        return new DataFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AnalyticsManager.getInstance(getContext()).sendData(AnalyticsManager.AnalyticsEvents.ENTER_DATA_FRAGMENT, null);

        if (view == null) {
            view = inflater.inflate(R.layout.data_fragment, container, false);
            initViewsAndListeners(view);
            fetchData();
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

    private void initViewsAndListeners(View view) {

        initAdView(view);
        dateFormatter = DateFormatter.getInstance();
        calendar = Calendar.getInstance();

        titleContainer = view.findViewById(R.id.data_fragment_item_title_container);
        mainHeaderDateTV = view.findViewById(R.id.data_fragment_main_header_date_text_view);
        mainHeaderDateTV.setOnClickListener(this);

        emptyStateTV = view.findViewById(R.id.data_fragment_empty_state_text_view);
        emptyStateBtn = view.findViewById(R.id.data_fragment_empty_state_button);
        emptyStateBtn.setOnClickListener(this);

        month = calendar.get(Calendar.MONDAY) + 1;
        year = calendar.get(Calendar.YEAR);

        mainHeaderDateTV.setText(String.format(Locale.getDefault(), "%d %s", year, dateFormatter.getHebrewMonth(month)));

        totalGrossTV = view.findViewById(R.id.data_fragment_total_gross_text_view);

        progressBar = view.findViewById(R.id.data_fragment_progress_bar);

        FirebaseUser firebaseUser = FirebaseManager.getInstance().getFirebaseUser();
        if (firebaseUser != null) {
            String userUid = firebaseUser.getUid();
            dbRef = FirebaseDatabase.getInstance().getReference().child(Constants.USERS_TABLE).child(userUid).child(Constants.SHIFTS);
        }


        shiftItems = new ArrayList<>();
        adapter = new DataAdapter(getContext(), shiftItems, DataFragment.this);
        RecyclerView recyclerView = view.findViewById(R.id.data_fragment_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void initAdView(View view) {
        AdView adView = view.findViewById(R.id.salary_details_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void fetchData() {
        final Context context = getContext();
        if (dbRef == null) {
            showEmptyState(true);
            return;
        }
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                shiftItems.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    ShiftItem shift = postSnapshot.getValue(ShiftItem.class);
                    if (shift != null) {
                        shift.setId(postSnapshot.getKey());
                        shiftItems.add(shift);
                    }
                }
                if (shiftItems.size() <= 0) showEmptyState(true);
                progressBar.setVisibility(View.GONE);

                if (context != null) pref = SharedPreferencesManager.getInstance(context).getPref();
                month = pref.getInt(Constants.MONTH, month);
                year = pref.getInt(Constants.YEAR, year);
                // get shifts by specific year and month
                adapter.notifyDateChange(month, year);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void showEmptyState(boolean show) {
        emptyStateTV.setVisibility(show ? View.VISIBLE : View.GONE);
        titleContainer.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyStateBtn.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setTotalGrossTV(int totalGross) {
        totalGrossTV.setText(String.valueOf(totalGross));
    }

    @Override
    public void onClick(View v) {
        Context context = getContext();
        if (context != null) {
            switch (v.getId()) {
                case R.id.data_fragment_main_header_date_text_view:
                    DatePickerDialog datePickerDialog = new DatePickerDialog(context, AlertDialog.THEME_HOLO_LIGHT, this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                    datePickerDialog.show();
                    break;
                case R.id.data_fragment_empty_state_button:
                    AnalyticsManager.getInstance(getContext()).sendData(AnalyticsManager.AnalyticsEvents.ENTER_INSERT_MANUALLY_DATA_FRAGMENT, null);
                    ((MainActivity) context).changeFragment(InsertManuallyFragment.newInstance(), true);
                    break;
            }
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        AnalyticsManager.getInstance(getContext()).sendData(AnalyticsManager.AnalyticsEvents.CHANGED_DATE, null);
        String selectedMonth = dateFormatter.getHebrewMonth(month + 1);
        pref = SharedPreferencesManager.getInstance(getContext()).getPref();
        SharedPreferences.Editor editor = SharedPreferencesManager.getInstance(getContext()).getEditor();
        editor.putInt(Constants.MONTH, month + 1);
        editor.putInt(Constants.YEAR, year);
        editor.apply();
        mainHeaderDateTV.setText(String.format(Locale.getDefault(), "%s %d", selectedMonth, year));
        adapter.notifyDateChange(month + 1, year);
    }

    public void confirmShiftDelete(ShiftItem shiftItem) {
        this.shiftItem = shiftItem;
        Context context = getContext();
        if (context != null) {
            BaseActivity baseActivity = (BaseActivity) getContext();
            baseActivity.setDataFragmentRef(this);
            baseActivity.showDialog(context, getString(R.string.delete_shift), getString(R.string.do_you_want_to_delete_shift), true);
        }
    }

    public void deleteShift() {
        final Context context = getContext();
        if (context != null) {
            AnalyticsManager.getInstance(context).sendData(AnalyticsManager.AnalyticsEvents.DELETE_SHIFT, null);
            progressBar.setVisibility(View.VISIBLE);
            dbRef.child(shiftItem.getId()).removeValue(new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Toast.makeText(context, getString(R.string.shift_deleted_successfully), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        adapter.notifyDateChange(shiftItem.getMonth(), shiftItem.getYear());
                    }
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (view != null) {
            SharedPreferences.Editor editor = SharedPreferencesManager.getInstance(getContext()).getEditor();
            editor.remove(Constants.MONTH);
            editor.remove(Constants.YEAR);
            editor.apply();
        }
    }
}
