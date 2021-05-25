package com.herma.apps.novelsandbooks;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.herma.apps.novelsandbooks.ui.about.About_us;
import com.herma.apps.novelsandbooks.usefull.CategoryAdapter;
import com.herma.apps.novelsandbooks.usefull.CategoryItem;
import com.herma.apps.novelsandbooks.usefull.DBHelper;
import com.herma.apps.novelsandbooks.usefull.PaginationListener;
import com.herma.apps.novelsandbooks.usefull.PostItem;
import com.herma.apps.novelsandbooks.usefull.PostRecyclerAdapter;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ReaderActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView desc, tv;
    FloatingActionButton bookmark, fab;
    DBHelper mydb;

    int id, blogposts_count, blogwriter_id;
    String writerName, categoryName, chapterName, blogContent;

    Button btnRetry;
    public RequestQueue queue;
    public ProgressBar progressBar;

    ArrayList<Object> items;

    ///////////////////////
// Recycler View object
    RecyclerView recyclerViewRelated;
    // Array list for recycler view data source
    ArrayList<Object> itemsRelated;
    // Layout Manager
    RecyclerView.LayoutManager RelatedRecyclerViewLayoutManager;
    // adapter class object
    private PostRecyclerAdapter adapterRelated;
    // Linear Layout Manager
    LinearLayoutManager HorizontalLayoutRelated;
//    View ChildViewRelated;
//    int RecyclerViewItemPositionRelated;
    ////////////////////////////////

    ///////////////////////
// Recycler View object
RecyclerView recyclerViewCategory;
    // Array list for recycler view data source
    ArrayList<CategoryItem> source;
    // Layout Manager
    RecyclerView.LayoutManager CategoryRecyclerViewLayoutManager;
    // adapter class object
    CategoryAdapter adapterCategory;
    // Linear Layout Manager
    LinearLayoutManager HorizontalLayoutCategory;
    View ChildViewCategory;
    int RecyclerViewItemPositionCategory;
    int byCategory = 0;
    ////////////////////////////////

    private AdView adView;
    private InterstitialAd mInterstitialAd;
    private FrameLayout adContainerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
        ;

        mydb = new DBHelper(this);

        ImageButton zoomIn;
        ImageButton zoomOut;

        tv = (TextView) findViewById(R.id.tvWait);

        desc = (TextView) findViewById(R.id.desc);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        zoomIn = (ImageButton) findViewById(R.id.zoom_in);
        zoomOut = (ImageButton) findViewById(R.id.zoom_out);

        zoomIn.setOnClickListener(this);
        zoomOut.setOnClickListener(this);

        bookmark = (FloatingActionButton) findViewById(R.id.bookmark);
        bookmark.setOnClickListener(this);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        id = getIntent().getIntExtra("id", 0);
        blogposts_count = getIntent().getIntExtra("blogposts_count", 0);
        writerName = getIntent().getStringExtra("writername");
        blogwriter_id = getIntent().getIntExtra("blogwriter_id", 0);
        categoryName = getIntent().getStringExtra("name");

        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(ReaderActivity.this);
        String book_writername = pre.getString("writername", "");
        if (!book_writername.equals(writerName)) {
            setPreference();
        }

        blogContent = "";
        setter(categoryName, writerName);

        initializeChapters();

        doApiCall();

        btnRetry = (Button) findViewById(R.id.btnRetry);

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                doApiCall();
                tv.setVisibility(View.VISIBLE);
                btnRetry.setVisibility(View.INVISIBLE);

            }
        });

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        MobileAds.setRequestConfiguration(
                new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345")).build());

        adContainerView = findViewById(R.id.ad_view_container);

        // Since we're loading the banner based on the adContainerView size, we need to wait until this
        // view is laid out before we can get the width.
        adContainerView.post(new Runnable() {
            @Override
            public void run() {
                loadBanner();
            }
        });

        MainActivity.interstitialCounter++;

        if(MainActivity.interstitialCounter % 5 == 0) buildInterstitialAd();

    }

    private void buildInterstitialAd() {


        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,getString(R.string.interestitial_ad), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;

                if (mInterstitialAd != null) {
                    mInterstitialAd.show(ReaderActivity.this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                }

            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                Log.i("TAG", loadAdError.getMessage());
                mInterstitialAd = null;
            }
        });

    }

    /** Called when leaving the activity */
    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    public void setPreference(){
//    SharedPreferences sharedPref = getApplication().getPreferences(Context.MODE_PRIVATE);
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ReaderActivity.this);

    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putInt("id", id);
    editor.putInt("blogposts_count", blogposts_count);
    editor.putString("writername", writerName);
    editor.putInt("blogwriter_id", blogwriter_id);
    editor.putString("name", categoryName);
    editor.apply();

}
    private void setter(String categoryName, String writerName) {
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setTitle(Html.fromHtml(categoryName.trim(), Html.FROM_HTML_MODE_COMPACT));
                desc.setText(Html.fromHtml("<b>"+categoryName.trim() + " by " + writerName.trim()+"</b><br>Chapters/Parts:- "+blogposts_count+"<br/>" + blogContent, Html.FROM_HTML_MODE_COMPACT));
            } else {
                setTitle(Html.fromHtml(categoryName.trim()));
                desc.setText(Html.fromHtml(categoryName.trim() + " by " + writerName.trim()+"<br>Chapters/Parts:- "+blogposts_count+"<br/>" + blogContent));
            }

            checkBookmark(id);
        }catch (Exception ds){}

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_rate_us:
                Toast.makeText(ReaderActivity.this, "Rate this app :)", Toast.LENGTH_SHORT).show();
                rateApp();
                return true;
            case R.id.action_app_store:
                Toast.makeText(ReaderActivity.this, "More apps :)", Toast.LENGTH_SHORT).show();
                openUrl("https://play.google.com/store/apps/developer?id=Herma%20plc");
                return true;
            case R.id.action_about:
                startActivity(new Intent(getApplicationContext(), About_us.class));
                return true;
            case R.id.action_exit:
                System.exit(0);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void openUrl(String url) {
        Uri uri = Uri.parse(url); // missing 'http://' will cause crashed
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
    public void rateApp() {
        try {
            Intent rateIntent = rateIntentForUrl("market://details");
            startActivity(rateIntent);
        } catch (ActivityNotFoundException e) {
            Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details");
            startActivity(rateIntent);
        }
    }
    private Intent rateIntentForUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, getPackageName())));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        if (Build.VERSION.SDK_INT >= 21) {
            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        } else {
            //noinspection deprecation
            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        }
        intent.addFlags(flags);
        return intent;
    }




    @Override
    public void onClick(View view) {
        float textSize = this.desc.getTextSize() / getApplicationContext().getResources().getDisplayMetrics().density;
        switch (view.getId()) {
            case R.id.bookmark:
                updateBookmark(id, blogposts_count, writerName, blogwriter_id, categoryName, chapterName, blogContent);
                    return;
            case R.id.fab:
                Intent intent = new Intent("android.intent.action.SEND");
                intent.setType("text/plain");
                intent.putExtra("android.intent.extra.TEXT", "https://play.google.com/store/apps/details?id=" + ReaderActivity.this.getPackageName()+"\n" + desc.getText().toString());
                startActivity(Intent.createChooser(intent, "Share via"));
                return;
            case R.id.zoom_in:
                if (textSize <= 30.0f) {
                    TextView textView = this.desc;
                    textView.setTextSize(0, textView.getTextSize() + 1.0f);
                    return;
                }
                return;
            case R.id.zoom_out:
                if (textSize >= 15.0f) {
                    TextView textView2 = this.desc;
                    textView2.setTextSize(0, textView2.getTextSize() - 1.0f);
                    return;
                }
                return;
            default:
                return;
        }
    }
    private void updateBookmark(int realId, int blogposts_count, String writerName, int blogwriter_id, String categoryName, String chapterName, String content) {
        if (mydb.isBookmarked(realId)) {
            mydb.removeBookmark(realId);
            bookmark.setImageResource(R.drawable.ic_un_bookmark);
            Toast.makeText(this, "Bookmark Removed", Toast.LENGTH_SHORT).show();
        } else if (mydb.addBookmark(realId, blogposts_count, writerName, blogwriter_id, categoryName, chapterName, content)) {
            bookmark.setImageResource(R.drawable.ic_bookmark);
            Toast.makeText(this, "Bookmark Added", Toast.LENGTH_SHORT).show();
        }
    }

    public void checkBookmark(int realId) {
        if (mydb.isBookmarked(realId)) {
            this.bookmark.setImageResource(R.drawable.ic_bookmark);
        } else {
            this.bookmark.setImageResource(R.drawable.ic_un_bookmark);
        }
    }

    private void doApiCall() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                queue = Volley.newRequestQueue(getApplicationContext());
// Request a string response from the provided URL.
                String url ="https://datascienceplc.com/apps/manager/api/items/blog/show?page=1";
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url+"&v=1.0&app_id=4&company_id=1&id="+id,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (response != null) {
                                    try {
                                        // Getting JSON Array node
                                        JSONObject jsonObj = new JSONObject(response);

                                        JSONArray datas = jsonObj.getJSONArray("blogpost");
                                        JSONObject c = datas.getJSONObject(0);

                                        if(c.has("blogcontent")) {
//                                            desc.setText(c.getString("blogcontent"));

                                            chapterName = c.getString("blogtitle");
                                            blogContent = c.getString("blogcontent");

                                            setter(categoryName, writerName);

                                            tv.setVisibility(View.INVISIBLE);
                                            progressBar.setVisibility(View.INVISIBLE);

                                            setChapters(response);
                                            setRelated(response);

                                        }

                                    } catch (final JSONException e) {
                                    }

                                }
                            }

                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try{
//                            Toast.makeText(getContext(), "That didn't work! " + error, Toast.LENGTH_LONG).show();

                            if (!isOnline()) {
                                showOptionDialog(1);
                            }
                            else if (error instanceof TimeoutError || error.getCause() instanceof SocketTimeoutException
                                    || error.getCause() instanceof ConnectTimeoutException
                                    || error.getCause() instanceof SocketException
                                    || (error.getCause().getMessage() != null
                                    && error.getCause().getMessage().contains("Connection timed out"))) {
                                Toast.makeText(getApplicationContext(), "Connection timeout error. \npls try again",
                                        Toast.LENGTH_LONG).show();

                                tv.setVisibility(View.INVISIBLE);
                                btnRetry.setVisibility(View.VISIBLE);

                            } else {
                                Toast.makeText(getApplicationContext(), "An unknown error occurred.\npls try again",
                                        Toast.LENGTH_LONG).show();

                                tv.setVisibility(View.INVISIBLE);
                                btnRetry.setVisibility(View.VISIBLE);

                            }
                        }catch (Exception j){}
                    }

                })
                {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("email", "bloger_api@datascienceplc.com");//public user
                        params.put("password", "public-password");
                        params.put("Authorization", "Basic YmxvZ2VyX2FwaUBkYXRhc2NpZW5jZXBsYy5jb206cHVibGljLXBhc3N3b3Jk");
                        return params;
                    }
                };

                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                        10000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                stringRequest.setTag(this);
// Add the request to the RequestQueue.
                queue.add(stringRequest);
            }
        }, 1500);
    }

    private void doChapterApiCall() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                queue = Volley.newRequestQueue(getApplicationContext());
// Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://datascienceplc.com/apps/manager/api/items/blog/categories?company_id=1&v=1.0&app_id=4&id="+id,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (response != null) {
                                    try {
                                        // Getting JSON Array node
                                        JSONObject jsonObj = new JSONObject(response);

                                        JSONArray datas = jsonObj.getJSONArray("blogpost");
                                        JSONObject c = datas.getJSONObject(0);

                                        if(c.has("blogcontent")) {
//                                            desc.setText(c.getString("blogcontent"));

                                            chapterName = c.getString("blogtitle");
                                            blogContent = c.getString("blogcontent");

                                            setter(categoryName, writerName);

                                            tv.setVisibility(View.INVISIBLE);
                                            progressBar.setVisibility(View.INVISIBLE);

                                        }

                                    } catch (final JSONException e) {
                                    }

                                }
                            }

                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try{
//                            Toast.makeText(getContext(), "That didn't work! " + error, Toast.LENGTH_LONG).show();

                            if (!isOnline()) {
                                showOptionDialog(1);
                            }
                            else if (error instanceof TimeoutError || error.getCause() instanceof SocketTimeoutException
                                    || error.getCause() instanceof ConnectTimeoutException
                                    || error.getCause() instanceof SocketException
                                    || (error.getCause().getMessage() != null
                                    && error.getCause().getMessage().contains("Connection timed out"))) {
                                Toast.makeText(getApplicationContext(), "Connection timeout error. \npls try again",
                                        Toast.LENGTH_LONG).show();

                                tv.setVisibility(View.INVISIBLE);
                                btnRetry.setVisibility(View.VISIBLE);

                            } else {
                                Toast.makeText(getApplicationContext(), "An unknown error occurred.\npls try again",
                                        Toast.LENGTH_LONG).show();

                                tv.setVisibility(View.INVISIBLE);
                                btnRetry.setVisibility(View.VISIBLE);

                            }
                        }catch (Exception j){}
                    }

                })
                {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("email", "bloger_api@datascienceplc.com");//public user
                        params.put("password", "public-password");
                        params.put("Authorization", "Basic YmxvZ2VyX2FwaUBkYXRhc2NpZW5jZXBsYy5jb206cHVibGljLXBhc3N3b3Jk");
                        return params;
                    }
                };

                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                        10000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                stringRequest.setTag(this);
// Add the request to the RequestQueue.
                queue.add(stringRequest);
            }
        }, 1500);
    }

    private void setRelated(String response) {

        // initialisation with id's
        recyclerViewRelated
                = (RecyclerView) findViewById(
                R.id.recyclerviewRelated);
        recyclerViewRelated.setHasFixedSize(true);

        RelatedRecyclerViewLayoutManager
                = new LinearLayoutManager(ReaderActivity.this);
        // Set LayoutManager on Recycler View
        recyclerViewRelated.setLayoutManager(
                RelatedRecyclerViewLayoutManager);
        TextView tvRelated = (TextView) findViewById(R.id.tvRelated);
        // Adding items to RecyclerView.

        if (response != null) {
            itemsRelated = new ArrayList<>();
            try {
                JSONObject jsonObj = new JSONObject(response);

                // Getting JSON Array node
                JSONArray datas = jsonObj.getJSONArray("related");

                // looping through All Contacts
                for (int i = 0; i < datas.length(); i++) {
                    JSONObject c = datas.getJSONObject(i);

                    ////////////////////////////////////////
                    PostItem postItem = new PostItem();
                    postItem.setRealId(c.getInt("id"));
                    postItem.setId(c.getInt("id"));
                    postItem.setBlogposts_count(c.getInt("blogposts_count"));
                    postItem.setCategoryName(c.getString("name"));
                    try{
                    postItem.setBlogwriter_name(c.getJSONObject("blogwriter").getJSONObject("blogwriter").getString("writername"));
                    postItem.setBlogwriter_id(c.getJSONObject("blogwriter").getInt("blogwriter_id"));
                    }catch (Exception kl){}
                    itemsRelated.add(postItem);
                }

                adapterRelated = new PostRecyclerAdapter(new ArrayList<Object>());
                // Set Horizontal Layout Manager
                // for Recycler view
                HorizontalLayoutRelated
                        = new LinearLayoutManager(
                        ReaderActivity.this,
                        LinearLayoutManager.HORIZONTAL,
                        false);
                recyclerViewRelated.setLayoutManager(HorizontalLayoutRelated);
                // Set adapter on recycler view
                recyclerViewRelated.setAdapter(adapterRelated);
                adapterRelated.addItems(itemsRelated);

                tvRelated.setVisibility(View.VISIBLE);

            } catch (final JSONException e) {
            }

        }
    }


    /**
     * Check if there is the network connectivity
     *
     * @return true if connected to the network
     */
    public boolean isOnline() {
        // Get a reference to the ConnectivityManager to check the state of network connectivity
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * Show a dialog when there is no internet connection
     *
     * @param option true if connected to the network
     */
    private void showOptionDialog(final int option) {

        // Create an AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(ReaderActivity.this, R.style.Theme_AppCompat_Dialog_Alert);

        if(option == 1) {//showNetworkDialog
            // Set an Icon and title, and message
            builder.setIcon(R.drawable.ic_warning);
            builder.setTitle(getString(R.string.no_network_title));
            builder.setMessage(getString(R.string.no_network_message));
            builder.setPositiveButton(getString(R.string.go_to_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 1234);
                }
            });
        }
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        tv.setVisibility(View.INVISIBLE);
        btnRetry.setVisibility(View.VISIBLE);
    }

    private void initializeChapters() {
        // initialisation with id's
        recyclerViewCategory
                = (RecyclerView) findViewById(
                R.id.recyclerviewCategory);
        CategoryRecyclerViewLayoutManager
                = new LinearLayoutManager(
                getApplicationContext());
        // Set LayoutManager on Recycler View
        recyclerViewCategory.setLayoutManager(
                CategoryRecyclerViewLayoutManager);
        // Adding items to RecyclerView.
//        AddItemsToRecyclerViewArrayList();
    }

    private void setChapters(String response) {

//System.out.println("WritersPrint " + response);
        if (response != null) {
            try {
                JSONObject jsonObj = new JSONObject(response);

                // Getting JSON Array node
                JSONArray datas = jsonObj.getJSONArray("blogchapters");

                // Adding items to ArrayList
                source = new ArrayList<CategoryItem>();

                // looping through All Contacts
                for (int i = 0; i < datas.length(); i++) {
                    JSONObject c = datas.getJSONObject(i);
                    source.add(new CategoryItem(c.getInt("id"),c.getString("blogtitle").trim()));
                }

                // calling constructor of adapter
                // with source list as a parameter
                adapterCategory = new CategoryAdapter(source, new CategoryAdapter.OnCategoryItemListener() {
                    @Override
                    public void onItemClick(CategoryItem item) {
//                                        Toast.makeText(getContext(), item.id+" " +item.categoryName, Toast.LENGTH_SHORT).show();

                        id = item.id;

                        blogContent = "";
                        setter(categoryName, writerName);

                        tv.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.VISIBLE);


                        doChapterApiCall();

                    }
                });
                // Set Horizontal Layout Manager
                // for Recycler view
                HorizontalLayoutCategory
                        = new LinearLayoutManager(
                        getApplicationContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false);
                recyclerViewCategory.setLayoutManager(HorizontalLayoutCategory);
                // Set adapter on recycler view
                recyclerViewCategory.setAdapter(adapterCategory);

            } catch (final JSONException e) {

            }

        }
    }
    public void loadBanner() {
        // Create an ad request.
        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.reader_banner_ad_unit_id));
        adContainerView.removeAllViews();
        adContainerView.addView(adView);

        AdSize adSize = getAdSize();
        adView.setAdSize(adSize);

        AdRequest adRequest = new AdRequest.Builder().build();

        // Start loading the ad in the background.
        adView.loadAd(adRequest);
    }
    private AdSize getAdSize() {
        // Determine the screen width (less decorations) to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;

        float adWidthPixels = adContainerView.getWidth();

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
//        return AdSize.getCurrentOrientationBannerAdSizeWithWidth(this, adWidth);
    }

}
