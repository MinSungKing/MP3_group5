/* Copyright 2014 Sheldon Neilson www.neilson.co.za
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package za.co.neilson.alarm.alert;

import za.co.neilson.alarm.Alarm;
import za.co.neilson.alarm.AlarmActivity;
import za.co.neilson.alarm.R;
import za.co.neilson.alarm.database.Database;
import za.co.neilson.alarm.login.ServerRequest;
import za.co.neilson.alarm.preferences.AlarmPreferencesActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.HapticFeedbackConstants;
import android.view.ViewManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AlarmAlertActivity extends Activity implements OnClickListener {
	private Alarm alarm;
	private MediaPlayer mediaPlayer;

	private StringBuilder answerBuilder = new StringBuilder();

	private MathProblem mathProblem;
	private Vibrator vibrator;

	private boolean alarmActive;

	private TextView problemView;
	private TextView answerView;
	private String answerString;

	List<NameValuePair> params;
	SharedPreferences pref;

	String email;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		setContentView(R.layout.alarm_alert);

		Bundle bundle = this.getIntent().getExtras();
		alarm = (Alarm) bundle.getSerializable("alarm");
		email = bundle.getString("email");

		email = Database.getEmail();
		pref = getSharedPreferences("AppPref", MODE_PRIVATE);

		this.setTitle(alarm.getAlarmName());

		switch (alarm.getDifficulty()) {
		case EASY:
			mathProblem = new MathProblem(3);
			break;
		case MEDIUM:
			mathProblem = new MathProblem(4);
			break;
		case HARD:
			mathProblem = new MathProblem(5);
			break;
		}

		answerString = String.valueOf(mathProblem.getAnswer());
		if (answerString.endsWith(".0")) {
			answerString = answerString.substring(0, answerString.length() - 2);
		}

		problemView = (TextView) findViewById(R.id.textView1);
		problemView.setText(mathProblem.toString());

		answerView = (TextView) findViewById(R.id.textView2);
		answerView.setText("= ?");

		((Button) findViewById(R.id.Button0)).setOnClickListener(this);
		((Button) findViewById(R.id.Button1)).setOnClickListener(this);
		((Button) findViewById(R.id.Button2)).setOnClickListener(this);
		((Button) findViewById(R.id.Button3)).setOnClickListener(this);
		((Button) findViewById(R.id.Button4)).setOnClickListener(this);
		((Button) findViewById(R.id.Button5)).setOnClickListener(this);
		((Button) findViewById(R.id.Button6)).setOnClickListener(this);
		((Button) findViewById(R.id.Button7)).setOnClickListener(this);
		((Button) findViewById(R.id.Button8)).setOnClickListener(this);
		((Button) findViewById(R.id.Button9)).setOnClickListener(this);
		((Button) findViewById(R.id.Button_clear)).setOnClickListener(this);
		((Button) findViewById(R.id.Button_decimal)).setOnClickListener(this);
		((Button) findViewById(R.id.Button_minus)).setOnClickListener(this);

		TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);

		PhoneStateListener phoneStateListener = new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				switch (state) {
				case TelephonyManager.CALL_STATE_RINGING:
					Log.d(getClass().getSimpleName(), "Incoming call: "
							+ incomingNumber);
					try {
						mediaPlayer.pause();
					} catch (IllegalStateException e) {

					}
					break;
				case TelephonyManager.CALL_STATE_IDLE:
					Log.d(getClass().getSimpleName(), "Call State Idle");
					try {
						//mediaPlayer.start();
					} catch (IllegalStateException e) {

					}
					break;
				}
				super.onCallStateChanged(state, incomingNumber);
			}
		};
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_CALL_STATE);

		// Toast.makeText(this, answerString, Toast.LENGTH_LONG).show();

		startAlarm();
	}

	@Override
	protected void onResume() {
		super.onResume();
		alarmActive = true;
	}

	private void startAlarm() {
		if (alarm.getAlarmTonePath() != "") {
			mediaPlayer = new MediaPlayer();
			if (alarm.getVibrate()) {
				vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
				long[] pattern = { 1000, 200, 200, 200 };
				vibrator.vibrate(pattern, 0);
			}
			try {
				mediaPlayer.setVolume(1.0f, 1.0f);
				mediaPlayer.setDataSource(this,
						Uri.parse(alarm.getAlarmTonePath()));
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				mediaPlayer.setLooping(true);
				mediaPlayer.prepare();
				//mediaPlayer.start();

			} catch (Exception e) {
				mediaPlayer.release();
				alarmActive = false;
			}
		}
	}

	public void onUserLeaveHint() {
		Context context = getApplicationContext(); // 자동으로 화면이동
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

		//Intent Intent = new Intent("a.a");
		Intent Intent = new Intent(AlarmAlertActivity.this, AlarmAlertActivity.class );
		Intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP );
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, Intent, 0);

		alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 0, pIntent);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		if (!alarmActive)
			super.onBackPressed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */

	@Override
	protected void onPause() {
		super.onPause();
		StaticWakeLock.lockOff(this);
	}


	@Override
	protected void onDestroy() {
		try {
			if (vibrator != null)
				vibrator.cancel();
		} catch (Exception e) {

		}
		try {
			mediaPlayer.stop();
		} catch (Exception e) {

		}
		try {
			mediaPlayer.release();
		} catch (Exception e) {

		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (!alarmActive)
			return;
		String button = (String) v.getTag();
		v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		if (button.equalsIgnoreCase("clear")) {
			if (answerBuilder.length() > 0) {
				answerBuilder.setLength(answerBuilder.length() - 1);
				answerView.setText(answerBuilder.toString());
			}
		} else if (button.equalsIgnoreCase(".")) {
			if (!answerBuilder.toString().contains(button)) {
				if (answerBuilder.length() == 0)
					answerBuilder.append(0);
				answerBuilder.append(button);
				answerView.setText(answerBuilder.toString());
			}
		} else if (button.equalsIgnoreCase("-")) {
			if (answerBuilder.length() == 0) {
				answerBuilder.append(button);
				answerView.setText(answerBuilder.toString());
			}
		} else {
			answerBuilder.append(button);
			answerView.setText(answerBuilder.toString());

			//문제를 풀었을때 정답이 맞았는지 확인해서 알람을 종료하거나 계속 울린다.
			if (isAnswerCorrect()) {
				/*
				//대기화면으로 덮어버림
				LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View waitLayout = (View) inflater.inflate(R.layout.wait_layout, null);
				addContentView(waitLayout, new LinearLayout.LayoutParams(
						500, 500
				));
				SystemClock.sleep(3000);
				*/


				/*
				ServerRequest sr = new ServerRequest();
				JSONObject json = sr.getJSON("http://168.188.123.218:8080/gameresult", params);
				if (json != null) {
					try {
						boolean everyoneClear = json.getBoolean("wait");
						while(!everyoneClear){
							SystemClock.sleep(1000);
						}

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				*/
				alarmActive = false;
				if (vibrator != null)
					vibrator.cancel();
				try {
					mediaPlayer.stop();
				} catch (IllegalStateException ise) {

				}
				try {
					mediaPlayer.release();
				} catch (Exception e) {

				}
				//SystemClock.sleep(3000);
				//덮었던 화면 없앰
				//((ViewManager) waitLayout.getParent()).removeView(waitLayout);
				this.finish();
				Intent intent = new Intent(AlarmAlertActivity.this, WaitActivity.class);
				intent.putExtra("alarm",alarm);
				intent.putExtra("email",email);
				startActivity(intent);
			}
		}
		if (answerView.getText().length() >= answerString.length()
				&& !isAnswerCorrect()) {
			answerView.setTextColor(Color.RED);
		} else {
			answerView.setTextColor(Color.BLACK);
		}
	}

	public boolean isAnswerCorrect() {
		boolean correct = false;
		try {
			correct = mathProblem.getAnswer() == Float.parseFloat(answerBuilder
					.toString());
		} catch (NumberFormatException e) {
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return correct;
	}

}
