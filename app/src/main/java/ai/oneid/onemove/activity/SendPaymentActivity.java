package ai.oneid.onemove.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.client.android.CaptureActivity;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

import ai.oneid.onemove.R;
import ai.oneid.onemove.adapter.PaymentContactsAdapter;
import ai.oneid.onemove.data.AppDelegate;
import ai.oneid.onemove.data.AsynRestClient;
import cz.msebera.android.httpclient.Header;

public class SendPaymentActivity extends AppCompatActivity {
    public AppCompatImageView imageBack;

    public TextView textBalance;
    public EditText inputAmountLeft;
    public EditText inputAmountRight;
    public EditText inputAddress;
    public TextView textFee;
    public TextView textTotal;
    public AppCompatImageView imageStepperIncrement;
    public AppCompatImageView imageStepperDecrement;
    public LinearLayout layoutAddNew;
    public LinearLayout layoutScan;
    public EditText inputComment;
    public Button buttonPay;

    public RecyclerView recyclerView;
    public PaymentContactsAdapter adapter;
    public DecimalFormat numberFormat;

    public ArrayList<String> contactIdList = new ArrayList<String>();
    public ArrayList<String> contactNameList = new ArrayList<String>();
    public ArrayList<String> contactAddressList = new ArrayList<String>();
    public ArrayList<String> contactContactList = new ArrayList<String>();
    public ArrayList<String> contactCreatedList = new ArrayList<String>();

    public static int BARCODE_REQUEST_CODE = 1;
    public static int SELECT_CONTACT_REQUEST_CODE = 2;
    public ProgressDialog progressDialog;
    public String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.loading));
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        final SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);

        data = getIntent().getExtras().getString("data");
        if(!data.equals(""))
        {
            try
            {
                JSONArray jsonArray = new JSONArray(data);
                contactIdList.clear();
                contactNameList.clear();
                contactAddressList.clear();
                contactContactList.clear();
                contactCreatedList.clear();
                for(int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject innerObject = jsonArray.getJSONObject(i);
                    String id = innerObject.getString("id");
                    String name = innerObject.getString("name");
                    String contact = innerObject.getString("contact");
                    String address = innerObject.getString("address");
                    String created = innerObject.getString("created");

                    if(contactIdList.contains(id))
                    {
                        if(preferences.getString(getString(R.string.param_libra_wallet_address), "").equals(address))
                            alert("", getString(R.string.error_unable_to_add_yourself));
                        else
                            alert("", getString(R.string.error_recipient_existed));
                    }
                    else
                    {
                        if(preferences.getString(getString(R.string.param_libra_wallet_address), "").equals(address))
                            alert("", getString(R.string.error_unable_to_add_yourself));
                        else {
                            contactIdList.add(id);
                            contactNameList.add(name);
                            contactContactList.add(contact);
                            contactAddressList.add(address);
                            contactCreatedList.add(created);
                        }
                    }
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

        setContentView(R.layout.activity_send_payment);

        imageBack = findViewById(R.id.image_back);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        textBalance = findViewById(R.id.text_balance);
        textFee = findViewById(R.id.text_fee);
        textTotal = findViewById(R.id.text_total);

        textBalance.setText(AppDelegate.currencyFormat(preferences.getString(getString(R.string.param_libra_balance), "")));
        inputAmountLeft = findViewById(R.id.input_amount_left);
        inputAmountRight = findViewById(R.id.input_amount_right);

        inputAmountLeft.setText(0 + "");
        inputAmountRight.setText(00 + "");
        numberFormat = new DecimalFormat(".00");
        imageStepperIncrement = findViewById(R.id.image_stepper_increment);
        imageStepperIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inputAmountRight.isFocused())
                {
                    inputAmountRight.setText((Integer.parseInt(inputAmountRight.getText().toString()) + 1) + "");
                }
                else
                    inputAmountLeft.setText((Integer.parseInt(inputAmountLeft.getText().toString()) + 1) + "");
                textTotal.setText(inputAmountLeft.getText().toString() + "." + inputAmountRight.getText().toString());
            }
        });

        imageStepperDecrement = findViewById(R.id.image_stepper_decrement);
        imageStepperDecrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inputAmountRight.isFocused())
                {
                    int finalValue = Integer.parseInt(inputAmountRight.getText().toString()) - 1;
                    inputAmountRight.setText(finalValue <= 0? "0":finalValue + "");
                }
                else
                {
                    int finalValue = Integer.parseInt(inputAmountLeft.getText().toString()) - 1;
                    inputAmountLeft.setText(finalValue <= 0? "0":finalValue + "");
                }
                textTotal.setText(inputAmountLeft.getText().toString() + "." + inputAmountRight.getText().toString());
            }
        });

        layoutAddNew = findViewById(R.id.layout_add_new);
        layoutAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickContactIntent = new Intent(SendPaymentActivity.this, SelectContactActivity.class);
                startActivityForResult(pickContactIntent, SELECT_CONTACT_REQUEST_CODE);
            }
        });
        layoutScan = findViewById(R.id.layout_scan);
        layoutScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SendPaymentActivity.this, CaptureActivity.class);
                intent.setAction("com.google.zxing.client.android.SCAN");
                intent.putExtra("SAVE_HISTORY", false);
                startActivityForResult(intent, BARCODE_REQUEST_CODE);
            }
        });

        inputComment = findViewById(R.id.input_comment);

        buttonPay = findViewById(R.id.button_pay);
        buttonPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputComment.setError(null);
                if(contactIdList.size() == 0 && inputAddress.getText().toString().equals(""))
                {
                    alert("", getString(R.string.error_recipient_cannot_be_empty));
                }
                else if(inputComment.getText().toString().equals(""))
                {
                    inputComment.setError(getString(R.string.error_required_field));
                }
                else
                {
                    if(Float.parseFloat(inputAmountLeft.getText().toString() + "." + inputAmountRight.getText().toString()) == 0.0)
                    {
                        alert("", getString(R.string.error_amount_cannot_be_zero));
                    }
                    else if(Float.parseFloat(inputAmountLeft.getText().toString() + "." + inputAmountRight.getText().toString())*contactIdList.size() > Float.parseFloat(preferences.getString(getString(R.string.param_libra_balance), "")))
                    {
                        alert("", getString(R.string.error_over_limit_transfer));
                    }
                    else
                    {
                        SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
                        String credential = preferences.getString(getString(R.string.param_oneid_credential), "");
                        if (credential.equals(""))
                        {
                            String lastTransferredDate = preferences.getString(getString(R.string.param_last_transferred_date), "");
                            String lastTransferredAmount = preferences.getString(getString(R.string.param_last_transferred_amount), "");

                            Log.e("Last transferred date", lastTransferredDate);
                            Log.e("Last transferred amount", lastTransferredAmount);
                            Date cDate = new Date();
                            String fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
                            if(!lastTransferredDate.equals("") && lastTransferredDate.equals(fDate))
                            {
                                if(Float.parseFloat(lastTransferredAmount) >= 50.0f) {
                                    alert("", getString(R.string.error_daily_transfer_limit_for_non_verify_account));
                                    return;
                                }


                                if(Float.parseFloat(inputAmountLeft.getText().toString() + "." + inputAmountRight.getText().toString()) > (50.0f - Float.parseFloat(lastTransferredAmount)))
                                {
                                    float difference = 50.0f - Float.parseFloat(lastTransferredAmount);
                                    alert("", getString(R.string.error_daily_transfer_limit_for_non_verify_account_leftover) + " " + AppDelegate.currencyFormat(difference + "")+ " USD "+ getString(R.string.error_daily_transfer_limit_for_non_verify_account_leftover_2));
                                    return;
                                }
                            }
                            else if(!lastTransferredDate.equals("") && !lastTransferredDate.equals(fDate))
                            {
                                if(Float.parseFloat(inputAmountLeft.getText().toString() + "." + inputAmountRight.getText().toString()) > 50.0f)
                                {
                                    alert("", getString(R.string.error_daily_transfer_limit_for_non_verify_account_leftover) + " 50.0 USD "+ getString(R.string.error_daily_transfer_limit_for_non_verify_account_leftover_2));
                                    return;
                                }
                            }
                            else
                            {
                                if(Float.parseFloat(inputAmountLeft.getText().toString() + "." + inputAmountRight.getText().toString()) > 50.0f){
                                    alert("", getString(R.string.error_daily_transfer_limit_for_non_verify_account));
                                    return;
                                }
                            }
                        }

                        String recipientAddress = inputAddress.getText().toString();
                        if(contactIdList.size() > 0)
                            recipientAddress = contactAddressList.get(0);
                        libraTransfer(recipientAddress);
                    }
                }
            }
        });

        inputAddress = findViewById(R.id.input_address);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        adapter = new PaymentContactsAdapter(this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        if(data.equals(""))
        {
            recyclerView.setVisibility(View.GONE);
            inputAddress.setVisibility(View.VISIBLE);
        }
        else
        {
            recyclerView.setVisibility(View.VISIBLE);
            inputAddress.setVisibility(View.GONE);
        }
    }

    public void alert(String title, String message)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        final SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        Log.e("requestCode", requestCode + "");
        if (requestCode == BARCODE_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                contactIdList.clear();
                contactNameList.clear();
                contactAddressList.clear();
                contactContactList.clear();
                contactCreatedList.clear();
                adapter.notifyDataSetChanged();
                String contents = intent.getStringExtra("SCAN_RESULT");
                //searchUser(contents);
                inputAddress.setText(contents);

                recyclerView.setVisibility(View.GONE);
                inputAddress.setVisibility(View.VISIBLE);
            }
            else if (resultCode == RESULT_CANCELED)
            {
            }
        }
        else if(requestCode == SELECT_CONTACT_REQUEST_CODE)
        {
            if(resultCode == RESULT_OK)
            {
                String returnedResult = intent.getData().toString();
                try
                {
                    contactIdList.clear();
                    contactNameList.clear();
                    contactAddressList.clear();
                    contactContactList.clear();
                    contactCreatedList.clear();

                    JSONArray jsonArray = new JSONArray(returnedResult);
                    for(int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject innerObject = jsonArray.getJSONObject(i);
                        String id = innerObject.getString("id");
                        String name = innerObject.getString("name");
                        String contact = innerObject.getString("contact");
                        String address = innerObject.getString("address");
                        String created = innerObject.getString("created");


                        if(contactIdList.contains(id))
                        {
                            if(preferences.getString(getString(R.string.param_libra_wallet_address), "").equals(address))
                                alert("", getString(R.string.error_unable_to_add_yourself));
                            else
                                alert("", getString(R.string.error_recipient_existed));
                        }
                        else
                        {
                            if(preferences.getString(getString(R.string.param_libra_wallet_address), "").equals(address))
                                alert("", getString(R.string.error_unable_to_add_yourself));
                            else {
                                contactIdList.add(id);
                                contactNameList.add(name);
                                contactContactList.add(contact);
                                contactAddressList.add(address);
                                contactCreatedList.add(created);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                    recyclerView.setVisibility(View.VISIBLE);
                    inputAddress.setVisibility(View.GONE);
                    inputAddress.setText("");
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private void searchUser(String content)
    {
        final SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", preferences.getString(getString(R.string.param_utility_user_id), ""));
            jsonObject.put("search", content);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.e("Json", jsonObject.toString());
        AsynRestClient.genericPost(this, AsynRestClient.utilitySearchUserUrl, jsonObject.toString(), new AsyncHttpResponseHandler() {
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

                            if(contactIdList.contains(id))
                            {
                                if(preferences.getString(getString(R.string.param_libra_wallet_address), "").equals(wallet_address))
                                    alert("", getString(R.string.error_unable_to_add_yourself));
                                else
                                    alert("", getString(R.string.error_recipient_existed));
                            }
                            else
                            {
                                if(preferences.getString(getString(R.string.param_libra_wallet_address), "").equals(wallet_address))
                                    alert("", getString(R.string.error_unable_to_add_yourself));
                                else {
                                    contactIdList.add(id);
                                    contactNameList.add(last_name + " " + first_name);
                                    contactContactList.add(contact);
                                    contactAddressList.add(wallet_address);
                                    contactCreatedList.add(created);
                                }
                            }
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

    private void libraTransfer(final String recipientAddress)
    {

        final SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("address", preferences.getString(getString(R.string.param_libra_wallet_address), ""));
            jsonObject.put("mnemonic", preferences.getString(getString(R.string.param_libra_mnemonic), ""));
            jsonObject.put("recipient_address", recipientAddress);
            jsonObject.put("amount", inputAmountLeft.getText().toString() + "." + inputAmountRight.getText().toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.e("Json", jsonObject.toString());
        AsynRestClient.genericPost(this, AsynRestClient.libraSendTransactionUrl, jsonObject.toString(), new AsyncHttpResponseHandler() {
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
                Log.e("Libra transfer", responseString);
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    if(jsonObject.has("error"))
                    {
                        JSONObject innerObject = jsonObject.getJSONObject("error");
                        alert(innerObject.getString("type"), innerObject.getString("message"));
                    }
                    else {
                        createTransaction(recipientAddress);
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                alert(getString(R.string.error), getString(R.string.error_please_try_again));
                String responseString = new String(errorResponse);
                Log.e("Response", responseString);
            }
        });
    }

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ip = Formatter.formatIpAddress(inetAddress.hashCode());
                        Log.i("OneMove", "***** IP="+ ip);
                        return ip;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("OneMove", ex.toString());
        }
        return null;
    }

    private void fiatDepositTransactionLog(String transactionId, String recipientAddress)
    {
        SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        String libraWalletAddress = preferences.getString(getResources().getString(R.string.param_libra_wallet_address), "");
        String firstName = preferences.getString(getString(R.string.param_first_name), "");
        String lastName = preferences.getString(getString(R.string.param_last_name), "");
        String email = preferences.getString(getString(R.string.param_email), "");
        String contact = preferences.getString(getString(R.string.param_contact), "");
        String gender = preferences.getString(getString(R.string.param_oneid_gender), "");
        String dob = preferences.getString(getString(R.string.param_oneid_dob), "");
        String nationality = preferences.getString(getString(R.string.param_oneid_nationality), "");
        String companyName = preferences.getString(getString(R.string.param_oneid_company_name), "");
        String jobTitle = preferences.getString(getString(R.string.param_oneid_job_title), "");
        String idType = preferences.getString(getString(R.string.param_oneid_id_type_name), "");
        String idNo = preferences.getString(getString(R.string.param_oneid_id_no), "");
        String selfie = preferences.getString(getString(R.string.param_oneid_selfie), "");
        String credDefId = preferences.getString(getResources().getString(R.string.param_oneid_credential), "");

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = telephonyManager.getSimCountryIso();

        String documentType = "";

        AsynRestClient.fiatDeportTransactionLog(transactionId, preferences.getString(getString(R.string.param_libra_wallet_address), ""), recipientAddress, inputAmountLeft.getText().toString() + "." + inputAmountRight.getText().toString(), getLocalIpAddress(),
                new AsyncHttpResponseHandler() {
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
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                        String responseString = new String(errorResponse);
                        Log.e("Response", responseString);
                        alert(getString(R.string.error), getString(R.string.error_please_try_again));
                        Log.e("Helo", "Hello");
                    }
                });
    }

    private void createTransaction(final String recipientAddress)
    {
        final SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        JSONArray jsonArray = new JSONArray();
        if(contactIdList.size() > 0) {
            for (int i = 0; i < contactIdList.size(); i++)
                jsonArray.put(contactIdList.get(i));
        }
        else
        {
            jsonArray.put(inputAddress.getText().toString());
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", preferences.getString(getString(R.string.param_libra_wallet_address), ""));
            jsonObject.put("recipient_id", jsonArray.toString());
            jsonObject.put("amount", inputAmountLeft.getText().toString() + "." + inputAmountRight.getText().toString());
            jsonObject.put("comment", inputComment.getText().toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.e("Json", jsonObject.toString());
        AsynRestClient.genericPost(this, AsynRestClient.utilityCreateTransactionUrl, jsonObject.toString(), new AsyncHttpResponseHandler() {
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
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    if(jsonObject.has("error"))
                    {
                        JSONObject innerObject = jsonObject.getJSONObject("error");
                        alert(innerObject.getString("type"), innerObject.getString("message"));
                    }
                    else {
                        String transactionId = jsonObject.getString("transaction_id");
                        fiatDepositTransactionLog(transactionId, recipientAddress);

                        Log.e("Response", responseString);
                        retrieveBalance();
                        alert("", getString(R.string.success_payment_transfer));

                        SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
                        String lastTransferredDate = preferences.getString(getString(R.string.param_last_transferred_date), "");
                        String lastTransferredAmount = preferences.getString(getString(R.string.param_last_transferred_amount), "");

                        Date cDate = new Date();
                        String fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
                        SharedPreferences.Editor editor = preferences.edit();
                        if(lastTransferredDate.equals("") || (!lastTransferredDate.equals("") && !lastTransferredDate.equals(fDate))) {
                            editor.putString(getResources().getString(R.string.param_last_transferred_date), fDate);
                            editor.putString(getResources().getString(R.string.param_last_transferred_amount), inputAmountLeft.getText().toString() + "." + inputAmountRight.getText().toString());
                        }
                        else
                        {
                            float totalAmount = Float.parseFloat(lastTransferredAmount) + Float.parseFloat(inputAmountLeft.getText().toString() + "." + inputAmountRight.getText().toString());
                            editor.putString(getResources().getString(R.string.param_last_transferred_amount), totalAmount + "");
                        }
                        editor.apply();
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                alert(getString(R.string.error), getString(R.string.error_please_try_again));
                String responseString = new String(errorResponse);
                Log.e("Response", responseString);
            }
        });
    }

    private void retrieveBalance()
    {
        final SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("address", preferences.getString(getString(R.string.param_libra_wallet_address), ""));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.e("Json", jsonObject.toString());
        AsynRestClient.genericPost(this, AsynRestClient.retrieveBalanceUrl + preferences.getString(getString(R.string.param_libra_wallet_address), ""), jsonObject.toString(), new AsyncHttpResponseHandler() {
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
                    String balance = jsonObject.getString("result");
                    double actualBalance = 0.0;
                    if(!balance.equals(""))
                        actualBalance = Double.parseDouble(balance)/1000000;

                    SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(getResources().getString(R.string.param_libra_balance), actualBalance + "");
                    editor.apply();
                    textBalance.setText(AppDelegate.currencyFormat(preferences.getString(getString(R.string.param_libra_balance), "")));
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.e("Helo", "Hello");
                //String responseString = new String(errorResponse);
                //Log.e("Response", responseString);
            }
        });
    }
}
