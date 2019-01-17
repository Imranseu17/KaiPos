package com.kaicomsol.kpos.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kaicomsol.kpos.models.Error;


import com.kaicomsol.kpos.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ErrorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Error> errorList;

    public ErrorAdapter(Context context, List<Error> errorList) {
        this.context = context;
        this.errorList = errorList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_error_item, parent, false);
        return new ViewHolder(view);


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Error error = errorList.get(position);
        ViewHolder vh = (ViewHolder) holder;
        vh.txt_group.setText(error.getGroup() !=null ? error.getGroup() : "N/A");
        vh.txt_type.setText(error.getType() !=null ? error.getType() : "N/A");
        vh.txt_date.setText(error.getTime() !=null ? error.getTime() : "N/A");

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return errorList != null ? errorList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //UI View Bind
        @BindView(R.id.item_layout)
        LinearLayout itemLayout;
        @BindView(R.id.txt_type)
        TextView txt_type;
        @BindView(R.id.txt_group)
        TextView txt_group;
        @BindView(R.id.txt_date)
        TextView txt_date;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

        }

    }
}

