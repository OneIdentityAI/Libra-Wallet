package ai.oneid.onemove.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import ai.oneid.onemove.R;
import ai.oneid.onemove.data.AppDelegate;

public class PaymentRequest extends AppCompatActivity {
    public AppCompatImageView imageBack;
    public AppCompatImageView imageQRCode;
    public TextView textAddress;
    public Button buttonCopyLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        final SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_payment_request);

        imageBack = findViewById(R.id.image_back);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        imageQRCode = findViewById(R.id.image_qrcode);
        try {
            imageQRCode.setImageBitmap(AppDelegate.encodeStringAsQRBitmap(preferences.getString(getResources().getString(R.string.param_libra_wallet_address), "")));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        textAddress = findViewById(R.id.text_address);
        textAddress.setText(preferences.getString(getResources().getString(R.string.param_libra_wallet_address), ""));
        buttonCopyLink = findViewById(R.id.button_copy_link);
        buttonCopyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", preferences.getString(getResources().getString(R.string.param_libra_wallet_address), ""));
                clipboard.setPrimaryClip(clip);
                Toast.makeText(PaymentRequest.this, getString(R.string.wallet_address_copied), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
