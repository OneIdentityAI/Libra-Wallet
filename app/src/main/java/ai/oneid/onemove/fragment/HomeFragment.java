package ai.oneid.onemove.fragment;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import ai.oneid.onemove.activity.PaymentRequest;
import ai.oneid.onemove.activity.SendPaymentActivity;
import ai.oneid.onemove.adapter.HorizontalContactAdapter;
import ai.oneid.onemove.adapter.TransactionAdapter;
import ai.oneid.onemove.data.AppDelegate;
import ai.oneid.onemove.data.AsynRestClient;
import cz.msebera.android.httpclient.Header;

public class HomeFragment extends Fragment
{
    public MainActivity activity;
    public AppCompatImageView imageSideMenu;

    public TextView textBalance;
    public TextView textTransaction;

    public RecyclerView recyclerViewHorizontalContact;
    public HorizontalContactAdapter adapterHorizontalContact;

    public RecyclerView recyclerViewTransaction;
    public TransactionAdapter adapterTransaction;

    public LinearLayout layoutAddNew;

    public LinearLayout layoutViewAll;
    //Transaction
    public ArrayList<String> transactionUserIdList = new ArrayList<String>();
    public ArrayList<String> transactionNameList = new ArrayList<String>();
    public ArrayList<String> transactionAmountList = new ArrayList<String>();
    public ArrayList<String> transactionCommentList = new ArrayList<String>();
    public ArrayList<String> transactionCreatedList = new ArrayList<String>();
    public ArrayList<String> transactionTransactionIdList = new ArrayList<String>();

    public ArrayList<String> contactIdList = new ArrayList<String>();
    public ArrayList<String> contactNameList = new ArrayList<String>();
    public ArrayList<String> contactAddressList = new ArrayList<String>();
    public ArrayList<String> contactContactList = new ArrayList<String>();
    public ArrayList<String> contactCreatedList = new ArrayList<String>();
    public AppCompatImageView imageQRCode;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        activity = (MainActivity)getActivity();

        SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);

        imageSideMenu = rootView.findViewById(R.id.image_side_menu);
        imageSideMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.drawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        textBalance = rootView.findViewById(R.id.text_balance);
        textBalance.setText(AppDelegate.currencyFormat(preferences.getString(getResources().getString(R.string.param_libra_balance), "")));
        textTransaction = rootView.findViewById(R.id.text_transaction);

        layoutAddNew = rootView.findViewById(R.id.layout_add_new);
        layoutAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, SendPaymentActivity.class);
                intent.putExtra("data", "");
                startActivity(intent);
            }
        });

        recyclerViewTransaction = rootView.findViewById(R.id.recycler_view_transaction);
        recyclerViewTransaction.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        adapterTransaction = new TransactionAdapter(this);
        recyclerViewTransaction.setAdapter(adapterTransaction);
        adapterTransaction.notifyDataSetChanged();

        layoutViewAll = rootView.findViewById(R.id.layout_view_all);
        layoutViewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.changeToActivityFragment();
            }
        });

        recyclerViewHorizontalContact = rootView.findViewById(R.id.recycler_view_horizontal_contact);
        recyclerViewHorizontalContact.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        adapterHorizontalContact = new HorizontalContactAdapter(this);
        recyclerViewHorizontalContact.setAdapter(adapterHorizontalContact);
        adapterHorizontalContact.notifyDataSetChanged();

        imageQRCode = rootView.findViewById(R.id.image_qrcode);

        imageQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, PaymentRequest.class);
                startActivity(intent);
            }
        });
        try {
            imageQRCode.setImageBitmap(AppDelegate.encodeStringAsQRBitmap(preferences.getString(getResources().getString(R.string.param_libra_wallet_address), "")));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        textBalance.setText(AppDelegate.currencyFormat(preferences.getString(getResources().getString(R.string.param_libra_balance), "")));
        retrieveFriend();
        retrieveTransaction();
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

    private void retrieveFriend()
    {
        SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", preferences.getString(getString(R.string.param_utility_user_id), ""));
            jsonObject.put("search", "");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.e("Json", jsonObject.toString());
        AsynRestClient.genericPost(activity, AsynRestClient.utilityRetrieveFriendUrl, jsonObject.toString(), new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
            }

            @Override
            public void onFinish() {
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
                        contactIdList.clear();
                        contactNameList.clear();
                        contactAddressList.clear();
                        contactContactList.clear();
                        contactCreatedList.clear();
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        for(int i = 0; i < jsonArray.length(); i++)
                        {
                            JSONObject innerObject = jsonArray.getJSONObject(i);
                            String id = innerObject.getString("id");
                            String first_name = innerObject.getString("first_name");
                            String last_name = innerObject.getString("last_name");
                            String contact = innerObject.getString("contact");
                            String wallet_address = innerObject.getString("wallet_address");
                            String created = innerObject.getString("created");

                            contactIdList.add(id);
                            contactNameList.add(last_name + " " + first_name);
                            contactContactList.add(contact);
                            contactAddressList.add(wallet_address);
                            contactCreatedList.add(created);
                        }
                        adapterHorizontalContact.notifyDataSetChanged();
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
            }

            @Override
            public void onFinish() {
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
                        textTransaction.setText(transactionTransactionIdList.size() + "");
                        adapterTransaction.notifyDataSetChanged();
                        Log.e("transactionNameList", transactionNameList.size() + "");
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
}