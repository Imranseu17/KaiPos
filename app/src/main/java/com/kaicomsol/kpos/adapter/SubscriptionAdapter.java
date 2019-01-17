package com.kaicomsol.kpos.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.models.Subscription;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SubscriptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Subscription> subscriptionList;

    public SubscriptionAdapter(Context context, List<Subscription> subscriptionList) {
        this.context = context;
        this.subscriptionList = subscriptionList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_subscription_item, parent, false);
        return new ViewHolder(view);


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final Subscription subscription = subscriptionList.get(position);
        ViewHolder vh = (ViewHolder) holder;

        vh.txt_item_no.setText(subscription.getItemNo() !=null ? subscription.getItemNo() : "N/A");
        vh.txt_amount.setText(subscription.getAmount() != 0 ? String.valueOf(subscription.getAmount() +" TK") : "N/A");

        vh.txt_cycle.setText(subscription.getCycle() != null ? subscription.getCycle() : "N/A");
        vh.txt_status.setText(subscription.getStatus() != null ? subscription.getStatus() : "N/A");

        vh.txt_description.setText(subscription.getDescription() != null ? subscription.getDescription() : "N/A");

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return subscriptionList != null ? subscriptionList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //UI View Bind
        @BindView(R.id.txt_item_no) TextView txt_item_no;
        @BindView(R.id.txt_amount) TextView txt_amount;
        @BindView(R.id.txt_cycle) TextView txt_cycle;
        @BindView(R.id.txt_status) TextView txt_status;
        @BindView(R.id.txt_description) TextView txt_description;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

        }
    }


}
