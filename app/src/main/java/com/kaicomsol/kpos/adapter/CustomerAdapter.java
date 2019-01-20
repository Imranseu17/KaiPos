package com.kaicomsol.kpos.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.callbacks.CustomerClickListener;
import com.kaicomsol.kpos.models.Customer;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM = 0;
    private static final int LOADING = 1;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private String errorMsg;


    private Context context;
    private List<Customer> customerList = new ArrayList<>();
    private CustomerClickListener listener;

    public CustomerAdapter(Context context, CustomerClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public List<Customer> getCustomers() {
        return customerList;
    }

    public void setCustomers(List<Customer> customerList,int currentPage) {
        if (currentPage == 1){
            if(this.customerList != null) this.customerList.clear();
            this.customerList.addAll(customerList);
        }else  this.customerList.addAll(customerList);

        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM:
                View viewItem = inflater.inflate(R.layout.recycler_customer_item, parent, false);
                viewHolder = new CustomerAdapter.ViewHolder(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new CustomerAdapter.LoadingVH(viewLoading);
                break;
        }
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final Customer customer = customerList.get(position);
        switch (getItemViewType(position)){
            case ITEM:
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
                break;

            case LOADING:
                CustomerAdapter.LoadingVH loadingVH = (CustomerAdapter.LoadingVH) holder;
                if (retryPageLoad) {
                    loadingVH.mErrorLayout.setVisibility(View.VISIBLE);
                    loadingVH.mProgressBar.setVisibility(View.GONE);

                    loadingVH.mErrorTxt.setText(
                            errorMsg != null ?
                                    errorMsg :
                                    context.getString(R.string.error_msg_unknown));
                } else {
                    loadingVH.mErrorLayout.setVisibility(View.GONE);
                    loadingVH.mProgressBar.setVisibility(View.VISIBLE);
                }
                break;


        }


    }


    @Override
    public int getItemCount() {

        return customerList != null ? customerList.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == customerList.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
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

    protected class LoadingVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        //UI View Bind
        @BindView(R.id.loadmore_progress)
        ProgressBar mProgressBar;
        @BindView(R.id.loadmore_retry)
        ImageButton mRetryBtn;
        @BindView(R.id.loadmore_errortxt) TextView mErrorTxt;
        @BindView(R.id.loadmore_errorlayout) LinearLayout mErrorLayout;


        public LoadingVH(View itemView) {
            super(itemView);

            ButterKnife.bind(this,itemView);
            mRetryBtn.setOnClickListener(this);
            mErrorLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.loadmore_retry:
                case R.id.loadmore_errorlayout:
                    showRetry(false, null);
                    listener.retryPageLoad();
                    break;
            }
        }
    }



    /*
        Helpers - Pagination
   _________________________________________________________________________________________________
    */

    public void add(Customer r) {
        customerList.add(r);
        notifyItemInserted(customerList.size() - 1);
    }

    public void addAll(List<Customer> moveContents) {
        for (Customer Content : moveContents) {
            add(Content);
        }
    }

    public void remove(Customer r) {
        int position = customerList.indexOf(r);
        if (position > -1) {
            customerList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }


    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new Customer());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = customerList.size() - 1;
        Customer Content = getItem(position);

        if (Content != null) {
            customerList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Customer getItem(int position) {
        return customerList.get(position);
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param show
     * @param errorMsg to display if page load fails
     */
    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(customerList.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }

}

