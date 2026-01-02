package com.example.fuelwiselog.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fuelwiselog.data.FuelRecord;
import com.example.fuelwiselog.databinding.ItemFuelRecordBinding;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FuelRecordAdapter extends ListAdapter<FuelRecordDisplay, FuelRecordAdapter.RecordVH> {

    public interface OnRecordLongClickListener {
        void onLongClick(FuelRecord record);
    }

    private OnRecordLongClickListener longClickListener;

    public void setOnRecordLongClickListener(OnRecordLongClickListener listener) {
        this.longClickListener = listener;
    }

    private static final DiffUtil.ItemCallback<FuelRecordDisplay> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<FuelRecordDisplay>() {
                @Override
                public boolean areItemsTheSame(@NonNull FuelRecordDisplay oldItem, @NonNull FuelRecordDisplay newItem) {
                    return oldItem.record.getId() == newItem.record.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull FuelRecordDisplay oldItem, @NonNull FuelRecordDisplay newItem) {
                    FuelRecord o = oldItem.record;
                    FuelRecord n = newItem.record;

                    return o.getTimestamp() == n.getTimestamp()
                            && o.getOdometerKm() == n.getOdometerKm()
                            && Double.compare(o.getLiters(), n.getLiters()) == 0
                            && Double.compare(o.getCostRm(), n.getCostRm()) == 0;
                }
            };

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    private final DecimalFormat df2 = new DecimalFormat("0.00");

    public FuelRecordAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public RecordVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFuelRecordBinding binding = ItemFuelRecordBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new RecordVH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordVH holder, int position) {
        FuelRecordDisplay item = getItem(position);
        holder.bind(item);
    }

    class RecordVH extends RecyclerView.ViewHolder {

        private final ItemFuelRecordBinding b;

        RecordVH(ItemFuelRecordBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(FuelRecordDisplay item) {
            FuelRecord r = item.record;

            b.tvDate.setText(dateFormat.format(new Date(r.getTimestamp())));

            String main = "Odo " + r.getOdometerKm() + " km • "
                    + df2.format(r.getLiters()) + " L • "
                    + "RM " + df2.format(r.getCostRm());
            b.tvMain.setText(main);

            String metrics;
            if (item.distanceKm == null) {
                metrics = "Distance — • L/100km — • RM/km —";
            } else {
                metrics = "Distance " + item.distanceKm + " km • "
                        + df2.format(item.litersPer100Km) + " L/100km • "
                        + df2.format(item.rmPerKm) + " RM/km";
            }
            b.tvMetrics.setText(metrics);

            View root = b.getRoot();
            root.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onLongClick(r);
                    return true;
                }
                return false;
            });
        }
    }
}
