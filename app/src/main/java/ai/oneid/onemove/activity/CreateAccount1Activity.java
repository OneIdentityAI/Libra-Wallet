package ai.oneid.onemove.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import ai.oneid.onemove.R;
import ai.oneid.onemove.data.AppDelegate;

public class CreateAccount1Activity extends AppCompatActivity {
    public EditText inputFirstName;
    public EditText inputLastName;
    public Button buttonContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setContentView(R.layout.activity_create_account_1);

        inputFirstName = findViewById(R.id.input_first_name);
        inputLastName = findViewById(R.id.input_last_name);
        buttonContinue = findViewById(R.id.button_continue);
        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputFirstName.setError(null);
                inputLastName.setError(null);
                if(inputFirstName.getText().toString().equals(""))
                    inputFirstName.setError(getString(R.string.error_required_field));
                if(inputLastName.getText().toString().equals(""))
                    inputLastName.setError(getString(R.string.error_required_field));

                if(!inputFirstName.getText().toString().equals("") && !inputLastName.getText().toString().equals(""))
                {
                    SharedPreferences preferences = getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(getResources().getString(R.string.param_first_name), inputFirstName.getText().toString());
                    editor.putString(getResources().getString(R.string.param_last_name), inputLastName.getText().toString());
                    editor.apply();

                    Intent intent = new Intent(CreateAccount1Activity.this, CreateAccount2Activity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    public void onBackPressed()
    {
        Intent i = new Intent(CreateAccount1Activity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
