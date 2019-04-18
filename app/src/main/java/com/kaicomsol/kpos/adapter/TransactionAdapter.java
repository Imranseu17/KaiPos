package com.kaicomsol.kpos.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.callbacks.TransactionHistoryClickListener;
import com.kaicomsol.kpos.golobal.Constants;
import com.kaicomsol.kpos.models.TransactionModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM = 0;
    private static final int LOADING = 1;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private String errorMsg;

    private Context context;
    private List<TransactionModel> transactionModelList = new ArrayList<>();
    private TransactionHistoryClickListener mCallback;

    public TransactionAdapter(Context context, TransactionHistoryClickListener clickListener) {
        this.context = context;
        this.mCallback = clickListener;
    }

    public void setTransaction(List<TransactionModel> transactionModelList, int currentPage){
        if (currentPage == 1){
            if(this.transactionModelList != null) this.transactionModelList.clear();
            this.transactionModelList.addAll(transactionModelList);
        }else  this.transactionModelList.addAll(transactionModelList);

        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM:
                View viewItem = inflater.inflate(R.layout.recycler_transaction_item, parent, false);
                viewHolder = new TransactionAdapter.ViewHolder(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new TransactionAdapter.ViewHolder(viewLoading);
                break;
        }
        return viewHolder;


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final TransactionModel transactionModel = transactionModelList.get(position);

        switch (getItemViewType(position)) {
            case ITEM:
                TransactionAdapter.ViewHolder vh = (TransactionAdapter.ViewHolder) holder;

                Date date = new Date(transactionModel.getPaymentDate());
                SimpleDateFormat targetFormat = new SimpleDateFormat(Constants.DATE_FORMAT+" "+Constants.TIME_FORMAT);
                String formatDate = targetFormat.format(date);

                vh.txt_date_time.setText(formatDate);
                vh.txt_deposit_amount.setText(transactionModel.getAmount()+" TK");

                break;

            case LOADING:
                LoadingVH  loadingVH = (LoadingVH) holder;
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
        return (position == transactionModelList.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }

    @Override
    public int getItemCount() {
        return transactionModelList != null ? transactionModelList.size() : 0;
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        //UI View Bind
        @BindView(R.id.item_layout)
        LinearLayout item_layout;
        @BindView(R.id.txt_date_time) TextView txt_date_time;
        @BindView(R.id.txt_deposit_amount) TextView txt_deposit_amount;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            item_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.onHistoryClick(transactionModelList.get(getAdapterPosition()));
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
        @BindView(R.id.loadmore_errorlayout)
        LinearLayout mErrorLayout;


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

    public void add(TransactionModel r) {
        transactionModelList.add(r);
        notifyItemInserted(transactionModelList.size() - 1);
    }

    public void addAll(List<TransactionModel> moveContents) {
        for (TransactionModel transactionModel : moveContents) {
            add(transactionModel);
        }
    }

    public void remove(TransactionModel r) {
        int position = transactionModelList.indexOf(r);
        if (position > -1) {
            transactionModelList.remove(position);
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
        add(new TransactionModel());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = transactionModelList.size() - 1;
        TransactionModel transactionModel = getItem(position);

        if (transactionModel != null) {
            transactionModelList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public TransactionModel getItem(int position) {
        return transactionModelList.get(position);
    }

    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(transactionModelList.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }


}
