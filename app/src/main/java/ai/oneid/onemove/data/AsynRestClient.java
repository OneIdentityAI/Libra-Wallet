package ai.oneid.onemove.data;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.entity.StringEntity;

public class AsynRestClient {
    public static String libraServiceUrl =  Build.VERSION.SDK_INT < Build.VERSION_CODES.M?"http://3.0.93.202/wallet/libra/":"https://pixel-staging.ddns.net/wallet/libra/";
    public static String libraCreateWalletUrl = libraServiceUrl + "create-wallet";
    public static String libraSendTransactionUrl = libraServiceUrl + "send-transaction";

    public static String identityServiceUrl =  Build.VERSION.SDK_INT < Build.VERSION_CODES.M?"http://pixel.ddns.net/":"https://api.1id.ai/v2/";
    public static String createIdentityWalletUrl = identityServiceUrl + "create-identity-wallet";
    public static String identityVerificationUrl = identityServiceUrl + "identity-verification";
    public static String viewUploadUrl = identityServiceUrl + "view-upload";
    public static String uploadUrl = identityServiceUrl + "upload" ;

    public static String utilityServiceUrl =  Build.VERSION.SDK_INT < Build.VERSION_CODES.M?"http://3.0.93.202/onemove/":"https://pixel-staging.ddns.net/onemove/";
    public static String utilityCreateAccountUrl = utilityServiceUrl + "create_account/";
    public static String utilityUpdateAccountUrl = utilityServiceUrl + "update_account/";
    public static String utilitySearchUserUrl = utilityServiceUrl + "search_user/";
    public static String utilityRetrieveFriendUrl = utilityServiceUrl + "retrieve_friend/";
    public static String utilityAddUserUrl = utilityServiceUrl + "add_user/";
    public static String utilityUploadUrl = utilityServiceUrl + "upload/" ;
    public static String utilityCreateTransactionUrl = utilityServiceUrl + "create_transaction/";
    public static String utilityRetrieveTransactionUrl = utilityServiceUrl + "retrieve_transaction/";

    public static String registerFromOneMoveUrl = "https://intapi.1id.ai/node/pixel-token-platform-api/api/v1/integrations/register-from-one-move";
    public static String fiatDepositTransactionLogUrl = "https://intapi.1id.ai/node/pixel-token-platform-api/api/v1/integrations/fiat-deposit-transaction-log";
    public static String faucet = "http://faucet.testnet.libra.org:80?amount=1000000000&address=";
    public static String retrieveBalanceUrl = "https://api-test.libexplorer.com/api?module=account&action=balance&address=";

    public static int timeOutMili = 20000;
    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void genericGet(Context context, String url, AsyncHttpResponseHandler responseHandler) {
        client.setTimeout(timeOutMili);
        client.get(context, url, responseHandler);
    }

    public static void genericPost(Context context, String url, String data, AsyncHttpResponseHandler responseHandler) {
        client.setSSLSocketFactory(MySSLSocketFactory.getFixedSocketFactory());
        client.setTimeout(timeOutMili);
        client.addHeader("Authorization", "Bearer " + AppDelegate.apikey);
        StringEntity entity = null;
        try {
            entity = new StringEntity(data);
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        client.post(context, url, entity, "application/json", responseHandler);
    }

    public static void upload(String token, String type, String imagePath, AsyncHttpResponseHandler responseHandler) {
        client.setSSLSocketFactory(MySSLSocketFactory.getFixedSocketFactory());
        client.setTimeout(timeOutMili);
        client.addHeader("Authorization", "Bearer " + AppDelegate.apikey);
        File myFile = new File(imagePath);
        RequestParams params = new RequestParams();
        try {
            params.put("token", token);
            params.put("type", type);
            params.put("file", myFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        client.post(uploadUrl, params, responseHandler);
    }

    public static void utilityUpload(String id, String tag, String imagePath, AsyncHttpResponseHandler responseHandler) {
        client.setSSLSocketFactory(MySSLSocketFactory.getFixedSocketFactory());
        client.setTimeout(timeOutMili);
        File myFile = new File(imagePath);
        RequestParams params = new RequestParams();
        try {
            params.put("id", id + tag);
            params.put("file", myFile);
            Log.e("Param", params.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        client.post(utilityUploadUrl, params, responseHandler);
    }

    public static void registerFromOneMove(String email, String password, String oneIdentityCredentialReferent, String registrationIP,
                                           String registrationIPCountry, String walletAddress, String firstName, String lastName,
                                           String title, String gender, String dateOfBirth, String addressLine1, String addressLine2,
                                           String zipCode, String city, String state, String countryOfResidence, String nationality,
                                           String placeOfBirth, String mobileNo, String passportNo, String occupation,
                                           String occupationIndustryID, String areYouAPoliticalExposedPerson, String areYouAUSCitizen,
                                           String isIncomeAssetAbove, String selfieImageUrl, String proofOfAddressImageUrl,
                                           String userDocumentTypeID, String userDocumentImageUrl, AsyncHttpResponseHandler responseHandler) {
        client.setSSLSocketFactory(MySSLSocketFactory.getFixedSocketFactory());
        client.setTimeout(timeOutMili);
        RequestParams params = new RequestParams();
        params.put("apiKey", "OneMove123");
        params.put("email", email);
        params.put("password", password);
        params.put("oneIdentityCredentialReferent", oneIdentityCredentialReferent);
        params.put("registrationIP", registrationIP);
        params.put("registrationIPCountry", registrationIPCountry);
        params.put("walletAddress", walletAddress);
        params.put("firstName", firstName);
        params.put("lastName", lastName);
        params.put("title", title);
        params.put("gender", gender);
        params.put("dateOfBirth", dateOfBirth);
        params.put("addressLine1", addressLine1);
        params.put("addressLine2", addressLine2);
        params.put("zipCode", zipCode);
        params.put("city", city);
        params.put("state", state);
        params.put("countryOfResidence", countryOfResidence);
        params.put("nationality", nationality);
        params.put("placeOfBirth", placeOfBirth);
        params.put("mobileNo", mobileNo);
        params.put("passportNo", passportNo);
        params.put("occupation", occupation);
        params.put("occupationIndustryID", occupationIndustryID);
        params.put("areYouAPoliticalExposedPerson", areYouAPoliticalExposedPerson);
        params.put("areYouAUSCitizen", areYouAUSCitizen);
        params.put("isIncomeAssetAbove", isIncomeAssetAbove);
        params.put("selfieImageUrl", selfieImageUrl);
        params.put("proofOfAddressImageUrl", proofOfAddressImageUrl);
        params.put("userDocumentTypeID", userDocumentTypeID);
        params.put("userDocumentImageUrl", userDocumentImageUrl);
        Log.e("Params", params.toString());
        client.post(registerFromOneMoveUrl, params, responseHandler);
    }


    public static void fiatDeportTransactionLog(String paymentRefNo, String fromAccount, String toAccount , String amount, String ip, AsyncHttpResponseHandler responseHandler) {
        client.setSSLSocketFactory(MySSLSocketFactory.getFixedSocketFactory());
        client.setTimeout(timeOutMili);
        RequestParams params = new RequestParams();
        params.put("apiKey", "OneMove123");
        params.put("paymentRefNo", paymentRefNo);
        params.put("paymentProvider", "OneMove");
        params.put("payerUserId", "");
        params.put("fromAccount", fromAccount);
        params.put("toAccount", toAccount);
        params.put("currency", "USD");
        params.put("amount", amount);
        params.put("IP", ip);
        client.post(fiatDepositTransactionLogUrl, params, responseHandler);
    }
}