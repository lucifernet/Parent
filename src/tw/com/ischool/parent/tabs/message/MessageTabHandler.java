package tw.com.ischool.parent.tabs.message;

import tw.com.ischool.parent.R;
import tw.com.ischool.parent.tabs.TabHandler;

public class MessageTabHandler extends TabHandler {

	@Override
	public String getTitle() {
		return mContext.getString(R.string.tab_message);
	}

	@Override
	public String getFragmentClassName() {	
		return MessageFragment.class.getName();
	}

}
