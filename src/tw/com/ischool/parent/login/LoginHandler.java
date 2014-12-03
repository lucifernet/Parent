package tw.com.ischool.parent.login;

import ischool.dsa.client.OnProgressListener;
import ischool.dsa.client.OnReceiveListener;
import ischool.utilities.StringUtil;

import java.util.List;

import tw.com.ischool.account.login.Accessable;
import tw.com.ischool.account.login.ConnectionData;
import tw.com.ischool.account.login.ConnectionHelper;
import tw.com.ischool.account.login.LoginHelper;
import tw.com.ischool.account.login.RefreshGreeningTokenTask;
import tw.com.ischool.parent.Children;
import tw.com.ischool.parent.ChildrenHelper;
import tw.com.ischool.parent.Parent;
import tw.com.ischool.parent.ParentLoginActivity;
import tw.com.ischool.parent.util.PreferenceHelper;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

public class LoginHandler {
	private AccountManager mAccountManager;
	private Context _context;
	private LoginListener _listener;

	public LoginHandler(Context context) {
		_context = context;
	}

	public void login(final LoginListener listener) {
		this._listener = listener;

		String currentAccount = PreferenceHelper.getCacheUseAccount(_context);

		if (StringUtil.isNullOrWhitespace(currentAccount)) {
			listener.onNotLogin();
			return;
		}

		// String currentAccountType =
		// pref.getString(PREF_KEY_CURRENT_ACCOUNT_TYPE, StringUtil.EMPTY);

		// 檢查 mobile 裝置內是否有 ischool 帳號
		mAccountManager = AccountManager.get(_context);

		final Account[] ischoolAccounts = mAccountManager
				.getAccountsByType(LoginHelper.ACCOUNT_TYPE);

		if (ischoolAccounts.length == 0) {
			listener.onNotLogin();
			return;
		}

		Account ischoolAccount = null;
		for (Account a : ischoolAccounts) {
			if (a.name.equalsIgnoreCase(currentAccount)) {
				ischoolAccount = a;
				break;
			}
		}

		if (ischoolAccount == null) {
			listener.onNotLogin();
			return;
		}

		// 取出 ischool refresh token 登入 greening, 交換新的 access_token
		String refreshToken = mAccountManager.getUserData(ischoolAccount,
				AccountManager.KEY_AUTHTOKEN);

		RefreshGreeningTokenTask task = new RefreshGreeningTokenTask(_context,
				mAccountManager, ischoolAccount,
				new OnProgressListener<ConnectionData>() {

					@Override
					public void onReceive(ConnectionData result) {
						getAccessables(result);
					}

					@Override
					public void onError(Exception ex) {
						_listener.onNotLogin();
					}

					@Override
					public void onProgressUpdate(String message) {
					}
				});
		task.execute(refreshToken);
	}


	// 取得可登入學校
	private void getAccessables(final ConnectionData data) {
		final ConnectionHelper ch = data.createConnectionHelper(_context);

		List<Accessable> accessables = PreferenceHelper.getAccessables(_context);
		if (accessables.size() == 0) {
			ch.getAccessables(_context, new OnReceiveListener<List<Accessable>>() {

				@Override
				public void onReceive(List<Accessable> result) {
					PreferenceHelper.cacheAccessables(_context, result);
				
					getChildren(ch, result);
				}

				@Override
				public void onError(Exception ex) {
					_listener.onNotLogin();
				}
			});
		} else {		
			getChildren(ch, accessables);
		}
		
//		final ConnectionHelper ch = data.createConnectionHelper(_context);
//		ch.getQuickAccessables(_context,
//				new OnReceiveListener<List<Accessable>>() {
//
//					@Override
//					public void onReceive(List<Accessable> result) {
//						getChildren(ch, result);
//					}
//
//					@Override
//					public void onError(Exception ex) {
//						_listener.onNotLogin();
//					}
//				});
	}

	// 取得小孩清單
	private void getChildren(final ConnectionHelper ch, List<Accessable> result) {

		// 如果無可連結學校, 那應該是新帳號, 直接請他輸入 parent code
		if (result.size() == 0) {
			onSuccess(ch, new Children());
			return;
		}

		ChildrenHelper childHelper = new ChildrenHelper(_context, ch, result);
		Children children = childHelper.getPrefChildren();

		// 若有小孩 cache, 出去吧
		if (children.getChildren().size() > 0) {
			onSuccess(ch, children);
			return;
		}

		childHelper.getChildren(new OnReceiveListener<Children>() {

			@Override
			public void onReceive(Children result) {
				onSuccess(ch, result);

			}

			@Override
			public void onError(Exception ex) {
				// 這裡不會被觸發
			}
		});
	}

	// 最後成功流程
	private void onSuccess(ConnectionHelper ch, Children children) {

		Parent.setConnectionHelper(ch);
		Parent.setChildren(children);

		try {
			PreferenceHelper.cacheUseAccount(_context, ch.getAccount().name);
		} catch (Exception ex) {

		}

		_listener.onReady();
	}

	public interface LoginListener {
		void onReady();

		void onNotLogin();
	}
}
