package com.liad.salarycalc.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.liad.salarycalc.R;
import com.liad.salarycalc.activities.MainActivity;
import com.liad.salarycalc.entities.MainCardItem;
import com.liad.salarycalc.fragments.DataFragment;
import com.liad.salarycalc.fragments.InsertManuallyFragment;
import com.liad.salarycalc.fragments.SettingsFragment;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class MainCardsAdapter extends RecyclerView.Adapter<MainCardsAdapter.ViewHolder> {

    private final Context context;
    private List<MainCardItem> cardItems;
    private Fragment[] fragments = {DataFragment.newInstance() , InsertManuallyFragment.newInstance() , SettingsFragment.newInstance()};

    public MainCardsAdapter(Context context , List<MainCardItem> cardItems) {
        this.context = context;
        this.cardItems = cardItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.main_card_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        MainCardItem item = cardItems.get(position);

        holder.titleTV.setText(item.getTitle());
        holder.subTitleTV.setText(item.getSubtitle());

        holder.listViewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)context).changeFragment(fragments[position] , true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cardItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView titleTV , subTitleTV;
        RelativeLayout listViewContainer;

        ViewHolder(View itemView) {
            super(itemView);
            titleTV = itemView.findViewById(R.id.list_item_title_card_text_view);
            subTitleTV = itemView.findViewById(R.id.list_item_subtitle_card_text_view);
            listViewContainer = itemView.findViewById(R.id.list_item_container);
        }
    }
}
