package tw.com.ischool.parent.tabs.others.settings;

import ischool.dsa.client.OnReceiveListener;

import java.util.List;

import tw.com.ischool.account.login.Accessable;
import tw.com.ischool.parent.Children;
import tw.com.ischool.parent.ChildrenHelper;
import tw.com.ischool.parent.Parent;
import tw.com.ischool.parent.R;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SettingsActivity extends Activity {

	private static final int CODE_SWITCH_ACCOUNT = 801;
	
	private LinearLayout mBtnSyncAccount;
	private TextView mTxtLoginAccount;
	private Button mBtnSwitchAccount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		mTxtLoginAccount = (TextView) this.findViewById(R.id.txtLoginName);
		mTxtLoginAccount
				.setText(Parent.getConnectionHelper().getAccount().name);

		mBtnSyncAccount = (LinearLayout) this.findViewById(R.id.layoutBtnSync);
		mBtnSyncAccount.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				syncAccount();
			}
		});

		mBtnSwitchAccount = (Button) findViewById(R.id.btnSwitchAccount);
		mBtnSwitchAccount.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				switchAccount();				
			}
		});
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
			finish();
			break;
		}

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == CODE_SWITCH_ACCOUNT && resultCode == RESULT_OK){
			String name = data.getStringExtra(SwitchAccountActivity.PARAM_ACCOUNT_NAME);
			String type = data.getStringExtra(SwitchAccountActivity.PARAM_ACCOUNT_TYPE);
			switchAccount(name,type);
		}
	}
	


	private void syncAccount() {
		final ProgressBar progress = (ProgressBar) findViewById(R.id.progressSync);
		final TextView txtSync = (TextView) findViewById(R.id.txtBtnSync);

		progress.setVisibility(View.VISIBLE);
		txtSync.setText(getString(R.string.settings_sync_ing));

		Parent.getConnectionHelper().getAccessables(this,
				new OnReceiveListener<List<Accessable>>() {
					@Override
					public void onReceive(List<Accessable> result) {
						ChildrenHelper childHelper = new ChildrenHelper(
								SettingsActivity.this, Parent
										.getConnectionHelper(), result);
						childHelper
								.getChildren(new OnReceiveListener<Children>() {

									@Override
									public void onReceive(Children result) {
										Parent.getChildren().clear();
										Parent.getChildren()
												.addChildren(result);

										progress.setVisibility(View.GONE);
										txtSync.setText(getString(R.string.settings_sync_account));
									}

									@Override
									public void onError(Exception ex) {
										progress.setVisibility(View.GONE);
										txtSync.setText(getString(R.string.settings_sync_account));
									}
								});
					}

					@Override
					public void onError(Exception ex) {
						// TODO Auto-generated method stub

					}
				});
	}
	
	private void switchAccount(){
		Intent intent = new Intent(this, SwitchAccountActivity.class);
		startActivityForResult(intent, CODE_SWITCH_ACCOUNT);
	}
	
	private void switchAccount(String name, String type) {		
		
		Intent i = new Intent(SwitchAccountActivity.BROADCAST_SWITCH_ACCOUNT);
		i.putExtra(SwitchAccountActivity.PARAM_ACCOUNT_NAME, name);
		i.putExtra(SwitchAccountActivity.PARAM_ACCOUNT_TYPE, type);
		
		sendBroadcast(i);
		
		finish();
	}
}
