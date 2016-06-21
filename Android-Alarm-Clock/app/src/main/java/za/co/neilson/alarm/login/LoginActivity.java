package za.co.neilson.alarm.login;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import za.co.neilson.alarm.AlarmActivity;
import za.co.neilson.alarm.R;
import za.co.neilson.alarm.database.Database;
import za.co.neilson.alarm.group.User;

public class LoginActivity extends ActionBarActivity {
    EditText idEditText;
    Button signInButton;
    EditText pwEditText;
    Button signUpButton;

    String emailtxt, passwordtxt;
    List<NameValuePair> params;
    SharedPreferences pref;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        idEditText = (EditText) findViewById(R.id.idEditText);
        signInButton = (Button) findViewById(R.id.signInButton);
        pwEditText = (EditText) findViewById(R.id.pwEditText);
        signUpButton = (Button) findViewById(R.id.signUpButton);

        pref = getSharedPreferences("AppPref", MODE_PRIVATE);

        signInButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                User user = Database.getUser(idEditText.getText().toString(), pwEditText.getText().toString());
                emailtxt = idEditText.getText().toString();
                passwordtxt = pwEditText.getText().toString();
                email = emailtxt;
                params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("email", emailtxt));
                params.add(new BasicNameValuePair("password", passwordtxt));
                ServerRequest sr = new ServerRequest();
                JSONObject json = sr.getJSON("http://168.188.123.218:8080/login", params);
                if (json != null) {
                    try {
                        String jsonstr = json.getString("response");
                        if (json.getBoolean("res")) {
                            //db에서 해당 유저에 저장된 알람 리스트를 전부 받아와보자
                            params = new ArrayList<NameValuePair>();
                            params.add(new BasicNameValuePair("email", email));
                            JSONObject alarmGetter = sr.getJSON("http://168.188.123.218:8080/loginset", params);
                            JSONArray alarmList = alarmGetter.getJSONArray("response");

                            //기존 db 초기화
                            Database.init(LoginActivity.this);
                            Database.setEmail(email);

                            if(Database.getDatabase() != null) {
                                Database.deleteAll();
                            }

                            for(int i = 0; i < alarmList.length(); i++) {
                                JSONObject alarm = alarmList.getJSONObject(i);
                                String name = alarm.getString("name");
                                String tone = alarm.getString("tone");
                                String time = alarm.getString("time");
                                boolean vibrate = alarm.getBoolean("vibrate");
                                int difficulty = alarm.getInt("difficulty");
                                JSONArray day = alarm.getJSONArray("days");

                                String[] days = new String[day.length()];
                                for (int j = 0; j< day.length(); j++) {
                                    days[j] = day.optString(j);
                                }
                                Database.create(name, time, tone, difficulty, vibrate, days);
                            }

                            SharedPreferences.Editor edit = pref.edit();
                            //Storing Data using SharedPreferences
                            edit.commit();
                            Intent intent = new Intent(getApplicationContext(), AlarmActivity.class);
                            intent.putExtra("user", user);
                            intent.putExtra("email",email);
                            startActivity(intent);
                        }

                        Toast.makeText(getApplication(), jsonstr, Toast.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivityForResult(intent,1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(requestCode == 1){
                Database.addUser((User)data.getSerializableExtra("user"));
            }
        }
    }
}