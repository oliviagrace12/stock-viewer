package com.oliviarojas.stockview;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private StockViewAdapter stockViewAdapter;
    private RecyclerView recyclerView;
    private List<Stock> stocks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        stockViewAdapter = new StockViewAdapter(stocks, this);
        recyclerView.setAdapter(stockViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        stocks.add(new Stock("OLI", "Olivia Company", 115.78, -2.3, -.15));
        stocks.add(new Stock("ARM", "Armando Company", 130.6, 1.9, .23));
        stockViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(this, "Stock selected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onLongClick(View v) {
        int position = recyclerView.getChildLayoutPosition(v);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.baseline_delete_black_18dp);
        builder.setPositiveButton("OK", (dialog, id) -> {
            stocks.remove(position);
            stockViewAdapter.notifyDataSetChanged();
        });
        builder.setNegativeButton("CANCEL", (dialog, id) -> {});
        builder.setMessage("Delete stock " + stocks.get(position).getSymbol() + "?");
        builder.setTitle("Delete Stock");
        AlertDialog dialog = builder.create();
        dialog.show();

        return true;
    }
}