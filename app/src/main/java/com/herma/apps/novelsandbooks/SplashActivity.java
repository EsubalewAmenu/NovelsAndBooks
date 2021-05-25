package com.herma.apps.novelsandbooks;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.herma.apps.novelsandbooks.usefull.PostItem;

import org.apache.http.conn.ConnectTimeoutException;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SplashActivity extends AppCompatActivity {

    String url ="https://datascienceplc.com/apps/manager/api/items/blog/post?page=1";
    public RequestQueue queue;
    ArrayList<PostItem> items;

    Button btnRetry;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);;


                doApiCall();

        tv = (TextView) findViewById(R.id.tvWait);
        btnRetry = (Button) findViewById(R.id.btnRetry);

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                doApiCall();
                tv.setVisibility(View.VISIBLE);
                btnRetry.setVisibility(View.INVISIBLE);

            }
        });
    }

    private void doApiCall() {
        items = new ArrayList<>();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                queue = Volley.newRequestQueue(getApplicationContext());
// Request a string response from the provided URL.

                final int random = new Random().nextInt((99999 - 1) + 1) + 1;

                StringRequest stringRequest = new StringRequest(Request.Method.GET, url+"&v=1.0&app_id=4&company_id=1&limit=10&rand="+random,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                /// start activity
                                Intent i=new Intent( SplashActivity.this, MainActivity.class);
                                i.putExtra("response", response);
                                i.putExtra("rand", random);
                                startActivity(i);
                                finish();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this, R.style.Theme_AppCompat_Dialog_Alert);

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
}
