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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

    public void addStock(Stock stock) {
        if (stock == null) {
            Log.w(TAG, "addStock: Cannot add null stock", null);
            return;
        }

        if (stocks.contains(stock)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Duplicate Stock");
            builder.setMessage("Existing stock cannot be re-added");
            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }

        stocks.add(stock);
        sortStocks();
        stockViewAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        stockViewAdapter = new StockViewAdapter(stocks, this);
        recyclerView.setAdapter(stockViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


//        stocks.add(new Stock("OLI", "Olivia Company", 115.78, -2.3, -.15));
//        stocks.add(new Stock("ARM", "Armando Company", 130.6, 1.9, .23));
//        stocks.add(new Stock("OLI1", "Olivia Company", 115.78, -2.3, -.15));
//        stocks.add(new Stock("ARM1", "Armando Company", 130.6, 1.9, .23));
//        stocks.add(new Stock("OLI2", "Olivia Company", 115.78, -2.3, -.15));
//        stocks.add(new Stock("ARM2", "Armando Company", 130.6, 1.9, .23));
//        stocks.add(new Stock("OLI1", "Olivia Company", 115.78, -2.3, -.15));
//        stocks.add(new Stock("ARM1", "Armando Company", 130.6, 1.9, .23));
//        stocks.add(new Stock("OLI2", "Olivia Company", 115.78, -2.3, -.15));
//        stocks.add(new Stock("ARM2", "Armando Company", 130.6, 1.9, .23));

        loadSavedStocks();
        stockViewAdapter.notifyDataSetChanged();

        swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(() -> {
            doRefresh();
            swiper.setRefreshing(false);
        });

        SymbolNameDownloader symbolNameDownloader = new SymbolNameDownloader();
        new Thread(symbolNameDownloader).start();
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
                stocks.add(stock);
            }
        } catch (Exception e) {
            Log.e(TAG, "loadSavedNotes: ", e);
        }
        sortStocks();
        stockViewAdapter.notifyDataSetChanged();
    }

    private void sortStocks() {
        stocks.sort((s1, s2) -> s1.getSymbol().compareTo(s2.getSymbol()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isConnectedToNetwork()) {
            showStockSearchDialogue();
        } else {
            showNoNetworkDialogue();
        }
        return true;
    }

    private void showStockSearchDialogue() {
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
    }

    private void showNoNetworkDialogue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Network Connection");
        builder.setMessage("Stock cannot be added without a network connection");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean isConnectedToNetwork() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    private void handleNewStock(EditText editText) {
        String searchTerm = editText.getText().toString();
        if (searchTerm.isEmpty()) {
            return;
        }
        List<String> matches = SymbolNameDownloader.findMatches(searchTerm);
        if (matches.size() == 0) {
            Toast.makeText(this, "No stocks found for " + searchTerm, Toast.LENGTH_SHORT).show();
        } else if (matches.size() == 1) {
            addNewStock(matches.get(0));
        } else {
            haveUserChooseStock(matches);
        }
    }

    private void addNewStock(String stockSymbolAndName) {
        String stockSymbol = stockSymbolAndName.split(" ")[0];
        new Thread(new StockInfoDownloader(this, stockSymbol)).start();
    }

    private void haveUserChooseStock(List<String> matches) {
        String[] matchesArray = matches.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose your stock");
//        builder.setIcon(R.drawable.icon2);
        builder.setItems(matchesArray, (dialog, which) -> {
            addNewStock(matchesArray[which]);
        });
        builder.setNegativeButton("Nevermind", (dialog, id) -> {
            Toast.makeText(MainActivity.this, "You changed your mind!", Toast.LENGTH_SHORT).show();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
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