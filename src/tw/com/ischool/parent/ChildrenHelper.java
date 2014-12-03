package tw.com.ischool.parent;

import ischool.dsa.client.OnReceiveListener;
import ischool.dsa.utility.DSRequest;
import ischool.dsa.utility.DSResponse;
import ischool.dsa.utility.XmlUtil;
import ischool.dsa.utility.http.Cancelable;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import tw.com.ischool.account.login.Accessable;
import tw.com.ischool.account.login.ConnectionHelper;
import tw.com.ischool.parent.util.PreferenceHelper;
import android.content.Context;

public class ChildrenHelper {
	public static final String PREF_LOGIN = "ParentLogin";
	public static final String PREF_KEY_CHILDREN_OF = "children_of_";

	private List<Accessable> mAccessables;
	private ConnectionHelper mConnectionHelper;
	private int mChildResponseCount;
	private Children mChildren;
	private int mTotalResponseCount;
	private Context mContext;
	private List<Accessable> mNoChildAccessables;	

	public ChildrenHelper(Context context, ConnectionHelper connectionHelper,
			List<Accessable> accessables) {
		mContext = context;
		mAccessables = accessables;
		mConnectionHelper = connectionHelper;
		mChildResponseCount = 0;
		mTotalResponseCount = mAccessables.size();
		mChildren = new Children();
		mNoChildAccessables = new ArrayList<Accessable>();
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
							List<Element> childrenElements = XmlUtil.selectElements(
									content, "Student");
							
							for (Element child : childrenElements) {
								ChildInfo info = new ChildInfo(access, child);
								mChildren.addChild(info);
							}
							
							if(childrenElements.size() == 0)
								mNoChildAccessables.add(access);
							
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
		return PreferenceHelper.getCacheChildren(mContext);		
	}

	private void getChildrenResult(OnReceiveListener<Children> listener) {
		mChildResponseCount++;

		if (mChildResponseCount == mTotalResponseCount) {
			PreferenceHelper.cacheChildren(mContext, mChildren);

			//TODO 如果沒有任何學生, 移除該校關聯
			for(Accessable a : mNoChildAccessables){
				mAccessables.remove(a);
			}
			
			PreferenceHelper.cacheAccessables(mContext, mAccessables);
			
			listener.onReceive(mChildren);
		}
	}
}
