package com.herma.apps.novelsandbooks;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.navigation.NavigationView;
import com.herma.apps.novelsandbooks.ui.about.About_us;
import com.herma.apps.novelsandbooks.ui.bookmarks.BookmarksFragment;
import com.herma.apps.novelsandbooks.ui.home.HomeFragment;
import com.herma.apps.novelsandbooks.usefull.PostItem;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.herma.apps.novelsandbooks.usefull.PaginationListener.PAGE_START;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private AdView adView;
    private FrameLayout adContainerView;

    TextView tvAds;
    public static String Ads = "";
    public static int Ads_font = 22;
//// search future req
    SearchView searchView;
    MenuItem myActionMenuItem;
    String url ="https://datascienceplc.com/apps/manager/api/items/blog/search?page=1";
    String searchQuery = "";
    public RequestQueue queue;
    ArrayList<PostItem> items;
    Button btnRetry;
    TextView tv;
/////
HomeFragment homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        tvAds = (TextView) findViewById(R.id.tvAds);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        try {


            FragmentManager fragmentManager = getSupportFragmentManager();
            homeFragment = new HomeFragment();

                Bundle bundle = new Bundle();
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                bundle.putString("response", extras.getString("response"));
                bundle.putInt("rand", extras.getInt("rand"));
                homeFragment.setArguments(bundle);

                setAd(extras.getString("response"));
            }

            fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, homeFragment).commit();
        }catch (Exception kl){}

        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        int started_book_id = pre.getInt("id", 0);
        if(started_book_id != 0){

            Intent intent = new Intent(MainActivity.this, ReaderActivity.class);
            intent.putExtra("id", pre.getInt("id", 0));
            intent.putExtra("blogposts_count", pre.getInt("blogposts_count", 0));
            intent.putExtra("writername", pre.getString("writername", ""));
            intent.putExtra("blogwriter_id", pre.getInt("blogwriter_id", 0));
            intent.putExtra("name", pre.getString("name", ""));
            startActivity(intent);
        }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        ////////////////
        myActionMenuItem = menu.findItem( R.id.search);
        searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Toast like print
                searchQuery = query;
                doSearchApiCall(searchQuery);
                if( ! searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                myActionMenuItem.collapseActionView();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                // UserFeedback.show( "SearchOnQueryTextChanged: " + s);
                return false;
            }
        });
        ////////////////
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
                Toast.makeText(MainActivity.this, "Rate this app :)", Toast.LENGTH_SHORT).show();
                rateApp();
                return true;
            case R.id.action_app_store:
                Toast.makeText(MainActivity.this, "More apps :)", Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentManager fragmentManager;
        HomeFragment homeFragment = new HomeFragment();

        if (id == R.id.nav_home) {
             homeFragment = new HomeFragment();
            fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, homeFragment).commit();

        } else if (id == R.id.nav_bookmarks) {
            homeFragment.onDestroyView();
            fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.nav_host_fragment,  new BookmarksFragment()).commit();

        }else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction("android.intent.action.SEND");
            sendIntent.putExtra("android.intent.extra.SUBJECT", MainActivity.this.getText(R.string.app_name));
            sendIntent.putExtra("android.intent.extra.TEXT", "Downloads \nhttps://play.google.com/store/apps/details?id=" + MainActivity.this.getPackageName());
            sendIntent.setType("text/plain");
            MainActivity.this.startActivity(Intent.createChooser(sendIntent, MainActivity.this.getText(R.string.app_name)));

        } else if (id == R.id.nav_rate) {
            Toast.makeText(MainActivity.this, "Rate this app :)", Toast.LENGTH_SHORT).show();
            rateApp();
            return true;
        } else if (id == R.id.nav_store) {
            Toast.makeText(MainActivity.this, "More apps by us :)", Toast.LENGTH_SHORT).show();
            openUrl("https://play.google.com/store/apps/developer?id=Herma%20plc");
            return true;
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(getApplicationContext(), About_us.class));
            return true;
        } else if (id == R.id.nav_exit) {
            System.exit(0);
            return true;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    public void setAd(String response){
        if (response != null) {
            try {
                // Getting JSON Array node
                JSONObject jsonObj = new JSONObject(response);
                Ads = jsonObj.getString("ads");

                if(jsonObj.has("font")) Ads_font = jsonObj.getInt("font");
//                                        System.out.println("ads is " + Ads);

                if(jsonObj.getString("open_ad").equalsIgnoreCase("my")) {
/////////////////////////////////////////////////////////////////////////////////////////////////
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        tvAds.setText(Html.fromHtml(Ads, Html.FROM_HTML_MODE_COMPACT));
                    } else {
                        tvAds.setText(Html.fromHtml(Ads));
                    }

                    tvAds.setTextSize(MainActivity.Ads_font);
                    tvAds.setMovementMethod(LinkMovementMethod.getInstance());
                    tvAds.setSelected(true);
/////////////////////////////////////////////////////////////////////////////////////////////////
                }
                else tvAds.setVisibility(View.GONE);

            } catch (final JSONException e) { tvAds.setVisibility(View.GONE);}

        }
    }
    public void clearOnSearch() {

        homeFragment.random = new Random().nextInt((99999 - 1) + 1) + 1;

        homeFragment.haveNext = true;

        homeFragment.itemCount = 0;
        homeFragment.currentPage = PAGE_START;
        homeFragment.isLastPage = false;
        homeFragment.adapter.clear();

        /////////////// Writer
    }
    private void doSearchApiCall(final String quer) {


        clearOnSearch();

        items = new ArrayList<>();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                queue = Volley.newRequestQueue(getApplicationContext());
// Request a string response from the provided URL.

                final int random = new Random().nextInt((99999 - 1) + 1) + 1;

                StringRequest stringRequest = new StringRequest(Request.Method.GET, url+"&v=1.0&app_id=4&company_id=1&limit=10&rand="+random+"&key="+quer,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                homeFragment.searchQuery = searchQuery;
                                homeFragment.setInitial(response);
                            }

                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try{
//                            Toast.makeText(getContext(), "That didn't work! " + error, Toast.LENGTH_LONG).show();

                            if (!homeFragment.isOnline()) {
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
    private void showOptionDialog(final int option) {

        tv = (TextView) findViewById(R.id.tvWait);
        btnRetry = (Button) findViewById(R.id.btnRetry);

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                doSearchApiCall(searchQuery);
                tv.setVisibility(View.VISIBLE);
                btnRetry.setVisibility(View.INVISIBLE);

            }
        });

        // Create an AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.Theme_AppCompat_Dialog_Alert);

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

    public void loadBanner() {
        // Create an ad request.
        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.home_banner_ad_unit_id));
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

        return AdSize.getCurrentOrientationBannerAdSizeWithWidth(this, adWidth);
    }
}
