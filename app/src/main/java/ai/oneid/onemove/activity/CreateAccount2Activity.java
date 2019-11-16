package ai.oneid.onemove.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import ai.oneid.onemove.R;
import ai.oneid.onemove.data.AppDelegate;
import ai.oneid.onemove.data.AsynRestClient;
import cz.msebera.android.httpclient.Header;

public class CreateAccount2Activity extends AppCompatActivity {
    public AppDelegate delegate;
    public EditText inputEmail;
    public EditText inputContact;
    public EditText inputPassword;
    public Button buttonContinue;
    public ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setContentView(R.layout.activity_create_account_2);

        delegate = (AppDelegate)getApplication();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.loading));
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);

        inputEmail = findViewById(R.id.input_email);
        inputContact = findViewById(R.id.input_contact);
        inputContact.setText(delegate.getCountryZipCode() + "");
        inputPassword = findViewById(R.id.input_password);
//        imageSwitch = findViewById(R.id.image_switch);
//        imageSwitch.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(haveReferralCode)
//                {
//                    haveReferralCode = false;
//                    Glide.with(CreateAccount2Activity.this).load(R.drawable.switch_off).into(imageSwitch);
//                    layoutReferralCode.setVisibility(View.GONE);
//                }
//                else {
//                    haveReferralCode = true;
//                    Glide.with(CreateAccount2Activity.this).load(R.drawable.switch_on).into(imageSwitch);
//                    layoutReferralCode.setVisibility(View.VISIBLE);
//                }
//            }
//        });
//        layoutReferralCode = findViewById(R.id.layout_referral_code);
//        inputReferralCode = findViewById(R.id.input_referral_code);
        buttonContinue = findViewById(R.id.button_continue);
        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputEmail.setError(null);
                inputContact.setError(null);
                inputPassword.setError(null);
                if(inputEmail.getText().toString().equals("") || !android.util.Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches())
                {
                    inputEmail.setError(getString(R.string.error_invalid_email));
                }
                if(inputContact.getText().toString().equals(""))
                {
                    inputContact.setError(getString(R.string.error_required_field));
                }
                if(inputPassword.getText().toString().equals(""))
                {
                    inputPassword.setError(getString(R.string.error_required_field));
                }

                if(!inputEmail.getText().toString().equals("") && android.util.Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches() && !inputContact.getText().toString().equals("") &&
                        !inputPassword.getText().toString().equals(""))
                {
                    SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(getResources().getString(R.string.param_email), inputEmail.getText().toString());
                    editor.putString(getResources().getString(R.string.param_contact), inputContact.getText().toString());
                    editor.putString(getResources().getString(R.string.param_password), inputPassword.getText().toString());
                    editor.apply();
                    createIdentityWallet();
                }
            }
        });
    }

    private void createIdentityWallet()
    {
        JSONObject jsonObject = new JSONObject();
        try {
            SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
            jsonObject.put("wallet_name", preferences.getString(getString(R.string.param_first_name)," ") + " " + preferences.getString(getString(R.string.param_last_name), ""));
            jsonObject.put("passphrase", inputPassword.getText().toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        AsynRestClient.genericPost(this, AsynRestClient.createIdentityWalletUrl, jsonObject.toString(), new AsyncHttpResponseHandler() {
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

                    if(jsonObject.has("error"))
                    {
                        JSONObject errorObject = jsonObject.getJSONObject("error");
                        alert(errorObject.getString("type"), errorObject.getString("message"));
                        return;
                    }
                    JSONObject innerObject = jsonObject.getJSONObject("data");
                    SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
                    final SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(getResources().getString(R.string.param_oneid_wallet_name), innerObject.getString("wallet_name"));
                    editor.putString(getResources().getString(R.string.param_oneid_wallet_address), innerObject.getString("wallet_id"));
                    editor.putString(getResources().getString(R.string.param_oneid_created_at), innerObject.getString("created_at"));
                    editor.putString(getResources().getString(R.string.param_oneid_endpoint_did), innerObject.getString("endpoint_did"));
                    editor.putString(getResources().getString(R.string.param_oneid_token), innerObject.getString("token"));
                    editor.apply();
                    createWallet();
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                //String responseString = new String(errorResponse);
                //Log.e("Response", responseString);
                alert(getString(R.string.error), getString(R.string.error_please_try_again));
                Log.e("Helo", "Hello");
            }
        });
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

    public void onBackPressed()
    {
        Intent i = new Intent(CreateAccount2Activity.this, CreateAccount1Activity.class);
        startActivity(i);
        finish();
    }

    private void createWallet()
    {
        SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", preferences.getString(getString(R.string.param_libra_wallet_address), ""));
            jsonObject.put("first_name", preferences.getString(getString(R.string.param_first_name), ""));
            jsonObject.put("last_name", preferences.getString(getString(R.string.param_last_name), ""));
            jsonObject.put("contact", preferences.getString(getString(R.string.param_contact), ""));
            jsonObject.put("wallet_address", preferences.getString(getString(R.string.param_libra_wallet_address), ""));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.e("Json", jsonObject.toString());
        AsynRestClient.genericPost(this, AsynRestClient.utilityUpdateAccountUrl, jsonObject.toString(), new AsyncHttpResponseHandler() {
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
                        String id = jsonObject.getString("id");
                        SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(getResources().getString(R.string.param_utility_user_id), id);
                        editor.apply();

                        Intent i = new Intent(CreateAccount2Activity.this, MainActivity.class);
                        startActivity(i);
                        finish();
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
