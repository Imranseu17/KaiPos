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
import com.kaicomsol.kpos.callbacks.HistoryClickListener;
import com.kaicomsol.kpos.models.Content;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SalesHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // View Types
    private static final int ITEM = 0;
    private static final int LOADING = 1;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;
    
    private String errorMsg;

    private Context context;
    private List<Content> contentList = new ArrayList<>();
    private HistoryClickListener mCallback;

    public SalesHistoryAdapter(Context context, HistoryClickListener listener) {
        this.context = context;
        this.mCallback = listener;
    }

    public void setHistory(List<Content> contentList, int currentPage){
        if (currentPage == 1){
            if(this.contentList != null) this.contentList.clear();
            this.contentList.addAll(contentList);
        }else  this.contentList.addAll(contentList);

        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM:
                View viewItem = inflater.inflate(R.layout.recycler_sales_history_item, parent, false);
                viewHolder = new ViewHolder(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingVH(viewLoading);
                break;
        }
        return viewHolder;


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final Content content = contentList.get(position);

        switch (getItemViewType(position)) {
            case ITEM:
                ViewHolder vh = (ViewHolder) holder;

                Date date = new Date(content.getSaleDateTimeInLong());
                SimpleDateFormat targetFormat = new SimpleDateFormat("MM-dd-yyyy hh:mm a");
                String formatDate = targetFormat.format(date);

                vh.txt_date_time.setText(formatDate);
                vh.txt_deposit_amount.setText(content.getAmount()+" TK");

//                vh.txt_account_no.setText(content.getCustomerAccNo() != null ?  content.getCustomerAccNo(): "N/A");
//                vh.txt_meter_serial.setText(content.getMeterSerialNo() != null ?  content.getMeterSerialNo(): "N/A");
//                vh.txt_pos_id.setText(content.getPosId() != null ?  content.getPosId(): "N/A");
//                vh.txt_amount.setText(content.getAmount()+" TK");
//                vh.txt_email.setText(content.getPosUser() != null ?  content.getPosUser(): "N/A");
//                vh.txt_payment_charge.setText(content.getPaymentCharge()+" TK");
//                vh.txt_total_amount.setText(content.getTotalAmount()+" TK");
//                vh.txt_payment_method.setText(content.getPaymentMethod() != null ?  ""+content.getPaymentMethod(): "N/A");
//                vh.txt_sales_date.setText(formatDate);
                break;

            case LOADING:
                LoadingVH loadingVH = (LoadingVH) holder;
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
    public int getItemViewType(int position) {

        return (position == contentList.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }

    @Override
    public int getItemCount() {

        return contentList != null ? contentList.size() : 0;
    }


    /**
     * Main list's content ViewHolder
     */

    public class ViewHolder extends RecyclerView.ViewHolder {
        //UI View Bind
        @BindView(R.id.txt_date_time) TextView txt_date_time;
        @BindView(R.id.txt_deposit_amount) TextView txt_deposit_amount;
//        @BindView(R.id.txt_meter_serial) TextView txt_meter_serial;
//        @BindView(R.id.txt_pos_id) TextView txt_pos_id;
//        @BindView(R.id.txt_amount) TextView txt_amount;
//        @BindView(R.id.txt_email) TextView txt_email;
//        @BindView(R.id.txt_payment_charge) TextView txt_payment_charge;
//        @BindView(R.id.txt_total_amount) TextView txt_total_amount;
//        @BindView(R.id.txt_payment_method) TextView txt_payment_method;
//        @BindView(R.id.txt_sales_date) TextView txt_sales_date;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

        }

    }


    protected class LoadingVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        //UI View Bind
        @BindView(R.id.loadmore_progress) ProgressBar mProgressBar;
        @BindView(R.id.loadmore_retry) ImageButton mRetryBtn;
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
                    mCallback.retryPageLoad();
                    break;
            }
        }
    }



    /*
        Helpers - Pagination
   _________________________________________________________________________________________________
    */

    public void add(Content r) {
        contentList.add(r);
        notifyItemInserted(contentList.size() - 1);
    }

    public void addAll(List<Content> moveContents) {
        for (Content Content : moveContents) {
            add(Content);
        }
    }

    public void remove(Content r) {
        int position = contentList.indexOf(r);
        if (position > -1) {
            contentList.remove(position);
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
        add(new Content());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = contentList.size() - 1;
        Content Content = getItem(position);

        if (Content != null) {
            contentList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Content getItem(int position) {
        return contentList.get(position);
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param show
     * @param errorMsg to display if page load fails
     */
    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(contentList.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }
}
