package tw.com.ischool.parent.tabs.calendar;

import tw.com.ischool.parent.R;
import tw.com.ischool.parent.tabs.TabHandler;

public class CalendarTabHandler extends TabHandler {

	@Override
	public String getTitle() {
		return mContext.getString(R.string.tab_calendar);
	}

	@Override
	public String getFragmentClassName() {
		// TODO Auto-generated method stub
		return CalendarFragment.class.getName();
	}


}
