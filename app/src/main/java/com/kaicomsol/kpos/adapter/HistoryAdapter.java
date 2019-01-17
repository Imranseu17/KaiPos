package com.kaicomsol.kpos.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.models.History;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<History> historyList;
    public HistoryAdapter(Context context, List<History> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_history_item, parent, false);
        return new ViewHolder(view);


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final History history = historyList.get(position);
        ViewHolder vh = (ViewHolder) holder;
        vh.txt_type.setText(history.getType());
        vh.txt_date.setText(history.getTime() !=null ? history.getTime() : "N/A");
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return historyList != null ? historyList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //UI View Bind
        @BindView(R.id.item_layout)
        LinearLayout itemLayout;
        @BindView(R.id.txt_type)
        TextView txt_type;
        @BindView(R.id.txt_date)
        TextView txt_date;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

        }

    }
}
