package com.oliviarojas.stockview;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StockViewHolder extends RecyclerView.ViewHolder {

    TextView symbol;
    TextView companyName;
    TextView latestPrice;
    TextView change;
    TextView changePercent;
    ImageView arrow;

    public StockViewHolder(@NonNull View itemView) {
        super(itemView);
        symbol = itemView.findViewById(R.id.symbol);
        companyName = itemView.findViewById(R.id.companyName);
        latestPrice = itemView.findViewById(R.id.latestPrice);
        change = itemView.findViewById(R.id.change);
        changePercent = itemView.findViewById(R.id.changePercent);
        arrow = itemView.findViewById(R.id.arrow);
    }
}
