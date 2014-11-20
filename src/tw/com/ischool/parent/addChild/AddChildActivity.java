package tw.com.ischool.parent.addChild;

import java.util.List;

import ischool.dsa.client.ContractConnection;
import ischool.dsa.client.OnReceiveListener;
import ischool.dsa.exception.DSAServerException;
import ischool.dsa.utility.DSRequest;
import ischool.dsa.utility.DSResponse;
import ischool.dsa.utility.XmlUtil;
import ischool.dsa.utility.http.Cancelable;
import ischool.utilities.StringUtil;

import org.w3c.dom.Element;

import tw.com.ischool.account.login.Accessable;
import tw.com.ischool.parent.Children;
import tw.com.ischool.parent.ChildrenHelper;
import tw.com.ischool.parent.Parent;
import tw.com.ischool.parent.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AddChildActivity extends Activity {

	private static final int CODE_SCAN = 0;

	public static final int RESULT_CHILD_ADDED = -100;

	private Button mBtnCode;
	private Button mBtnScan;
	private TextView mTxtMesg;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_child);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		mTxtMesg = (TextView) this.findViewById(R.id.txtMessage);
		mBtnCode = (Button) this.findViewById(R.id.btnCode);
		mBtnScan = (Button) this.findViewById(R.id.btnScan);

		mBtnCode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mTxtMesg.setText(StringUtil.EMPTY);
				Intent intent = new Intent(AddChildActivity.this,
						ParentCodeActivity.class);
				startActivityForResult(intent,
						ParentCodeActivity.REQUEST_CODE_PARENT_CODE);
			}
		});

		mBtnScan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				try {
					mTxtMesg.setText(StringUtil.EMPTY);
					Intent intent = new Intent(
							"com.google.zxing.client.android.SCAN");
					intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
					intent.putExtra("SAVE_HISTORY", false);
					startActivityForResult(intent, CODE_SCAN);
				} catch (Exception ex) {
					// 如果發生錯誤，則表示應該還未安裝 ZXing 程式，幫他導到 market 下載吧
					Uri marketUri = Uri
							.parse("market://details?id=com.google.zxing.client.android");
					Intent marketIntent = new Intent(Intent.ACTION_VIEW,
							marketUri);
					startActivity(marketIntent);
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CODE_SCAN) {
			if (resultCode == RESULT_OK) {
				// content sample : EA47FF@test.b.nehs.hc.edu.tw
				String contents = data.getStringExtra("SCAN_RESULT");
				if (!contents.contains("@")) {
					setError(R.string.add_child_error_invalid_qrcode);
					return;
				}
				String[] array = contents.split("@");
				String code = array[0];
				String dsns = array[1];

				addChild(dsns, code);
			} else if (resultCode == RESULT_CANCELED) {

			}
		} else if (requestCode == ParentCodeActivity.REQUEST_CODE_PARENT_CODE){
			//String code = data.getStringExtra(ParentCodeActivity.PARAM_)
			//TODO
			if(resultCode == RESULT_OK) {
				syncAccessable();
			}
		}
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
	
	private void addChild(final String dsns, final String code) {
		setMessage(R.string.add_child_connect_accessable);
		
		ContractConnection greening = Parent.getConnectionHelper()
				.getGreening();
		greening.connectAnotherByPassportAsync(dsns, Parent.CONTRACT_JOIN,
				true, new OnReceiveListener<ContractConnection>() {

					@Override
					public void onReceive(ContractConnection result) {
						setMessage(R.string.add_child_add_parent);
						joinParent(result, dsns, code, null);
					}

					@Override
					public void onError(Exception ex) {
						setError(R.string.add_child_error_connect_accessable);						
					}
				});

	}

	private void joinParent(ContractConnection connection, final String dsns,
			String code, String relationship) {
		if (StringUtil.isNullOrWhitespace(relationship))
			relationship = getString(R.string.add_child_default_relation);

		DSRequest request = new DSRequest();
		Element content = XmlUtil.createElement("Request");
		XmlUtil.addElement(content, "ParentCode", code);
		XmlUtil.addElement(content, "Relationship", relationship);
		request.setContent(content);

		connection.sendAsyncRequest(Parent.SERVICE_JOIN_PARENT, request,
				new OnReceiveListener<DSResponse>() {

					@Override
					public void onReceive(DSResponse result) {
						setMessage(R.string.add_child_add_parent_completed);
						addApplicationRef(dsns);
					}

					@Override
					public void onError(Exception ex) {
						mTxtMesg.setTextColor(Color.RED);
						mTxtMesg.setText(ex.getMessage());							
					}
				}, new Cancelable());
	}

	private void addApplicationRef(String dsns) {		
		
		//如果該校已經存在的話應該就不用呼叫了
		boolean contains = false;
		for(Accessable a : Parent.getAccessables()){
			if(a.getAccessPoint().equals(dsns)){
				contains = true;
				break;
			}
		}
		
		if(contains){
			syncChildren();
			return;
		}
		
		setMessage(R.string.add_child_save_accessable);
		DSRequest request = new DSRequest();
		Element content = XmlUtil.createElement("Request");
		Element applications = XmlUtil.addElement(content, "Applications");
		Element application = XmlUtil.addElement(applications, "Application");
		XmlUtil.addElement(application, "AccessPoint", dsns);
		XmlUtil.addElement(application, "Type", "dynpkg");
		request.setContent(content);

		ContractConnection greening = Parent.getConnectionHelper()
				.getGreening();
		greening.sendAsyncRequest(Parent.SERVICE_ADD_APPLICATION_REF, request,
				new OnReceiveListener<DSResponse>() {

					@Override
					public void onReceive(DSResponse result) {
						syncAccessable();
					}

					@Override
					public void onError(Exception ex) {
						// 這裡要
						if(ex instanceof DSAServerException){
							DSAServerException dsaEx = (DSAServerException) ex;
							if(dsaEx.getStatusMessage().contains("duplicate key value violates unique constraint")){
								syncChildren();
								return;
							}								
						}
						
						//setResult(RESULT_CHILD_ADDED);
						setError(R.string.add_child_error_save_accessable);
						//finish();
					}
				}, new Cancelable());
	}
	
	private void setMessage(int stringId){
		mTxtMesg.setTextColor(Color.BLACK);
		mTxtMesg.setText(stringId);		
	}
	
	private void setError(int message){
		mTxtMesg.setTextColor(Color.RED);
		mTxtMesg.setText(message);		
	}
	
	private void syncAccessable(){
		setMessage(R.string.add_child_sync_accessable);
		Parent.getConnectionHelper().getAccessables(this, new OnReceiveListener<List<Accessable>>() {
			
			@Override
			public void onReceive(List<Accessable> result) {
				syncChildren();				
			}
			
			@Override
			public void onError(Exception ex) {
				setError(R.string.add_child_error_sync_accessable);				
			}
		});
	}
	
	private void syncChildren(){
		setMessage(R.string.add_child_sync_children);
		
		ChildrenHelper helper = new ChildrenHelper(this, Parent.getConnectionHelper(), Parent.getAccessables());
		helper.getChildren(new OnReceiveListener<Children>() {
			
			@Override
			public void onReceive(Children result) {
				setResult(RESULT_CHILD_ADDED);
				finish();			
			}
			
			@Override
			public void onError(Exception ex) {
				setError(R.string.add_child_error_sync_children);				
			}
		});
	}
}
