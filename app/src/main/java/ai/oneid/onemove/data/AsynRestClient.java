package ai.oneid.onemove.data;

import android.content.Context;
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
    public static String libraServiceUrl = "https://pixel.ddns.net/wallet/libra/";
    public static String libraCreateWalletUrl = libraServiceUrl + "create-wallet";
    public static String libraRetrieveBalanceUrl = libraServiceUrl + "retrieve-balance";
    public static String libraSendTransactionUrl = libraServiceUrl + "send-transaction";
    public static String libraMintCoinUrl = libraServiceUrl + "mint-coin";

    public static String identityServiceUrl = "https://api.1id.ai/v2/";
    //public static String identityServiceUrl = "https://pixel.ddns.net/indyv2/";
    public static String createIdentityWalletUrl = identityServiceUrl + "create-identity-wallet";
    public static String identityVerificationUrl = identityServiceUrl + "identity-verification";
    public static String viewUploadUrl = identityServiceUrl + "view-upload";
    public static String uploadUrl = identityServiceUrl + "upload" ;

    public static String utilityServiceUrl = "https://pixel.ddns.net/onemove/";
    public static String utilityCreateAccountUrl = utilityServiceUrl + "create_account/";
    public static String utilityUpdateAccountUrl = utilityServiceUrl + "update_account/";
    public static String utilitySearchUserUrl = utilityServiceUrl + "search_user/";
    public static String utilityRetrieveFriendUrl = utilityServiceUrl + "retrieve_friend/";
    public static String utilityAddUserUrl = utilityServiceUrl + "add_user/";
    public static String utilityUploadUrl = utilityServiceUrl + "upload/" ;
    public static String utilityCreateTransactionUrl = utilityServiceUrl + "create_transaction/";
    public static String utilityRetrieveTransactionUrl = utilityServiceUrl + "retrieve_transaction/";

    public static int timeOutMili = 20000;
    private static AsyncHttpClient client = new AsyncHttpClient();

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

    public static void utilityUpload(String id, String imagePath, AsyncHttpResponseHandler responseHandler) {
        client.setSSLSocketFactory(MySSLSocketFactory.getFixedSocketFactory());
        client.setTimeout(timeOutMili);
        File myFile = new File(imagePath);
        RequestParams params = new RequestParams();
        try {
            params.put("id", id);
            params.put("file", myFile);
            Log.e("Param", params.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        client.post(utilityUploadUrl, params, responseHandler);
    }
}