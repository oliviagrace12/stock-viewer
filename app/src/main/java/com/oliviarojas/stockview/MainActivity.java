package com.oliviarojas.stockview;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private StockViewAdapter stockViewAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swiper;
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

        swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(() -> {
            doRefresh();
            swiper.setRefreshing(false);
        });
    }

    public void doRefresh() {
        Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(this, "Add new stock", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(this, "Stock selected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onLongClick(View view) {
        int position = recyclerView.getChildLayoutPosition(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.baseline_delete_black_18dp);
        builder.setPositiveButton("OK", (dialog, id) -> {
            stocks.remove(position);
            stockViewAdapter.notifyDataSetChanged();
        });
        builder.setNegativeButton("CANCEL", (dialog, id) -> {
        });
        builder.setMessage("Delete stock " + stocks.get(position).getSymbol() + "?");
        builder.setTitle("Delete Stock");
        AlertDialog dialog = builder.create();
        dialog.show();

        return true;
    }
}