package com.gladystoledo.newsgateway;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ArticleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ArticleFragment extends Fragment {
    private static final String TAG = "ArticleFragment";

    TextView headline;
    TextView date;
    TextView authors;
    ImageView articleImage;
    TextView articleText;
    TextView page;

    // TODO: Rename and change types of parameters
    private String mArticle;
    private String mIndex;
    private String mMax;

    public ArticleFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param article Parameter 1.
     * @param index Parameter 2.
     * @param max Parameter 3.
     * @return A new instance of fragment ArticleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ArticleFragment newInstance(Article article, int index, int max) {
        ArticleFragment fragment = new ArticleFragment();
        Bundle args = new Bundle();
        args.putSerializable("ARTICLE", article);
        args.putSerializable("INDEX", index);
        args.putSerializable("MAX", max);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mArticle = getArguments().getString("ARTICLE");
            mIndex = getArguments().getString("INDEX");
            mMax = getArguments().getString("MAX");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Start");
        // Inflate the layout for this fragment
        View fragment_layout = inflater.inflate(R.layout.fragment_article, container, false);

        Bundle args = getArguments();
        if (args != null) {
            final Article currentArticle = (Article) args.getSerializable("ARTICLE");
            if (currentArticle == null) {
                return null;
            }
            int index = args.getInt("INDEX");
            int total = args.getInt("MAX");

            headline = fragment_layout.findViewById(R.id.headline);
            date = fragment_layout.findViewById(R.id.date);
            authors = fragment_layout.findViewById(R.id.authors);
            articleImage = fragment_layout.findViewById(R.id.articleImage);
            articleText = fragment_layout.findViewById(R.id.articleText);
            page = fragment_layout.findViewById(R.id.page);

            if(currentArticle.getTitle().equals("") || currentArticle.getTitle().equals("null")){
                headline.setText(" ");
            }else{
                headline.setText(currentArticle.getTitle());
            }




            if(currentArticle.getPublishedAt().equals("") || currentArticle.getPublishedAt().equals("null")){
                date.setText(" ");
            }else{
                date.setText(currentArticle.getPublishedAt());
            }
            //TODO: formate date

            if(currentArticle.getAuthor().equals("") || currentArticle.getAuthor().equals("null")){
                authors.setText(" ");
            }else{
                authors.setText(currentArticle.getAuthor());
            }


            if(!currentArticle.getUrlToImage().isEmpty()){
                loadRemoteImage(currentArticle.getUrlToImage());
            }


            if(currentArticle.getDescription().equals("") || currentArticle.getDescription().equals("null")){
                articleText.setText(" ");
            }else{
                articleText.setText(currentArticle.getDescription());
            }

            if(!currentArticle.getUrl().isEmpty()){
                headline.setOnClickListener(v -> clickArticle(currentArticle.getUrl()));
                articleImage.setOnClickListener(v -> clickArticle(currentArticle.getUrl()));
                articleText.setOnClickListener(v -> clickArticle(currentArticle.getUrl()));
            }

            page.setText(String.format(Locale.US, "%d of %d", index, total));

            Log.d(TAG, "onCreateView: END");
            return fragment_layout;
        } else {
            return null;
        }
        
    }
    public void clickArticle(String url) {
        Log.d(TAG, "clickArticle: title "+ headline.getText().toString());
        String URL = url;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
        startActivity(intent);
    }

    private void makeErrorAlert(String msg) {
        Log.d(TAG, "makeErrorAlert: msg = " + msg);
    }

    private void loadRemoteImage(final String imageURL) {
        // Needs gradle  implementation 'com.squareup.picasso:picasso:2.71828'
        Log.d(TAG, "loadRemoteImage: ");
        Picasso.get().load(imageURL)
                .error(R.drawable.no_image_available)
                .placeholder(R.drawable.no_image_available)
                .into(articleImage);
    }
}