package za.co.neilson.alarm.alert;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import za.co.neilson.alarm.Alarm;
import za.co.neilson.alarm.R;
import za.co.neilson.alarm.database.Database;
import za.co.neilson.alarm.group.User;
import za.co.neilson.alarm.login.ServerRequest;

/**
 * Created by 11 on 2016-06-08.
 */
public class WaitActivity extends Activity {
    private Alarm alarm;
    private String email;
    List<NameValuePair> params;
    private boolean everyoneSuccess;

    Vibrator vibrator;

    private TimerTask mTask;
    private TimerTask mTask2;
    private TimerTask mTask3;


    private Timer mTimer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.wait_layout);

        Bundle bundle = this.getIntent().getExtras();
        alarm = (Alarm) bundle.getSerializable("alarm");
        Handler mHandler = new Handler();
        mTask = new TimerTask() {
            @Override
            public void run() {
                checkEveryoneSuccess();
            }
        };

        mTask2 = new TimerTask() {
            @Override
            public void run() {
                finishAlarm();
            }
        };

        mTask3 = new TimerTask() {
            @Override
            public void run() {
                resetUserSuccess();
            }
        };

        email = Database.getEmail();
        everyoneSuccess = false;

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        long[] pattern = { 1000, 200, 200, 200 };
        vibrator.vibrate(pattern, 0);

        sendSuccess();

        mTimer = new Timer();
        mHandler.postDelayed(mTask, 3000); // 3초후에 실행
    }

    private Runnable checkMeSuccess = new Runnable() {
        @Override
        public void run() {
            waitMoment();
        }
    };

    private Runnable checkEveryoneSuccess = new Runnable() {
        @Override
        public void run() {
            Log.d("ㄹㄹㄹㄹㄹㄹㄹㄹㄹ", "ㄹㄹㄹㄹㄹㄹㄹㄹㄹㄹㄹㄹㄹ");
            checkEveryoneSuccess();
        }
    };

    private void finishAlarm(){
        vibrator.cancel();
        this.finish();
    }

    private void waitMoment(){
        Intent intent = new Intent(WaitActivity.this, AlarmAlertActivity.class );
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("alarm",alarm);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }

    private void sendSuccess() {
        ServerRequest sr = new ServerRequest();
        params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("name", alarm.getAlarmName()));
        JSONObject json = sr.getJSON("http://168.188.123.218:8080/success", params);
        if (json != null) {
            try {
                Log.d("성공했다고 저장 잘 했나요?: ", ""+json.get("response"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void resetUserSuccess(){
        ServerRequest sr = new ServerRequest();
        params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("name", alarm.getAlarmName()));
        JSONObject json = sr.getJSON("http://168.188.123.218:8080/resetuser", params);
        if (json != null) {
            try {
                String result = json.getString("response");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkEveryoneSuccess(){
        Log.d("ㅁㅁㅁㅁㅁㅁㅁㅁ", "ㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁ");

        ServerRequest sr = new ServerRequest();
        params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("name", alarm.getAlarmName()));
        JSONObject json = sr.getJSON("http://168.188.123.218:8080/gameresult", params);
        if (json != null) {
            try {
                boolean everyoneClear = json.getBoolean("wait");
                while (!everyoneClear) {
                    Log.d("누군가가 실패했습니다.", "성공할때까지 반복문 계속 진행");
                    params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("name", alarm.getAlarmName()));
                    JSONObject json2 = sr.getJSON("http://168.188.123.218:8080/gameresult", params);
                    everyoneClear = json2.getBoolean("wait");
                }
                everyoneSuccess = true;

            } catch (JSONException e)             {
                e.printStackTrace();
            }

            if(everyoneSuccess){
                Handler mHandler = new Handler();
                Handler mHandler2 = new Handler();
                mHandler.postDelayed(mTask2, 1000);
                mHandler2.postDelayed(mTask3, 9000);
            }
        }
    }
}