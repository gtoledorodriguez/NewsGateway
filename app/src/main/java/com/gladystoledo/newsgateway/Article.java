package com.gladystoledo.newsgateway;

import android.util.Log;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Article implements Serializable {
    private static final String TAG = "Article";
    private ArrayList<SimpleDateFormat> knownPatterns = new ArrayList<>();
    private final String author;
    private final String title;
    private final String description;
    private final String url;
    private final String urlToImage;
    private String publishedAt;

    public Article(String author, String title, String description, String url, String urlToImage, String publishedAt) {
        Log.d(TAG, "Article: New");
        fillKnownPatterns();
        this.author = author;
        this.title = title;
        this.description = description;
        this.url = url;
        this.urlToImage = urlToImage;
        setPublishedAt(publishedAt); //date

    }
    public void fillKnownPatterns(){
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm.ss'Z'"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"));
    }
    public void setPublishedAt(String publishedAt){
        Log.d(TAG, "setPublishedAt: Start");
        String date = "";
        boolean parse = false;
        Date d = new Date();
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");

        for (int i = 0; i<knownPatterns.size();i++){
            SimpleDateFormat sdf = knownPatterns.get(i);
            try {
                d = sdf.parse(publishedAt);
                parse = true;
                Log.d(TAG, "setPublishedAt: d = " + d);
            }
            catch (ParseException excpt) {
                excpt.printStackTrace();
            }
            if(parse == true){
                date = outputFormat.format(d);
                parse = false;
                break;
            }
            Log.d(TAG, "setPublishedAt: date");
        }
        this.publishedAt = date;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public String getUrlToImage() {
        return urlToImage;
    }

    public String getPublishedAt() {
        return publishedAt;
    }
}
