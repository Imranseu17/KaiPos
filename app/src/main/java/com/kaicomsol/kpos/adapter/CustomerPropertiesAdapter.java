package com.kaicomsol.kpos.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.models.CustomerProperties;


import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomerPropertiesAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


    private Context context;
    private List<CustomerProperties> valueList;

    public CustomerPropertiesAdapter(Context context, List<CustomerProperties> valueList) {
        this.context = context;
        this.valueList = valueList;
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.customer_adapter, parent, false);
        return new ViewHolder(view);


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final CustomerProperties value = valueList.get(position);
        CustomerPropertiesAdapter.ViewHolder vh =
                (CustomerPropertiesAdapter.ViewHolder) holder;
        vh.txt_label.setText(value.getLabel());
        vh.txt_value.setText(value.getValue() != null ? value.getValue() : "N/A");

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {

        return valueList != null ? valueList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //UI View Bind
        @BindView(R.id.item_layout)
        LinearLayout item_layout;
        @BindView(R.id.txt_label)
        TextView txt_label;
        @BindView(R.id.txt_value)
        TextView txt_value;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

        }

    }

}
