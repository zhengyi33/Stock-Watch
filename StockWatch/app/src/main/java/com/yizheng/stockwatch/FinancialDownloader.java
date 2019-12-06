package com.yizheng.stockwatch;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class FinancialDownloader extends AsyncTask<String, Void, String> {

    private static final String TAG = "FinancialDownloader";

    private static final String apiBase = "https://cloud.iexapis.com/stable/stock";

    private MainActivity ma;

    public FinancialDownloader(MainActivity ma) {
        this.ma = ma;
    }

    @Override
    protected void onPostExecute(String s){
        Stock stock = parseJson(s);
        if (stock != null){
            ma.acceptResult(stock);
        }
        else {
            ma.acceptResult(null);
        }
    }

    @Override
    protected String doInBackground(String... strings) {
        String symbol = strings[0];
        String token = strings[1];
        Uri.Builder builder = Uri.parse(apiBase).buildUpon();
        builder.appendEncodedPath(symbol);
        builder.appendEncodedPath("quote");
        builder.appendQueryParameter("token", token);
        String urlToUse = builder.build().toString();

        StringBuilder sb = new StringBuilder();
        try{
            URL url = new URL(urlToUse);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }catch(Exception e){
            Log.e(TAG, "doInBackground: ", e);
            return null;
        }
        return sb.toString();
    }

    private Stock parseJson(String s){
        Stock stock;
        try{
            JSONObject jsonObject = new JSONObject(s);
            String symbol = jsonObject.getString("symbol");
            String companyName  = jsonObject.getString("companyName");
            double latestPrice = jsonObject.getDouble("latestPrice");
            double change = jsonObject.getDouble("change");
            double changePercent = jsonObject.getDouble("changePercent");
            stock = new Stock(symbol,companyName,latestPrice,change,changePercent*100);
        }catch(Exception e){
            Log.d(TAG, "parseJson: "+e);
            return null;
        }
        return stock;
    }
}
