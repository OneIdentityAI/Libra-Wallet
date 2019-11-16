package ai.oneid.onemove.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;

import ai.oneid.liveness.android.activity.CameraActivity;
import ai.oneid.onemove.R;
import ai.oneid.onemove.activity.MainActivity;
import ai.oneid.onemove.activity.VerificationResultActivity;
import ai.oneid.onemove.data.AppDelegate;
import ai.oneid.onemove.data.AsynRestClient;
import cz.msebera.android.httpclient.Header;

public class ProfileFragment extends Fragment implements DatePickerDialog.OnDateSetListener
{
    public boolean selfieUploadStatus = false;
    public MainActivity activity;
    public AppCompatImageView imageSideMenu;
    public AppCompatImageView imageArrow;

    public LinearLayout layoutCompleteProfileSection;
    public AppCompatImageView imageProfile;
    public TextView textInformation;
    public EditText inputFirstName;
    public EditText inputLastName;
    public EditText inputEmail;
    public EditText inputContact;
    public EditText inputGender;
    public EditText inputDob;
    public EditText inputNationality;
    public EditText inputIdType;
    public EditText inputIdNo;
    public Button buttonAutomaticVerification;
    public Button buttonCreateIdentity;
    public Button buttonVerificationResult;
    public AppCompatImageView imageUploadId;
    public LinearLayout layoutHidden1;
    public LinearLayout layoutHidden2;
    public LinearLayout layoutHidden3;

    public ArrayList<String> nationalityList = new ArrayList<String>();
    public ArrayList<String> code3List = new ArrayList<String>();
    public ArrayList<String> code2List = new ArrayList<String>();
    public ProgressDialog progressDialog;
    public boolean isShowDetail = false;

    public Uri uri;
    public String imageName = "";
    public String imagePath = "";
    public String imageType = "";
    public int LIVENESS_REQUEST_CODE = 1357;
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        activity = (MainActivity)getActivity();

        activity.delegate.createDirectoryIfNotExist(Environment.getExternalStorageDirectory().toString() + AppDelegate.appPath);

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

        layoutCompleteProfileSection = rootView.findViewById(R.id.layout_complete_profile_section);
        imageProfile = rootView.findViewById(R.id.image_profile);

        textInformation = rootView.findViewById(R.id.text_information);
        inputFirstName = rootView.findViewById(R.id.input_first_name);
        inputFirstName.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(getString(R.string.param_first_name), inputFirstName.getText().toString());
                editor.apply();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        inputLastName = rootView.findViewById(R.id.input_last_name);
        inputLastName.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(getString(R.string.param_last_name), inputLastName.getText().toString());
                editor.apply();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        inputEmail = rootView.findViewById(R.id.input_email);
        inputContact = rootView.findViewById(R.id.input_contact);
        inputContact.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(getString(R.string.param_contact), inputContact.getText().toString());
                editor.apply();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        inputGender = rootView.findViewById(R.id.input_gender);
        inputGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Choose gender");
                final String[] data = getResources().getStringArray(R.array.gender);
                builder.setItems(data, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        inputGender.setText(data[which]);
                        SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(getResources().getString(R.string.param_oneid_gender), inputGender.getText().toString());
                        editor.apply();

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        inputDob = rootView.findViewById(R.id.input_dob);
        inputDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Calendar now = Calendar.getInstance();
                try {
                    now.setTime(simpleDateFormat.parse(inputDob.getText().toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        ProfileFragment.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );

                dpd.show(activity.getFragmentManager(), "Datepickerdialog");
            }
        });
        inputNationality = rootView.findViewById(R.id.input_nationality);
        inputNationality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Choose your nationality");
                final String[] item = nationalityList.toArray(new String[nationalityList.size()]);
                builder.setItems(item, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        inputNationality.setText(item[which]);
                        SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(getString(R.string.param_oneid_nationality), inputNationality.getText().toString());
                        editor.putString(getString(R.string.param_oneid_nationality_id), code3List.get(which));
                        editor.apply();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        inputIdType = rootView.findViewById(R.id.input_id_type);
        inputIdType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(getString(R.string.choose_id_type));
                final String[] dataId = getResources().getStringArray(R.array.id_type);
                final String[] dataName = getResources().getStringArray(R.array.id_type_name);
                builder.setItems(dataName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        inputIdType.setText(dataName[which]);
                        SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
                        final SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(getResources().getString(R.string.param_oneid_id_type), dataId[0]);
                        editor.putString(getResources().getString(R.string.param_oneid_id_type_name), dataName[0]);
                        editor.apply();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        inputIdNo = rootView.findViewById(R.id.input_id_no);
        inputIdNo.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(getString(R.string.param_oneid_id_no), inputIdNo.getText().toString());
                editor.apply();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        imageUploadId = rootView.findViewById(R.id.image_upload_id);
        imageUploadId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
                //if(preferences.getString(getString(R.string.param_oneid_credential), "").equals("")) {
                    inputIdNo.setError(null);
                    if(inputIdNo.getText().toString().equals(""))
                        inputIdNo.setError(getString(R.string.error_required_field));
                    else {
                        imageType = "id";
                        ImagePicker.with(ProfileFragment.this)
                                .setToolbarColor("#58b3ff")
                                .setStatusBarColor("#027FE5")       //  StatusBar color (works with SDK >= 21  )
                                .setToolbarTextColor("#FFFFFF")     //  Toolbar text color (Title and Done button)
                                .setToolbarIconColor("#FFFFFF")     //  Toolbar icon color (Back and Camera button)
                                .setProgressBarColor("#027FE5")     //  ProgressBar color
                                .setBackgroundColor("#FFFFFF")      //  Background color
                                .setCameraOnly(false)               //  Camera mode
                                .setMultipleMode(false)              //  Select multiple images or single image
                                .setFolderMode(true)                //  Folder mode
                                .setShowCamera(true)                //  Show camera button
                                .setMaxSize(1)                     //  Max images can be selected
                                .setAlwaysShowDoneButton(true)      //  Set always show done button in multiple mode
                                .setKeepScreenOn(true)              //  Keep screen on when selecting images
                                .start();                           //  Start ImagePicker
                    }
                }
            //}
        });
        try {
            JSONArray jsonArray = new JSONArray("[\n" +
                    "    {\n" +
                    "        \"num_code\": \"4\",\n" +
                    "        \"alpha_2_code\": \"AF\",\n" +
                    "        \"alpha_3_code\": \"AFG\",\n" +
                    "        \"en_short_name\": \"Afghanistan\",\n" +
                    "        \"nationality\": \"Afghan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"248\",\n" +
                    "        \"alpha_2_code\": \"AX\",\n" +
                    "        \"alpha_3_code\": \"ALA\",\n" +
                    "        \"en_short_name\": \"\\u00c5land Islands\",\n" +
                    "        \"nationality\": \"\\u00c5land Island\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"8\",\n" +
                    "        \"alpha_2_code\": \"AL\",\n" +
                    "        \"alpha_3_code\": \"ALB\",\n" +
                    "        \"en_short_name\": \"Albania\",\n" +
                    "        \"nationality\": \"Albanian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"12\",\n" +
                    "        \"alpha_2_code\": \"DZ\",\n" +
                    "        \"alpha_3_code\": \"DZA\",\n" +
                    "        \"en_short_name\": \"Algeria\",\n" +
                    "        \"nationality\": \"Algerian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"16\",\n" +
                    "        \"alpha_2_code\": \"AS\",\n" +
                    "        \"alpha_3_code\": \"ASM\",\n" +
                    "        \"en_short_name\": \"American Samoa\",\n" +
                    "        \"nationality\": \"American Samoan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"20\",\n" +
                    "        \"alpha_2_code\": \"AD\",\n" +
                    "        \"alpha_3_code\": \"AND\",\n" +
                    "        \"en_short_name\": \"Andorra\",\n" +
                    "        \"nationality\": \"Andorran\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"24\",\n" +
                    "        \"alpha_2_code\": \"AO\",\n" +
                    "        \"alpha_3_code\": \"AGO\",\n" +
                    "        \"en_short_name\": \"Angola\",\n" +
                    "        \"nationality\": \"Angolan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"660\",\n" +
                    "        \"alpha_2_code\": \"AI\",\n" +
                    "        \"alpha_3_code\": \"AIA\",\n" +
                    "        \"en_short_name\": \"Anguilla\",\n" +
                    "        \"nationality\": \"Anguillan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"10\",\n" +
                    "        \"alpha_2_code\": \"AQ\",\n" +
                    "        \"alpha_3_code\": \"ATA\",\n" +
                    "        \"en_short_name\": \"Antarctica\",\n" +
                    "        \"nationality\": \"Antarctic\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"28\",\n" +
                    "        \"alpha_2_code\": \"AG\",\n" +
                    "        \"alpha_3_code\": \"ATG\",\n" +
                    "        \"en_short_name\": \"Antigua and Barbuda\",\n" +
                    "        \"nationality\": \"Antiguan or Barbudan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"32\",\n" +
                    "        \"alpha_2_code\": \"AR\",\n" +
                    "        \"alpha_3_code\": \"ARG\",\n" +
                    "        \"en_short_name\": \"Argentina\",\n" +
                    "        \"nationality\": \"Argentine\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"51\",\n" +
                    "        \"alpha_2_code\": \"AM\",\n" +
                    "        \"alpha_3_code\": \"ARM\",\n" +
                    "        \"en_short_name\": \"Armenia\",\n" +
                    "        \"nationality\": \"Armenian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"533\",\n" +
                    "        \"alpha_2_code\": \"AW\",\n" +
                    "        \"alpha_3_code\": \"ABW\",\n" +
                    "        \"en_short_name\": \"Aruba\",\n" +
                    "        \"nationality\": \"Aruban\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"36\",\n" +
                    "        \"alpha_2_code\": \"AU\",\n" +
                    "        \"alpha_3_code\": \"AUS\",\n" +
                    "        \"en_short_name\": \"Australia\",\n" +
                    "        \"nationality\": \"Australian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"40\",\n" +
                    "        \"alpha_2_code\": \"AT\",\n" +
                    "        \"alpha_3_code\": \"AUT\",\n" +
                    "        \"en_short_name\": \"Austria\",\n" +
                    "        \"nationality\": \"Austrian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"31\",\n" +
                    "        \"alpha_2_code\": \"AZ\",\n" +
                    "        \"alpha_3_code\": \"AZE\",\n" +
                    "        \"en_short_name\": \"Azerbaijan\",\n" +
                    "        \"nationality\": \"Azerbaijani, Azeri\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"44\",\n" +
                    "        \"alpha_2_code\": \"BS\",\n" +
                    "        \"alpha_3_code\": \"BHS\",\n" +
                    "        \"en_short_name\": \"Bahamas\",\n" +
                    "        \"nationality\": \"Bahamian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"48\",\n" +
                    "        \"alpha_2_code\": \"BH\",\n" +
                    "        \"alpha_3_code\": \"BHR\",\n" +
                    "        \"en_short_name\": \"Bahrain\",\n" +
                    "        \"nationality\": \"Bahraini\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"50\",\n" +
                    "        \"alpha_2_code\": \"BD\",\n" +
                    "        \"alpha_3_code\": \"BGD\",\n" +
                    "        \"en_short_name\": \"Bangladesh\",\n" +
                    "        \"nationality\": \"Bangladeshi\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"52\",\n" +
                    "        \"alpha_2_code\": \"BB\",\n" +
                    "        \"alpha_3_code\": \"BRB\",\n" +
                    "        \"en_short_name\": \"Barbados\",\n" +
                    "        \"nationality\": \"Barbadian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"112\",\n" +
                    "        \"alpha_2_code\": \"BY\",\n" +
                    "        \"alpha_3_code\": \"BLR\",\n" +
                    "        \"en_short_name\": \"Belarus\",\n" +
                    "        \"nationality\": \"Belarusian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"56\",\n" +
                    "        \"alpha_2_code\": \"BE\",\n" +
                    "        \"alpha_3_code\": \"BEL\",\n" +
                    "        \"en_short_name\": \"Belgium\",\n" +
                    "        \"nationality\": \"Belgian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"84\",\n" +
                    "        \"alpha_2_code\": \"BZ\",\n" +
                    "        \"alpha_3_code\": \"BLZ\",\n" +
                    "        \"en_short_name\": \"Belize\",\n" +
                    "        \"nationality\": \"Belizean\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"204\",\n" +
                    "        \"alpha_2_code\": \"BJ\",\n" +
                    "        \"alpha_3_code\": \"BEN\",\n" +
                    "        \"en_short_name\": \"Benin\",\n" +
                    "        \"nationality\": \"Beninese, Beninois\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"60\",\n" +
                    "        \"alpha_2_code\": \"BM\",\n" +
                    "        \"alpha_3_code\": \"BMU\",\n" +
                    "        \"en_short_name\": \"Bermuda\",\n" +
                    "        \"nationality\": \"Bermudian, Bermudan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"64\",\n" +
                    "        \"alpha_2_code\": \"BT\",\n" +
                    "        \"alpha_3_code\": \"BTN\",\n" +
                    "        \"en_short_name\": \"Bhutan\",\n" +
                    "        \"nationality\": \"Bhutanese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"68\",\n" +
                    "        \"alpha_2_code\": \"BO\",\n" +
                    "        \"alpha_3_code\": \"BOL\",\n" +
                    "        \"en_short_name\": \"Bolivia (Plurinational State of)\",\n" +
                    "        \"nationality\": \"Bolivian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"535\",\n" +
                    "        \"alpha_2_code\": \"BQ\",\n" +
                    "        \"alpha_3_code\": \"BES\",\n" +
                    "        \"en_short_name\": \"Bonaire, Sint Eustatius and Saba\",\n" +
                    "        \"nationality\": \"Bonaire\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"70\",\n" +
                    "        \"alpha_2_code\": \"BA\",\n" +
                    "        \"alpha_3_code\": \"BIH\",\n" +
                    "        \"en_short_name\": \"Bosnia and Herzegovina\",\n" +
                    "        \"nationality\": \"Bosnian or Herzegovinian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"72\",\n" +
                    "        \"alpha_2_code\": \"BW\",\n" +
                    "        \"alpha_3_code\": \"BWA\",\n" +
                    "        \"en_short_name\": \"Botswana\",\n" +
                    "        \"nationality\": \"Motswana, Botswanan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"74\",\n" +
                    "        \"alpha_2_code\": \"BV\",\n" +
                    "        \"alpha_3_code\": \"BVT\",\n" +
                    "        \"en_short_name\": \"Bouvet Island\",\n" +
                    "        \"nationality\": \"Bouvet Island\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"76\",\n" +
                    "        \"alpha_2_code\": \"BR\",\n" +
                    "        \"alpha_3_code\": \"BRA\",\n" +
                    "        \"en_short_name\": \"Brazil\",\n" +
                    "        \"nationality\": \"Brazilian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"86\",\n" +
                    "        \"alpha_2_code\": \"IO\",\n" +
                    "        \"alpha_3_code\": \"IOT\",\n" +
                    "        \"en_short_name\": \"British Indian Ocean Territory\",\n" +
                    "        \"nationality\": \"BIOT\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"96\",\n" +
                    "        \"alpha_2_code\": \"BN\",\n" +
                    "        \"alpha_3_code\": \"BRN\",\n" +
                    "        \"en_short_name\": \"Brunei Darussalam\",\n" +
                    "        \"nationality\": \"Bruneian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"100\",\n" +
                    "        \"alpha_2_code\": \"BG\",\n" +
                    "        \"alpha_3_code\": \"BGR\",\n" +
                    "        \"en_short_name\": \"Bulgaria\",\n" +
                    "        \"nationality\": \"Bulgarian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"854\",\n" +
                    "        \"alpha_2_code\": \"BF\",\n" +
                    "        \"alpha_3_code\": \"BFA\",\n" +
                    "        \"en_short_name\": \"Burkina Faso\",\n" +
                    "        \"nationality\": \"Burkinab\\u00e9\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"108\",\n" +
                    "        \"alpha_2_code\": \"BI\",\n" +
                    "        \"alpha_3_code\": \"BDI\",\n" +
                    "        \"en_short_name\": \"Burundi\",\n" +
                    "        \"nationality\": \"Burundian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"132\",\n" +
                    "        \"alpha_2_code\": \"CV\",\n" +
                    "        \"alpha_3_code\": \"CPV\",\n" +
                    "        \"en_short_name\": \"Cabo Verde\",\n" +
                    "        \"nationality\": \"Cabo Verdean\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"116\",\n" +
                    "        \"alpha_2_code\": \"KH\",\n" +
                    "        \"alpha_3_code\": \"KHM\",\n" +
                    "        \"en_short_name\": \"Cambodia\",\n" +
                    "        \"nationality\": \"Cambodian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"120\",\n" +
                    "        \"alpha_2_code\": \"CM\",\n" +
                    "        \"alpha_3_code\": \"CMR\",\n" +
                    "        \"en_short_name\": \"Cameroon\",\n" +
                    "        \"nationality\": \"Cameroonian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"124\",\n" +
                    "        \"alpha_2_code\": \"CA\",\n" +
                    "        \"alpha_3_code\": \"CAN\",\n" +
                    "        \"en_short_name\": \"Canada\",\n" +
                    "        \"nationality\": \"Canadian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"136\",\n" +
                    "        \"alpha_2_code\": \"KY\",\n" +
                    "        \"alpha_3_code\": \"CYM\",\n" +
                    "        \"en_short_name\": \"Cayman Islands\",\n" +
                    "        \"nationality\": \"Caymanian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"140\",\n" +
                    "        \"alpha_2_code\": \"CF\",\n" +
                    "        \"alpha_3_code\": \"CAF\",\n" +
                    "        \"en_short_name\": \"Central African Republic\",\n" +
                    "        \"nationality\": \"Central African\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"148\",\n" +
                    "        \"alpha_2_code\": \"TD\",\n" +
                    "        \"alpha_3_code\": \"TCD\",\n" +
                    "        \"en_short_name\": \"Chad\",\n" +
                    "        \"nationality\": \"Chadian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"152\",\n" +
                    "        \"alpha_2_code\": \"CL\",\n" +
                    "        \"alpha_3_code\": \"CHL\",\n" +
                    "        \"en_short_name\": \"Chile\",\n" +
                    "        \"nationality\": \"Chilean\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"156\",\n" +
                    "        \"alpha_2_code\": \"CN\",\n" +
                    "        \"alpha_3_code\": \"CHN\",\n" +
                    "        \"en_short_name\": \"China\",\n" +
                    "        \"nationality\": \"Chinese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"162\",\n" +
                    "        \"alpha_2_code\": \"CX\",\n" +
                    "        \"alpha_3_code\": \"CXR\",\n" +
                    "        \"en_short_name\": \"Christmas Island\",\n" +
                    "        \"nationality\": \"Christmas Island\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"166\",\n" +
                    "        \"alpha_2_code\": \"CC\",\n" +
                    "        \"alpha_3_code\": \"CCK\",\n" +
                    "        \"en_short_name\": \"Cocos (Keeling) Islands\",\n" +
                    "        \"nationality\": \"Cocos Island\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"170\",\n" +
                    "        \"alpha_2_code\": \"CO\",\n" +
                    "        \"alpha_3_code\": \"COL\",\n" +
                    "        \"en_short_name\": \"Colombia\",\n" +
                    "        \"nationality\": \"Colombian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"174\",\n" +
                    "        \"alpha_2_code\": \"KM\",\n" +
                    "        \"alpha_3_code\": \"COM\",\n" +
                    "        \"en_short_name\": \"Comoros\",\n" +
                    "        \"nationality\": \"Comoran, Comorian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"178\",\n" +
                    "        \"alpha_2_code\": \"CG\",\n" +
                    "        \"alpha_3_code\": \"COG\",\n" +
                    "        \"en_short_name\": \"Congo (Republic of the)\",\n" +
                    "        \"nationality\": \"Congolese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"180\",\n" +
                    "        \"alpha_2_code\": \"CD\",\n" +
                    "        \"alpha_3_code\": \"COD\",\n" +
                    "        \"en_short_name\": \"Congo (Democratic Republic of the)\",\n" +
                    "        \"nationality\": \"Congolese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"184\",\n" +
                    "        \"alpha_2_code\": \"CK\",\n" +
                    "        \"alpha_3_code\": \"COK\",\n" +
                    "        \"en_short_name\": \"Cook Islands\",\n" +
                    "        \"nationality\": \"Cook Island\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"188\",\n" +
                    "        \"alpha_2_code\": \"CR\",\n" +
                    "        \"alpha_3_code\": \"CRI\",\n" +
                    "        \"en_short_name\": \"Costa Rica\",\n" +
                    "        \"nationality\": \"Costa Rican\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"384\",\n" +
                    "        \"alpha_2_code\": \"CI\",\n" +
                    "        \"alpha_3_code\": \"CIV\",\n" +
                    "        \"en_short_name\": \"C\\u00f4te d'Ivoire\",\n" +
                    "        \"nationality\": \"Ivorian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"191\",\n" +
                    "        \"alpha_2_code\": \"HR\",\n" +
                    "        \"alpha_3_code\": \"HRV\",\n" +
                    "        \"en_short_name\": \"Croatia\",\n" +
                    "        \"nationality\": \"Croatian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"192\",\n" +
                    "        \"alpha_2_code\": \"CU\",\n" +
                    "        \"alpha_3_code\": \"CUB\",\n" +
                    "        \"en_short_name\": \"Cuba\",\n" +
                    "        \"nationality\": \"Cuban\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"531\",\n" +
                    "        \"alpha_2_code\": \"CW\",\n" +
                    "        \"alpha_3_code\": \"CUW\",\n" +
                    "        \"en_short_name\": \"Cura\\u00e7ao\",\n" +
                    "        \"nationality\": \"Cura\\u00e7aoan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"196\",\n" +
                    "        \"alpha_2_code\": \"CY\",\n" +
                    "        \"alpha_3_code\": \"CYP\",\n" +
                    "        \"en_short_name\": \"Cyprus\",\n" +
                    "        \"nationality\": \"Cypriot\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"203\",\n" +
                    "        \"alpha_2_code\": \"CZ\",\n" +
                    "        \"alpha_3_code\": \"CZE\",\n" +
                    "        \"en_short_name\": \"Czech Republic\",\n" +
                    "        \"nationality\": \"Czech\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"208\",\n" +
                    "        \"alpha_2_code\": \"DK\",\n" +
                    "        \"alpha_3_code\": \"DNK\",\n" +
                    "        \"en_short_name\": \"Denmark\",\n" +
                    "        \"nationality\": \"Danish\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"262\",\n" +
                    "        \"alpha_2_code\": \"DJ\",\n" +
                    "        \"alpha_3_code\": \"DJI\",\n" +
                    "        \"en_short_name\": \"Djibouti\",\n" +
                    "        \"nationality\": \"Djiboutian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"212\",\n" +
                    "        \"alpha_2_code\": \"DM\",\n" +
                    "        \"alpha_3_code\": \"DMA\",\n" +
                    "        \"en_short_name\": \"Dominica\",\n" +
                    "        \"nationality\": \"Dominican\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"214\",\n" +
                    "        \"alpha_2_code\": \"DO\",\n" +
                    "        \"alpha_3_code\": \"DOM\",\n" +
                    "        \"en_short_name\": \"Dominican Republic\",\n" +
                    "        \"nationality\": \"Dominican\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"218\",\n" +
                    "        \"alpha_2_code\": \"EC\",\n" +
                    "        \"alpha_3_code\": \"ECU\",\n" +
                    "        \"en_short_name\": \"Ecuador\",\n" +
                    "        \"nationality\": \"Ecuadorian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"818\",\n" +
                    "        \"alpha_2_code\": \"EG\",\n" +
                    "        \"alpha_3_code\": \"EGY\",\n" +
                    "        \"en_short_name\": \"Egypt\",\n" +
                    "        \"nationality\": \"Egyptian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"222\",\n" +
                    "        \"alpha_2_code\": \"SV\",\n" +
                    "        \"alpha_3_code\": \"SLV\",\n" +
                    "        \"en_short_name\": \"El Salvador\",\n" +
                    "        \"nationality\": \"Salvadoran\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"226\",\n" +
                    "        \"alpha_2_code\": \"GQ\",\n" +
                    "        \"alpha_3_code\": \"GNQ\",\n" +
                    "        \"en_short_name\": \"Equatorial Guinea\",\n" +
                    "        \"nationality\": \"Equatorial Guinean, Equatoguinean\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"232\",\n" +
                    "        \"alpha_2_code\": \"ER\",\n" +
                    "        \"alpha_3_code\": \"ERI\",\n" +
                    "        \"en_short_name\": \"Eritrea\",\n" +
                    "        \"nationality\": \"Eritrean\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"233\",\n" +
                    "        \"alpha_2_code\": \"EE\",\n" +
                    "        \"alpha_3_code\": \"EST\",\n" +
                    "        \"en_short_name\": \"Estonia\",\n" +
                    "        \"nationality\": \"Estonian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"231\",\n" +
                    "        \"alpha_2_code\": \"ET\",\n" +
                    "        \"alpha_3_code\": \"ETH\",\n" +
                    "        \"en_short_name\": \"Ethiopia\",\n" +
                    "        \"nationality\": \"Ethiopian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"238\",\n" +
                    "        \"alpha_2_code\": \"FK\",\n" +
                    "        \"alpha_3_code\": \"FLK\",\n" +
                    "        \"en_short_name\": \"Falkland Islands (Malvinas)\",\n" +
                    "        \"nationality\": \"Falkland Island\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"234\",\n" +
                    "        \"alpha_2_code\": \"FO\",\n" +
                    "        \"alpha_3_code\": \"FRO\",\n" +
                    "        \"en_short_name\": \"Faroe Islands\",\n" +
                    "        \"nationality\": \"Faroese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"242\",\n" +
                    "        \"alpha_2_code\": \"FJ\",\n" +
                    "        \"alpha_3_code\": \"FJI\",\n" +
                    "        \"en_short_name\": \"Fiji\",\n" +
                    "        \"nationality\": \"Fijian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"246\",\n" +
                    "        \"alpha_2_code\": \"FI\",\n" +
                    "        \"alpha_3_code\": \"FIN\",\n" +
                    "        \"en_short_name\": \"Finland\",\n" +
                    "        \"nationality\": \"Finnish\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"250\",\n" +
                    "        \"alpha_2_code\": \"FR\",\n" +
                    "        \"alpha_3_code\": \"FRA\",\n" +
                    "        \"en_short_name\": \"France\",\n" +
                    "        \"nationality\": \"French\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"254\",\n" +
                    "        \"alpha_2_code\": \"GF\",\n" +
                    "        \"alpha_3_code\": \"GUF\",\n" +
                    "        \"en_short_name\": \"French Guiana\",\n" +
                    "        \"nationality\": \"French Guianese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"258\",\n" +
                    "        \"alpha_2_code\": \"PF\",\n" +
                    "        \"alpha_3_code\": \"PYF\",\n" +
                    "        \"en_short_name\": \"French Polynesia\",\n" +
                    "        \"nationality\": \"French Polynesian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"260\",\n" +
                    "        \"alpha_2_code\": \"TF\",\n" +
                    "        \"alpha_3_code\": \"ATF\",\n" +
                    "        \"en_short_name\": \"French Southern Territories\",\n" +
                    "        \"nationality\": \"French Southern Territories\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"266\",\n" +
                    "        \"alpha_2_code\": \"GA\",\n" +
                    "        \"alpha_3_code\": \"GAB\",\n" +
                    "        \"en_short_name\": \"Gabon\",\n" +
                    "        \"nationality\": \"Gabonese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"270\",\n" +
                    "        \"alpha_2_code\": \"GM\",\n" +
                    "        \"alpha_3_code\": \"GMB\",\n" +
                    "        \"en_short_name\": \"Gambia\",\n" +
                    "        \"nationality\": \"Gambian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"268\",\n" +
                    "        \"alpha_2_code\": \"GE\",\n" +
                    "        \"alpha_3_code\": \"GEO\",\n" +
                    "        \"en_short_name\": \"Georgia\",\n" +
                    "        \"nationality\": \"Georgian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"276\",\n" +
                    "        \"alpha_2_code\": \"DE\",\n" +
                    "        \"alpha_3_code\": \"DEU\",\n" +
                    "        \"en_short_name\": \"Germany\",\n" +
                    "        \"nationality\": \"German\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"288\",\n" +
                    "        \"alpha_2_code\": \"GH\",\n" +
                    "        \"alpha_3_code\": \"GHA\",\n" +
                    "        \"en_short_name\": \"Ghana\",\n" +
                    "        \"nationality\": \"Ghanaian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"292\",\n" +
                    "        \"alpha_2_code\": \"GI\",\n" +
                    "        \"alpha_3_code\": \"GIB\",\n" +
                    "        \"en_short_name\": \"Gibraltar\",\n" +
                    "        \"nationality\": \"Gibraltar\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"300\",\n" +
                    "        \"alpha_2_code\": \"GR\",\n" +
                    "        \"alpha_3_code\": \"GRC\",\n" +
                    "        \"en_short_name\": \"Greece\",\n" +
                    "        \"nationality\": \"Greek, Hellenic\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"304\",\n" +
                    "        \"alpha_2_code\": \"GL\",\n" +
                    "        \"alpha_3_code\": \"GRL\",\n" +
                    "        \"en_short_name\": \"Greenland\",\n" +
                    "        \"nationality\": \"Greenlandic\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"308\",\n" +
                    "        \"alpha_2_code\": \"GD\",\n" +
                    "        \"alpha_3_code\": \"GRD\",\n" +
                    "        \"en_short_name\": \"Grenada\",\n" +
                    "        \"nationality\": \"Grenadian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"312\",\n" +
                    "        \"alpha_2_code\": \"GP\",\n" +
                    "        \"alpha_3_code\": \"GLP\",\n" +
                    "        \"en_short_name\": \"Guadeloupe\",\n" +
                    "        \"nationality\": \"Guadeloupe\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"316\",\n" +
                    "        \"alpha_2_code\": \"GU\",\n" +
                    "        \"alpha_3_code\": \"GUM\",\n" +
                    "        \"en_short_name\": \"Guam\",\n" +
                    "        \"nationality\": \"Guamanian, Guambat\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"320\",\n" +
                    "        \"alpha_2_code\": \"GT\",\n" +
                    "        \"alpha_3_code\": \"GTM\",\n" +
                    "        \"en_short_name\": \"Guatemala\",\n" +
                    "        \"nationality\": \"Guatemalan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"831\",\n" +
                    "        \"alpha_2_code\": \"GG\",\n" +
                    "        \"alpha_3_code\": \"GGY\",\n" +
                    "        \"en_short_name\": \"Guernsey\",\n" +
                    "        \"nationality\": \"Channel Island\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"324\",\n" +
                    "        \"alpha_2_code\": \"GN\",\n" +
                    "        \"alpha_3_code\": \"GIN\",\n" +
                    "        \"en_short_name\": \"Guinea\",\n" +
                    "        \"nationality\": \"Guinean\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"624\",\n" +
                    "        \"alpha_2_code\": \"GW\",\n" +
                    "        \"alpha_3_code\": \"GNB\",\n" +
                    "        \"en_short_name\": \"Guinea-Bissau\",\n" +
                    "        \"nationality\": \"Bissau-Guinean\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"328\",\n" +
                    "        \"alpha_2_code\": \"GY\",\n" +
                    "        \"alpha_3_code\": \"GUY\",\n" +
                    "        \"en_short_name\": \"Guyana\",\n" +
                    "        \"nationality\": \"Guyanese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"332\",\n" +
                    "        \"alpha_2_code\": \"HT\",\n" +
                    "        \"alpha_3_code\": \"HTI\",\n" +
                    "        \"en_short_name\": \"Haiti\",\n" +
                    "        \"nationality\": \"Haitian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"334\",\n" +
                    "        \"alpha_2_code\": \"HM\",\n" +
                    "        \"alpha_3_code\": \"HMD\",\n" +
                    "        \"en_short_name\": \"Heard Island and McDonald Islands\",\n" +
                    "        \"nationality\": \"Heard Island or McDonald Islands\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"336\",\n" +
                    "        \"alpha_2_code\": \"VA\",\n" +
                    "        \"alpha_3_code\": \"VAT\",\n" +
                    "        \"en_short_name\": \"Vatican City State\",\n" +
                    "        \"nationality\": \"Vatican\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"340\",\n" +
                    "        \"alpha_2_code\": \"HN\",\n" +
                    "        \"alpha_3_code\": \"HND\",\n" +
                    "        \"en_short_name\": \"Honduras\",\n" +
                    "        \"nationality\": \"Honduran\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"344\",\n" +
                    "        \"alpha_2_code\": \"HK\",\n" +
                    "        \"alpha_3_code\": \"HKG\",\n" +
                    "        \"en_short_name\": \"Hong Kong\",\n" +
                    "        \"nationality\": \"Hong Kong, Hong Kongese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"348\",\n" +
                    "        \"alpha_2_code\": \"HU\",\n" +
                    "        \"alpha_3_code\": \"HUN\",\n" +
                    "        \"en_short_name\": \"Hungary\",\n" +
                    "        \"nationality\": \"Hungarian, Magyar\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"352\",\n" +
                    "        \"alpha_2_code\": \"IS\",\n" +
                    "        \"alpha_3_code\": \"ISL\",\n" +
                    "        \"en_short_name\": \"Iceland\",\n" +
                    "        \"nationality\": \"Icelandic\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"356\",\n" +
                    "        \"alpha_2_code\": \"IN\",\n" +
                    "        \"alpha_3_code\": \"IND\",\n" +
                    "        \"en_short_name\": \"India\",\n" +
                    "        \"nationality\": \"Indian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"360\",\n" +
                    "        \"alpha_2_code\": \"ID\",\n" +
                    "        \"alpha_3_code\": \"IDN\",\n" +
                    "        \"en_short_name\": \"Indonesia\",\n" +
                    "        \"nationality\": \"Indonesian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"364\",\n" +
                    "        \"alpha_2_code\": \"IR\",\n" +
                    "        \"alpha_3_code\": \"IRN\",\n" +
                    "        \"en_short_name\": \"Iran\",\n" +
                    "        \"nationality\": \"Iranian, Persian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"368\",\n" +
                    "        \"alpha_2_code\": \"IQ\",\n" +
                    "        \"alpha_3_code\": \"IRQ\",\n" +
                    "        \"en_short_name\": \"Iraq\",\n" +
                    "        \"nationality\": \"Iraqi\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"372\",\n" +
                    "        \"alpha_2_code\": \"IE\",\n" +
                    "        \"alpha_3_code\": \"IRL\",\n" +
                    "        \"en_short_name\": \"Ireland\",\n" +
                    "        \"nationality\": \"Irish\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"833\",\n" +
                    "        \"alpha_2_code\": \"IM\",\n" +
                    "        \"alpha_3_code\": \"IMN\",\n" +
                    "        \"en_short_name\": \"Isle of Man\",\n" +
                    "        \"nationality\": \"Manx\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"376\",\n" +
                    "        \"alpha_2_code\": \"IL\",\n" +
                    "        \"alpha_3_code\": \"ISR\",\n" +
                    "        \"en_short_name\": \"Israel\",\n" +
                    "        \"nationality\": \"Israeli\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"380\",\n" +
                    "        \"alpha_2_code\": \"IT\",\n" +
                    "        \"alpha_3_code\": \"ITA\",\n" +
                    "        \"en_short_name\": \"Italy\",\n" +
                    "        \"nationality\": \"Italian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"388\",\n" +
                    "        \"alpha_2_code\": \"JM\",\n" +
                    "        \"alpha_3_code\": \"JAM\",\n" +
                    "        \"en_short_name\": \"Jamaica\",\n" +
                    "        \"nationality\": \"Jamaican\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"392\",\n" +
                    "        \"alpha_2_code\": \"JP\",\n" +
                    "        \"alpha_3_code\": \"JPN\",\n" +
                    "        \"en_short_name\": \"Japan\",\n" +
                    "        \"nationality\": \"Japanese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"832\",\n" +
                    "        \"alpha_2_code\": \"JE\",\n" +
                    "        \"alpha_3_code\": \"JEY\",\n" +
                    "        \"en_short_name\": \"Jersey\",\n" +
                    "        \"nationality\": \"Channel Island\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"400\",\n" +
                    "        \"alpha_2_code\": \"JO\",\n" +
                    "        \"alpha_3_code\": \"JOR\",\n" +
                    "        \"en_short_name\": \"Jordan\",\n" +
                    "        \"nationality\": \"Jordanian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"398\",\n" +
                    "        \"alpha_2_code\": \"KZ\",\n" +
                    "        \"alpha_3_code\": \"KAZ\",\n" +
                    "        \"en_short_name\": \"Kazakhstan\",\n" +
                    "        \"nationality\": \"Kazakhstani, Kazakh\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"404\",\n" +
                    "        \"alpha_2_code\": \"KE\",\n" +
                    "        \"alpha_3_code\": \"KEN\",\n" +
                    "        \"en_short_name\": \"Kenya\",\n" +
                    "        \"nationality\": \"Kenyan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"296\",\n" +
                    "        \"alpha_2_code\": \"KI\",\n" +
                    "        \"alpha_3_code\": \"KIR\",\n" +
                    "        \"en_short_name\": \"Kiribati\",\n" +
                    "        \"nationality\": \"I-Kiribati\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"408\",\n" +
                    "        \"alpha_2_code\": \"KP\",\n" +
                    "        \"alpha_3_code\": \"PRK\",\n" +
                    "        \"en_short_name\": \"Korea (Democratic People's Republic of)\",\n" +
                    "        \"nationality\": \"North Korean\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"410\",\n" +
                    "        \"alpha_2_code\": \"KR\",\n" +
                    "        \"alpha_3_code\": \"KOR\",\n" +
                    "        \"en_short_name\": \"Korea (Republic of)\",\n" +
                    "        \"nationality\": \"South Korean\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"414\",\n" +
                    "        \"alpha_2_code\": \"KW\",\n" +
                    "        \"alpha_3_code\": \"KWT\",\n" +
                    "        \"en_short_name\": \"Kuwait\",\n" +
                    "        \"nationality\": \"Kuwaiti\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"417\",\n" +
                    "        \"alpha_2_code\": \"KG\",\n" +
                    "        \"alpha_3_code\": \"KGZ\",\n" +
                    "        \"en_short_name\": \"Kyrgyzstan\",\n" +
                    "        \"nationality\": \"Kyrgyzstani, Kyrgyz, Kirgiz, Kirghiz\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"418\",\n" +
                    "        \"alpha_2_code\": \"LA\",\n" +
                    "        \"alpha_3_code\": \"LAO\",\n" +
                    "        \"en_short_name\": \"Lao People's Democratic Republic\",\n" +
                    "        \"nationality\": \"Lao, Laotian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"428\",\n" +
                    "        \"alpha_2_code\": \"LV\",\n" +
                    "        \"alpha_3_code\": \"LVA\",\n" +
                    "        \"en_short_name\": \"Latvia\",\n" +
                    "        \"nationality\": \"Latvian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"422\",\n" +
                    "        \"alpha_2_code\": \"LB\",\n" +
                    "        \"alpha_3_code\": \"LBN\",\n" +
                    "        \"en_short_name\": \"Lebanon\",\n" +
                    "        \"nationality\": \"Lebanese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"426\",\n" +
                    "        \"alpha_2_code\": \"LS\",\n" +
                    "        \"alpha_3_code\": \"LSO\",\n" +
                    "        \"en_short_name\": \"Lesotho\",\n" +
                    "        \"nationality\": \"Basotho\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"430\",\n" +
                    "        \"alpha_2_code\": \"LR\",\n" +
                    "        \"alpha_3_code\": \"LBR\",\n" +
                    "        \"en_short_name\": \"Liberia\",\n" +
                    "        \"nationality\": \"Liberian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"434\",\n" +
                    "        \"alpha_2_code\": \"LY\",\n" +
                    "        \"alpha_3_code\": \"LBY\",\n" +
                    "        \"en_short_name\": \"Libya\",\n" +
                    "        \"nationality\": \"Libyan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"438\",\n" +
                    "        \"alpha_2_code\": \"LI\",\n" +
                    "        \"alpha_3_code\": \"LIE\",\n" +
                    "        \"en_short_name\": \"Liechtenstein\",\n" +
                    "        \"nationality\": \"Liechtenstein\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"440\",\n" +
                    "        \"alpha_2_code\": \"LT\",\n" +
                    "        \"alpha_3_code\": \"LTU\",\n" +
                    "        \"en_short_name\": \"Lithuania\",\n" +
                    "        \"nationality\": \"Lithuanian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"442\",\n" +
                    "        \"alpha_2_code\": \"LU\",\n" +
                    "        \"alpha_3_code\": \"LUX\",\n" +
                    "        \"en_short_name\": \"Luxembourg\",\n" +
                    "        \"nationality\": \"Luxembourg, Luxembourgish\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"446\",\n" +
                    "        \"alpha_2_code\": \"MO\",\n" +
                    "        \"alpha_3_code\": \"MAC\",\n" +
                    "        \"en_short_name\": \"Macao\",\n" +
                    "        \"nationality\": \"Macanese, Chinese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"807\",\n" +
                    "        \"alpha_2_code\": \"MK\",\n" +
                    "        \"alpha_3_code\": \"MKD\",\n" +
                    "        \"en_short_name\": \"Macedonia (the former Yugoslav Republic of)\",\n" +
                    "        \"nationality\": \"Macedonian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"450\",\n" +
                    "        \"alpha_2_code\": \"MG\",\n" +
                    "        \"alpha_3_code\": \"MDG\",\n" +
                    "        \"en_short_name\": \"Madagascar\",\n" +
                    "        \"nationality\": \"Malagasy\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"454\",\n" +
                    "        \"alpha_2_code\": \"MW\",\n" +
                    "        \"alpha_3_code\": \"MWI\",\n" +
                    "        \"en_short_name\": \"Malawi\",\n" +
                    "        \"nationality\": \"Malawian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"458\",\n" +
                    "        \"alpha_2_code\": \"MY\",\n" +
                    "        \"alpha_3_code\": \"MYS\",\n" +
                    "        \"en_short_name\": \"Malaysia\",\n" +
                    "        \"nationality\": \"Malaysian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"462\",\n" +
                    "        \"alpha_2_code\": \"MV\",\n" +
                    "        \"alpha_3_code\": \"MDV\",\n" +
                    "        \"en_short_name\": \"Maldives\",\n" +
                    "        \"nationality\": \"Maldivian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"466\",\n" +
                    "        \"alpha_2_code\": \"ML\",\n" +
                    "        \"alpha_3_code\": \"MLI\",\n" +
                    "        \"en_short_name\": \"Mali\",\n" +
                    "        \"nationality\": \"Malian, Malinese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"470\",\n" +
                    "        \"alpha_2_code\": \"MT\",\n" +
                    "        \"alpha_3_code\": \"MLT\",\n" +
                    "        \"en_short_name\": \"Malta\",\n" +
                    "        \"nationality\": \"Maltese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"584\",\n" +
                    "        \"alpha_2_code\": \"MH\",\n" +
                    "        \"alpha_3_code\": \"MHL\",\n" +
                    "        \"en_short_name\": \"Marshall Islands\",\n" +
                    "        \"nationality\": \"Marshallese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"474\",\n" +
                    "        \"alpha_2_code\": \"MQ\",\n" +
                    "        \"alpha_3_code\": \"MTQ\",\n" +
                    "        \"en_short_name\": \"Martinique\",\n" +
                    "        \"nationality\": \"Martiniquais, Martinican\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"478\",\n" +
                    "        \"alpha_2_code\": \"MR\",\n" +
                    "        \"alpha_3_code\": \"MRT\",\n" +
                    "        \"en_short_name\": \"Mauritania\",\n" +
                    "        \"nationality\": \"Mauritanian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"480\",\n" +
                    "        \"alpha_2_code\": \"MU\",\n" +
                    "        \"alpha_3_code\": \"MUS\",\n" +
                    "        \"en_short_name\": \"Mauritius\",\n" +
                    "        \"nationality\": \"Mauritian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"175\",\n" +
                    "        \"alpha_2_code\": \"YT\",\n" +
                    "        \"alpha_3_code\": \"MYT\",\n" +
                    "        \"en_short_name\": \"Mayotte\",\n" +
                    "        \"nationality\": \"Mahoran\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"484\",\n" +
                    "        \"alpha_2_code\": \"MX\",\n" +
                    "        \"alpha_3_code\": \"MEX\",\n" +
                    "        \"en_short_name\": \"Mexico\",\n" +
                    "        \"nationality\": \"Mexican\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"583\",\n" +
                    "        \"alpha_2_code\": \"FM\",\n" +
                    "        \"alpha_3_code\": \"FSM\",\n" +
                    "        \"en_short_name\": \"Micronesia (Federated States of)\",\n" +
                    "        \"nationality\": \"Micronesian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"498\",\n" +
                    "        \"alpha_2_code\": \"MD\",\n" +
                    "        \"alpha_3_code\": \"MDA\",\n" +
                    "        \"en_short_name\": \"Moldova (Republic of)\",\n" +
                    "        \"nationality\": \"Moldovan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"492\",\n" +
                    "        \"alpha_2_code\": \"MC\",\n" +
                    "        \"alpha_3_code\": \"MCO\",\n" +
                    "        \"en_short_name\": \"Monaco\",\n" +
                    "        \"nationality\": \"Mon\\u00e9gasque, Monacan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"496\",\n" +
                    "        \"alpha_2_code\": \"MN\",\n" +
                    "        \"alpha_3_code\": \"MNG\",\n" +
                    "        \"en_short_name\": \"Mongolia\",\n" +
                    "        \"nationality\": \"Mongolian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"499\",\n" +
                    "        \"alpha_2_code\": \"ME\",\n" +
                    "        \"alpha_3_code\": \"MNE\",\n" +
                    "        \"en_short_name\": \"Montenegro\",\n" +
                    "        \"nationality\": \"Montenegrin\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"500\",\n" +
                    "        \"alpha_2_code\": \"MS\",\n" +
                    "        \"alpha_3_code\": \"MSR\",\n" +
                    "        \"en_short_name\": \"Montserrat\",\n" +
                    "        \"nationality\": \"Montserratian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"504\",\n" +
                    "        \"alpha_2_code\": \"MA\",\n" +
                    "        \"alpha_3_code\": \"MAR\",\n" +
                    "        \"en_short_name\": \"Morocco\",\n" +
                    "        \"nationality\": \"Moroccan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"508\",\n" +
                    "        \"alpha_2_code\": \"MZ\",\n" +
                    "        \"alpha_3_code\": \"MOZ\",\n" +
                    "        \"en_short_name\": \"Mozambique\",\n" +
                    "        \"nationality\": \"Mozambican\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"104\",\n" +
                    "        \"alpha_2_code\": \"MM\",\n" +
                    "        \"alpha_3_code\": \"MMR\",\n" +
                    "        \"en_short_name\": \"Myanmar\",\n" +
                    "        \"nationality\": \"Burmese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"516\",\n" +
                    "        \"alpha_2_code\": \"NA\",\n" +
                    "        \"alpha_3_code\": \"NAM\",\n" +
                    "        \"en_short_name\": \"Namibia\",\n" +
                    "        \"nationality\": \"Namibian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"520\",\n" +
                    "        \"alpha_2_code\": \"NR\",\n" +
                    "        \"alpha_3_code\": \"NRU\",\n" +
                    "        \"en_short_name\": \"Nauru\",\n" +
                    "        \"nationality\": \"Nauruan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"524\",\n" +
                    "        \"alpha_2_code\": \"NP\",\n" +
                    "        \"alpha_3_code\": \"NPL\",\n" +
                    "        \"en_short_name\": \"Nepal\",\n" +
                    "        \"nationality\": \"Nepali, Nepalese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"528\",\n" +
                    "        \"alpha_2_code\": \"NL\",\n" +
                    "        \"alpha_3_code\": \"NLD\",\n" +
                    "        \"en_short_name\": \"Netherlands\",\n" +
                    "        \"nationality\": \"Dutch, Netherlandic\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"540\",\n" +
                    "        \"alpha_2_code\": \"NC\",\n" +
                    "        \"alpha_3_code\": \"NCL\",\n" +
                    "        \"en_short_name\": \"New Caledonia\",\n" +
                    "        \"nationality\": \"New Caledonian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"554\",\n" +
                    "        \"alpha_2_code\": \"NZ\",\n" +
                    "        \"alpha_3_code\": \"NZL\",\n" +
                    "        \"en_short_name\": \"New Zealand\",\n" +
                    "        \"nationality\": \"New Zealand, NZ\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"558\",\n" +
                    "        \"alpha_2_code\": \"NI\",\n" +
                    "        \"alpha_3_code\": \"NIC\",\n" +
                    "        \"en_short_name\": \"Nicaragua\",\n" +
                    "        \"nationality\": \"Nicaraguan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"562\",\n" +
                    "        \"alpha_2_code\": \"NE\",\n" +
                    "        \"alpha_3_code\": \"NER\",\n" +
                    "        \"en_short_name\": \"Niger\",\n" +
                    "        \"nationality\": \"Nigerien\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"566\",\n" +
                    "        \"alpha_2_code\": \"NG\",\n" +
                    "        \"alpha_3_code\": \"NGA\",\n" +
                    "        \"en_short_name\": \"Nigeria\",\n" +
                    "        \"nationality\": \"Nigerian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"570\",\n" +
                    "        \"alpha_2_code\": \"NU\",\n" +
                    "        \"alpha_3_code\": \"NIU\",\n" +
                    "        \"en_short_name\": \"Niue\",\n" +
                    "        \"nationality\": \"Niuean\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"574\",\n" +
                    "        \"alpha_2_code\": \"NF\",\n" +
                    "        \"alpha_3_code\": \"NFK\",\n" +
                    "        \"en_short_name\": \"Norfolk Island\",\n" +
                    "        \"nationality\": \"Norfolk Island\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"580\",\n" +
                    "        \"alpha_2_code\": \"MP\",\n" +
                    "        \"alpha_3_code\": \"MNP\",\n" +
                    "        \"en_short_name\": \"Northern Mariana Islands\",\n" +
                    "        \"nationality\": \"Northern Marianan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"578\",\n" +
                    "        \"alpha_2_code\": \"NO\",\n" +
                    "        \"alpha_3_code\": \"NOR\",\n" +
                    "        \"en_short_name\": \"Norway\",\n" +
                    "        \"nationality\": \"Norwegian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"512\",\n" +
                    "        \"alpha_2_code\": \"OM\",\n" +
                    "        \"alpha_3_code\": \"OMN\",\n" +
                    "        \"en_short_name\": \"Oman\",\n" +
                    "        \"nationality\": \"Omani\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"586\",\n" +
                    "        \"alpha_2_code\": \"PK\",\n" +
                    "        \"alpha_3_code\": \"PAK\",\n" +
                    "        \"en_short_name\": \"Pakistan\",\n" +
                    "        \"nationality\": \"Pakistani\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"585\",\n" +
                    "        \"alpha_2_code\": \"PW\",\n" +
                    "        \"alpha_3_code\": \"PLW\",\n" +
                    "        \"en_short_name\": \"Palau\",\n" +
                    "        \"nationality\": \"Palauan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"275\",\n" +
                    "        \"alpha_2_code\": \"PS\",\n" +
                    "        \"alpha_3_code\": \"PSE\",\n" +
                    "        \"en_short_name\": \"Palestine, State of\",\n" +
                    "        \"nationality\": \"Palestinian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"591\",\n" +
                    "        \"alpha_2_code\": \"PA\",\n" +
                    "        \"alpha_3_code\": \"PAN\",\n" +
                    "        \"en_short_name\": \"Panama\",\n" +
                    "        \"nationality\": \"Panamanian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"598\",\n" +
                    "        \"alpha_2_code\": \"PG\",\n" +
                    "        \"alpha_3_code\": \"PNG\",\n" +
                    "        \"en_short_name\": \"Papua New Guinea\",\n" +
                    "        \"nationality\": \"Papua New Guinean, Papuan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"600\",\n" +
                    "        \"alpha_2_code\": \"PY\",\n" +
                    "        \"alpha_3_code\": \"PRY\",\n" +
                    "        \"en_short_name\": \"Paraguay\",\n" +
                    "        \"nationality\": \"Paraguayan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"604\",\n" +
                    "        \"alpha_2_code\": \"PE\",\n" +
                    "        \"alpha_3_code\": \"PER\",\n" +
                    "        \"en_short_name\": \"Peru\",\n" +
                    "        \"nationality\": \"Peruvian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"608\",\n" +
                    "        \"alpha_2_code\": \"PH\",\n" +
                    "        \"alpha_3_code\": \"PHL\",\n" +
                    "        \"en_short_name\": \"Philippines\",\n" +
                    "        \"nationality\": \"Philippine, Filipino\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"612\",\n" +
                    "        \"alpha_2_code\": \"PN\",\n" +
                    "        \"alpha_3_code\": \"PCN\",\n" +
                    "        \"en_short_name\": \"Pitcairn\",\n" +
                    "        \"nationality\": \"Pitcairn Island\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"616\",\n" +
                    "        \"alpha_2_code\": \"PL\",\n" +
                    "        \"alpha_3_code\": \"POL\",\n" +
                    "        \"en_short_name\": \"Poland\",\n" +
                    "        \"nationality\": \"Polish\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"620\",\n" +
                    "        \"alpha_2_code\": \"PT\",\n" +
                    "        \"alpha_3_code\": \"PRT\",\n" +
                    "        \"en_short_name\": \"Portugal\",\n" +
                    "        \"nationality\": \"Portuguese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"630\",\n" +
                    "        \"alpha_2_code\": \"PR\",\n" +
                    "        \"alpha_3_code\": \"PRI\",\n" +
                    "        \"en_short_name\": \"Puerto Rico\",\n" +
                    "        \"nationality\": \"Puerto Rican\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"634\",\n" +
                    "        \"alpha_2_code\": \"QA\",\n" +
                    "        \"alpha_3_code\": \"QAT\",\n" +
                    "        \"en_short_name\": \"Qatar\",\n" +
                    "        \"nationality\": \"Qatari\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"638\",\n" +
                    "        \"alpha_2_code\": \"RE\",\n" +
                    "        \"alpha_3_code\": \"REU\",\n" +
                    "        \"en_short_name\": \"R\\u00e9union\",\n" +
                    "        \"nationality\": \"R\\u00e9unionese, R\\u00e9unionnais\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"642\",\n" +
                    "        \"alpha_2_code\": \"RO\",\n" +
                    "        \"alpha_3_code\": \"ROU\",\n" +
                    "        \"en_short_name\": \"Romania\",\n" +
                    "        \"nationality\": \"Romanian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"643\",\n" +
                    "        \"alpha_2_code\": \"RU\",\n" +
                    "        \"alpha_3_code\": \"RUS\",\n" +
                    "        \"en_short_name\": \"Russian Federation\",\n" +
                    "        \"nationality\": \"Russian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"646\",\n" +
                    "        \"alpha_2_code\": \"RW\",\n" +
                    "        \"alpha_3_code\": \"RWA\",\n" +
                    "        \"en_short_name\": \"Rwanda\",\n" +
                    "        \"nationality\": \"Rwandan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"652\",\n" +
                    "        \"alpha_2_code\": \"BL\",\n" +
                    "        \"alpha_3_code\": \"BLM\",\n" +
                    "        \"en_short_name\": \"Saint Barth\\u00e9lemy\",\n" +
                    "        \"nationality\": \"Barth\\u00e9lemois\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"654\",\n" +
                    "        \"alpha_2_code\": \"SH\",\n" +
                    "        \"alpha_3_code\": \"SHN\",\n" +
                    "        \"en_short_name\": \"Saint Helena, Ascension and Tristan da Cunha\",\n" +
                    "        \"nationality\": \"Saint Helenian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"659\",\n" +
                    "        \"alpha_2_code\": \"KN\",\n" +
                    "        \"alpha_3_code\": \"KNA\",\n" +
                    "        \"en_short_name\": \"Saint Kitts and Nevis\",\n" +
                    "        \"nationality\": \"Kittitian or Nevisian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"662\",\n" +
                    "        \"alpha_2_code\": \"LC\",\n" +
                    "        \"alpha_3_code\": \"LCA\",\n" +
                    "        \"en_short_name\": \"Saint Lucia\",\n" +
                    "        \"nationality\": \"Saint Lucian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"663\",\n" +
                    "        \"alpha_2_code\": \"MF\",\n" +
                    "        \"alpha_3_code\": \"MAF\",\n" +
                    "        \"en_short_name\": \"Saint Martin (French part)\",\n" +
                    "        \"nationality\": \"Saint-Martinoise\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"666\",\n" +
                    "        \"alpha_2_code\": \"PM\",\n" +
                    "        \"alpha_3_code\": \"SPM\",\n" +
                    "        \"en_short_name\": \"Saint Pierre and Miquelon\",\n" +
                    "        \"nationality\": \"Saint-Pierrais or Miquelonnais\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"670\",\n" +
                    "        \"alpha_2_code\": \"VC\",\n" +
                    "        \"alpha_3_code\": \"VCT\",\n" +
                    "        \"en_short_name\": \"Saint Vincent and the Grenadines\",\n" +
                    "        \"nationality\": \"Saint Vincentian, Vincentian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"882\",\n" +
                    "        \"alpha_2_code\": \"WS\",\n" +
                    "        \"alpha_3_code\": \"WSM\",\n" +
                    "        \"en_short_name\": \"Samoa\",\n" +
                    "        \"nationality\": \"Samoan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"674\",\n" +
                    "        \"alpha_2_code\": \"SM\",\n" +
                    "        \"alpha_3_code\": \"SMR\",\n" +
                    "        \"en_short_name\": \"San Marino\",\n" +
                    "        \"nationality\": \"Sammarinese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"678\",\n" +
                    "        \"alpha_2_code\": \"ST\",\n" +
                    "        \"alpha_3_code\": \"STP\",\n" +
                    "        \"en_short_name\": \"Sao Tome and Principe\",\n" +
                    "        \"nationality\": \"S\\u00e3o Tom\\u00e9an\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"682\",\n" +
                    "        \"alpha_2_code\": \"SA\",\n" +
                    "        \"alpha_3_code\": \"SAU\",\n" +
                    "        \"en_short_name\": \"Saudi Arabia\",\n" +
                    "        \"nationality\": \"Saudi, Saudi Arabian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"686\",\n" +
                    "        \"alpha_2_code\": \"SN\",\n" +
                    "        \"alpha_3_code\": \"SEN\",\n" +
                    "        \"en_short_name\": \"Senegal\",\n" +
                    "        \"nationality\": \"Senegalese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"688\",\n" +
                    "        \"alpha_2_code\": \"RS\",\n" +
                    "        \"alpha_3_code\": \"SRB\",\n" +
                    "        \"en_short_name\": \"Serbia\",\n" +
                    "        \"nationality\": \"Serbian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"690\",\n" +
                    "        \"alpha_2_code\": \"SC\",\n" +
                    "        \"alpha_3_code\": \"SYC\",\n" +
                    "        \"en_short_name\": \"Seychelles\",\n" +
                    "        \"nationality\": \"Seychellois\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"694\",\n" +
                    "        \"alpha_2_code\": \"SL\",\n" +
                    "        \"alpha_3_code\": \"SLE\",\n" +
                    "        \"en_short_name\": \"Sierra Leone\",\n" +
                    "        \"nationality\": \"Sierra Leonean\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"702\",\n" +
                    "        \"alpha_2_code\": \"SG\",\n" +
                    "        \"alpha_3_code\": \"SGP\",\n" +
                    "        \"en_short_name\": \"Singapore\",\n" +
                    "        \"nationality\": \"Singaporean\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"534\",\n" +
                    "        \"alpha_2_code\": \"SX\",\n" +
                    "        \"alpha_3_code\": \"SXM\",\n" +
                    "        \"en_short_name\": \"Sint Maarten (Dutch part)\",\n" +
                    "        \"nationality\": \"Sint Maarten\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"703\",\n" +
                    "        \"alpha_2_code\": \"SK\",\n" +
                    "        \"alpha_3_code\": \"SVK\",\n" +
                    "        \"en_short_name\": \"Slovakia\",\n" +
                    "        \"nationality\": \"Slovak\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"705\",\n" +
                    "        \"alpha_2_code\": \"SI\",\n" +
                    "        \"alpha_3_code\": \"SVN\",\n" +
                    "        \"en_short_name\": \"Slovenia\",\n" +
                    "        \"nationality\": \"Slovenian, Slovene\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"90\",\n" +
                    "        \"alpha_2_code\": \"SB\",\n" +
                    "        \"alpha_3_code\": \"SLB\",\n" +
                    "        \"en_short_name\": \"Solomon Islands\",\n" +
                    "        \"nationality\": \"Solomon Island\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"706\",\n" +
                    "        \"alpha_2_code\": \"SO\",\n" +
                    "        \"alpha_3_code\": \"SOM\",\n" +
                    "        \"en_short_name\": \"Somalia\",\n" +
                    "        \"nationality\": \"Somali, Somalian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"710\",\n" +
                    "        \"alpha_2_code\": \"ZA\",\n" +
                    "        \"alpha_3_code\": \"ZAF\",\n" +
                    "        \"en_short_name\": \"South Africa\",\n" +
                    "        \"nationality\": \"South African\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"239\",\n" +
                    "        \"alpha_2_code\": \"GS\",\n" +
                    "        \"alpha_3_code\": \"SGS\",\n" +
                    "        \"en_short_name\": \"South Georgia and the South Sandwich Islands\",\n" +
                    "        \"nationality\": \"South Georgia or South Sandwich Islands\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"728\",\n" +
                    "        \"alpha_2_code\": \"SS\",\n" +
                    "        \"alpha_3_code\": \"SSD\",\n" +
                    "        \"en_short_name\": \"South Sudan\",\n" +
                    "        \"nationality\": \"South Sudanese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"724\",\n" +
                    "        \"alpha_2_code\": \"ES\",\n" +
                    "        \"alpha_3_code\": \"ESP\",\n" +
                    "        \"en_short_name\": \"Spain\",\n" +
                    "        \"nationality\": \"Spanish\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"144\",\n" +
                    "        \"alpha_2_code\": \"LK\",\n" +
                    "        \"alpha_3_code\": \"LKA\",\n" +
                    "        \"en_short_name\": \"Sri Lanka\",\n" +
                    "        \"nationality\": \"Sri Lankan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"729\",\n" +
                    "        \"alpha_2_code\": \"SD\",\n" +
                    "        \"alpha_3_code\": \"SDN\",\n" +
                    "        \"en_short_name\": \"Sudan\",\n" +
                    "        \"nationality\": \"Sudanese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"740\",\n" +
                    "        \"alpha_2_code\": \"SR\",\n" +
                    "        \"alpha_3_code\": \"SUR\",\n" +
                    "        \"en_short_name\": \"Suriname\",\n" +
                    "        \"nationality\": \"Surinamese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"744\",\n" +
                    "        \"alpha_2_code\": \"SJ\",\n" +
                    "        \"alpha_3_code\": \"SJM\",\n" +
                    "        \"en_short_name\": \"Svalbard and Jan Mayen\",\n" +
                    "        \"nationality\": \"Svalbard\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"748\",\n" +
                    "        \"alpha_2_code\": \"SZ\",\n" +
                    "        \"alpha_3_code\": \"SWZ\",\n" +
                    "        \"en_short_name\": \"Swaziland\",\n" +
                    "        \"nationality\": \"Swazi\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"752\",\n" +
                    "        \"alpha_2_code\": \"SE\",\n" +
                    "        \"alpha_3_code\": \"SWE\",\n" +
                    "        \"en_short_name\": \"Sweden\",\n" +
                    "        \"nationality\": \"Swedish\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"756\",\n" +
                    "        \"alpha_2_code\": \"CH\",\n" +
                    "        \"alpha_3_code\": \"CHE\",\n" +
                    "        \"en_short_name\": \"Switzerland\",\n" +
                    "        \"nationality\": \"Swiss\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"760\",\n" +
                    "        \"alpha_2_code\": \"SY\",\n" +
                    "        \"alpha_3_code\": \"SYR\",\n" +
                    "        \"en_short_name\": \"Syrian Arab Republic\",\n" +
                    "        \"nationality\": \"Syrian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"158\",\n" +
                    "        \"alpha_2_code\": \"TW\",\n" +
                    "        \"alpha_3_code\": \"TWN\",\n" +
                    "        \"en_short_name\": \"Taiwan, Province of China\",\n" +
                    "        \"nationality\": \"Chinese, Taiwanese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"762\",\n" +
                    "        \"alpha_2_code\": \"TJ\",\n" +
                    "        \"alpha_3_code\": \"TJK\",\n" +
                    "        \"en_short_name\": \"Tajikistan\",\n" +
                    "        \"nationality\": \"Tajikistani\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"834\",\n" +
                    "        \"alpha_2_code\": \"TZ\",\n" +
                    "        \"alpha_3_code\": \"TZA\",\n" +
                    "        \"en_short_name\": \"Tanzania, United Republic of\",\n" +
                    "        \"nationality\": \"Tanzanian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"764\",\n" +
                    "        \"alpha_2_code\": \"TH\",\n" +
                    "        \"alpha_3_code\": \"THA\",\n" +
                    "        \"en_short_name\": \"Thailand\",\n" +
                    "        \"nationality\": \"Thai\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"626\",\n" +
                    "        \"alpha_2_code\": \"TL\",\n" +
                    "        \"alpha_3_code\": \"TLS\",\n" +
                    "        \"en_short_name\": \"Timor-Leste\",\n" +
                    "        \"nationality\": \"Timorese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"768\",\n" +
                    "        \"alpha_2_code\": \"TG\",\n" +
                    "        \"alpha_3_code\": \"TGO\",\n" +
                    "        \"en_short_name\": \"Togo\",\n" +
                    "        \"nationality\": \"Togolese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"772\",\n" +
                    "        \"alpha_2_code\": \"TK\",\n" +
                    "        \"alpha_3_code\": \"TKL\",\n" +
                    "        \"en_short_name\": \"Tokelau\",\n" +
                    "        \"nationality\": \"Tokelauan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"776\",\n" +
                    "        \"alpha_2_code\": \"TO\",\n" +
                    "        \"alpha_3_code\": \"TON\",\n" +
                    "        \"en_short_name\": \"Tonga\",\n" +
                    "        \"nationality\": \"Tongan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"780\",\n" +
                    "        \"alpha_2_code\": \"TT\",\n" +
                    "        \"alpha_3_code\": \"TTO\",\n" +
                    "        \"en_short_name\": \"Trinidad and Tobago\",\n" +
                    "        \"nationality\": \"Trinidadian or Tobagonian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"788\",\n" +
                    "        \"alpha_2_code\": \"TN\",\n" +
                    "        \"alpha_3_code\": \"TUN\",\n" +
                    "        \"en_short_name\": \"Tunisia\",\n" +
                    "        \"nationality\": \"Tunisian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"792\",\n" +
                    "        \"alpha_2_code\": \"TR\",\n" +
                    "        \"alpha_3_code\": \"TUR\",\n" +
                    "        \"en_short_name\": \"Turkey\",\n" +
                    "        \"nationality\": \"Turkish\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"795\",\n" +
                    "        \"alpha_2_code\": \"TM\",\n" +
                    "        \"alpha_3_code\": \"TKM\",\n" +
                    "        \"en_short_name\": \"Turkmenistan\",\n" +
                    "        \"nationality\": \"Turkmen\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"796\",\n" +
                    "        \"alpha_2_code\": \"TC\",\n" +
                    "        \"alpha_3_code\": \"TCA\",\n" +
                    "        \"en_short_name\": \"Turks and Caicos Islands\",\n" +
                    "        \"nationality\": \"Turks and Caicos Island\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"798\",\n" +
                    "        \"alpha_2_code\": \"TV\",\n" +
                    "        \"alpha_3_code\": \"TUV\",\n" +
                    "        \"en_short_name\": \"Tuvalu\",\n" +
                    "        \"nationality\": \"Tuvaluan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"800\",\n" +
                    "        \"alpha_2_code\": \"UG\",\n" +
                    "        \"alpha_3_code\": \"UGA\",\n" +
                    "        \"en_short_name\": \"Uganda\",\n" +
                    "        \"nationality\": \"Ugandan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"804\",\n" +
                    "        \"alpha_2_code\": \"UA\",\n" +
                    "        \"alpha_3_code\": \"UKR\",\n" +
                    "        \"en_short_name\": \"Ukraine\",\n" +
                    "        \"nationality\": \"Ukrainian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"784\",\n" +
                    "        \"alpha_2_code\": \"AE\",\n" +
                    "        \"alpha_3_code\": \"ARE\",\n" +
                    "        \"en_short_name\": \"United Arab Emirates\",\n" +
                    "        \"nationality\": \"Emirati, Emirian, Emiri\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"826\",\n" +
                    "        \"alpha_2_code\": \"GB\",\n" +
                    "        \"alpha_3_code\": \"GBR\",\n" +
                    "        \"en_short_name\": \"United Kingdom of Great Britain and Northern Ireland\",\n" +
                    "        \"nationality\": \"British, UK\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"581\",\n" +
                    "        \"alpha_2_code\": \"UM\",\n" +
                    "        \"alpha_3_code\": \"UMI\",\n" +
                    "        \"en_short_name\": \"United States Minor Outlying Islands\",\n" +
                    "        \"nationality\": \"American\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"840\",\n" +
                    "        \"alpha_2_code\": \"US\",\n" +
                    "        \"alpha_3_code\": \"USA\",\n" +
                    "        \"en_short_name\": \"United States of America\",\n" +
                    "        \"nationality\": \"American\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"858\",\n" +
                    "        \"alpha_2_code\": \"UY\",\n" +
                    "        \"alpha_3_code\": \"URY\",\n" +
                    "        \"en_short_name\": \"Uruguay\",\n" +
                    "        \"nationality\": \"Uruguayan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"860\",\n" +
                    "        \"alpha_2_code\": \"UZ\",\n" +
                    "        \"alpha_3_code\": \"UZB\",\n" +
                    "        \"en_short_name\": \"Uzbekistan\",\n" +
                    "        \"nationality\": \"Uzbekistani, Uzbek\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"548\",\n" +
                    "        \"alpha_2_code\": \"VU\",\n" +
                    "        \"alpha_3_code\": \"VUT\",\n" +
                    "        \"en_short_name\": \"Vanuatu\",\n" +
                    "        \"nationality\": \"Ni-Vanuatu, Vanuatuan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"862\",\n" +
                    "        \"alpha_2_code\": \"VE\",\n" +
                    "        \"alpha_3_code\": \"VEN\",\n" +
                    "        \"en_short_name\": \"Venezuela (Bolivarian Republic of)\",\n" +
                    "        \"nationality\": \"Venezuelan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"704\",\n" +
                    "        \"alpha_2_code\": \"VN\",\n" +
                    "        \"alpha_3_code\": \"VNM\",\n" +
                    "        \"en_short_name\": \"Vietnam\",\n" +
                    "        \"nationality\": \"Vietnamese\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"92\",\n" +
                    "        \"alpha_2_code\": \"VG\",\n" +
                    "        \"alpha_3_code\": \"VGB\",\n" +
                    "        \"en_short_name\": \"Virgin Islands (British)\",\n" +
                    "        \"nationality\": \"British Virgin Island\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"850\",\n" +
                    "        \"alpha_2_code\": \"VI\",\n" +
                    "        \"alpha_3_code\": \"VIR\",\n" +
                    "        \"en_short_name\": \"Virgin Islands (U.S.)\",\n" +
                    "        \"nationality\": \"U.S. Virgin Island\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"876\",\n" +
                    "        \"alpha_2_code\": \"WF\",\n" +
                    "        \"alpha_3_code\": \"WLF\",\n" +
                    "        \"en_short_name\": \"Wallis and Futuna\",\n" +
                    "        \"nationality\": \"Wallis and Futuna, Wallisian or Futunan\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"732\",\n" +
                    "        \"alpha_2_code\": \"EH\",\n" +
                    "        \"alpha_3_code\": \"ESH\",\n" +
                    "        \"en_short_name\": \"Western Sahara\",\n" +
                    "        \"nationality\": \"Sahrawi, Sahrawian, Sahraouian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"887\",\n" +
                    "        \"alpha_2_code\": \"YE\",\n" +
                    "        \"alpha_3_code\": \"YEM\",\n" +
                    "        \"en_short_name\": \"Yemen\",\n" +
                    "        \"nationality\": \"Yemeni\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"894\",\n" +
                    "        \"alpha_2_code\": \"ZM\",\n" +
                    "        \"alpha_3_code\": \"ZMB\",\n" +
                    "        \"en_short_name\": \"Zambia\",\n" +
                    "        \"nationality\": \"Zambian\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"num_code\": \"716\",\n" +
                    "        \"alpha_2_code\": \"ZW\",\n" +
                    "        \"alpha_3_code\": \"ZWE\",\n" +
                    "        \"en_short_name\": \"Zimbabwe\",\n" +
                    "        \"nationality\": \"Zimbabwean\"\n" +
                    "    }\n" +
                    "]");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsobObject = jsonArray.getJSONObject(i);
                nationalityList.add(jsobObject.getString("nationality"));
                code3List.add(jsobObject.getString("alpha_3_code"));
                code2List.add(jsobObject.getString("alpha_2_code"));
            }
        }
        catch (Exception e)
        {

        }
        imageArrow = rootView.findViewById(R.id.image_arrow);
        imageArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isShowDetail)
                {
                    isShowDetail = false;
                    triggerHiddenLayout();
                }
                else {
                    isShowDetail = true;
                    triggerHiddenLayout();
                }
            }
        });

        buttonAutomaticVerification = rootView.findViewById(R.id.button_automatic_verification);
        buttonAutomaticVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
                Intent intent = new Intent(activity, CameraActivity.class);
                Log.e("Token", preferences.getString(getString(R.string.param_oneid_token), ""));
                intent.putExtra("token", preferences.getString(getString(R.string.param_oneid_token), ""));
                intent.putExtra("apikey", AppDelegate.apikey);
                startActivityForResult(intent, LIVENESS_REQUEST_CODE);
            }
        });
        buttonCreateIdentity = rootView.findViewById(R.id.button_create_identity);
        buttonCreateIdentity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputGender.setError(null);
                inputDob.setError(null);
                inputNationality.setError(null);
                inputIdType.setError(null);
                inputIdNo.setError(null);
                if(inputGender.getText().toString().equals(""))
                    inputGender.setError(getString(R.string.error_required_field));
                if(inputDob.getText().toString().equals(""))
                    inputDob.setError(getString(R.string.error_required_field));
                if(inputNationality.getText().toString().equals(""))
                    inputNationality.setError(getString(R.string.error_required_field));
                if(inputIdType.getText().toString().equals(""))
                    inputIdType.setError(getString(R.string.error_required_field));
                if(inputIdNo.getText().toString().equals(""))
                    inputIdNo.setError(getString(R.string.error_required_field));

                SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
                if(preferences.getString(getResources().getString(R.string.param_oneid_selfie), "").equals(""))
                {
                    alert(getString(R.string.error), getString(R.string.error_missing_automatic_verification));
                }
                else if(preferences.getString(getResources().getString(R.string.param_oneid_id_image), "").equals(""))
                {
                    alert(getString(R.string.error), getString(R.string.error_missing_identification_image));
                }

                if(!inputGender.getText().toString().equals("") && !inputDob.getText().toString().equals("") &&
                        !inputNationality.getText().toString().equals("") && !inputIdType.getText().toString().equals("") &&
                        !inputIdNo.getText().toString().equals("") &&
                        !preferences.getString(getString(R.string.param_oneid_selfie), "").equals("") &&
                        !preferences.getString(getString(R.string.param_oneid_id_image), "").equals(""))
                {
                    createOneIdentity();
                }
            }
        });
        buttonVerificationResult = rootView.findViewById(R.id.button_verification_result);
        buttonVerificationResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, VerificationResultActivity.class);
                startActivity(intent);
            }
        });
        layoutHidden1 = rootView.findViewById(R.id.layout_hidden_1);
        layoutHidden2 = rootView.findViewById(R.id.layout_hidden_2);
        layoutHidden3 = rootView.findViewById(R.id.layout_hidden_3);
        layoutHidden2.setVisibility(View.GONE);
        layoutHidden3.setVisibility(View.GONE);

        SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        inputFirstName.setText(preferences.getString(getString(R.string.param_first_name), ""));
        inputLastName.setText(preferences.getString(getString(R.string.param_last_name), ""));
        inputEmail.setText(preferences.getString(getString(R.string.param_email), ""));
        inputContact.setText(preferences.getString(getString(R.string.param_contact), ""));
        inputGender.setText(preferences.getString(getString(R.string.param_oneid_gender), ""));
        inputDob.setText(preferences.getString(getString(R.string.param_oneid_dob), ""));
        inputNationality.setText(preferences.getString(getString(R.string.param_oneid_nationality), ""));
        TelephonyManager tm = (TelephonyManager)activity.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCodeValue = tm.getNetworkCountryIso().toUpperCase();
        Log.e("Locale", countryCodeValue + "");
        SharedPreferences.Editor editor = preferences.edit();
        if(preferences.getString(getString(R.string.param_oneid_nationality), "").equals(""))
        {
            int index = code2List.indexOf(countryCodeValue);
            if(index != -1) {
                editor.putString(getString(R.string.param_oneid_nationality), nationalityList.get(index));
                editor.putString(getString(R.string.param_oneid_nationality_id), code3List.get(index));
                editor.apply();
                inputNationality.setText(preferences.getString(getString(R.string.param_oneid_nationality), ""));
            }
        }
        inputIdType.setText(preferences.getString(getString(R.string.param_oneid_id_type_name), "passport"));
        inputIdNo.setText(preferences.getString(getString(R.string.param_oneid_id_no), ""));

        String selfie = preferences.getString(getString(R.string.param_oneid_selfie), "");
        if(!selfie.equals("")) {
            viewUpload(selfie, "selfie");
        }
        triggerHiddenLayout();
        String credential = preferences.getString(getString(R.string.param_oneid_credential), "");
        if(credential.equals(""))
        {
            buttonVerificationResult.setVisibility(View.GONE);
        }
        else
        {
            buttonVerificationResult.setVisibility(View.VISIBLE);
        }
        return rootView;
    }

    public void triggerHiddenLayout()
    {
        SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        String idImage = preferences.getString(getString(R.string.param_oneid_id_image), "");
        String credential = preferences.getString(getString(R.string.param_oneid_credential), "");
        if(isShowDetail)
        {
            if(!idImage.equals(""))
            {
                viewUpload(idImage, "id");
            }
            if(credential.equals(""))
            {
                layoutHidden1.setVisibility(View.VISIBLE);
                layoutHidden2.setVisibility(View.VISIBLE);
                layoutHidden3.setVisibility(View.VISIBLE);
                buttonVerificationResult.setVisibility(View.GONE);
            }
            else
            {
                layoutHidden1.setVisibility(View.GONE);
                layoutHidden2.setVisibility(View.VISIBLE);
                layoutHidden3.setVisibility(View.VISIBLE);
                buttonVerificationResult.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            layoutHidden1.setVisibility(View.GONE);
            layoutHidden2.setVisibility(View.GONE);
            layoutHidden3.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void viewUpload(String documentName, final String type)
    {
        final SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
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
        AsynRestClient.genericPost(activity, AsynRestClient.viewUploadUrl, jsonObject.toString(), new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                if(!type.equals("id") && !type.equals("address"))
                    progressDialog.show();
            }

            @Override
            public void onFinish() {
                if(!type.equals("id") && !type.equals("address"))
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
                    String imageBase64 = jsonObject.getString("data");
                    byte[] imageByteArray = Base64.decode(imageBase64, Base64.DEFAULT);
                    if(type.equals("selfie")) {
                        Glide.with(activity).load(imageByteArray).apply(RequestOptions.circleCropTransform()).into(imageProfile);

                        if(selfieUploadStatus) {
                            selfieUploadStatus = false;

                            try {
                                File f = new File(Environment.getExternalStorageDirectory().toString() + AppDelegate.appPath + "/" + preferences.getString(getString(R.string.param_utility_user_id), "") + ".jpg");
                                f.createNewFile();
                                FileOutputStream fo = new FileOutputStream(f);
                                fo.write(imageByteArray);
                                fo.flush();
                                fo.close();
                                utilityUpload(Environment.getExternalStorageDirectory().toString() + AppDelegate.appPath + "/" + preferences.getString(getString(R.string.param_utility_user_id), "") + ".jpg", preferences.getString(getString(R.string.param_libra_wallet_address), ""));
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                    else if(type.equals("id")) {
                        Glide.with(activity).load(imageByteArray).into(imageUploadId);
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
                //alert(getString(R.string.error), getString(R.string.error_please_try_again));
            }
        });
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

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        monthOfYear += 1;
        String monthString = monthOfYear < 10 ? "0" + monthOfYear : "" + monthOfYear;
        String dayString = dayOfMonth < 10 ? "0" + dayOfMonth : "" + dayOfMonth;
        String date = year + "-" + monthString + "-" + dayString;
        inputDob.setText(date);

        SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getResources().getString(R.string.param_oneid_dob), inputDob.getText().toString());
        editor.apply();
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

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == LIVENESS_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String returnedResult = data.getData().toString();
                Log.e("Result", returnedResult);
                try {
                    JSONObject json = new JSONObject(returnedResult);
                    String fileName = json.getString("selfie_image");

                    SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
                    final SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(getResources().getString(R.string.param_oneid_selfie), fileName);
                    editor.apply();
                    selfieUploadStatus = true;
                    viewUpload(fileName, "selfie");
                    activity.refreshProfileImage();
                }
                catch (Exception e) {
                }
            }
        }

        if (requestCode == Config.RC_PICK_IMAGES && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);
            imagePath = images.get(0).getPath();
            imagePath = AppDelegate.getResizedImagePathFromOriginalPath(activity, imagePath, 1000, 1000);
            imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1, imagePath.length());
            Log.e("imagePath", imagePath);
            Log.e("imageName", imageName);

            SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
            if(imageType.equals("id") ) {
                Glide.with(this).load(new File(imagePath)).into(imageUploadId);
                String documentType = preferences.getString(getResources().getString(R.string.param_oneid_id_type), "");
                upload(documentType);

                utilityUpload(imagePath, preferences.getString(getString(R.string.param_libra_wallet_address), "") + "_id");
            }
        }
    }

    public void upload(String documentType)
    {
        SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        String token = preferences.getString(getResources().getString(R.string.param_oneid_token), "");
        Log.e("documentType", documentType);
        AsynRestClient.upload(token, documentType, imagePath, new AsyncHttpResponseHandler()
        {
            @Override
            public void onStart() {
                progressDialog.show();
            }

            @Override
            public void onFinish() {
                progressDialog.dismiss();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response)
            {
                String responseString = new String(response);
                Log.e("Upload response", responseString);
                try {
                    JSONObject jsonObject = new JSONObject(responseString.replace("null", "\"\""));
                    if(jsonObject.has("error"))
                    {
                        JSONObject errorObject = jsonObject.getJSONObject("error");
                        alert(errorObject.getString("type"), errorObject.getString("message"));
                        return;
                    }
                    String status = jsonObject.getString("status");
                    if (status.equals("200"))
                    {
                        String fileName = jsonObject.getString("file_name");
                        SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
                        final SharedPreferences.Editor editor = preferences.edit();
                        if(imageType.equals("id")) {
                            editor.putString(getResources().getString(R.string.param_oneid_id_image), fileName);
                            editor.putString(getResources().getString(R.string.param_oneid_id_no), inputIdNo.getText().toString());
                        }
                        else if(imageType.equals("address"))
                        {
                            editor.putString(getResources().getString(R.string.param_oneid_address_image), fileName);
                        }
                        editor.apply();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                //String responseString = new String(errorResponse);
                //Log.e("Upload response", responseString);
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                int progress = Math.round((bytesWritten/totalSize)*100);
                progressDialog.setProgress(progress);
                progressDialog.setMax(100);
            }
        });
    }

    private void createOneIdentity()
    {
        SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("token", preferences.getString(getString(R.string.param_oneid_token), ""));
            jsonObject.put("job_title", "");
            jsonObject.put("first_name", preferences.getString(getString(R.string.param_first_name), ""));
            jsonObject.put("last_name", preferences.getString(getString(R.string.param_last_name), ""));
            jsonObject.put("email", preferences.getString(getString(R.string.param_email), ""));
            jsonObject.put("gender", preferences.getString(getString(R.string.param_oneid_gender), ""));
            jsonObject.put("dob", preferences.getString(getString(R.string.param_oneid_dob), ""));
            jsonObject.put("nationality", preferences.getString(getString(R.string.param_oneid_nationality_id), ""));
            jsonObject.put("id_no", preferences.getString(getString(R.string.param_oneid_id_no), ""));
            jsonObject.put("id_image", preferences.getString(getString(R.string.param_oneid_id_image), ""));
            jsonObject.put("proof_address_image", preferences.getString(getString(R.string.param_oneid_address_image), ""));
            jsonObject.put("selfie_image", preferences.getString(getString(R.string.param_oneid_selfie), ""));
            jsonObject.put("company_name", "");
            jsonObject.put("doc_type", preferences.getString(getString(R.string.param_oneid_id_type), ""));
            jsonObject.put("bypass_address", "1");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.e("Json", jsonObject.toString());
        AsynRestClient.genericPost(activity, AsynRestClient.identityVerificationUrl, jsonObject.toString(), new AsyncHttpResponseHandler() {
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

                    String status = jsonObject.getString("status");
                    if(status.equals("422"))
                    {
                        String type = jsonObject.getString("type");
                        String message = jsonObject.getString("message");
                        alert(type, message);
                        SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(getResources().getString(R.string.param_oneid_credential), jsonObject.toString());
                        editor.apply();
                        triggerHiddenLayout();
                    }
                    else if(status.equals("200"))
                    {
                        alert(getString(R.string.success), getString(R.string.success_identity_credential));
                        SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(getResources().getString(R.string.param_oneid_credential), jsonObject.toString());
                        editor.apply();
                        triggerHiddenLayout();
                        String credDefId = jsonObject.getString("id");
                        registerFromOneMove(credDefId);
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
                //String responseString = new String(errorResponse);
                //Log.e("Response", responseString);
                alert(getString(R.string.error), getString(R.string.error_please_try_again));
                //Log.e("Helo", "Hello");
            }
        });
    }

    public void utilityUpload(String path, String tag)
    {
        SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        String id = preferences.getString(getResources().getString(R.string.param_utility_user_id), "");

        AsynRestClient.utilityUpload(id, tag, path, new AsyncHttpResponseHandler()
        {
            @Override
            public void onStart() {
                progressDialog.show();
            }

            @Override
            public void onFinish() {
                progressDialog.dismiss();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response)
            {
                String responseString = new String(response);
                Log.e("Upload response", responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                //String responseString = new String(errorResponse);
                //Log.e("Upload response", responseString);
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                int progress = Math.round((bytesWritten/totalSize)*100);
                progressDialog.setProgress(progress);
                progressDialog.setMax(100);
            }
        });
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void registerFromOneMove(String credDefId)
    {
        SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        String libraWalletAddress = preferences.getString(getResources().getString(R.string.param_libra_wallet_address), "");
        String firstName = preferences.getString(getString(R.string.param_first_name), "");
        String lastName = preferences.getString(getString(R.string.param_last_name), "");
        String email = preferences.getString(getString(R.string.param_email), "");
        String contact = preferences.getString(getString(R.string.param_contact), "");
        String gender = preferences.getString(getString(R.string.param_oneid_gender), "");
        String dob = preferences.getString(getString(R.string.param_oneid_dob), "");
        String nationality = preferences.getString(getString(R.string.param_oneid_nationality_id), "");
        String companyName = preferences.getString(getString(R.string.param_oneid_company_name), "");
        String jobTitle = preferences.getString(getString(R.string.param_oneid_job_title), "");
        String idType = preferences.getString(getString(R.string.param_oneid_id_type_name), "");
        String idNo = preferences.getString(getString(R.string.param_oneid_id_no), "");
        String selfie = preferences.getString(getString(R.string.param_oneid_selfie), "");
        //String credDefId = preferences.getString(getResources().getString(R.string.param_oneid_credential), "");

        TelephonyManager telephonyManager = (TelephonyManager)activity.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = telephonyManager.getSimCountryIso();

        String documentType = "5";
        if(idType.equals("passport"))
            documentType = "1";
        if(idType.equals("id"))
            documentType = "5";
        if(idType.equals("license"))
            documentType = "6";

        AsynRestClient.registerFromOneMove(email, "MTIzNDU2", credDefId, getLocalIpAddress(), countryCode, libraWalletAddress, firstName, lastName,
                "", gender, dob, "Bangsar South, 59200 Kuala Lumpur, Federal Territory of Kuala Lumpur", "", "59200",
                "Kuala Lumpur", "WP Kuala Lumpur", "Malaysia", "MY", "Kuala Lumpur", "0123456789",
                idNo, "Programmer", "75FE7ED6-0BAC-4C55-9C20-732E4D1CB11B", "false", "false",
                "false", AsynRestClient.utilityServiceUrl + "uploads/" + libraWalletAddress + ".jpg",
                AsynRestClient.utilityServiceUrl + "uploads/" + libraWalletAddress + "_address.jpg",
                documentType,
                AsynRestClient.utilityServiceUrl + "uploads/" + libraWalletAddress + "_id.jpg", new AsyncHttpResponseHandler() {
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
                        //String responseString = new String(errorResponse);
                        //Log.e("Response", responseString);
                        //alert(getString(R.string.error), getString(R.string.error_please_try_again));
                        //Log.e("Helo", "Hello");
                    }
                });
    }
}