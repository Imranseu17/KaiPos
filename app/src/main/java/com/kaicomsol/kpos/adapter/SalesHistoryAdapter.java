package com.kaicomsol.kpos.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.callbacks.HistoryClickListener;
import com.kaicomsol.kpos.model.Content;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SalesHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Content> contentList;
    private HistoryClickListener listener;

    public SalesHistoryAdapter(Context context, List<Content> contentList, HistoryClickListener listener) {
        this.context = context;
        this.contentList = contentList;
        this.listener = listener;
    }

    public void setListener(HistoryClickListener listener){
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_sales_history_item, parent, false);
        return new ViewHolder(view);


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final Content content = contentList.get(position);
        ViewHolder vh = (ViewHolder) holder;

        Date date = new Date(content.getSaleDateTimeInLong());
        SimpleDateFormat targetFormat = new SimpleDateFormat("MM-dd-yyyy");
        String formatDate = targetFormat.format(date);

        vh.txt_account_no.setText(content.getCustomerAccNo() != null ?  content.getCustomerAccNo(): "N/A");
        vh.txt_meter_serial.setText(content.getMeterSerialNo() != null ?  content.getMeterSerialNo(): "N/A");
        vh.txt_pos_id.setText(content.getPosId() != null ?  content.getPosId(): "N/A");
        vh.txt_amount.setText(content.getAmount()+" TK");
        vh.txt_email.setText(content.getPosUser() != null ?  content.getPosUser(): "N/A");
        vh.txt_payment_charge.setText(content.getPaymentCharge()+" TK");
        vh.txt_total_amount.setText(content.getTotalAmount()+" TK");
        vh.txt_payment_method.setText(content.getPaymentMethod() != null ?  ""+content.getPaymentMethod(): "N/A");
        vh.txt_sales_date.setText(formatDate);

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {

        return contentList != null ? contentList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //UI View Bind
        @BindView(R.id.card_content) CardView card_content;
        @BindView(R.id.txt_account_no) TextView txt_account_no;
        @BindView(R.id.txt_meter_serial) TextView txt_meter_serial;
        @BindView(R.id.txt_pos_id) TextView txt_pos_id;
        @BindView(R.id.txt_amount) TextView txt_amount;
        @BindView(R.id.txt_email) TextView txt_email;
        @BindView(R.id.txt_payment_charge) TextView txt_payment_charge;
        @BindView(R.id.txt_total_amount) TextView txt_total_amount;
        @BindView(R.id.txt_payment_method) TextView txt_payment_method;
        @BindView(R.id.txt_sales_date) TextView txt_sales_date;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            card_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onHistoryClick(contentList.get(getAdapterPosition()));
                }
            });
        }

    }
}
