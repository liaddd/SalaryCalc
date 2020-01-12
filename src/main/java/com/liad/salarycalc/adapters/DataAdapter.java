package com.liad.salarycalc.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.liad.salarycalc.Constants;
import com.liad.salarycalc.R;
import com.liad.salarycalc.activities.MainActivity;
import com.liad.salarycalc.entities.ShiftItem;
import com.liad.salarycalc.fragments.DataFragment;
import com.liad.salarycalc.fragments.InsertManuallyFragment;
import com.liad.salarycalc.managers.AnalyticsManager;
import com.liad.salarycalc.utills.DateFormatter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private final Context context;
    private final DataFragment dataFragment;
    private List<ShiftItem> shiftItems, sortedShiftsItems = new ArrayList<>();
    private DateFormatter dateFormatter;

    public DataAdapter(Context context, List<ShiftItem> shiftItems, DataFragment dataFragment) {
        this.context = context;
        this.shiftItems = shiftItems;
        this.dataFragment = dataFragment;
        dateFormatter = DateFormatter.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.data_list_item, parent , false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final ShiftItem shiftItem = sortedShiftsItems.get(position);

        holder.totalGrossTV.setText(shiftItem.getTotalGross());
        holder.startTV.setText(dateFormatter.getFormattedHour(shiftItem.getStartTimeInMillis()));
        holder.endTV.setText(dateFormatter.getFormattedHour(shiftItem.getEndtimeInMillis()));
        holder.totalV.setText(dateFormatter.getRangeBetweenHours(shiftItem.getStartTimeInMillis(), shiftItem.getEndtimeInMillis()));
        holder.dateTV.setText(dateFormatter.getFormattedDateInHebrew(shiftItem.getStartTime() * 1000));

        holder.rowItemContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnalyticsManager.getInstance(context).sendData(AnalyticsManager.AnalyticsEvents.OPEN_EDIT_SHIFT, null);
                addEditFragment(shiftItem);
            }
        });

        holder.rowItemContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                dataFragment.confirmShiftDelete(shiftItem);
                return false;
            }
        });
    }

    private void addEditFragment(ShiftItem shift) {
        MainActivity mainActivity = (MainActivity) this.context;
        if (mainActivity != null) {
            FragmentManager supportFragmentManager = mainActivity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
            InsertManuallyFragment fragment = InsertManuallyFragment.newInstance();

            Bundle bundle = new Bundle();
            bundle.putBoolean(Constants.EDIT_MODE, true);
            bundle.putString(Constants.SHIFT_ID, shift.getId());
            bundle.putLong(Constants.START_SHIFT, shift.getStartTimeInMillis());
            bundle.putLong(Constants.END_SHIFT, shift.getEndtimeInMillis());
            bundle.putInt(Constants.USER_HOURLY_SALARY, (int) shift.getHourSalary());

            fragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.main_activity_frame_layout, fragment, null);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    @Override
    public int getItemCount() {
        return sortedShiftsItems.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        TextView dateTV, startTV, endTV, totalV, totalGrossTV;
        LinearLayout rowItemContainer, listViewContainer;

        ViewHolder(View itemView) {
            super(itemView);
            dateTV = itemView.findViewById(R.id.data_list_item_date_text_view);
            startTV = itemView.findViewById(R.id.data_list_item_start_text_view);
            endTV = itemView.findViewById(R.id.data_list_item_end_text_view);
            totalV = itemView.findViewById(R.id.data_list_item_total_text_view);
            totalGrossTV = itemView.findViewById(R.id.data_list_item_total_gross_text_view);
            listViewContainer = itemView.findViewById(R.id.data_list_item_container);
            rowItemContainer = itemView.findViewById(R.id.data_list_item_row_linear_layout);
        }
    }

    public void notifyDateChange(int month, int year) {
        sortedShiftsItems.clear();
        int totalGross = 0;
        for (int i = 0; i < shiftItems.size(); i++) {
            ShiftItem shiftItem = shiftItems.get(i);
            if (shiftItem.getYear() == year && shiftItem.getMonth() == month) {
                sortedShiftsItems.add(shiftItem);
                totalGross += Double.parseDouble(shiftItem.getTotalGross());
            }
        }
        if (sortedShiftsItems.size() > 0) {
            dataFragment.showEmptyState(false);
            notifyDataSetChanged();
        } else {
            dataFragment.showEmptyState(true);
        }
        dataFragment.setTotalGrossTV(totalGross);
    }

}
