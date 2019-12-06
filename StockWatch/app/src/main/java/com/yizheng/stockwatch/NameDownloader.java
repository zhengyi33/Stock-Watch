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
import java.util.HashMap;

import javax.security.auth.login.LoginException;

public class NameDownloader extends AsyncTask<String, Void, String > {

    private static final String TAG = "NameDownloader";

    private static final String apiBase = "https://api.iextrading.com/1.0/ref-data/symbols";

    private static final HashMap<String, String> hashMap = new HashMap<>();

    @Override
    protected void onPostExecute(String s){
        parseJson(s);
    }

    public static boolean hashMapIsNull(){
        return hashMap==null;
    }

    public static ArrayList<String> getMatches(String s){
        ArrayList<String> a = new ArrayList<>();
        for(String key: hashMap.keySet()){
            String value = hashMap.get(key);
            if (key.toUpperCase().contains(s)){
                a.add(key+" - "+value);
            }
            else if (value.toUpperCase().contains(s)){
                a.add(key+" - "+value);
            }
        }
        return a;
    }

    @Override
    protected String doInBackground(String... strings) {
        Uri dataUri = Uri.parse(apiBase);
        String urlToUse = dataUri.toString();
        StringBuilder stringBuilder = new StringBuilder();
        try{
            URL url = new URL(urlToUse);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null){
                stringBuilder.append(line).append('\n');
            }
        }catch (Exception e){
            Log.e(TAG, "doInBackground: ", e);
            return null;
        }

        return stringBuilder.toString();
    }

    private void parseJson(String s){
        try{
            JSONArray jsonArray = new JSONArray(s);
            for (int i = 0; i<jsonArray.length(); i++){
                JSONObject jStock = jsonArray.getJSONObject(i);
                String symbol = jStock.getString("symbol");
                String name = jStock.getString("name");
                hashMap.put(symbol,name);
            }
        }catch(Exception e){
            Log.d(TAG, "parseJson: "+e);
        }
    }
}
