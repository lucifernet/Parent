package tw.com.ischool.parent;

import java.util.ArrayList;
import java.util.List;

import tw.com.ischool.account.login.Accessable;
import tw.com.ischool.account.login.ConnectionHelper;
import tw.com.ischool.parent.addChild.AddChildActivity;
import tw.com.ischool.parent.addChild.ParentCodeActivity;
import tw.com.ischool.parent.tabs.ITabHandler;
import tw.com.ischool.parent.tabs.message.MessageTabHandler;
import tw.com.ischool.parent.tabs.others.OthersTabHandler;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends FragmentActivity {

	public static final String TAG = "Parent";
	public static final String PARAM_EXITS = "exits";
	public static final int CODE_ADD_CHILD = 100;
	private static int INIT_FLAG = -1;
	

	private static ConnectionHelper sConnectionHelper;
	private static List<Accessable> sAccessables;
	private static Children sChildren;

	private ViewPager mViewPager;
	private DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;
	private ArrayList<ITabHandler> mTabPages;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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
		if (sConnectionHelper == null)
			login();
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

	public static ConnectionHelper getConnectionHelper() {
		return sConnectionHelper;
	}

	public static List<Accessable> getAccessables() {
		if (sAccessables == null)
			sAccessables = new ArrayList<Accessable>();
		return sAccessables;
	}

	public static Children getChildren() {
		return sChildren;
	}

	public static int getInitFlag() {
		return INIT_FLAG;
	}

	private void login() {
		Intent intent = new Intent(this, ParentLoginActivity.class);
		startActivityForResult(intent, ParentLoginActivity.REQUEST_CODE_LOGIN);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ParentLoginActivity.REQUEST_CODE_LOGIN) {
			if (resultCode == ParentLoginActivity.RESULT_LOGIN_OK) {

				sConnectionHelper = ParentLoginActivity.getConnectionHelper();
				sChildren = (Children) data
						.getSerializableExtra(ParentLoginActivity.PARAM_CHILDREN);
				sAccessables = sConnectionHelper.getAccessables();

				if (sChildren.getChildren().size() == 0) {
					Intent intent = new Intent(this, AddChildActivity.class);
					startActivityForResult(intent, CODE_ADD_CHILD);
				}
			} else if (resultCode == ParentLoginActivity.RESULT_LOGIN_FAILURE) {
				String errorMessage = data
						.getStringExtra(ParentLoginActivity.PARAM_CONNECTION_FAIL_MESSAGE);
				onDeadError(errorMessage);
			}

		}

		if (requestCode == CODE_ADD_CHILD) {
			// TODO
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

		// @Override
		// public Object instantiateItem(ViewGroup container, int position) {
		// Fragment fragment = (Fragment) super.instantiateItem(container,
		// position);
		// registeredFragments.put(position, fragment);
		// return fragment;
		// }

		// @Override
		// public void destroyItem(ViewGroup container, int position, Object
		// object) {
		// registeredFragments.remove(position);
		// super.destroyItem(container, position, object);
		// }

		// public Fragment getRegisteredFragment(int position) {
		// return registeredFragments.get(position);
		// }

		// public SparseArray<Fragment> getRegisteredFragments() {
		// return registeredFragments;
		// }
	}

}
