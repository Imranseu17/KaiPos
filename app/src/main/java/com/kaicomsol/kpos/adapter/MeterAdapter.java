package com.kaicomsol.kpos.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.callbacks.MeterClickListener;
import com.kaicomsol.kpos.golobal.Constants;
import com.kaicomsol.kpos.models.MeterList;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MeterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<MeterList> meterList;;
    private MeterClickListener listener;

    public MeterAdapter(Context context, MeterClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setMeterList(List<MeterList> meterList) {
        this.meterList = meterList;
        notifyDataSetChanged();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_meter_item, parent, false);
        return new ViewHolder(view);


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final MeterList meter = meterList.get(position);
        ViewHolder vh = (ViewHolder) holder;
        Date date = new Date(meter.getInstallationDate());
        SimpleDateFormat targetFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
        String formatDate = targetFormat.format(date);

        vh.txt_meter_serial.setText(meter.getMeterSerialNo() !=null ? meter.getMeterSerialNo() : "N/A");
        vh.txt_meter_type.setText(meter.getMeterType() !=null ? meter.getMeterType() : "N/A");

        vh.txt_status.setText(meter.getStatus() !=null ? meter.getStatus() : "N/A");
        vh.txt_tariff_id.setText(meter.getTariffId() != 0 ? String.valueOf(meter.getTariffId()) : "N/A");

        vh.txt_pending_invoice.setText(String.valueOf(meter.isPendingInvoice()));
        vh.txt_installation_date.setText(formatDate);

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return meterList != null ? meterList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        //UI View Bind
        @BindView(R.id.card_content) CardView card_content;
        @BindView(R.id.txt_meter_serial) TextView txt_meter_serial;
        @BindView(R.id.txt_meter_type) TextView txt_meter_type;
        @BindView(R.id.txt_status) TextView txt_status;
        @BindView(R.id.txt_tariff_id) TextView txt_tariff_id;
        @BindView(R.id.txt_tariff) TextView txt_tariff;
        @BindView(R.id.txt_pending_invoice) TextView txt_pending_invoice;
        @BindView(R.id.txt_installation_date) TextView txt_installation_date;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            card_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onMeterClick(meterList.get(getAdapterPosition()));
                }
            });

        }

    }
}
