package tw.com.ischool.parent.addChild;

import ischool.dsa.client.ContractConnection;
import ischool.dsa.client.OnReceiveListener;
import ischool.dsa.exception.DSAServerException;
import ischool.dsa.utility.DSRequest;
import ischool.dsa.utility.DSResponse;
import ischool.dsa.utility.XmlUtil;
import ischool.dsa.utility.http.Cancelable;
import ischool.utilities.StringUtil;

import org.w3c.dom.Element;

import tw.com.ischool.parent.MainActivity;
import tw.com.ischool.parent.Parent;
import tw.com.ischool.parent.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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
					setError("不合法的 QRCode");
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
			if(resultCode == RESULT_OK) {
				setResult(RESULT_CHILD_ADDED);
				finish();
			}
		}
	}

	private void addChild(final String dsns, final String code) {
		setMessage("連結學校中...");
		
		ContractConnection greening = MainActivity.getConnectionHelper()
				.getGreening();
		greening.connectAnotherByPassportAsync(dsns, Parent.CONTRACT_JOIN,
				true, new OnReceiveListener<ContractConnection>() {

					@Override
					public void onReceive(ContractConnection result) {
						setMessage("加入家長中...");
						joinParent(result, dsns, code, null);
					}

					@Override
					public void onError(Exception ex) {
						setError("連結學校時發生錯誤 : " + ex.getMessage());						
					}
				});

	}

	private void joinParent(ContractConnection connection, final String dsns,
			String code, String relationship) {
		if (StringUtil.isNullOrWhitespace(relationship))
			relationship = "家長";

		DSRequest request = new DSRequest();
		Element content = XmlUtil.createElement("Request");
		XmlUtil.addElement(content, "ParentCode", code);
		XmlUtil.addElement(content, "Relationship", relationship);
		request.setContent(content);

		connection.sendAsyncRequest(Parent.SERVICE_JOIN_PARENT, request,
				new OnReceiveListener<DSResponse>() {

					@Override
					public void onReceive(DSResponse result) {
						setMessage("加入家長完成");
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
		setMessage("儲存個人關聯學校中...");
		
		DSRequest request = new DSRequest();
		Element content = XmlUtil.createElement("Request");
		Element applications = XmlUtil.addElement(content, "Applications");
		Element application = XmlUtil.addElement(applications, "Application");
		XmlUtil.addElement(application, "AccessPoint", dsns);
		XmlUtil.addElement(application, "Type", "dynpkg");
		request.setContent(content);

		ContractConnection greening = MainActivity.getConnectionHelper()
				.getGreening();
		greening.sendAsyncRequest(Parent.SERVICE_ADD_APPLICATION_REF, request,
				new OnReceiveListener<DSResponse>() {

					@Override
					public void onReceive(DSResponse result) {
						setResult(RESULT_CHILD_ADDED);
						finish();
					}

					@Override
					public void onError(Exception ex) {
						// 這裡要
						if(ex instanceof DSAServerException){
							DSAServerException dsaEx = (DSAServerException) ex;
							if(dsaEx.getStatusMessage().contains("duplicate key value violates unique constraint")){
								setResult(RESULT_CHILD_ADDED);
								finish();
								return;
							}								
						}
						
						//setResult(RESULT_CHILD_ADDED);
						setError("儲存個人關聯學校時發生錯誤");
						//finish();
					}
				}, new Cancelable());
	}
	
	private void setMessage(String message){
		mTxtMesg.setTextColor(Color.BLACK);
		mTxtMesg.setText(message);		
	}
	
	private void setError(String message){
		mTxtMesg.setTextColor(Color.RED);
		mTxtMesg.setText(message);		
	}
}
