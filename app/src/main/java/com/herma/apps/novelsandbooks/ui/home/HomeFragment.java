package com.herma.apps.novelsandbooks.ui.home;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.herma.apps.novelsandbooks.usefull.CategoryAdapter;
import com.herma.apps.novelsandbooks.usefull.CategoryItem;
import com.herma.apps.novelsandbooks.usefull.PaginationListener;
import com.herma.apps.novelsandbooks.usefull.PostItem;
import com.herma.apps.novelsandbooks.usefull.PostRecyclerAdapter;
import com.herma.apps.novelsandbooks.R;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener
{
    RecyclerView mRecyclerView;
    SwipeRefreshLayout swipeRefresh;
    public PostRecyclerAdapter adapter;
    public int currentPage = PaginationListener.PAGE_START;
    public boolean isLastPage = false;
    private int totalPage = 10;
    private boolean isLoading = false;
    public int itemCount = 0;
    ArrayList<Object> items;
    String url, //defult ="http://192.168.43.198/consol/api/items/blog/post?page=1";
    defult ="https://datascienceplc.com/apps/manager/api/items/blog/post?page=1";
    public boolean haveNext = true;
    public RequestQueue queue;
    public int random = 0;

    public String searchQuery;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);


        mRecyclerView = (RecyclerView) root.findViewById(R.id.recyclerView);
        swipeRefresh = (SwipeRefreshLayout) root.findViewById(R.id.swipeRefresh);

        url = defult;
        swipeRefresh.setOnRefreshListener(this);

        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);

//        adapter = new PostRecyclerAdapter(new ArrayList<>());
        adapter = new PostRecyclerAdapter(new ArrayList<Object>());
        mRecyclerView.setAdapter(adapter);
        adapter.addLoading();

        Bundle bundle = this.getArguments();
        if(bundle != null)
        {
            random =  bundle.getInt("rand");
            setInitial(bundle.getString("response"));

        }
        else
            doApiCall();

        /**
         * add scroll listener while user reach in bottom load more will call
         */
        mRecyclerView.addOnScrollListener(new PaginationListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage++;
                doApiCall();
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

    return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        System.out.println("Inside this method");
        if (queue != null) {
//            System.out.println("Inside second this method");
            queue.stop();
//            queue.cancelAll(this);
        }
    }
    private void doApiCall() {
        if(haveNext == false) {
            adapter.removeLoading();
            Toast.makeText(getContext(), "This is the last page!", Toast.LENGTH_LONG).show();
        }
        else{
            items = new ArrayList<Object>();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {

                    if (queue == null )
                        queue = Volley.newRequestQueue(getContext());
// Request a string response from the provided URL.
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url+"&v=1.0&app_id=4&company_id=1&limit=10&rand="+random,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {


//                    System.out.println(response);

                                    if (response != null) {
                                        try {
                                            JSONObject jsonObj = new JSONObject(response).getJSONObject("titles");

                                            // Getting JSON Array node
                                            JSONArray datas = jsonObj.getJSONArray("data");

                                            // looping through All Contacts
                                            for (int i = 0; i < datas.length(); i++) {
                                                JSONObject c = datas.getJSONObject(i);

                                                ////////////////////////////////////////
                                                PostItem postItem = new PostItem();
                                                postItem.setRealId(c.getInt("id"));
                                                postItem.setId(c.getInt("id"));
                                                postItem.setCategoryName(c.getString("name"));
                                                postItem.setBlogposts_count(c.getInt("blogposts_count"));
                                                try{
                                                postItem.setBlogwriter_name(c.getJSONObject("blogwriter").getJSONObject("blogwriter").getString("writername"));
                                                postItem.setBlogwriter_id(c.getJSONObject("blogwriter").getInt("blogwriter_id"));
                                                }catch (Exception kl){}
                                                items.add(postItem);
                                            }

                                        } catch (final JSONException e) {
//                                  Log.e(TAG, "Json parsing error: " + e.getMessage());
//                                            runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
////                                          Toast.makeText(getApplicationContext(),
////                                                  "Json parsing error: " + e.getMessage(),
////                                                  Toast.LENGTH_LONG).show();
//                                                }
//                                            });

                                        }

                                    }

                          if (currentPage != PaginationListener.PAGE_START) adapter.removeLoading();

                                    try{if (url.equals(defult)) adapter.removeLoading();}catch(Exception k){}
                                    adapter.addItems((ArrayList<Object>) items);
                                    swipeRefresh.setRefreshing(false);

                                    // check weather is last page or not
                                    if (currentPage < totalPage) {

                                        adapter.addLoading();
                                    } else {
                                        isLastPage = true;
                                    }
                                    isLoading = false;

                                    setIfNextPage(response);
                                    //                    setResponseOnView(response);
                                }

                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
//                  System.out.println("That didn't work! " + error);
                        try{
//                            Toast.makeText(getContext(), "That didn't work! " + error, Toast.LENGTH_LONG).show();

                            if (!isOnline()) {
                                showNetworkDialog(false);
                            }
                            else if (error instanceof TimeoutError || error.getCause() instanceof SocketTimeoutException
                                    || error.getCause() instanceof ConnectTimeoutException
                                    || error.getCause() instanceof SocketException
                                    || (error.getCause().getMessage() != null
                                    && error.getCause().getMessage().contains("Connection timed out"))) {
                                Toast.makeText(getActivity(), "Connection timeout error. \npls Swipe to reload",
                                        Toast.LENGTH_LONG).show();


                            } else {
                                Toast.makeText(getActivity(), "An unknown error occurred.\npls swap to refresh",
                                        Toast.LENGTH_LONG).show();
System.out.println("Error on sys:"+error);
                                try{
                                    swipeRefresh.setRefreshing(false);
                                }catch(Exception k){}

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
    }
    public void setIfNextPage(String response){

//System.out.println(response);
        if (response != null) {
            try {
                // Getting JSON Array node
                JSONObject jsonObj = new JSONObject(response).getJSONObject("titles");

              if(!jsonObj.getString("next_page_url").equalsIgnoreCase("null")){
                    url = jsonObj.getString("next_page_url");

                  if(searchQuery!=null) {
                      url+="&key=" + searchQuery;
                  }

               }
                else haveNext = false;

//System.out.println(" next link " + c.getString("next_page_url"));
            } catch (final JSONException e) {
//              Log.e(TAG, "Json parsing error: " + e.getMessage());
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
////                      Toast.makeText(getApplicationContext(),
////                              "Json parsing error: " + e.getMessage(),
////                              Toast.LENGTH_LONG).show();
//                    }
//                });

            }

        }
    }
    @Override
    public void onRefresh() {

        random = new Random().nextInt((99999 - 1) + 1) + 1;

        url =defult;
        haveNext = true;

        itemCount = 0;
        currentPage = PaginationListener.PAGE_START;
        isLastPage = false;
        adapter.clear();
        doApiCall();
    }
    /**
     * Check if there is the network connectivity
     *
     * @return true if connected to the network
     */
    public boolean isOnline() {
        // Get a reference to the ConnectivityManager to check the state of network connectivity
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * Show a dialog when there is no internet connection
     *
     * @param isOnline true if connected to the network
     */
    private void showNetworkDialog(final boolean isOnline) {
            // Create an AlertDialog.Builder
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Dialog_Alert);
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
            builder.setNegativeButton(getString(R.string.cancel), null);

            // Create and show the AlertDialog
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

    }
    public void setInitial(String response){

        items = new ArrayList<>();
        if (response != null) {
            try {
                JSONObject jsonObj = new JSONObject(response).getJSONObject("titles");

                // Getting JSON Array node
                JSONArray datas = jsonObj.getJSONArray("data");

                // looping through All Contacts
                for (int i = 0; i < datas.length(); i++) {
                    JSONObject c = datas.getJSONObject(i);



                    ////////////////////////////////////////
                    PostItem postItem = new PostItem();
                    postItem.setRealId(c.getInt("id"));
                    postItem.setId(c.getInt("id"));
                    postItem.setCategoryName(c.getString("name"));
                    postItem.setBlogposts_count(c.getInt("blogposts_count"));
                    try{
                    postItem.setBlogwriter_name(c.getJSONObject("blogwriter").getJSONObject("blogwriter").getString("writername"));
                    postItem.setBlogwriter_id(c.getJSONObject("blogwriter").getInt("blogwriter_id"));
                    }catch (Exception kl){}
                    items.add(postItem);
                }
            } catch (final JSONException e) {
//                                  Log.e(TAG, "Json parsing error: " + e.getMessage());
//                                            runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
////                                          Toast.makeText(getApplicationContext(),
////                                                  "Json parsing error: " + e.getMessage(),
////                                                  Toast.LENGTH_LONG).show();
//                                                }
//                                            });

            }

        }

//                          if (currentPage != PAGE_START) adapter.removeLoading();
        try{if (url.equals(defult)) adapter.removeLoading();}catch(Exception k){}
//        System.out.println(items.size());
        adapter.addItems(items);
        swipeRefresh.setRefreshing(false);

        // check weather is last page or not
        if (currentPage < totalPage) {

            adapter.addLoading();
        } else {
            isLastPage = true;
        }
        isLoading = false;

        setIfNextPage(response);
    }

}
