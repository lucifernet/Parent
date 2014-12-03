package tw.com.ischool.parent;

import ischool.dsa.utility.http.Cancelable;
import ischool.dsa.utility.http.HttpUtil;
import ischool.utilities.JSONUtil;
import ischool.utilities.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONObject;

import tw.com.ischool.account.login.ConnectionHelper;
import tw.com.ischool.parent.addChild.AddChildActivity;
import tw.com.ischool.parent.tabs.ITabHandler;
import tw.com.ischool.parent.tabs.message.MessageTabHandler;
import tw.com.ischool.parent.tabs.others.OthersTabHandler;
import tw.com.ischool.parent.tabs.others.settings.SwitchAccountActivity;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class MainActivity extends FragmentActivity {

	/* ---------------------- above GMS property---------------------------- */
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private static final String GCM_SERVER_URL = "http://dev.ischool.com.tw:8080/gcm/";
	/**
	 * Substitute you own sender ID here. This is the project number you got
	 * from the API Console, as described in "Getting Started."
	 */
	String SENDER_ID = "1086483844456";

	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	SharedPreferences prefs;
	Context context;

	String regid;
	/* ---------------------- uppon GMS property---------------------------- */

	public static final String TAG = "Parent";
	public static final String PARAM_EXITS = "exits";
	public static final int CODE_ADD_CHILD = 100;
	private static int INIT_FLAG = -1;

	private ViewPager mViewPager;
	private DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;
	private ArrayList<ITabHandler> mTabPages;
	private SwitchBroadcastReceiver mReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		context = this;
		
		mTabPages = new ArrayList<ITabHandler>();
		mTabPages.add(new MessageTabHandler());
		// mTabPages.add(new ContactTabHandler());
		// mTabPages.add(new CalendarTabHandler());
		mTabPages.add(new OthersTabHandler());

		for (ITabHandler tabHandler : mTabPages) {
			tabHandler.setContext(this);
		}

		// ViewPager and its adapters use support library
		// fragments, so use getSupportFragmentManager.
		mDemoCollectionPagerAdapter = new DemoCollectionPagerAdapter(
				getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setOffscreenPageLimit(mTabPages.size());
		mViewPager.setAdapter(mDemoCollectionPagerAdapter);

		final ActionBar actionBar = getActionBar();

		// Specify that tabs should be displayed in the action bar.
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create a tab listener that is called when the user changes tabs.
		ActionBar.TabListener tabListener = new ActionBar.TabListener() {

			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {

			}

			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				mViewPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {

			}

		};

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// When swiping between pages, select the
				// corresponding tab.
				getActionBar().setSelectedNavigationItem(position);

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

		// Add 3 tabs, specifying the tab's text and TabListener
		for (ITabHandler tab : mTabPages) {
			actionBar.addTab(actionBar.newTab().setText(tab.getTitle())
					.setTabListener(tabListener));
		}

		// 登入處理
		if (Parent.getConnectionHelper() == null)
			login();

		IntentFilter mFilter01 = new IntentFilter(
				SwitchAccountActivity.BROADCAST_SWITCH_ACCOUNT);
		mReceiver = new SwitchBroadcastReceiver(); // ←實作一個BroadcastReceiver來篩選
		registerReceiver(mReceiver, mFilter01);

	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		unregisterReceiver(mReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	//
	// if (item.getItemId() == R.id.action_switch_user) {
	// mLoginHelper.switchAccount(new MainLoginListener());
	// }
	//
	// return true;
	// }

	// public static ConnectionHelper getConnectionHelper() {
	// return sConnectionHelper;
	// }
	//
	// public static List<Accessable> getAccessables() {
	// if (sAccessables == null)
	// sAccessables = new ArrayList<Accessable>();
	// return sAccessables;
	// }
	//
	// public static Children getChildren() {
	// return sChildren;
	// }

	public static int getInitFlag() {
		return INIT_FLAG;
	}

	private void login() {
		login(null, null);
	}

	private void login(String name, String type) {
		Intent intent = new Intent(this, ParentLoginActivity.class);
		if (!StringUtil.isNullOrWhitespace(name)) {
			intent.putExtra(SwitchAccountActivity.PARAM_ACCOUNT_NAME, name);
			intent.putExtra(SwitchAccountActivity.PARAM_ACCOUNT_TYPE, type);
		}
		startActivityForResult(intent, ParentLoginActivity.REQUEST_CODE_LOGIN);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ParentLoginActivity.REQUEST_CODE_LOGIN) {
			if (resultCode == ParentLoginActivity.RESULT_LOGIN_OK) {

				ConnectionHelper connectionHelper = ParentLoginActivity
						.getConnectionHelper();
				
				if(data == null){
					return;
				}
				
				Children children = (Children) data
						.getSerializableExtra(ParentLoginActivity.PARAM_CHILDREN);

				Parent.setConnectionHelper(connectionHelper);
				Parent.setChildren(children);

				if (Parent.getChildren().getChildren().size() == 0) {
					Intent intent = new Intent(this, AddChildActivity.class);
					startActivityForResult(intent, CODE_ADD_CHILD);
				}

				// Check device for Play Services APK. If check succeeds,
				// proceed with
				// GCM registration.
				if (checkPlayServices()) {
					gcm = GoogleCloudMessaging.getInstance(this);
					regid = getRegistrationId(context);

					if (regid.isEmpty()) {
						registerInBackground();
					}
				} else {
					Log.i(TAG, "No valid Google Play Services APK found.");
				}

			} else if (resultCode == ParentLoginActivity.RESULT_LOGIN_FAILURE) {
				String errorMessage = data
						.getStringExtra(ParentLoginActivity.PARAM_CONNECTION_FAIL_MESSAGE);
				onDeadError(errorMessage);
			}

		}

		if (requestCode == CODE_ADD_CHILD) {
			if (resultCode == AddChildActivity.RESULT_CHILD_ADDED) {
				login();
			}
		}
	}

	private void onDeadError(String errorMessage) {
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.error_login)
				.setMessage(errorMessage)
				.setNeutralButton(R.string.close,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}

						}).show();
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it
	 * doesn't, display a dialog that allows users to download the APK from the
	 * Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences,
		// but
		// how you store the regID in your app is up to you.
		return getSharedPreferences(MainActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * Sends the registration ID to your server over HTTP, so it can use
	 * GCM/HTTP or CCS to send messages to your app. Not needed for this demo
	 * since the device sends upstream messages to a server that echoes back the
	 * message using the 'from' address in the message.
	 */
	private void sendRegistrationIdToBackend() {
		// TODO Your implementation here.
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				String url = GCM_SERVER_URL
						+ "reg?type=gcm&account=%s&token=%s";
				url = String.format(url, Parent.getConnectionHelper()
						.getAccount().name, regid);
				return HttpUtil.getString(url, new Cancelable());
			}
		}.execute();

	}

	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	public class SwitchBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent data) {
			String name = data
					.getStringExtra(SwitchAccountActivity.PARAM_ACCOUNT_NAME);
			String type = data
					.getStringExtra(SwitchAccountActivity.PARAM_ACCOUNT_TYPE);

			login(name, type);
		}

	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					regid = gcm.register(SENDER_ID);
					msg = "Device registered, registration ID=" + regid;

					// You should send the registration ID to your server over
					// HTTP,
					// so it can use GCM/HTTP or CCS to send messages to your
					// app.
					// The request to your server should be authenticated if
					// your app
					// is using accounts.
					sendRegistrationIdToBackend();

					// For this demo: we don't need to send it because the
					// device
					// will send upstream messages to a server that echo back
					// the
					// message using the 'from' address in the message.

					// Persist the regID - no need to register again.
					storeRegistrationId(context, regid);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				// mDisplay.append(msg + "\n");
			}
		}.execute(null, null, null);
	}

	// Since this is an object collection, use a FragmentStatePagerAdapter,
	// and NOT a FragmentPagerAdapter.
	public class DemoCollectionPagerAdapter extends FragmentPagerAdapter {

		// SparseArray<Fragment> registeredFragments = new
		// SparseArray<Fragment>();

		public DemoCollectionPagerAdapter(FragmentManager fm) {
			super(fm);

		}

		@Override
		public Fragment getItem(int i) {
			Log.d(TAG, "get item : " + i);
			ITabHandler tab = mTabPages.get(i);

			Fragment fragment = Fragment.instantiate(MainActivity.this,
					tab.getFragmentClassName());

			return fragment;
		}

		@Override
		public int getCount() {
			return mTabPages.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mTabPages.get(position).getTitle();
		}
	}
}
