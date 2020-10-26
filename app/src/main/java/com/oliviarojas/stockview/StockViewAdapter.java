package com.oliviarojas.stockview;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StockViewAdapter extends RecyclerView.Adapter<StockViewHolder> {

    private List<Stock> stocks;
    private MainActivity mainActivity;

    public StockViewAdapter(List<Stock> stocks, MainActivity mainActivity) {
        this.stocks = stocks;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_recycler_item, parent, false);
        itemView.setOnClickListener(mainActivity);
        itemView.setOnLongClickListener(mainActivity);

        return new StockViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Stock stock = stocks.get(position);

        int color = stock.getChange() < 0 ?
                mainActivity.getResources().getColor(R.color.colorRedText, null) :
                mainActivity.getResources().getColor(R.color.colorGreenText, null);

        holder.symbol.setText(stock.getSymbol());
        holder.symbol.setTextColor(color);
        holder.companyName.setText(stock.getCompanyName());
        holder.companyName.setTextColor(color);
        holder.latestPrice.setText(String.valueOf(stock.getLatestPrice()));
        holder.latestPrice.setTextColor(color);
        holder.change.setText(String.valueOf(stock.getChange()));
        holder.change.setTextColor(color);
        holder.changePercent.setText(String.valueOf(stock.getChangePercent()));
        holder.changePercent.setTextColor(color);
        if (stock.getChange() < 0) {
            holder.arrow.setImageResource(R.drawable.baseline_arrow_drop_down_white_18dp);
            holder.arrow.setColorFilter(color);
        } else {
            holder.arrow.setImageResource(R.drawable.baseline_arrow_drop_up_white_18dp);
        }
        holder.arrow.setColorFilter(color);
    }

    @Override
    public int getItemCount() {
        return stocks.size();
    }
}
