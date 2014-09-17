package tw.com.ischool.parent.tabs.others;

import tw.com.ischool.parent.R;
import tw.com.ischool.parent.tabs.TabHandler;

public class OthersTabHandler extends TabHandler {

	@Override
	public String getTitle() {
		return mContext.getString(R.string.tab_others);
	}

	@Override
	public String getFragmentClassName() {	
		return OthersFragment.class.getName();
	}
}
