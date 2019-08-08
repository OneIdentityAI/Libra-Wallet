package ai.oneid.onemove.fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import ai.oneid.onemove.R;
import ai.oneid.onemove.activity.MainActivity;
import ai.oneid.onemove.adapter.ActivityAdapter;
import ai.oneid.onemove.adapter.HorizontalContactAdapter;
import ai.oneid.onemove.adapter.TransactionAdapter;
import ai.oneid.onemove.data.AppDelegate;
import ai.oneid.onemove.data.AsynRestClient;
import cz.msebera.android.httpclient.Header;

public class ActivityFragment extends Fragment
{
    public MainActivity activity;
    public AppCompatImageView imageSideMenu;

    public RecyclerView recyclerView;
    public ActivityAdapter adapter;

    public ArrayList<String> transactionUserIdList = new ArrayList<String>();
    public ArrayList<String> transactionNameList = new ArrayList<String>();
    public ArrayList<String> transactionAmountList = new ArrayList<String>();
    public ArrayList<String> transactionCommentList = new ArrayList<String>();
    public ArrayList<String> transactionCreatedList = new ArrayList<String>();
    public ArrayList<String> transactionTransactionIdList = new ArrayList<String>();

    public ProgressDialog progressDialog;
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_activity, container, false);
        activity = (MainActivity)getActivity();

        progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle(getString(R.string.loading));
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);

        imageSideMenu = rootView.findViewById(R.id.image_side_menu);
        imageSideMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.drawerLayout.openDrawer(Gravity.LEFT);
            }
        });
        recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        adapter = new ActivityAdapter(this);
        recyclerView.setAdapter(adapter);

        retrieveTransaction();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        // Handle item selection
        return super.onOptionsItemSelected(item);
    }

    private void retrieveTransaction()
    {
        SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", preferences.getString(getString(R.string.param_libra_wallet_address), ""));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.e("Json", jsonObject.toString());
        AsynRestClient.genericPost(activity, AsynRestClient.utilityRetrieveTransactionUrl, jsonObject.toString(), new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                progressDialog.show();
            }

            @Override
            public void onFinish() {
                progressDialog.dismiss();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String responseString = new String(responseBody);
                Log.e("Response", responseString);
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    String status = jsonObject.getString("status");
                    if(status.equals("1"))
                    {
                        transactionUserIdList.clear();
                        transactionNameList.clear();
                        transactionAmountList.clear();
                        transactionCommentList.clear();
                        transactionCreatedList.clear();
                        transactionTransactionIdList.clear();
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                        SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        for(int i = 0; i < jsonArray.length(); i++)
                        {
                            JSONObject innerObject = jsonArray.getJSONObject(i);
                            String user_id = innerObject.getString("wallet_address");
                            String name = innerObject.getString("last_name") + " " + innerObject.getString("first_name");
                            if(name.trim().equals(""))
                                name = "Unknown";
                            String amount = innerObject.getString("amount");
                            String comment = innerObject.getString("comment");
                            String created = innerObject.getString("created");
                            String transaction_id = innerObject.getString("transaction_id");
                            try
                            {
                                Date formattedDatetime = format.parse(created);
                                created = newFormat.format(formattedDatetime);
                            }
                            catch (Exception e)
                            {
                            }
                            transactionUserIdList.add(user_id);
                            transactionNameList.add(name);
                            transactionAmountList.add(AppDelegate.currencyFormat(amount));
                            transactionCommentList.add(comment);
                            transactionCreatedList.add(created);
                            transactionTransactionIdList.add(transaction_id);
                        }
                        adapter.notifyDataSetChanged();
                    }
                    else
                    {
                        String message = jsonObject.getString("message");
                        alert("Error", message);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                alert(getString(R.string.error), getString(R.string.error_please_try_again));
                Log.e("Helo", "Hello");
            }
        });
    }

    public void alert(String title, String message)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}