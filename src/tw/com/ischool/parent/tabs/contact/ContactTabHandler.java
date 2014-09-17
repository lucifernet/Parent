package tw.com.ischool.parent.tabs.contact;

import tw.com.ischool.parent.R;
import tw.com.ischool.parent.tabs.TabHandler;

public class ContactTabHandler extends TabHandler {

	@Override
	public String getTitle() {
		return mContext.getString(R.string.tab_contact);
	}

	@Override
	public String getFragmentClassName() {	
		return ContactFragment.class.getName();
	}

}
