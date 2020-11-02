package com.oliviarojas.stockview;

import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class StocksDataRefresher implements Runnable {

    private static final String TAG = "StocksDataRefresher";
    private final MainActivity mainActivity;
    private final List<Stock> stocks;

    public StocksDataRefresher(MainActivity mainActivity, List<Stock> stocks) {
        this.mainActivity = mainActivity;
        this.stocks = stocks;
    }

    @Override
    public void run() {
        List<Stock> refreshedStocks = new ArrayList<>();
        for (Stock stock : stocks) {
            Stock refreshedStock = downloadStockData(stock.getSymbol());
            if (refreshedStock != null) {
                refreshedStocks.add(refreshedStock);
            }
        }

        mainActivity.runOnUiThread(() -> {
            if (!refreshedStocks.isEmpty()) {
                mainActivity.replaceStocks(refreshedStocks);
            }
            mainActivity.finishedRefreshing();
        });
    }

    private static final String URL_BEGIN = "https://cloud.iexapis.com/stable/stock/";
    private static final String URL_END = "/quote?token=pk_0c8d92259c164874a9761bead5cf8e3f";

    public Stock downloadStockData(String stockSymbol) {
        Uri dataUri = Uri.parse(URL_BEGIN + stockSymbol + URL_END);
        String urlToUse = dataUri.toString();
        Log.d(TAG, "run: " + urlToUse);

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: HTTP ResponseCode NOT OK: " + conn.getResponseCode());
                return null;
            }
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            Log.d(TAG, "run: " + sb.toString());
        } catch (Exception e) {
            Log.e(TAG, "run: ", e);
            return null;
        }

        return process(sb.toString());
    }

    private Stock process(String stockJson) {
        Stock stock = new Stock();
        try {
            JSONObject jsonObject = new JSONObject(stockJson);
            stock.setSymbol(jsonObject.getString("symbol"));
            stock.setCompanyName(jsonObject.optString("companyName"));
            stock.setLatestPrice(jsonObject.optDouble("latestPrice", 0));
            stock.setChange(jsonObject.optDouble("change", 0));
            stock.setChangePercent(jsonObject.optDouble("changePercent", 0));
        } catch (JSONException e) {
            Log.e(TAG, "Could not process stock json response: " + stockJson, null);
            return null;
        }

        return stock;
    }
}
