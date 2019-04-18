package com.kaicomsol.kpos.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.activity.Transaction;
import com.kaicomsol.kpos.callbacks.PendingListener;
import com.kaicomsol.kpos.models.Item;

import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PendingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Transaction> transactionList;
    private PendingListener mListener;
    public PendingAdapter(List<Transaction> transactionList, PendingListener listener) {
        this.transactionList = transactionList;
        this.mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_pending_item, parent, false);
        return new ViewHolder(view);


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final Transaction transaction = transactionList.get(position);
        ViewHolder vh = (ViewHolder) holder;
        vh.txt_card_no.setText(transaction.getCardIdm());
        vh.txt_meter_no.setText(transaction.getMeterSerialNo());

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {

        return transactionList != null ? transactionList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        //UI View Bind
        @BindView(R.id.item_layout) LinearLayout item_layout;
        @BindView(R.id.txt_card_no) TextView txt_card_no;
        @BindView(R.id.txt_meter_no) TextView txt_meter_no;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            item_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) mListener.onClick(transactionList.get(getAdapterPosition()).getPaymentId());
                }
            });

        }

    }
}
