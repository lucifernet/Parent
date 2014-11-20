package tw.com.ischool.parent;

import ischool.dsa.client.OnReceiveListener;
import ischool.dsa.utility.DSRequest;
import ischool.dsa.utility.DSResponse;
import ischool.dsa.utility.XmlUtil;
import ischool.dsa.utility.http.Cancelable;
import ischool.utilities.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.List;

import org.w3c.dom.Element;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;
import tw.com.ischool.account.login.Accessable;
import tw.com.ischool.account.login.ConnectionHelper;

public class ChildrenHelper {
	public static final String PREF_LOGIN = "ParentLogin";
	public static final String PREF_KEY_CHILDREN_OF = "children_of_";

	private List<Accessable> mAccessables;
	private ConnectionHelper mConnectionHelper;
	private int mChildResponseCount;
	private Children mChildren;
	private int mTotalResponseCount;
	private Context mContext;

	public ChildrenHelper(Context context, ConnectionHelper connectionHelper,
			List<Accessable> accessables) {
		mContext = context;
		mAccessables = accessables;
		mConnectionHelper = connectionHelper;
		mChildResponseCount = 0;
		mTotalResponseCount = mAccessables.size();
		mChildren = new Children();
	}

	public void getChildren(final OnReceiveListener<Children> listener) {
		mChildren.clear();
		for (final Accessable access : mAccessables) {

			mConnectionHelper.callService(access, Parent.CONTRACT_PARENT,
					Parent.SERVICE_GET_MY_CHILD, new DSRequest(),
					new OnReceiveListener<DSResponse>() {

						@Override
						public void onReceive(DSResponse result) {

							Element content = result.getBody();
							for (Element child : XmlUtil.selectElements(
									content, "Student")) {
								ChildInfo info = new ChildInfo(access, child);
								mChildren.addChild(info);
							}

							getChildrenResult(listener);
						}

						@Override
						public void onError(Exception ex) {
							getChildrenResult(listener);
						}
					}, new Cancelable());
		}
	}

	public Children getPrefChildren() {
		String name = mConnectionHelper.getAccount().name;
		SharedPreferences pref = mContext.getSharedPreferences(PREF_LOGIN, 0);
		String contentString = pref.getString(PREF_KEY_CHILDREN_OF + name,
				StringUtil.EMPTY);
		if (!StringUtil.isNullOrWhitespace(contentString)) {
			return parseChildren(contentString);
		}
		return new Children();
	}

	private Children parseChildren(String content) {
		byte[] bytes = Base64.decode(content, Base64.DEFAULT);

		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		ObjectInputStream is = null;
		Children children;

		try {
			is = new ObjectInputStream(in);
			Object obj = is.readObject();
			children = (Children) obj;
		} catch (Exception ex) {
			children = new Children();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				in.close();
			} catch (IOException e) {
			}
		}
		return children;
	}

	private void getChildrenResult(OnReceiveListener<Children> listener) {
		mChildResponseCount++;

		if (mChildResponseCount == mTotalResponseCount) {
			String childrenString = convertChildToString(mChildren);
			SharedPreferences pref = mContext.getSharedPreferences(PREF_LOGIN,
					0);
			Editor editor = pref.edit();
			editor.putString(
					PREF_KEY_CHILDREN_OF + mConnectionHelper.getAccount().name,
					childrenString);
			editor.commit();

			listener.onReceive(mChildren);
		}
	}

	private String convertChildToString(Children children) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		String base64String = StringUtil.EMPTY;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(children);
			byte[] yourBytes = bos.toByteArray();

			base64String = Base64.encodeToString(yourBytes, Base64.DEFAULT);
		} catch (IOException ex) {
			base64String = StringUtil.EMPTY;
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException ex) {
				// ignore close exception
			}
			try {
				bos.close();
			} catch (IOException ex) {
				// ignore close exception
			}
		}
		return base64String;
	}
}
