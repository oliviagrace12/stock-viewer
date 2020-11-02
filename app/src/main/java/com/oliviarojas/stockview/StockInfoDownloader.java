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

public class StockInfoDownloader implements Runnable {

    private static final String TAG = "StockInfoDownloader";

    private static final String URL_BEGIN = "https://cloud.iexapis.com/stable/stock/";
    private static final String URL_END = "/quote?token=pk_0c8d92259c164874a9761bead5cf8e3f";
    private final String stockSymbol;
    private final MainActivity mainActivity;

    public StockInfoDownloader(MainActivity mainActivity, String stockSymbol) {
        this.mainActivity = mainActivity;
        this.stockSymbol = stockSymbol;
    }

    @Override
    public void run() {
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
                return;
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
            return;
        }

        try {
            process(sb.toString());
        } catch (JSONException e) {
            Log.w(TAG, "run: Could not process json: " + sb.toString());
        }
    }

    private void process(String stockJson) throws JSONException {
        Stock stock = new Stock();
        JSONObject jsonObject = new JSONObject(stockJson);
        stock.setSymbol(jsonObject.getString("symbol"));
        stock.setCompanyName(jsonObject.optString("companyName"));
        stock.setLatestPrice(jsonObject.optDouble("latestPrice", 0));
        stock.setChange(jsonObject.optDouble("change", 0));
        stock.setChangePercent(jsonObject.optDouble("changePercent", 0));

        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.addStock(stock);
            }
        });
    }
}
