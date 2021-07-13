package com.gladystoledo.newsgateway;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    static final String ARTICLE_FROM_SERVICE = "ARTICLE_FROM_SERVICE";
    static final String MESSAGE_FROM_SERVICE = "MESSAGE_FROM_SERVICE";
    static final String ARTICLE_DATA = "ARTICLE_DATA";
    static final String COUNT_DATA = "COUNT_DATA";
    static final String MESSAGE_DATA = "MESSAGE_DATA";
    static final String ACTION_NEWS_STORY = "ACTION_NEWS_STORY";

    static final String ACTION_MSG_TO_SVC = "ACTION_MSG_TO_SVC";

    NewsReceiver nr = new NewsReceiver();

    private final ArrayList<SpannableString> categoryDisplayed = new ArrayList<SpannableString>();
    private final HashMap<String, ArrayList<Source>> articleData = new HashMap<>();
    private final HashMap<String, Source> articleNameToSource = new HashMap<>();
    private final HashMap<String, Integer> categoryToColor = new HashMap<>();
    private int[] colors;
    private Menu opt_menu;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private List<Fragment> fragments;
    private MyPageAdapter pageAdapter;
    private ArrayAdapter<SpannableString> arrayAdapter;
    private ViewPager pager;
    private SpannableString currentSource;
    private String currentId;
    ArrayList<Source> allSources = new ArrayList<>();
    ArrayList<SpannableString> sourceNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, NewsService.class);
        startService(intent);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.drawer_list);

        // Set up the drawer item click callback method
        mDrawerList.setOnItemClickListener(
                (parent, view, position, id) -> {
                    onItemClick(position);
                    mDrawerLayout.closeDrawer(mDrawerList);
                }
        );

        // Create the drawer toggle
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        );

        fragments = new ArrayList<>();

        Resources res = getResources();
        colors = new int[] {res.getColor(R.color.red),res.getColor(R.color.orange),res.getColor(R.color.pink),res.getColor(R.color.mint),res.getColor(R.color.green),res.getColor(R.color.cerulean),res.getColor(R.color.light_blue),res.getColor(R.color.purple_200),res.getColor(R.color.maroon)};

        /*arrayAdapter = new ArrayAdapter<>(this, R.layout.drawer_item, sourceNames);
        mDrawerList.setAdapter(arrayAdapter);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }*/

        pageAdapter = new MyPageAdapter(getSupportFragmentManager());
        pager = findViewById(R.id.viewpager);
        pager.setAdapter(pageAdapter);

        // Load the data
        if (articleData.isEmpty()) {
            SourceDownloadRunnable arl = new SourceDownloadRunnable(this);
            new Thread(arl).start();
        }

    }

    public void setSources(HashMap<String, HashSet<Source>> categoryIn) {

        articleData.clear();

        for (String s : categoryIn.keySet()) {
            HashSet<Source> hSet = categoryIn.get(s);
            if (hSet == null)
                continue;

            ArrayList<Source> subSources = new ArrayList<>(hSet); //ex: cnn
            for(int i = 0; i<subSources.size();i++){
                allSources.add(subSources.get(i));
            }
            Collections.sort(subSources);
            articleData.put(s, subSources);
        }
        Collections.sort(allSources);
        articleData.put("All", allSources);

        ArrayList<String> tempList = new ArrayList<>(articleData.keySet());

        Collections.sort(tempList);
        //TODO: Change Menu
        /*for (String s : tempList)
            opt_menu.add(s);*/


        ArrayList<Source> lst = articleData.get(tempList.get(0));
        //ArrayList<SpannableString> sourcelst = new ArrayList<>();
        for(int i = 0; i<lst.size();i++){
            if (!articleNameToSource.containsKey(lst.get(i).toString())) {
                articleNameToSource.put(lst.get(i).toString(), lst.get(i));
            }
            SpannableString spanString = new SpannableString(lst.get(i).getName());
            categoryDisplayed.add(spanString);
        }
        //Collections.sort(sourcelst);
        /*
        if (lst != null) {
            //categoryDisplayed.addAll(sourcelst);
        }
         */

        makeMenu(tempList);

        arrayAdapter = new ArrayAdapter<>(this, R.layout.drawer_item, categoryDisplayed);
        mDrawerList.setAdapter(arrayAdapter);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    //TODO: makeMenu Change Colors
    public void makeMenu(ArrayList<String> tempList){
        opt_menu.clear();
        for(int i = 0; i<tempList.size(); i++){
            int count = i;
            if(i>= colors.length){
                count = 0;
            }
            opt_menu.add(tempList.get(i));
            SpannableString spanString = new SpannableString(opt_menu.getItem(i).getTitle().toString());
            spanString.setSpan(new ForegroundColorSpan(colors[count]), 0, spanString.length(), 0);
            categoryToColor.put(tempList.get(i), colors[count]);
            MenuItem menuItem = opt_menu.getItem(i);
            menuItem.setTitle(spanString);
        }

        for(int i = 0; i<categoryDisplayed.size();i++){
            SpannableString spanString = new SpannableString(categoryDisplayed.get(i));
            String sString = String.valueOf(spanString);

            String category = articleNameToSource.get(sString).getCategory();
            Integer catColor = categoryToColor.get(category);

            spanString.setSpan(new ForegroundColorSpan(categoryToColor.get(category)), 0, spanString.length(), 0);
            categoryDisplayed.set(i,spanString);
        }
    }

    public void reDoFragments(ArrayList<Article> articlesList) {
        Log.d(TAG, "reDoFragments: ");
        //TODO
        Log.d(TAG, "setArticles: START");
        setTitle(currentSource);

        Log.d(TAG, "reDoFragments: count = "+pageAdapter.getCount());
        for (int i = 0; i < pageAdapter.getCount(); i++)
            pageAdapter.notifyChangeInPosition(i);

        fragments.clear();
        Log.d(TAG, "setArticles: size = " + articlesList.size());
        int n = articlesList.size();
        if(n>10){
            n = 10;
        }
        Log.d(TAG, "setArticles: n = " + n);
        for (int i = 0; i < n; i++) {

            fragments.add(
                    ArticleFragment.newInstance(articlesList.get(i), i+1, articlesList.size()));

        }

        pageAdapter.notifyDataSetChanged();
        pager.setCurrentItem(0);
    }
    

    private void onItemClick(int position) {

        pager.setBackground(null);

        currentSource = categoryDisplayed.get(position);

        Source currSource = articleNameToSource.get(String.valueOf(categoryDisplayed.get(position)));

        //TODO: This broadcasts to NewsService;
        Intent intent = new Intent(ACTION_MSG_TO_SVC);
        intent.putExtra("SOURCE", currSource);
        Log.d(TAG, "onItemClick: source = " + currSource.getId());
        sendBroadcast(intent);
        Log.d(TAG, "onItemClick: intent = " + intent.getAction());


        mDrawerLayout.closeDrawer(mDrawerList);

    }

    // You need the 2 below to make the drawer-toggle work properly:

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: ");
        Log.d(TAG, "onOptionsItemSelected: "  + mDrawerToggle.onOptionsItemSelected(item));
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            Log.d(TAG, "onOptionsItemSelected: mDrawerToggle " + item);
            return true;
        }

        setTitle(item.getTitle());

        categoryDisplayed.clear();
        ArrayList<Source> lst = articleData.get(item.getTitle().toString());
        ArrayList<SpannableString> lstSources = new ArrayList<>();
            for(int i = 0; i<lst.size();i++){
                SpannableString spanString = new SpannableString(lst.get(i).toString());
                lstSources.add(spanString);
            }

        if (lstSources != null) {
            categoryDisplayed.addAll(lstSources);
        }


        for(int i = 0; i<categoryDisplayed.size();i++){
            SpannableString spanString = new SpannableString(categoryDisplayed.get(i));
            String sString = String.valueOf(spanString);

            String category = articleNameToSource.get(sString).getCategory();
            Integer catColor = categoryToColor.get(category);

            spanString.setSpan(new ForegroundColorSpan(categoryToColor.get(category)), 0, spanString.length(), 0);
            categoryDisplayed.set(i,spanString);
        }

        arrayAdapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(item);

    }
    // You need this to set up the options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        opt_menu = menu;
        return true;
    }

    @Override
    protected void onResume() {
        //TODO: onResume
        IntentFilter filter1 = new IntentFilter(MainActivity.ACTION_NEWS_STORY);
        registerReceiver(nr, filter1);
        super.onResume();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(nr);
        super.onStop();
    }
    ////////////////////////////////////// Fragment //////////////////////////////////////

    private class MyPageAdapter extends FragmentPagerAdapter {
        private long baseId = 0;


        MyPageAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public long getItemId(int position) {
            // give an ID different from position when position has been changed
            return baseId + position;
        }

        /**
         * Notify that the position of a fragment has been changed.
         * Create a new ID for each position to force recreation of the fragment
         * @param n number of items which have been changed
         */
        void notifyChangeInPosition(int n) {
            // shift the ID returned by getItemId outside the range of all previous fragments
            baseId += getCount() + n;
        }

    }

////////////////////////////////////// News Receiver //////////////////////////////////////
//TODO: News Receiver
    private class NewsReceiver extends BroadcastReceiver {
    private static final String TAG = "NewsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null || context == null){
            return;
        }
        if(intent.getAction().equals(MainActivity.ACTION_NEWS_STORY)){
            Log.d(TAG, "onReceive: MainActivity");
            ArrayList<Article> aList = (ArrayList<Article>) intent.getSerializableExtra("ARTICLE_DATA");
            reDoFragments(aList);
            
        }

    }
}
}
