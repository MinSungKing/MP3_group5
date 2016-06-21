package za.co.neilson.alarm.login;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.content.SharedPreferences;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import za.co.neilson.alarm.R;
import za.co.neilson.alarm.group.User;

public class SignUpActivity extends ActionBarActivity {
    EditText inputIdEditText;
    EditText inputPwEditText;
    Button okButton;

    String emailtxt, passwordtxt;
    List<NameValuePair> params;
    SharedPreferences pref;
    ServerRequest sr;


    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        inputIdEditText = (EditText) findViewById(R.id.inputIdEditText);
        inputPwEditText = (EditText) findViewById(R.id.inputPwEditText);
        okButton = (Button) findViewById(R.id.okButton);

        sr = new ServerRequest();
        pref = getSharedPreferences("AppPref", MODE_PRIVATE);


        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = new User(inputIdEditText.getText().toString(), inputPwEditText.getText().toString());

                emailtxt = inputIdEditText.getText().toString();
                passwordtxt = inputPwEditText.getText().toString();
                params = new ArrayList<NameValuePair>();

                params.add(new BasicNameValuePair("email", emailtxt));
                params.add(new BasicNameValuePair("password", passwordtxt));
                ServerRequest sr = new ServerRequest();
                JSONObject json = sr.getJSON("http://168.188.123.218:8080/register", params);
                if (json != null) {
                    try {
                        String jsonstr = json.getString("response");

                        Toast.makeText(getApplication(), jsonstr, Toast.LENGTH_LONG).show();

                        Log.d("Hello", jsonstr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Intent intent = getIntent();
                intent.putExtra("user", user);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
