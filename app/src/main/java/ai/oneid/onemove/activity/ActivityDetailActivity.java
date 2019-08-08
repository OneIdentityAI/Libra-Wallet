package ai.oneid.onemove.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import ai.oneid.onemove.R;

public class ActivityDetailActivity extends AppCompatActivity {
    public AppCompatImageView imageBack;
    public TextView textDate;
    public TextView textComment;
    public TextView textAmount;
    public TextView textTransactionId;
    public TextView textTransactionDatetime;
    public String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setContentView(R.layout.activity_activity_detail);

        textDate = findViewById(R.id.text_date);
        textComment = findViewById(R.id.text_comment);
        textAmount = findViewById(R.id.text_amount);
        textTransactionId = findViewById(R.id.text_transaction_id);
        textTransactionDatetime = findViewById(R.id.text_transaction_datetime);

        data = getIntent().getExtras().getString("data");
        if(!data.equals(""))
        {
            try
            {
                JSONObject innerObject = new JSONObject(data);
                String userId = innerObject.getString("user_id");
                String name = innerObject.getString("name");
                String amount = innerObject.getString("amount");
                String comment = innerObject.getString("comment");
                String created = innerObject.getString("created");
                String transactionId = innerObject.getString("transaction_id");
                textDate.setText(created.split(" ")[0]);
                textComment.setText(comment);
                textAmount.setText(amount);
                textTransactionId.setText(transactionId);
                textTransactionDatetime.setText(created.split(" ")[1]);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

        imageBack = findViewById(R.id.image_back);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
