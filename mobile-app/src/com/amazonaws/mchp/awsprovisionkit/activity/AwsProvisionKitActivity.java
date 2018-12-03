package com.amazonaws.mchp.awsprovisionkit.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;


import com.amazonaws.mchp.awsprovisionkit.R;
import com.amazonaws.mchp.awsprovisionkit.model.AwsRouter;
import com.amazonaws.mchp.awsprovisionkit.model.infoContainer;
import com.amazonaws.mchp.awsprovisionkit.model.itemInfo;
import com.amazonaws.mchp.awsprovisionkit.service.AwsService;
import com.amazonaws.mchp.awsprovisionkit.task.json.AwsJsonMsg;
import com.amazonaws.mchp.awsprovisionkit.task.json.AwsShadowJsonMsg;
import com.amazonaws.mchp.awsprovisionkit.task.ui.VerticalSwipeRefreshLayout;
import com.amazonaws.mchp.awsprovisionkit.utils.AppHelper;
import com.amazonaws.mchp.awsprovisionkit.utils.ServiceConstant;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;

import java.util.ArrayList;

@SuppressLint("HandlerLeak")
public class AwsProvisionKitActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, Spinner.OnItemSelectedListener {

	static final String LOG_TAG = AwsProvisionKitActivity.class.getCanonicalName();

	/**
	 * cuase time is tight ,  hard code for demo .
	 */


	private static final String CMD_CTRL_LED1 = "LED_1";

	//user define
	private static final int CMD_GET_DEV_STATUS =0x0;
    private static final int CMD_UPDATE_UI =0x1;
	private static final int PULL_TO_REFRESH = 0x2;
	private static final int CMD_CTRL_COLOR = 0x3;
	private static final int CMD_CTRL_DEV =0x1000;



	private VerticalSwipeRefreshLayout mSwipeLayout;
	private CheckBox cbLed1, cbLed2, cbLed3;
	private RadioButton rbButton1, rbButton2, rbButton3;
	private NavigationView nDrawer;
	private DrawerLayout mDrawer;
	private ActionBarDrawerToggle mDrawerToggle;
	private Toolbar toolbar;
	private	Spinner led1_spinner;
	private TextView tvThingName;

	private CognitoUser user;
	private String username;

	private String thing_name;
	private String deviceName;

	private boolean requestGetDevState = false;

	awsProvisionKitReceiver receiver;

	enum updateType {
		UNKNOWN, LED, BUTTON
	}


	Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case CMD_CTRL_DEV:
					//Toast.makeText(wifiSensorBoardControlActivity.this, "ctrl "+msg.obj.toString() + " " + msg.arg1, Toast.LENGTH_SHORT).show();

					break;

				case CMD_GET_DEV_STATUS:


					break;
				case PULL_TO_REFRESH:
					mSwipeLayout.setRefreshing(false);

					break;
				case CMD_CTRL_COLOR:

					break;
                case CMD_UPDATE_UI:
                    break;
			}
		}
		;
	};




	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_kit);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		username = AppHelper.getCurrUser();
		user = AppHelper.getPool().getUser(username);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
		thing_name = extras.getString(ServiceConstant.ThingName);
		deviceName = extras.getString(ServiceConstant.DevName);

		initDevice();
		initView();

		receiver = new awsProvisionKitReceiver();
		handler.sendEmptyMessage(CMD_GET_DEV_STATUS);
	}

	private void initDevice() {
		Intent intent = getIntent();

	}

	// Perform the action for the selected navigation item
	private void performAction(MenuItem item) {
		// Close the navigation drawer
		Log.d(LOG_TAG, "Debug 7777777 item="+item.getItemId());
		mDrawer.closeDrawers();

		// Find which item was selected
		switch(item.getItemId()) {
			case R.id.nav_user_sign_out:
				// Start sign-out
				user.signOut();

				Intent intent = new Intent();
				if(username == null)
					username = "";
				intent.putExtra("name",username);
				setResult(RESULT_OK, intent);

				//unsubscribe the channel
				Intent subscribe_intent = new Intent(AwsProvisionKitActivity.this, AwsService.class);
				subscribe_intent.putExtra(ServiceConstant.AWSThingName,thing_name);
				subscribe_intent.setAction(ServiceConstant.JSONMsgUnSubscribeShadowDelta);
				startService(subscribe_intent);

				subscribe_intent.putExtra(ServiceConstant.AWSThingName,thing_name);
				subscribe_intent.setAction(ServiceConstant.JSONMsgUnSubscribeShadowUpdate);
				startService(subscribe_intent);


				finish();

				Intent activity_intent = new Intent(AwsProvisionKitActivity.this, MainActivity.class);
				startActivity(activity_intent);

				break;
			case R.id.nav_about:
				// For the inquisitive
				//Intent aboutAppActivity = new Intent(this, AboutApp.class);
				//startActivity(aboutAppActivity);
				break;

		}
	}

	// Private methods
	// Handle when the a navigation item is selected
	private void setNavDrawer() {
		nDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(MenuItem item) {
				performAction(item);
				return true;
			}
		});
	}

	private void initView() {

		tvThingName = (TextView) findViewById(R.id.thing_name);
		tvThingName.setText(thing_name.substring(0,18)+"...");



		mSwipeLayout = (VerticalSwipeRefreshLayout) findViewById(R.id.id_swipe_aws_provision_kit);
		mSwipeLayout.setOnRefreshListener(this);
		mSwipeLayout.setVisibility(View.VISIBLE);
		// Set toolbar for this screen
		toolbar = (Toolbar) findViewById(R.id.main_toolbar);
		toolbar.setTitle("");
		TextView main_title = (TextView) findViewById(R.id.main_toolbar_title);
		main_title.setText(deviceName);
		setSupportActionBar(toolbar);

		// Set navigation drawer for this screen
		mDrawer = (DrawerLayout) findViewById(R.id.device_kit_drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this,mDrawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
		mDrawer.addDrawerListener(mDrawerToggle);
		mDrawerToggle.syncState();
		nDrawer = (NavigationView) findViewById(R.id.nav_gatewaylist_view);
		setNavDrawer();

		View navigationHeader = nDrawer.getHeaderView(0);
		TextView navHeaderSubTitle = (TextView) navigationHeader.findViewById(R.id.textViewNavUserSub);
		navHeaderSubTitle.setText(username);

		cbLed1 = (CheckBox) findViewById(R.id.led1);
		cbLed2 = (CheckBox) findViewById(R.id.led2);
		cbLed3 = (CheckBox) findViewById(R.id.led3);

		rbButton1 = (RadioButton) findViewById(R.id.button1);
		rbButton2 = (RadioButton) findViewById(R.id.button2);
		rbButton3 = (RadioButton) findViewById(R.id.button3);


	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// Another interface callback
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view,
							   int pos, long id) {

		if (requestGetDevState == false) {
			Log.e(LOG_TAG, "----------------->onItemSelected In, pos = " + pos);
			Message message = new Message();
			message.what = CMD_CTRL_COLOR;
			switch (parent.getId()) {
				case R.id.led_spinner:
					message.obj = CMD_CTRL_LED1;
					message.arg1 = pos;
					break;
			}

			handler.sendMessage(message);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(LOG_TAG, "onResume In");

		IntentFilter filter = new IntentFilter(ServiceConstant.CloudStatus);
		filter.addAction(ServiceConstant.JSONShadowMsgReport);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		registerReceiver(receiver, filter);

		Intent subscribe_intent = new Intent(AwsProvisionKitActivity.this, AwsService.class);
		subscribe_intent.putExtra(ServiceConstant.AWSThingName,thing_name);
		subscribe_intent.setAction(ServiceConstant.JSONMsgSubscribeShadowDelta);
		startService(subscribe_intent);

		subscribe_intent.putExtra(ServiceConstant.AWSThingName,thing_name);
		subscribe_intent.setAction(ServiceConstant.JSONMsgSubscribeShadowUpdate);
		startService(subscribe_intent);


		onRefresh();

	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
		requestGetDevState = false;
		handler.removeCallbacks(publishPublishGetCmd);

		//unsubscribe the channel
		Intent subscribe_intent = new Intent(AwsProvisionKitActivity.this, AwsService.class);
		subscribe_intent.putExtra(ServiceConstant.AWSThingName,thing_name);
		subscribe_intent.setAction(ServiceConstant.JSONMsgUnSubscribeShadowDelta);
		startService(subscribe_intent);

		subscribe_intent.putExtra(ServiceConstant.AWSThingName,thing_name);
		subscribe_intent.setAction(ServiceConstant.JSONMsgUnSubscribeShadowUpdate);
		startService(subscribe_intent);


		// TODO GosMessageHandler.getSingleInstance().SetHandler(null);

	}




	public void onCheckboxClicked(View view) {
		// Is the view now checked?
		boolean checked = ((CheckBox) view).isChecked();

		AwsShadowJsonMsg json_shadow_msg = new AwsShadowJsonMsg();
		itemInfo item_obj = new itemInfo();
		// Check which checkbox was clicked
		switch(view.getId()) {
			case R.id.led1:
				item_obj.item = "led1";
				if (checked)
					item_obj.value = "on";
				else
					item_obj.value = "off";
				break;
			case R.id.led2:
				item_obj.item = "led2";
				if (checked)
					item_obj.value = "on";
            	else
					item_obj.value = "off";
				break;
			case R.id.led3:
				item_obj.item = "led3";
				if (checked)
					item_obj.value = "on";
            	else
					item_obj.value = "off";
				break;
		}
		json_shadow_msg.desire_info.add(item_obj);
		String msg = json_shadow_msg.generateJsonMsg(json_shadow_msg.AWS_JSON_COMMAND_GENERATE_DESIRE_MSG);

		Intent subscribe_intent = new Intent(AwsProvisionKitActivity.this, AwsService.class);
		subscribe_intent.putExtra(ServiceConstant.AWSThingName,thing_name);
		subscribe_intent.putExtra(ServiceConstant.JSONMsgObject,msg);
		subscribe_intent.setAction(ServiceConstant.JSONMsgShadowUpdate);
		startService(subscribe_intent);
	}



	//@Override


	public void onRefresh() {
		handler.sendEmptyMessageDelayed(PULL_TO_REFRESH, 2000);
		//updateUI();
		requestGetDevState = true;
		handler.post(publishPublishGetCmd);

	}

	private Runnable publishPublishGetCmd= new Runnable() {
		public void run() {

			Intent subscribe_intent = new Intent(AwsProvisionKitActivity.this, AwsService.class);
			subscribe_intent.putExtra(ServiceConstant.AWSThingName,thing_name);
			subscribe_intent.setAction(ServiceConstant.JSONMsgShadowGet);
			startService(subscribe_intent);


		}
	};


	public void shadowGetUpdateUI(ArrayList<itemInfo> shadow_msg)
	{
		updateType update_option;
		RadioButton cur_radio_btn = rbButton1;
		CheckBox cur_check_box = cbLed1;

		for (int i=0; i<shadow_msg.size(); i++){
			Log.d(LOG_TAG, "Debug:" + shadow_msg.get(i).item + "=" + shadow_msg.get(i).value);



			switch (shadow_msg.get(i).item.toString())
			{
				case "led1":
					cur_check_box = cbLed1;
					update_option = updateType.LED;
					break;
				case "led2":
					cur_check_box = cbLed2;
					update_option = updateType.LED;
					break;
				case "led3":
					cur_check_box = cbLed3;
					update_option = updateType.LED;
					break;
				case "button1":
					cur_radio_btn = rbButton1;
					update_option = updateType.BUTTON;
					break;
				case "button2":
					cur_radio_btn = rbButton2;
					update_option = updateType.BUTTON;
					break;
				case "button3":
					cur_radio_btn = rbButton3;
					update_option = updateType.BUTTON;
					break;
				default:
					update_option = updateType.UNKNOWN;
					break;
			}
			if (update_option == updateType.LED) {
				if (shadow_msg.get(i).value.equals("on"))
					cur_check_box.setChecked(true);
				else
					cur_check_box.setChecked(false);
			}
			else if (update_option == updateType.BUTTON) {
				if (shadow_msg.get(i).value.equals("down"))
					cur_radio_btn.setChecked(true);
				else
					cur_radio_btn.setChecked(false);
			}
		}
	}
	private class awsProvisionKitReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(ServiceConstant.CloudStatus)) {
				String connMessage = intent.getStringExtra(ServiceConstant.CloudStatusConn);
				if (connMessage.equals("Connected")) {
				}
			}
			else if (intent.getAction().equals(ServiceConstant.JSONShadowMsgReport)) {
				AwsShadowJsonMsg jsonShadowMsgObj = intent.getParcelableExtra(ServiceConstant.JSONShadowMsgObject);
				if (jsonShadowMsgObj != null) {
					Log.d(LOG_TAG, "Receive AWS Shadow JSON message");
					jsonShadowMsgObj.printDebugLog();
					ArrayList<itemInfo> report_info_shadow = jsonShadowMsgObj.getReportInfo();
					shadowGetUpdateUI(report_info_shadow);

					ArrayList<itemInfo> desire_info_shadow = jsonShadowMsgObj.getDesireInfo();
					shadowGetUpdateUI(desire_info_shadow);

					String topic = intent.getStringExtra(ServiceConstant.MQTTChannelName);
					Log.d(LOG_TAG, "Topic:" + topic);
					String[] split = topic.split("/");

					AwsRouter
							router = new AwsRouter();

					router.setDeviceName("AWS Zero Touch Kit");

					router.setMacAddr(split[2]);
					router.setDevType(split[2]);
					router.setThingName(split[2]);
					
				}
			}
		}
	}
}
