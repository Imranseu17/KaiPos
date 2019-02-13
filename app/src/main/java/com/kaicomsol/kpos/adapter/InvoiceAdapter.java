package com.kaicomsol.kpos.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.models.Invoice;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InvoiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context context;
    private List<Invoice> invoiceList;;

    public InvoiceAdapter(Context context, List<Invoice> invoiceList) {
        this.context = context;
        this.invoiceList = invoiceList;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == 0){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_invoice_header, parent, false);
            return new HeaderVH(view);
        }else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_invoice_item, parent, false);
            return new ViewHolder(view);
        }



    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (position > 0){
            final Invoice invoice = invoiceList.get(position-1);
            ViewHolder vh = (ViewHolder) holder;
            Date date = new Date(invoice.getCreateDateTime());
            SimpleDateFormat targetFormat = new SimpleDateFormat("MM-dd-yyyy");
            String formatDate = targetFormat.format(date);

            vh.txt_item.setText(invoice.getItemName() !=null ? invoice.getItemName() : "N/A");

            vh.txt_amount.setText(String.valueOf(invoice.getAmount())+" TK");
            vh.txt_date.setText(formatDate);
        }

    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }

    @Override
    public int getItemCount() {

        return invoiceList != null ? invoiceList.size()+1 : 0;
    }

    public class HeaderVH extends RecyclerView.ViewHolder {

        public HeaderVH(View view) {
            super(view);
            ButterKnife.bind(this, view);

        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        //UI View Bind
        @BindView(R.id.txt_item) TextView txt_item;
        @BindView(R.id.txt_amount) TextView txt_amount;
        @BindView(R.id.txt_date) TextView txt_date;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

        }

    }
}
