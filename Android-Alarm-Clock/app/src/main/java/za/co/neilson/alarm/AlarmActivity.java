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
package za.co.neilson.alarm;


import java.util.ArrayList;
import java.util.List;

import za.co.neilson.alarm.database.Database;
import za.co.neilson.alarm.group.User;
import za.co.neilson.alarm.login.ServerRequest;
import za.co.neilson.alarm.preferences.AlarmPreferencesActivity;
import za.co.neilson.alarm.R;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AlarmActivity extends BaseActivity {

	ImageButton newButton;
	ListView mathAlarmListView;
	AlarmListAdapter alarmListAdapter;
	User user;
	String email;
	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.alarm_activity);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null && bundle.containsKey("user")) {
			user = (User) bundle.getSerializable("user");
		}
		email = bundle.getString("email");

		mathAlarmListView = (ListView) findViewById(android.R.id.list);
		mathAlarmListView.setLongClickable(true);
		mathAlarmListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
				view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
				final Alarm alarm = (Alarm) alarmListAdapter.getItem(position);
				Builder dialog = new Builder(AlarmActivity.this);
				dialog.setTitle("Delete");
				dialog.setMessage("Delete this alarm?");
				dialog.setPositiveButton("Ok", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						Database.init(AlarmActivity.this);
						Database.setEmail(email);



						if (alarm.getId() < 1) {
							// Alarm not saved
						} else {
							params = new ArrayList<NameValuePair>();
							params.add(new BasicNameValuePair("email", email));
							params.add(new BasicNameValuePair("time", alarm.getAlarmTimeString()));
							Log.d("이메일에 뭐가들어간거야 : ",""+email);
							ServerRequest sr = new ServerRequest();
							JSONObject json = sr.getJSON("http://168.188.123.218:8080/deletealarm", params);

							Database.deleteEntry(alarm);
							AlarmActivity.this.callMathAlarmScheduleService();
						}
						updateAlarmList();
						Database.setEmail(email);

						/* 원래코드
						Database.init(AlarmActivity.this);
						Database.deleteEntry(alarm);
						AlarmActivity.this.callMathAlarmScheduleService();

						updateAlarmList();
						*/
					}
				});
				dialog.setNegativeButton("Cancel", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

				dialog.show();

				return true;
			}
		});

		callMathAlarmScheduleService();

		alarmListAdapter = new AlarmListAdapter(this);
		this.mathAlarmListView.setAdapter(alarmListAdapter);
		mathAlarmListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				Alarm alarm = (Alarm) alarmListAdapter.getItem(position);
				Intent intent = new Intent(AlarmActivity.this, AlarmPreferencesActivity.class);
				intent.putExtra("alarm", alarm);
				intent.putExtra("email",email);
				startActivity(intent);
			}

		});
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.findItem(R.id.menu_item_save).setVisible(false);
		menu.findItem(R.id.menu_item_delete).setVisible(false);
		return result;
	}

	@Override
	protected void onPause() {
		// setListAdapter(null);
		Database.deactivate();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateAlarmList();
		Database.setEmail(email);

	}

	public void updateAlarmList() {
		Database.init(AlarmActivity.this);
		Database.setEmail(email);

		final List<Alarm> alarms = Database.getAll();
		alarmListAdapter.setMathAlarms(alarms);

		runOnUiThread(new Runnable() {
			public void run() {
				// reload content
				AlarmActivity.this.alarmListAdapter.notifyDataSetChanged();
				if (alarms.size() > 0) {
					findViewById(android.R.id.empty).setVisibility(View.INVISIBLE);
				} else {
					findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.checkBox_alarm_active) {
			CheckBox checkBox = (CheckBox) v;
			Alarm alarm = (Alarm) alarmListAdapter.getItem((Integer) checkBox.getTag());
			alarm.setAlarmActive(checkBox.isChecked());
			Database.update(alarm);
			Database.setEmail(email);
			AlarmActivity.this.callMathAlarmScheduleService();
			if (checkBox.isChecked()) {
				Toast.makeText(AlarmActivity.this, alarm.getTimeUntilNextAlarmMessage(), Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client.connect();
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Alarm Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://za.co.neilson.alarm/http/host/path")
		);
		AppIndex.AppIndexApi.start(client, viewAction);
	}

	@Override
	public void onStop() {
		super.onStop();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Alarm Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://za.co.neilson.alarm/http/host/path")
		);
		AppIndex.AppIndexApi.end(client, viewAction);
		client.disconnect();
	}

	@Override
	protected void startAlarmPreferencesActivity() {
		Intent intent = new Intent(getApplicationContext(), AlarmPreferencesActivity.class);
		intent.putExtra("user",user);
		intent.putExtra("email",email);
		startActivity(intent);
	}
	private byte[] obejctToByteArray(Object ob){
		return ((ob.toString()).getBytes());
	}

	@Override
	protected void startDialog() {

		Builder dialog = new Builder(AlarmActivity.this);
		dialog.setTitle("Join");
		dialog.setMessage("방 제목을 입력하세요");
		final EditText editText = new EditText(getApplicationContext());
		editText.setTextColor(Color.parseColor("#000000"));
		//editText.setLinkTextColor(Color.parseColor("#000000"));
		//editText.setBackgroundColor(Color.parseColor("#000000"));

		//LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		//View layout = inflater.inflate(R.layout.content_join_dialog, (ViewGroup) findViewById(R.id.joinDialog));
		dialog.setView(editText);

		dialog.setPositiveButton("Ok", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ServerRequest sr = new ServerRequest();
				params = new ArrayList<NameValuePair>();
				String title = editText.getText().toString();
				params.add(new BasicNameValuePair("email", email));
				params.add(new BasicNameValuePair("name", title));

				JSONObject json = sr.getJSON("http://168.188.123.218:8080/getalarmdata", params);
				if (json != null) {
					try {
						Alarm alarm = new Alarm();
						String jsonstr = json.getString("response");
						JSONObject a = json.getJSONObject("response");
						String name = a.getString("name");
						String tone = a.getString("tone");
						String time = a.getString("time");
						boolean vibrate = a.getBoolean("vibrate");
						int difficulty = a.getInt("difficulty");
						JSONArray day = a.getJSONArray("days");

						Log.v("서버에서 가져온 알람의 name", "" + name);
						Log.v("서버에서 가져온 알람의 time", "" + time);
						Log.v("서버에서 가져온 알람의 tone", "" + tone);
						Log.v("서버에서 가져온 알람의 vibrate", "" + vibrate);
						Log.v("서버에서 가져온 알람의 difficulty", "" + difficulty);
						Log.v("서버에서 가져온 알람의 days", "" + day);

						String[] days = new String[day.length()];
						for(int i=0; i < day.length(); i++){
							days[i] = day.optString(i);
						}
						Log.v("서버에서 가져온 알람의 day", "" + days);

						Database.create(name,time,tone,difficulty,vibrate,days);
						//setAlarmUsingDB();

						SharedPreferences.Editor edit = pref.edit();
						//Storing Data using SharedPreferences

						edit.commit();
						Intent intent = new Intent(getApplicationContext(), AlarmActivity.class);
						intent.putExtra("user", user);
						intent.putExtra("email", email);

						startActivity(intent);
						finish();

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				//Toast.makeText(getApplicationContext(), value, Toast.LENGTH_LONG).show();
			}
		});

		dialog.setNegativeButton("Cancel", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	protected void setAlarmUsingDB(){
		String time = "13:43";
		int difficulty = 1;
		String tone = "content://settings/system/alarm_alert";
		boolean vibrate = true;
		String name = "mp";
		String[] days = {"MONDAY"};
		Database.create(name,time,tone,difficulty,vibrate,days);
	}
}