package ai.oneid.onemove.fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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

import java.util.ArrayList;

import ai.oneid.onemove.R;
import ai.oneid.onemove.activity.MainActivity;
import ai.oneid.onemove.activity.SearchContactActivity;
import ai.oneid.onemove.adapter.ContactsAdapter;
import ai.oneid.onemove.data.AppDelegate;
import ai.oneid.onemove.data.AsynRestClient;
import cz.msebera.android.httpclient.Header;

public class ContactsFragment extends Fragment
{
    public MainActivity activity;
    public AppCompatImageView imageSideMenu;

    public EditText inputSearch;
    public LinearLayout layoutAddNew;

    public RecyclerView recyclerView;
    public ContactsAdapter adapter;

    public ArrayList<String> idList = new ArrayList<String>();
    public ArrayList<String> nameList = new ArrayList<String>();
    public ArrayList<String> addressList = new ArrayList<String>();
    public ArrayList<String> contactList = new ArrayList<String>();
    public ArrayList<String> createdList = new ArrayList<String>();

    public ProgressDialog progressDialog;
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
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

        inputSearch = rootView.findViewById(R.id.input_search);
        inputSearch.setCompoundDrawablesWithIntrinsicBounds(R.drawable.search_inactive, 0, 0, 0);
        inputSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // Your piece of code on keyboard search click
                    retrieveFriend();
                    hideSoftKeyboard();
                    return true;
                }
                return false;
            }
        });

        layoutAddNew = rootView.findViewById(R.id.layout_add_new);
        layoutAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, SearchContactActivity.class);
                startActivity(intent);
            }
        });

        recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        adapter = new ContactsAdapter(this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        return rootView;
    }

    public void hideSoftKeyboard()
    {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        retrieveFriend();
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
            jsonObject.put("search", inputSearch.getText().toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.e("Json", jsonObject.toString());
        AsynRestClient.genericPost(activity, AsynRestClient.utilityRetrieveFriendUrl, jsonObject.toString(), new AsyncHttpResponseHandler() {
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