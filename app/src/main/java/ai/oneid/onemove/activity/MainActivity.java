package ai.oneid.onemove.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.javiersantos.appupdater.AppUpdater;
import com.google.android.material.navigation.NavigationView;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.pedro.library.AutoPermissions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import ai.oneid.onemove.R;
import ai.oneid.onemove.data.AppDelegate;
import ai.oneid.onemove.data.AsynRestClient;
import ai.oneid.onemove.fragment.ActivityFragment;
import ai.oneid.onemove.fragment.ContactsFragment;
import ai.oneid.onemove.fragment.HomeFragment;
import ai.oneid.onemove.fragment.ProfileFragment;
import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    public AppDelegate delegate;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle drawerToggle;
    public NavigationView navigationView;

    public AppCompatImageView imageProfile;
    public TextView textBalance;
    public LinearLayout layoutHome;
    public LinearLayout layoutContacts;
    public LinearLayout layoutActivity;
    public LinearLayout layoutProfile;
    public ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.loading));
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        delegate = (AppDelegate)getApplication();
        delegate.createDirectoryIfNotExist(Environment.getExternalStorageDirectory().toString() + AppDelegate.appPath);

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        navigationView = findViewById(R.id.nav_view);
        imageProfile = navigationView.getHeaderView(0).findViewById(R.id.image_profile);
        textBalance = navigationView.getHeaderView(0).findViewById(R.id.text_balance);
        layoutHome = navigationView.getHeaderView(0).findViewById(R.id.layout_home);
        layoutHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToHomeFragment();
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
        });
        layoutContacts = navigationView.getHeaderView(0).findViewById(R.id.layout_contacts);
        layoutContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AppDelegate.isRegistered(MainActivity.this)) {
                    changeToContactsFragment();
                }
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
        });
        layoutActivity = navigationView.getHeaderView(0).findViewById(R.id.layout_activity);
        layoutActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToActivityFragment();
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
        });
        layoutProfile = navigationView.getHeaderView(0).findViewById(R.id.layout_profile);
        layoutProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AppDelegate.isRegistered(MainActivity.this)) {
                    changeToProfileFragment();
                }
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
        });

        changeToHomeFragment();
        refreshProfileImage();

        SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        Log.e("Libra address", preferences.getString(getString(R.string.param_libra_wallet_address), ""));

        delegate.createDirectoryIfNotExist(Environment.getExternalStorageDirectory().toString() + AppDelegate.appPath + AppDelegate.tempFolder);
        AutoPermissions.Companion.loadAllPermissions(this, 1);

        AppUpdater appUpdater = new AppUpdater(this);
        appUpdater.start();
    }

    public void refreshProfileImage()
    {
        SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        String selfie = preferences.getString(getResources().getString(R.string.param_oneid_selfie), "");
        if(!selfie.equals("")) {
            viewUpload(selfie);
        }
    }

    private void viewUpload(String documentName)
    {
        SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("token", preferences.getString(getString(R.string.param_oneid_token), ""));
            jsonObject.put("document_name", documentName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.e("Input", jsonObject.toString());
        AsynRestClient.genericPost(this, AsynRestClient.viewUploadUrl, jsonObject.toString(), new AsyncHttpResponseHandler() {
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
                    if(jsonObject.has("error"))
                    {
                        JSONObject errorObject = jsonObject.getJSONObject("error");
                        return;
                    }
                    String imageBase64 = jsonObject.getString("data");
                    byte[] imageByteArray = Base64.decode(imageBase64, Base64.DEFAULT);
                    Glide.with(MainActivity.this).load(imageByteArray).apply(RequestOptions.circleCropTransform()).into(imageProfile);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                //alert(getString(R.string.error), getString(R.string.error_please_try_again));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(drawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    public void changeToHomeFragment()
    {
        hideSoftKeyboard();
        HomeFragment fragment = new HomeFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        popBackStack();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();
    }

    public void changeToActivityFragment()
    {
        hideSoftKeyboard();
        ActivityFragment fragment = new ActivityFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        popBackStack();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();
    }

    public void changeToContactsFragment()
    {
        hideSoftKeyboard();
        ContactsFragment fragment = new ContactsFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        popBackStack();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();
    }

    public void changeToProfileFragment()
    {
        hideSoftKeyboard();
        ProfileFragment fragment = new ProfileFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        popBackStack();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();
    }

    public void popBackStack()
    {
        try {
            getSupportFragmentManager().popBackStack(null, android.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void hideSoftKeyboard()
    {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void onResume()
    {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        retrieveBalance();
        textBalance.setText(AppDelegate.currencyFormat(preferences.getString(getResources().getString(R.string.param_libra_balance), "")));
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
                //progressDialog.show();
            }

            @Override
            public void onFinish() {
                //progressDialog.dismiss();
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
            }
        });
    }
}
