package com.gladystoledo.newsgateway;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

public class ArticleDownloaderRunnable  implements Runnable{
    private final NewsService newsService;
    private static final String TAG = "ArticleDownloaderRunnab";
    private String dataURL = "https://newsapi.org/v2/top-headlines?sources=";
    private String sources = "";
    private final String apiKey = "&language=en&apiKey=" + "5e40f039f4634f8dbcd5bf641551c7d3";
    private int count = 0;

    public ArticleDownloaderRunnable(NewsService newsService, Source sources) { //sources is article id

        this.newsService = newsService;
        this.sources = sources.getId();
        this.dataURL = this.dataURL + this.sources + this.apiKey;
        Log.d(TAG, "ArticleDownloaderRunnable: dataURL = "+ dataURL);
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
            Log.d(TAG, "run: Before is");
            InputStream is = conn.getInputStream();
            Log.d(TAG, "run: After");
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }


        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        handleResults(sb.toString());
    }
    private void handleResults(String sourceInfo) {
        if (sourceInfo == null) {
            Log.d(TAG, "handleResults: Failure in data download");
            return;
        }

        final ArrayList<Article> aList = parseJSON(sourceInfo);
        newsService.setArticles(aList);
    }

    private ArrayList<Article> parseJSON(String s) {

        ArrayList<Article> articleList = new ArrayList<>();
        try {
            Log.d(TAG, "parseJSON: Start");
            JSONObject jObjMain = new JSONObject(s);
            JSONArray jObjMainArray = jObjMain.getJSONArray("articles");
            
            // Here we only want to regions and subregions
            for (int i = 0; i < jObjMainArray.length(); i++) {

                JSONObject jSource = (JSONObject) jObjMainArray.get(i);
                String author = jSource .getString("author");
                Log.d(TAG, "parseJSON: author = " + author);
                String title = jSource .getString("title");
                String description = jSource .getString("description");
                String url = jSource .getString("url");
                String urlToImage = jSource .getString("urlToImage");
                String publishedAt = jSource .getString("publishedAt");

                articleList.add(
                        new Article(author, title, description, url, urlToImage, publishedAt));

            }
            return articleList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
