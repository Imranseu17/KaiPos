package com.kaicomsol.kpos.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.models.Item;

import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PrintAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Item> itemList;
    private DecimalFormat decimalFormat;
    public PrintAdapter(List<Item> itemList) {
        this.itemList = itemList;
        //decimalFormat = new DecimalFormat(".##");
        decimalFormat = new DecimalFormat("#,##0.00");
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_print_item, parent, false);
        return new ViewHolder(view);


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final Item item = itemList.get(position);
        ViewHolder vh = (ViewHolder) holder;
        vh.txt_item.setText(item.getName() !=null ? item.getName() : "N/A");
        vh.txt_price.setText(String.valueOf(decimalFormat.format(item.getPrice())));
        vh.txt_qty.setText(String.valueOf(item.getQuantity()));
        vh.txt_amount.setText(String.valueOf(decimalFormat.format(item.getTotal())));

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        //UI View Bind
        @BindView(R.id.txt_item) TextView txt_item;
        @BindView(R.id.txt_price) TextView txt_price;
        @BindView(R.id.txt_qty) TextView txt_qty;
        @BindView(R.id.txt_amount) TextView txt_amount;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

        }

    }
}
