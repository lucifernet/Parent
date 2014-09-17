package tw.com.ischool.parent.tabs;

import android.content.Context;

public interface ITabHandler {
	String getTitle();
	
	String getFragmentClassName();

	void setContext(Context context);
	
}
