package tw.com.ischool.parent.tabs;

import android.content.Context;

public abstract class TabHandler implements ITabHandler {

	protected Context mContext;

	@Override
	public void setContext(Context context) {
		mContext = context;
	}

	@Override
	public abstract String getTitle();

	@Override
	public abstract String getFragmentClassName();

}
