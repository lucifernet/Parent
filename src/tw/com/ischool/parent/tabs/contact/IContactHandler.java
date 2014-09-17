package tw.com.ischool.parent.tabs.contact;

import android.view.LayoutInflater;
import android.view.View;

public interface IContactHandler {
	View inflate(LayoutInflater inflater, View convertView);

	void onSelected();
}
