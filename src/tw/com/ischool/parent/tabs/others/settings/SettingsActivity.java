package tw.com.ischool.parent.tabs.others.settings;

import ischool.dsa.client.OnReceiveListener;
import ischool.dsa.utility.DSResponse;
import ischool.dsa.utility.XmlUtil;
import ischool.dsa.utility.http.Cancelable;

import java.util.List;

import org.w3c.dom.Element;

import tw.com.ischool.account.login.Accessable;
import tw.com.ischool.parent.ChildInfo;
import tw.com.ischool.parent.Children;
import tw.com.ischool.parent.ChildrenHelper;
import tw.com.ischool.parent.Parent;
import tw.com.ischool.parent.R;
import tw.com.ischool.parent.SchoolInfo;
import tw.com.ischool.parent.util.PreferenceHelper;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {

	private static final int CODE_SWITCH_ACCOUNT = 801;
	
	private LinearLayout mBtnSyncAccount;
	private TextView mTxtLoginAccount;
	private Button mBtnSwitchAccount;
	private LinearLayout mLayoutChildren;

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
		
		mLayoutChildren = (LinearLayout) this.findViewById(R.id.layoutChildren);
		bindChildren();
	}

	private void bindChildren() {
		LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		int index = 0;
		for(ChildInfo child : Parent.getChildren().getChildren()){
			View view = inflater.inflate(R.layout.item_setting_children, mLayoutChildren, false);
			TextView txtChildName = (TextView)view.findViewById(R.id.txtChildName);
			txtChildName.setText(child.getStudentName());
			
			Button btnRemoveChild = (Button) view.findViewById(R.id.btnRemoveChild);
			btnRemoveChild.setTag(child);
			
			btnRemoveChild.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					ChildInfo child = (ChildInfo)v.getTag();
					removeChild(v, child);
				}
			});
			
			// 偷跑, 載入各校資料
			child.getSchoolInfo(this, new OnReceiveListener<SchoolInfo>() {
				
				@Override
				public void onReceive(SchoolInfo result) {					
				}
				
				@Override
				public void onError(Exception ex) {
										
				}
			});
			
			view.setTag(child);
			mLayoutChildren.addView(view, index,new  ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			
			index++;
		}
		
		mLayoutChildren.invalidate();
	}

	private void removeChild(final View button, final ChildInfo child) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.confirm));
		
		String message = getString(R.string.settings_remove_child_message);
		message = String.format(message, child.getStudentName());
		builder.setMessage(message);
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				//從 server 移除
				Element request = XmlUtil.createElement("Request");
				Element sp = XmlUtil.addElement(request, "StudentParent");
				XmlUtil.addElement(sp, "StudentID", child.getStudentId());
				
				child.callParentService(Parent.SERVICE_REMOVE_CHILD, request, new OnReceiveListener<DSResponse>() {
					
					@Override
					public void onReceive(DSResponse result) {
						Children children = Parent.getChildren();
						//從小孩清單移除
						children.getChildren().remove(child);
						
						//從快取中移除					
						PreferenceHelper.cacheChildren(SettingsActivity.this, children);
						
						//從畫面移除
						mLayoutChildren.removeView((ViewGroup)button.getParent());
									
					}
					
					@Override
					public void onError(Exception ex) {
						String text = getString(R.string.settings_remove_child_error);
						Toast.makeText(SettingsActivity.this, text, Toast.LENGTH_LONG).show();
					}
				}, new Cancelable());
				
				
				
			}
			
		});
		
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}			
		});
		
		builder.show();
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
