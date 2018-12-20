package com.kaicomsol.kpos.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.model.Transaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Transaction> transactionList;

    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_transaction_item, parent, false);
        return new ViewHolder(view);


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final Transaction transaction = transactionList.get(position);
        ViewHolder vh = (ViewHolder) holder;
        Date date = new Date(transaction.getPaymentDate());
        SimpleDateFormat targetFormat = new SimpleDateFormat("MM-dd-yyyy");
        String formatDate = targetFormat.format(date);

        vh.txt_invoice_type.setText(transaction.getInvoiceType() !=null ? transaction.getInvoiceType() : "N/A");
        vh.txt_price.setText(transaction.getPrice() != 0 ? String.valueOf(transaction.getPrice() +" TK") : "N/A");

        vh.txt_amount.setText(transaction.getAmount() != 0 ? String.valueOf(transaction.getAmount() +" TK") : "N/A");
        vh.txt_percentage.setText(String.valueOf(transaction.getPercentage()));

        vh.txt_description.setText(transaction.getDescription() != null ? transaction.getDescription() : "N/A");
        vh.txt_payment_date.setText(formatDate);

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
        @BindView(R.id.txt_invoice_type) TextView txt_invoice_type;
        @BindView(R.id.txt_price) TextView txt_price;
        @BindView(R.id.txt_amount) TextView txt_amount;
        @BindView(R.id.txt_percentage) TextView txt_percentage;
        @BindView(R.id.txt_description) TextView txt_description;
        @BindView(R.id.txt_payment_date) TextView txt_payment_date;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

        }
    }


}
