package ai.oneid.onemove.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import ai.oneid.onemove.R;
import ai.oneid.onemove.data.AppDelegate;
import ai.oneid.onemove.data.AsynRestClient;
import cz.msebera.android.httpclient.Header;
import ozaydin.serkan.com.image_zoom_view.ImageViewZoom;

public class VerificationResultActivity extends AppCompatActivity {
    public AppCompatImageView imageBack;
    public TextView textCreatedOn;
    public TextView textPepIsPep;
    public TextView textPepSource;
    public TextView textPepRef;
    public TextView textSanctionIsSdn;
    public TextView textSanctionSdnSource;
    public TextView textSanctionSdnRef;
    public TextView textSanctionSdnSearchType;
    public TextView textAgeCheck;
    public TextView textDOB;
    public TextView textDOBCheck;
    public TextView textNationality;
    public TextView textNationalityCheck;
    public TextView textGenderCheck;
    public TextView textIdValidityCheck;
    public TextView textIdNo;
    public TextView textIdNoCheck;
    public TextView textName;
    public TextView textNameCheck;
    public TextView textFaceMatchImageType;
    public TextView textFaceMatchScore;
    public TextView textFaceMatchResult;
    public TextView textOverallResult;
    public ImageViewZoom imageId;
    public ImageViewZoom imageSelfie;
    public String idImage;
    public byte[] byteImageId;
    public String selfieImage;
    public byte[] byteImageSelfie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        final SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_verification_result);


        imageBack = findViewById(R.id.image_back);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        textCreatedOn = findViewById(R.id.text_created_on);
        textPepIsPep = findViewById(R.id.text_pep_is_pep);
        textPepSource = findViewById(R.id.text_pep_source);
        textPepRef = findViewById(R.id.text_pep_ref);
        textSanctionIsSdn = findViewById(R.id.text_sanction_is_sdn);
        textSanctionSdnSource = findViewById(R.id.text_sanction_sdn_source);
        textSanctionSdnRef = findViewById(R.id.text_sanction_sdn_ref);
        textSanctionSdnSearchType = findViewById(R.id.text_sanction_search_type);

        textAgeCheck = findViewById(R.id.text_age_check);
        textDOB = findViewById(R.id.text_dob);
        textDOBCheck = findViewById(R.id.text_dob_check);
        textNationality = findViewById(R.id.text_nationality);
        textNationalityCheck = findViewById(R.id.text_nationality_check);
        textGenderCheck = findViewById(R.id.text_gender_check);
        textIdValidityCheck = findViewById(R.id.text_id_validity_check);
        textIdNo = findViewById(R.id.text_id_no);
        textIdNoCheck = findViewById(R.id.text_id_no_check);
        textName = findViewById(R.id.text_name);
        textNameCheck = findViewById(R.id.text_name_check);
        textFaceMatchImageType = findViewById(R.id.text_face_match_image_type);
        textFaceMatchScore = findViewById(R.id.text_face_match_score);
        textFaceMatchResult = findViewById(R.id.text_face_match_result);
        textOverallResult = findViewById(R.id.text_overall_result);

        imageId = findViewById(R.id.image_id);
        imageSelfie = findViewById(R.id.image_selfie);
        try {
            String jsonResponse = preferences.getString(getResources().getString(R.string.param_oneid_credential), "");
            JSONObject jsonObject = new JSONObject(jsonResponse);
            String createdOn = jsonObject.getString("created_at");

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                Date formattedDatetime = format.parse(createdOn);
                createdOn = newFormat.format(formattedDatetime);
                textCreatedOn.setText(createdOn);
            } catch (Exception e) {
            }
            JSONObject pepObject = jsonObject.getJSONObject("pep_data");
            textPepIsPep.setText(pepObject.getString("isPEP"));
            textPepSource.setText(pepObject.getString("PEPSource"));
            textPepRef.setText(pepObject.getString("PEPRef"));
            JSONObject sanctionObject = jsonObject.getJSONObject("sanction_data");
            textSanctionIsSdn.setText(sanctionObject.getString("isSDN"));
            textSanctionSdnSource.setText(sanctionObject.getString("SDNSource"));
            textSanctionSdnRef.setText(sanctionObject.getString("SDNRef"));
            textSanctionSdnSearchType.setText(sanctionObject.getString("searchType"));

            JSONObject facialSimilarityObject = jsonObject.getJSONObject("facial_similarity");
            JSONObject attributesObject = jsonObject.getJSONObject("non_verifiable_attributes");
            JSONObject nameCheckObject = jsonObject.getJSONObject("name_check");
            JSONObject idNoCheckObject = jsonObject.getJSONObject("id_no_check");
            JSONObject idValidityCheckObject = jsonObject.getJSONObject("id_validity_check");
            JSONObject genderCheckObject = jsonObject.getJSONObject("gender_check");
            JSONObject nationalityCheckObject = jsonObject.getJSONObject("nationality_check");
            JSONObject dobCheckObject = jsonObject.getJSONObject("dob_check");
            JSONObject ageCheckObject = jsonObject.getJSONObject("age_check");
            textAgeCheck.setText(ageCheckObject.getString("result"));
            textDOB.setText(attributesObject.getString("dob"));
            textDOBCheck.setText(dobCheckObject.getString("result"));
            textNationality.setText(attributesObject.getString("nationality"));
            textNationalityCheck.setText(nationalityCheckObject.getString("result"));
            textGenderCheck.setText(genderCheckObject.getString("result"));
            textIdValidityCheck.setText(idValidityCheckObject.getString("result"));
            textIdNo.setText(attributesObject.getString("id_no"));
            textIdNoCheck.setText(idNoCheckObject.getString("result"));
            textName.setText(attributesObject.getString("first_name") + " " + attributesObject.getString("last_name"));
            textNameCheck.setText(nameCheckObject.getString("result"));
            textFaceMatchImageType.setText(facialSimilarityObject.getString("image_type"));
            textFaceMatchScore.setText(facialSimilarityObject.getString("score"));
            textFaceMatchResult.setText(facialSimilarityObject.getString("result"));
            idImage = attributesObject.getString("id_image");
            selfieImage = attributesObject.getString("selfie_image");

            String status = jsonObject.getString("status");
            if(status.equals("422"))
                textOverallResult.setText(getString(R.string.fail));
            else
                textOverallResult.setText(getString(R.string.success));

            viewUpload("id");
        } catch (JSONException e) {
        }
    }

    public void viewUpload(final String type)
    {
        final SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("token", preferences.getString(getString(R.string.param_oneid_token), ""));
            if(type.equals("id"))
                jsonObject.put("document_name", idImage);
            else
                jsonObject.put("document_name", selfieImage);
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
                    if(type.equals("selfie")) {
                        byteImageSelfie = imageByteArray;
                        Glide.with(VerificationResultActivity.this).load(imageByteArray).into(imageSelfie);
                    }
                    else if(type.equals("id")) {
                        byteImageId = imageByteArray;
                        Glide.with(VerificationResultActivity.this).load(imageByteArray).into(imageId);
                        viewUpload("selfie");
                    }
                }
                catch (Exception e)
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
}
