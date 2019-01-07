package com.kaicomsol.kpos.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.callbacks.CustomerClickListener;
import com.kaicomsol.kpos.model.Customer;

import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private Context context;
    private List<Customer> customerList;
    private CustomerClickListener listener;

    public CustomerAdapter(Context context, CustomerClickListener listener) {
        this.context = context;
        this.customerList = customerList;
        this.listener = listener;
    }

    public List<Customer> getCustomers() {
        return customerList;
    }

    public void setCustomers(List<Customer> customerList) {
        this.customerList = customerList;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recycler_customer_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final Customer customer = customerList.get(position);
        ViewHolder vh = (ViewHolder) holder;
        vh.txt_customer_code.setText(customer.getCustomerCode() != null ? customer.getCustomerCode() : "N/A");
        vh.txt_account_no.setText(customer.getAccountNo() != null ? customer.getAccountNo() : "N/A");

        vh.txt_card_no.setText(customer.getCardNo() != null ? customer.getCardNo() : "N/A");
        vh.txt_erp_code.setText(customer.getErpCode() != null ? customer.getErpCode() : "N/A");

        vh.txt_metro.setText(customer.getMetro() != null ? customer.getMetro() : "N/A");
        vh.txt_zone.setText(customer.getZone() != null ? customer.getZone() : "N/A");

        vh.txt_area.setText(customer.getArea() != null ? customer.getArea() : "N/A");
        vh.txt_sub_area.setText(customer.getSubArea() != null ? customer.getSubArea() : "N/A");

        vh.txt_address.setText(customer.getAddress() != null ? customer.getAddress() : "N/A");
        vh.txt_apartment.setText(customer.getApartment() != null ? customer.getApartment() : "N/A");

        vh.txt_meter_serial.setText(customer.getMeterSerial() != null ? customer.getMeterSerial() : "N/A");
        vh.txt_balance.setText(customer.getBalance() != 0 ? String.valueOf(customer.getBalance()) : "N/A");

        vh.txt_status.setText(customer.getStatus() != null ? customer.getStatus() : "N/A");

    }


    @Override
    public int getItemCount() {

        return customerList != null ? customerList.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    /*
        View Holders
        ===================================
    */

    public class ViewHolder extends RecyclerView.ViewHolder {
        //UI View Bind
        @BindView(R.id.card_content)
        CardView card_content;
        @BindView(R.id.txt_customer_code)
        TextView txt_customer_code;
        @BindView(R.id.txt_account_no)
        TextView txt_account_no;
        @BindView(R.id.txt_card_no)
        TextView txt_card_no;
        @BindView(R.id.txt_erp_code)
        TextView txt_erp_code;
        @BindView(R.id.txt_metro)
        TextView txt_metro;
        @BindView(R.id.txt_zone)
        TextView txt_zone;
        @BindView(R.id.txt_area)
        TextView txt_area;
        @BindView(R.id.txt_sub_area)
        TextView txt_sub_area;
        @BindView(R.id.txt_address)
        TextView txt_address;
        @BindView(R.id.txt_apartment)
        TextView txt_apartment;
        @BindView(R.id.txt_meter_serial)
        TextView txt_meter_serial;
        @BindView(R.id.txt_balance)
        TextView txt_balance;
        @BindView(R.id.txt_status)
        TextView txt_status;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            card_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onCustomerClick(customerList.get(getAdapterPosition()));
                }
            });
        }

    }

}
