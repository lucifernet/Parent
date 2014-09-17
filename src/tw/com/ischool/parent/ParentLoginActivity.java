package tw.com.ischool.parent;

import ischool.dsa.client.ContractConnection;
import ischool.dsa.client.OnProgressListener;
import ischool.dsa.client.OnReceiveListener;
import ischool.dsa.utility.DSRequest;
import ischool.dsa.utility.DSResponse;
import ischool.dsa.utility.XmlUtil;
import ischool.dsa.utility.http.Cancelable;
import ischool.utilities.JSONUtil;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.w3c.dom.Element;

import tw.com.ischool.account.login.Accessable;
import tw.com.ischool.account.login.ConnectionData;
import tw.com.ischool.account.login.ConnectionHelper;
import tw.com.ischool.account.login.ExchangeGreeningTokenTask;
import tw.com.ischool.account.login.GoogleAuthenticateTask;
import tw.com.ischool.account.login.GoogleAuthenticateTask.GoogleOAuthTokenListener;
import tw.com.ischool.account.login.GreeningConnectionTask;
import tw.com.ischool.account.login.LoginHelper;
import tw.com.ischool.account.login.RefreshGreeningTokenTask;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthUtil;

public class ParentLoginActivity extends AccountAuthenticatorActivity {

	public static final int RESULT_LOGIN_FAILURE = -100;
	public static final int RESULT_LOGIN_OK = 0;

	public static final int REQUEST_CODE_GOOGLE_OAUTH = 1001;
	public static final int REQUEST_CODE_LOGIN = 999;

	//public static final String PARAM_CONNECTION_DATA = "ConnectionData";	
	public static final String PARAM_CONNECTION_FAIL_MESSAGE = "FailMessage";
	public static final String PARAM_CONNECTION_FAIL_DETAIL = "FailDetail";
	public static final String PARAM_CHILDREN = "Children";
	
	private static ConnectionHelper sConnectionHelper;

	private AccountManager mAccountManager;
	private int mChildResponseCount = 0;
	private int mTotalResponseCount = 0;
	private Children mChildren;
	private TextView mProgressMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parent_login);

		getActionBar().hide();
		mProgressMessage = (TextView) findViewById(R.id.txtLoginMessage);
		
		login();
	}

	private void login() {
		// 檢查 mobile 裝置內是否有 ischool 帳號
		mAccountManager = AccountManager.get(this);

		final Account[] ischoolAccounts = mAccountManager
				.getAccountsByType(LoginHelper.ACCOUNT_TYPE);


		// 無 ischool 帳號
		if (ischoolAccounts.length == 1) {
			Account account = ischoolAccounts[0];
			mProgressMessage.setText("使用 ischool 帳戶「 " + account.name
					+ " 」登入.");

			loginIschoolAccount(account);
		} else if (ischoolAccounts.length == 0) {
			final Account[] googleAccounts = mAccountManager
					.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);

			//TODO 先只抓第一筆 google 帳號出來, 實際上應該要讓使用者選擇
			if (googleAccounts.length >= 1) {
				final Account googleAccount = googleAccounts[0];
				exchangeGreeningTokenWithGoogleAccount(googleAccount.name);
			}
			
		} else {
			final AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle(getString(R.string.login_dialog_title));
			adb.setCancelable(false);

			ArrayList<String> names = new ArrayList<String>();
			for (Account account : ischoolAccounts) {
				names.add(account.name);
			}

			String[] items = new String[names.size()];
			items = names.toArray(items);

			// final AlertDialog mDialog;
			adb.setSingleChoiceItems(items, 0, new OnClickListener() {

				@Override
				public void onClick(DialogInterface d, int n) {
					d.dismiss();

					mProgressMessage.setText("ischool 登入認證.");

					Account ischoolAccount = ischoolAccounts[n];
					exchangeGreeningTokenWithGoogleAccount(ischoolAccount.name);
				}
			});

			adb.show();
		}
	}

	// 取出 ischool refresh token 登入 greening, 交換新的 access_token
	private void loginIschoolAccount(final Account ischoolAccount) {
		// 取出 ischool refresh token 登入 greening, 交換新的 access_token
		String refreshToken = mAccountManager.getUserData(ischoolAccount,
				AccountManager.KEY_AUTHTOKEN);

		RefreshGreeningTokenTask task = new RefreshGreeningTokenTask(this,
				mAccountManager, ischoolAccount, new OnProgressListener<ConnectionData>() {
					
					@Override
					public void onReceive(ConnectionData result) {
						getAccessables(result);
					}
					
					@Override
					public void onError(Exception ex) {
						// 這裡應該是用 GOOGLE 帳號登入, 但在目前的情境下幾乎都是 googleAccount =
						// ischoolAccount
						exchangeGreeningTokenWithGoogleAccount(ischoolAccount.name);
					}
					
					@Override
					public void onProgressUpdate(String message) {
						mProgressMessage.setText(message);
					}
				});
		task.execute(refreshToken);
	}

	// private void prepareAccessable(final ConnectionHelper connectionHelper) {
	// mProgressDialog.setMessage("取得可連結學校...");
	// connectionHelper
	// .getAccessables(new OnReceiveListener<List<Accessable>>() {
	//
	// @Override
	// public void onReceive(List<Accessable> result) {
	// sConnectionHelper = connectionHelper;
	// setResult(RESULT_OK);
	// if (mProgressDialog != null)
	// mProgressDialog.dismiss();
	// finish();
	// }
	//
	// @Override
	// public void onError(Exception ex) {
	//
	// }
	// });
	//
	// }

	// 拿 Google Account 去登入 Greening Account
	private void exchangeGreeningTokenWithGoogleAccount(
			final String googleAccountName) {

		// 先取回 google access token
		mProgressMessage.setText("取得 Google OAuth Token.");
		GoogleAuthenticateTask auth = new GoogleAuthenticateTask(this);
		auth.setListener(new GoogleOAuthTokenListener() {

			@Override
			public void onReceive(String result) {
				// 人家已經答應過了
				mProgressMessage.setText("取得 Google OAuth Token 完成.");
				exchangeGreeningToken(googleAccountName, result);
			}

			@Override
			public void onError(Exception ex) {
				onDeadError("取得 Google OAuth Token 發生錯誤.", ex);		
			}

			@Override
			public void onUserRecoverable(Intent intent) {
				// 第一次總是要人家答應的
				mProgressMessage.setText("使用者授權中.");
			
				startActivityForResult(intent, REQUEST_CODE_GOOGLE_OAUTH);
			}
		});

		auth.execute(googleAccountName);
	}

	// 拿到 Google token 後向 greening 交換 token
	private void exchangeGreeningToken(final String googleAccount,
			String googleToken) {
		mProgressMessage.setText("交換 ischool 憑證.");
		ExchangeGreeningTokenTask task = new ExchangeGreeningTokenTask(
				Parent.CLIENT_ID, Parent.CLIENT_SEC);
		task.setListener(new OnReceiveListener<String>() {

			@Override
			public void onReceive(String greeningTokenJSONString) {
				// 這邊要建立 ischool Account 把換到的 greening token 存到 Account 裡,
				// 然後交換 greening 連線
				JSONObject json = JSONUtil.parseJSON(greeningTokenJSONString);
				ConnectionData data = new ConnectionData(googleAccount, json);

				String refreshToken = JSONUtil.getString(json, "refresh_token");
				String accessToken = JSONUtil.getString(json, "access_token");

				mProgressMessage.setText("新增 ischool 帳號.");
				// Account
				Account account = new Account(googleAccount,
						LoginHelper.ACCOUNT_TYPE);

				Bundle userdata = new Bundle();
				userdata.putString(AccountManager.KEY_AUTHTOKEN, refreshToken);

				// 這邊如果該帳號已存在時會傳回 false
				if (mAccountManager.addAccountExplicitly(account, null,
						userdata)) {
					Bundle result = new Bundle();
					result.putString(AccountManager.KEY_ACCOUNT_NAME,
							googleAccount);
					result.putString(AccountManager.KEY_AUTHENTICATOR_TYPES,
							LoginHelper.ACCOUNT_TYPE);
					result.putString(AccountManager.KEY_AUTHTOKEN, accessToken);
					setAccountAuthenticatorResult(result);

					mAccountManager.setUserData(account,
							AccountManager.KEY_AUTHTOKEN, refreshToken);

				} else {
					// 帳號已存在, 把新的 refresh token 存進帳號中
					mAccountManager.setUserData(account,
							AccountManager.KEY_AUTHTOKEN, refreshToken);
				}

				buildGreeningConnection(data);
			}

			@Override
			public void onError(Exception ex) {
				onDeadError("交換 Greening Token 時發生錯誤", ex);
			}
		});
		task.execute(googleToken);
	}

	// 建立 Greening 連線
	private void buildGreeningConnection(final ConnectionData data) {
		mProgressMessage.setText("建立 Greening 連線中...");

		GreeningConnectionTask task = new GreeningConnectionTask(
				new OnReceiveListener<ContractConnection>() {

					@Override
					public void onReceive(ContractConnection result) {
						data.setGreeningConnection(result);

						getAccessables(data);
					}

					@Override
					public void onError(Exception ex) {
						onDeadError("建立 Greening 連線時發生錯誤", ex);
					}
				});

		task.execute(data.getAccessToken());
	}

	//取得可登入學校
	private void getAccessables(final ConnectionData data){
		mProgressMessage.setText("取得可登入學校...");
		
		final ConnectionHelper ch = data.createConnectionHelper(this);
		ch.getAccessables(new OnReceiveListener<List<Accessable>>() {
			
			@Override
			public void onReceive(List<Accessable> result) {
				mProgressMessage.setText("取得學校完成");
				getChildren(ch, result);
			}
			
			@Override
			public void onError(Exception ex) {
				onDeadError("取得學校時發生錯誤", ex);
			}
		});		
	}
	
	//取得小孩清單
	private void getChildren(final ConnectionHelper ch, List<Accessable> result){
		mProgressMessage.setText("取得小孩清單...");
		
		mChildResponseCount = 0;
		mTotalResponseCount = result.size();
		if(mChildren == null)
			mChildren = new Children();
		mChildren.clear();
		
		for(final Accessable access : result){			
			
			ch.callService(access, Parent.CONTRACT_PARENT, Parent.SERVICE_GET_MY_CHILD, new DSRequest(), new OnReceiveListener<DSResponse>() {
				
				@Override
				public void onReceive(DSResponse result) {
					
					Element content = result.getBody();
					for(Element child : XmlUtil.selectElements(content,"Student")) {
						ChildInfo info = new ChildInfo(access, child);
						mChildren.addChild(info);
					}
					
					getChildrenResult(ch);
				}
				
				@Override
				public void onError(Exception ex) {
					getChildrenResult(ch);
				}
			}, new Cancelable());
		}
	}
	
	private void getChildrenResult(ConnectionHelper ch){
		mChildResponseCount++;
		
		if(mChildResponseCount == mTotalResponseCount)
			onSuccess(ch, mChildren);
	}
	
	// 最後成功流程
	private void onSuccess(ConnectionHelper ch, Children children) {
		mProgressMessage.setText("完成登入!");

		sConnectionHelper = ch;
		
		Intent intent = new Intent();
		intent.putExtra(PARAM_CHILDREN, children);
		setResult(RESULT_LOGIN_OK, intent);

		finish();
	}

	// 最終失敗流程
	private void onDeadError(String message, Exception exception) {
		mProgressMessage.setText("登入失敗!");

		Intent intent = new Intent();
		intent.putExtra(PARAM_CONNECTION_FAIL_MESSAGE, message);
		intent.putExtra(PARAM_CONNECTION_FAIL_DETAIL, exception);
		setResult(RESULT_LOGIN_FAILURE, intent);

		finish();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_GOOGLE_OAUTH) {
			if (resultCode == RESULT_OK) {
				login();
			}
		}
	}

	public static ConnectionHelper getConnectionHelper() {
		return sConnectionHelper;
	}

}
