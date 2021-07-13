package com.gladystoledo.newsgateway;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class NewsService extends Service {
    private static final String TAG = "NewsService";

    private boolean running = true;
    ArrayList<Article> articlesList = new ArrayList<>();
    private String id = "";
    private int count = 1;
    ServiceReceiver sr = new ServiceReceiver();

    @Override
    public void onCreate() {
        super.onCreate();
        //IntentFilter filter1 = new IntentFilter(MainActivity.ACTION_NEWS_STORY);
        //registerReceiver(sr, filter1);
        IntentFilter filter = new IntentFilter(MainActivity.ACTION_MSG_TO_SVC);
        registerReceiver(sr,filter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //IntentFilter filter = new IntentFilter(MainActivity.ACTION_MSG_TO_SVC);
        //registerReceiver(sr,filter);

        //Creating new thread for my service
        //ALWAYS write your long running tasks
        // in a separate thread, to avoid an ANR

        new Thread(() -> {

            while (running) {
                Log.d(TAG, "onStartCommand: ");

                try {
                    //noinspection BusyWait
                    Thread.sleep(250);
                    //Log.d(TAG, "onStartCommand: id = " + id);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(!articlesList.isEmpty()){
                    sendArticle(articlesList);
                }

            }

            sendMessage("Service Thread Stopped");

            Log.d(TAG, "run: Ending loop");
        }).start();

        return Service.START_NOT_STICKY;
    }

    private void sendArticle(ArrayList<Article> aList) {
        Log.d(TAG, "sendArticle: ");
        Intent intent = new Intent();
        intent.setAction(MainActivity.ACTION_NEWS_STORY);
        intent.putExtra(MainActivity.ARTICLE_DATA, aList);
        intent.putExtra(MainActivity.COUNT_DATA, count++);
        sendBroadcast(intent);
        this.articlesList.clear();
    }

    private void sendMessage(String msg) {
        Intent intent = new Intent();
        intent.setAction(MainActivity.MESSAGE_FROM_SERVICE);
        intent.putExtra(MainActivity.MESSAGE_DATA, msg);
        sendBroadcast(intent);
    }
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        sendMessage("Service Destroyed");
        unregisterReceiver(sr);
        running = false;
        super.onDestroy();
    }

    public void setArticles(ArrayList<Article> articlesList) {
        this.articlesList.clear();
        this.articlesList.addAll(articlesList);
    }

    ////////////////////////////////////// Service Receiver //////////////////////////////////////
private class ServiceReceiver extends BroadcastReceiver{
        private static final String TAG = "ServiceReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ");
        if(intent == null || context == null){
            return;
        }
        Log.d(TAG, "onReceive: intent = " + intent.getAction());
        if(intent.getAction().equals(MainActivity.ACTION_MSG_TO_SVC)){
            Source source = (Source) intent.getSerializableExtra("SOURCE");
            Log.d(TAG, "onReceive: Before ArticleRunnable");
            ArticleDownloaderRunnable articleDownloaderRunnable = new ArticleDownloaderRunnable(NewsService.this,source);
            new Thread(articleDownloaderRunnable).start();
        }

    }
}

}