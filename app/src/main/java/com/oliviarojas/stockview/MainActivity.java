package com.oliviarojas.stockview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.JsonWriter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "MainActivity";
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
        // loadSavedStocks();
        stockViewAdapter.notifyDataSetChanged();

        swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(() -> {
            doRefresh();
            swiper.setRefreshing(false);
        });

        SymbolNameDownloader rd = new SymbolNameDownloader();
        new Thread(rd).start();
    }

    public void doRefresh() {
        Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();
    }

    private void loadSavedStocks() {
        try {
            InputStream is = getApplicationContext().openFileInput(getString(R.string.file_name));
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JSONArray jsonArray = new JSONArray(sb.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject stockJson = jsonArray.getJSONObject(i);
                Stock stock = new Stock();
                stock.setSymbol(stockJson.getString("symbol"));
                stock.setCompanyName(stockJson.getString("companyName"));
                stock.setLatestPrice(stockJson.getDouble("latestPrice"));
                stock.setChange(stockJson.getDouble("change"));
                stock.setChangePercent(stockJson.getDouble("changePercent"));
            }
        } catch (Exception e) {
            Log.e(TAG, "loadSavedNotes: ", e);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please enter the stock you want to add");
        builder.setTitle("New Stock");
        builder.setIcon(R.drawable.baseline_assessment_black_36dp);

        LayoutInflater inflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams") final View view = inflater.inflate(R.layout.new_stock_dialogue, null);
        builder.setView(view);
        final EditText editText = view.findViewById(R.id.enterStockValue);
        builder.setPositiveButton("OK", (dialog, id) -> {
            handleNewStock(editText);
        });
        builder.setNegativeButton("CANCEL", (dialog, id) -> {
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }

    private void handleNewStock(EditText editText) {
        String newStockName = editText.getText().toString();
        if (newStockName.isEmpty()) {
            return;
        }
        Toast.makeText(this, "You searched for " + newStockName, Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onPause() {
        if (!stocks.isEmpty()) {
            saveStocks();
        }

        super.onPause();
    }

    private void saveStocks() {
        try {
            FileOutputStream fos = getApplicationContext().
                    openFileOutput(getString(R.string.file_name), Context.MODE_PRIVATE);

            JsonWriter writer = new JsonWriter(new OutputStreamWriter(fos, getString(R.string.encoding)));
            buildJson(writer);

            // LOGGING
            StringWriter sw = new StringWriter();
            writer = new JsonWriter(sw);
            buildJson(writer);
            Log.d(TAG, "Saving notes: \n" + sw.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildJson(JsonWriter writer) throws IOException {
        writer.setIndent("  ");
        writer.beginArray();
        for (Stock stock : stocks) {
            writer.beginObject();
            writer.name("symbol").value(stock.getSymbol());
            writer.name("companyName").value(stock.getCompanyName());
            writer.name("latestPrice").value(stock.getLatestPrice());
            writer.name("change").value(stock.getChange());
            writer.name("changePercent").value(stock.getChangePercent());
            writer.endObject();
        }
        writer.endArray();
        writer.close();
    }
}