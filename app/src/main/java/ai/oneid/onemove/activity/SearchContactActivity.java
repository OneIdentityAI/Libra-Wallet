package ai.oneid.onemove.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.zxing.client.android.CaptureActivity;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import ai.oneid.onemove.R;
import ai.oneid.onemove.adapter.ContactsAdapter;
import ai.oneid.onemove.adapter.SearchContactAdapter;
import ai.oneid.onemove.data.AppDelegate;
import ai.oneid.onemove.data.AsynRestClient;
import cz.msebera.android.httpclient.Header;

public class SearchContactActivity extends AppCompatActivity {
    public AppCompatImageView imageBack;

    public EditText inputSearch;
    public LinearLayout layoutAddNew;

    public RecyclerView recyclerView;
    public SearchContactAdapter adapter;

    public ArrayList<String> idList = new ArrayList<String>();
    public ArrayList<String> nameList = new ArrayList<String>();
    public ArrayList<String> addressList = new ArrayList<String>();
    public ArrayList<String> contactList = new ArrayList<String>();
    public ArrayList<String> createdList = new ArrayList<String>();
    public ProgressDialog progressDialog;

    public static int BARCODE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setContentView(R.layout.activity_search_contact);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.loading));
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);

        imageBack = findViewById(R.id.image_back);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        inputSearch = findViewById(R.id.input_search);
        inputSearch.setCompoundDrawablesWithIntrinsicBounds(R.drawable.search_inactive, 0, 0, 0);
        inputSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // Your piece of code on keyboard search click
                    searchUser();
                    hideSoftKeyboard();
                    return true;
                }
                return false;
            }
        });
        layoutAddNew = findViewById(R.id.layout_add_new);
        layoutAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchContactActivity.this, CaptureActivity.class);
                intent.setAction("com.google.zxing.client.android.SCAN");
                intent.putExtra("SAVE_HISTORY", false);
                startActivityForResult(intent, BARCODE_REQUEST_CODE);
            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        adapter = new SearchContactAdapter(this);
        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();
    }

    public void hideSoftKeyboard()
    {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void searchUser()
    {
        SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", preferences.getString(getString(R.string.param_utility_user_id), ""));
            jsonObject.put("search", inputSearch.getText().toString());
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
                        idList.clear();
                        nameList.clear();
                        addressList.clear();
                        contactList.clear();
                        createdList.clear();
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

                            idList.add(id);
                            nameList.add(last_name + " " + first_name);
                            contactList.add(contact);
                            addressList.add(wallet_address);
                            createdList.add(created);
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

    public void addUser(String id)
    {
        SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", preferences.getString(getString(R.string.param_utility_user_id), ""));
            jsonObject.put("user_id", id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.e("Json", jsonObject.toString());
        AsynRestClient.genericPost(this, AsynRestClient.utilityAddUserUrl, jsonObject.toString(), new AsyncHttpResponseHandler() {
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
                    String message = jsonObject.getString("message");
                    alert("", message);
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

        if (requestCode == BARCODE_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                String contents = intent.getStringExtra("SCAN_RESULT");
                inputSearch.setText(contents);
                searchUser();
            }
            else if (resultCode == RESULT_CANCELED)
            {
            }
        }
    }
}
