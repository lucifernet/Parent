package tw.com.ischool.parent.tabs.others.settings;

import java.util.ArrayList;

import tw.com.ischool.account.login.LoginHelper;
import tw.com.ischool.parent.R;
import tw.com.ischool.parent.util.PreferenceHelper;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthUtil;

public class SwitchAccountActivity extends Activity {

	public static final String PARAM_ACCOUNT_NAME = "name";
	public static final String PARAM_ACCOUNT_TYPE = "type";
	public static final String BROADCAST_SWITCH_ACCOUNT = "switch_account";
	public static final String FACEBOOK_ACCOUNT = "com.facebook.auth.login";
	
	private ListView mListView;
	private ArrayList<Account> mAccountList;
	private AccountAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_switch_account);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		mListView = (ListView) findViewById(R.id.lvSwitchAccount);

		mAccountList = new ArrayList<Account>();

		String currentAccount = PreferenceHelper.getCacheUseAccount(this);

		AccountManager am = AccountManager.get(this);
		for (Account a : am.getAccountsByType(LoginHelper.ACCOUNT_TYPE)) {
			if (!currentAccount.equalsIgnoreCase(a.name))
				mAccountList.add(a);
		}

		// 加入 google 帳號
		for (Account a : am
				.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)) {
			if (!currentAccount.equalsIgnoreCase(a.name))
				mAccountList.add(a);
		}
		
		for (Account a : am
				.getAccountsByType(FACEBOOK_ACCOUNT)) {
			if (!currentAccount.equalsIgnoreCase(a.name))
				mAccountList.add(a);
		}

		mAdapter = new AccountAdapter(this);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int positon, long itemId) {
				Account a = mAccountList.get(positon);
				Intent data = new Intent();
				data.putExtra(PARAM_ACCOUNT_NAME, a.name);
				data.putExtra(PARAM_ACCOUNT_TYPE, a.type);
				setResult(RESULT_OK, data);
				finish();
			}
		});
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
			setResult(RESULT_CANCELED);
			finish();
			break;
		}

		return true;
	}

	private class AccountAdapter extends BaseAdapter {

		private LayoutInflater _inflater;

		AccountAdapter(Context context) {
			_inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mAccountList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mAccountList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = _inflater.inflate(R.layout.item_account, parent,
						false);

				holder = new ViewHolder();
				holder.txtAccount = (TextView) convertView
						.findViewById(R.id.txtAccount);
				holder.imgAccountType = (ImageView) convertView
						.findViewById(R.id.imgAccountType);
				convertView.setTag(holder);
			}

			Account a = mAccountList.get(position);
			if (a.type.equals(LoginHelper.ACCOUNT_TYPE)) {
				holder.imgAccountType.setImageResource(R.drawable.icon);
			} else if (a.type.equals(FACEBOOK_ACCOUNT)) {
				holder.imgAccountType.setImageResource(R.drawable.facebook);				
			} else {
				holder.imgAccountType.setImageResource(R.drawable.google);
			}
			holder.txtAccount.setText(a.name);
			return convertView;
		}
	}

	static class ViewHolder {
		public TextView txtAccount;
		public ImageView imgAccountType;
	}
}
