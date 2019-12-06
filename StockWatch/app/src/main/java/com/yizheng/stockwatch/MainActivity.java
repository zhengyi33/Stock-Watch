package com.yizheng.stockwatch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

//sk_5d2e5450050d47669e8d382e199f5c78

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "MainActivity";
    private ArrayList<Stock> stocks = new ArrayList<>();

    private ArrayList<Stock> aux = new ArrayList<>();

    private RecyclerView recyclerView;
    private StockAdapter stockAdapter;
    private SwipeRefreshLayout swiper;

    private static final String token = "sk_5d2e5450050d47669e8d382e199f5c78";

    private static final String marketWatch = "http://www.marketwatch.com/investing/stock";

    private int asyncCounter = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycler);
        stockAdapter = new StockAdapter(stocks, this);
        recyclerView.setAdapter(stockAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(() -> doRefresh());
        doRead();

        Boolean connected = doNetCheck();

        if (connected == null){
            Log.d(TAG, "doRead: Cannot access ConnectivityManager");
            return;
        }

        else if (!connected){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            //builder.setMessage("Stock Cannot Be Added Without A Network Connection");
            builder.setTitle("No Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
            for (Stock s: aux){
                stocks.add(new Stock(s.getStockSymbol(),s.getCompanyName(),0,0,0));
            }
            Collections.sort(stocks);
            stockAdapter.notifyDataSetChanged();
            return;
        }

        else {
            new NameDownloader().execute();
            for (Stock s: aux){
                //new FinancialDownloader(this).execute(s.getStockSymbol(),token);
                doFinancialDownload(s.getStockSymbol());
            }
            return;
        }
        //makeList();
    }

    private void doFinancialDownload(String symbol){
        new FinancialDownloader(this).execute(symbol,token);
        asyncCounter++;
    }

    private void doRefresh() {
        //Toast.makeText(this, TAG, Toast.LENGTH_SHORT).show();
        Boolean connected = doNetCheck();

        if (!connected || connected == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Stock Cannot Be Updated Without A Network Connection");
            builder.setTitle("No Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
            for (Stock s: stocks){
                s.setPrice(0);
                s.setPriceChange(0);
                s.setChangePercentage(0);
            }
            stockAdapter.notifyDataSetChanged();
            swiper.setRefreshing(false);
            return;
        }
        stocks.clear();
        doRead();
        if (aux.isEmpty()){
            swiper.setRefreshing(false);
        }
        for(Stock s: aux){
            //new FinancialDownloader(this).execute(s.getStockSymbol(),token);
            doFinancialDownload(s.getStockSymbol());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add_menu_icon:
                Boolean connected = doNetCheck();

                if (!connected || connected == null){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Stock Cannot Be Added Without A Network Connection");
                    builder.setTitle("No Network Connection");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return true;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Stock Selection");
                builder.setMessage("Please enter a stock symbol:");
                EditText editText = new EditText(this);
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                editText.setGravity(Gravity.CENTER_HORIZONTAL);
                editText.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
                builder.setView(editText);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(MainActivity.this, TAG + editText.getText(), Toast.LENGTH_SHORT).show();
                        String userInput = editText.getText().toString();
//                        for (Stock s: stocks){
//                            if (s.getStockSymbol().toUpperCase().equals(userInput)){
//                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//                                builder.setIcon(R.drawable.baseline_warning_black_48);
//                                builder.setMessage("Stock Symbol "+userInput+" is already displayed");
//                                builder.setTitle("Duplicate Stock");
//                                AlertDialog ad = builder.create();
//                                ad.show();
//                                return;
//                            }
//                        }
                        if (NameDownloader.hashMapIsNull()){
                            new NameDownloader().execute();
                        }
                        ArrayList<String> a = NameDownloader.getMatches(userInput);
                        if (a.isEmpty()){
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("Data for stock symbol");
                            builder.setTitle("Symbol Not Found: "+userInput);
                            AlertDialog ad = builder.create();
                            ad.show();
                        }
                        else if (a.size() == 1){
                            String symbol = a.get(0).split("-")[0].trim();
                            for (Stock s: stocks){
                                if (s.getStockSymbol().toUpperCase().equals(symbol)){
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setIcon(R.drawable.baseline_warning_black_48);
                                    builder.setMessage("Stock Symbol "+s.getStockSymbol().toUpperCase()+" is already displayed");
                                    builder.setTitle("Duplicate Stock");
                                    AlertDialog ad = builder.create();
                                    ad.show();
                                    return;
                                }
                            }
                            //new FinancialDownloader(MainActivity.this).execute(symbol,token);
                            doFinancialDownload(symbol);
                        }
                        else {
                            CharSequence[] sArray = new CharSequence[a.size()];
                            for (int i = 0; i < a.size(); i++) {
                                sArray[i] = a.get(i);
                            }
                            Arrays.sort(sArray);
                            AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                            b.setTitle("Make a selection");
                            b.setItems(sArray, (d, w) -> {
                                String symbol = sArray[w].toString().split("-")[0].trim();
                                for (Stock s: stocks){
                                    if (s.getStockSymbol().toUpperCase().equals(symbol)){
                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                        builder.setIcon(R.drawable.baseline_warning_black_48);
                                        builder.setMessage("Stock Symbol "+s.getStockSymbol().toUpperCase()+" is already displayed");
                                        builder.setTitle("Duplicate Stock");
                                        AlertDialog ad = builder.create();
                                        ad.show();
                                        return;
                                    }
                                }
                                //new FinancialDownloader(MainActivity.this).execute(symbol, token);
                                doFinancialDownload(symbol);
                            });
                            b.setNegativeButton("NEVERMIND", (d, w) -> {
                            });
                            AlertDialog ad = b.create();
                            ad.show();
                        }
                    }
                });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        //Toast.makeText(this, TAG, Toast.LENGTH_SHORT).show();
        int pos = recyclerView.getChildLayoutPosition(v);
        Stock s = stocks.get(pos);
        String symbol = s.getStockSymbol();
        Uri.Builder builder = Uri.parse(marketWatch).buildUpon();
        builder.appendEncodedPath(symbol);
        Uri uri = builder.build();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    public boolean onLongClick(View v) {
        int pos = recyclerView.getChildLayoutPosition(v);
        String symbol = stocks.get(pos).getStockSymbol();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.baseline_delete_black_48);
        builder.setTitle("Delete Stock");
        builder.setMessage("Delete Stock Symbol " + symbol + "?");
        builder.setPositiveButton("DELETE", (DialogInterface dialog, int which) -> {
            stocks.remove(pos);
            doWrite();
            stockAdapter.notifyDataSetChanged();
        });
        builder.setNegativeButton("CANCEL", (dialog, which) -> {
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }

//    private void makeList() {
//        for (int i = 0; i < 10; i++) {
//            Stock n = new Stock("symbol " + i, "name " + i, i * 1.0, i * 1.0 - 5, i * 1.0);
//            stocks.add(n);
//        }
//    }

    public void acceptResult(Stock s){
        if (s == null){
            Log.d(TAG, "acceptResult: no result comes back");
            return;
        }
        stocks.add(s);
        Collections.sort(stocks);
        asyncCounter--;
        if (asyncCounter == 0) {
            stockAdapter.notifyDataSetChanged();
            swiper.setRefreshing(false);
            doWrite();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        doWrite();
    }

    private void doWrite(){

        JSONArray jsonArray = new JSONArray();

        for (Stock s: stocks){
            try{
                JSONObject stockJson = new JSONObject();
                stockJson.put("symbolText", s.getStockSymbol());
                stockJson.put("nameText", s.getCompanyName());
                stockJson.put("price", s.getPrice());
                stockJson.put("change", s.getPriceChange());
                stockJson.put("changePercent", s.getChangePercentage());
                jsonArray.put(stockJson);
            }catch (JSONException e){
                Log.d(TAG, "doWrite: "+e.toString());
            }
        }

        String jsonText = jsonArray.toString();

        try{
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("stocks.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(jsonText);
            outputStreamWriter.close();
        }catch (IOException e){
            Log.d(TAG, "doWrite: File write failed: "+e.toString());
        }
    }

    private Boolean doNetCheck(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null){
            return null;
        }
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()){
            return true;
        }
        else{
            return false;
        }
    }

//    private void updateData(String symbol){
//        new FinancialDownloader(this).execute(symbol, token);
//    }

    private void doRead(){

        try{
            InputStream inputStream = openFileInput("stocks.txt");
            if (inputStream != null){
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null){
                    stringBuilder.append(receiveString);
                }

                inputStream.close();

                String jsonText = stringBuilder.toString();
                JSONArray jsonArray = new JSONArray(jsonText);
                aux.clear();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String symbol = jsonObject.getString("symbolText");
                    String name = jsonObject.getString("nameText");
                    double price = jsonObject.getDouble("price");
                    double change = jsonObject.getDouble("change");
                    double changePercent = jsonObject.getDouble("changePercent");
                    Stock s = new Stock(symbol, name,price,change,changePercent);
                    aux.add(s);
                }
                /*try{
                    JSONArray jsonArray = new JSONArray(jsonText);

                    Boolean connected = doNetCheck();

                    if (connected == null){
                        Log.d(TAG, "doRead: Cannot access ConnectivityManager");
                        return;
                    }

                    else if (!connected){
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage("Stock Cannot Be Added Without A Network Connection");
                        builder.setTitle("No Network Connection");
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    else {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String symbol = jsonObject.getString("symbolText");
                            //String name = jsonObject.getString("nameText");
                            //Stock s = new Stock(symbol, name, -1,-1,-1);
                            updateData(symbol);
                            //stocks.add(s);
                        }
                    }
                }catch(JSONException e){
                    Log.d(TAG, "doRead: "+e.toString());
                }*/
            }
        }catch (FileNotFoundException e){
            Log.d(TAG, "doRead: File not found: "+e.toString());
        }catch (IOException e){
            Log.d(TAG, "doRead: Can not read file: "+e.toString());
        }catch(JSONException e){
            Log.d(TAG, "doRead: "+e.toString());
        }
    }
}
