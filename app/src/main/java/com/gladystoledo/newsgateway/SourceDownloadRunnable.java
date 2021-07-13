package com.gladystoledo.newsgateway;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;

public class SourceDownloadRunnable implements Runnable{
    private final MainActivity mainActivity;
    private static final String TAG = "SourceDownloadRunnable";
    private String dataURL = "https://newsapi.org/v2/sources?language=en&country=us&category=";
    private String category = "";
    private final String apiKey = "&apiKey=" + "5e40f039f4634f8dbcd5bf641551c7d3";
    private int count = 0;

    public SourceDownloadRunnable(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.dataURL = this.dataURL + this.category + this.apiKey;
        Log.d(TAG, "SourceDownloadRunnable: dataURL = " + dataURL);
    }
    private void processResults(String s) {
        final HashMap<String, HashSet<Source>> regionMap = parseJSON(s);
        Log.d(TAG, "processResults: ");
        if (regionMap != null) {
            mainActivity.runOnUiThread(() -> mainActivity.setSources(regionMap));
        }
    }

    @Override
    public void run() {

        Uri dataUri = Uri.parse(dataURL);
        String urlToUse = dataUri.toString();

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.addRequestProperty("User-Agent","");
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        processResults(sb.toString());
    }

    private HashMap<String, HashSet<Source>> parseJSON(String s) {

        HashMap<String, HashSet<Source>> regionMap = new HashMap<>();
        try {
            Log.d(TAG, "parseJSON: Start");
            JSONObject jObjMain = new JSONObject(s);
            JSONArray jObjMainArray = jObjMain.getJSONArray("sources");
            Log.d(TAG, "parseJSON:  Before Loop");
            // Here we only want to regions and subregions
            Log.d(TAG, "parseJSON: len = " + jObjMainArray.length());
            for (int i = 0; i < jObjMainArray.length(); i++) {

                JSONObject jSource = (JSONObject) jObjMainArray.get(i);
                String category = jSource .getString("category");
                String name = jSource .getString("name");
                String id = jSource .getString("id");

                if (category.isEmpty())
                    continue;

                if (name.isEmpty())
                    name = id;

                if (!regionMap.containsKey(category))
                    regionMap.put(category, new HashSet<>());

                HashSet<Source> rSet = regionMap.get(category);
                Log.d(TAG, "parseJSON: id = " + id + ", name = " + name + ", category = " + category);
                if (rSet != null) {
                    rSet.add(new Source(id, name, category));
                }
                count++;
            }
            return regionMap;
        } catch (
                Exception e) {
            e.printStackTrace();
        }
        return null;
    }




}
