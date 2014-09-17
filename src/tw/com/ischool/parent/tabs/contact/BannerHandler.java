package tw.com.ischool.parent.tabs.contact;

import tw.com.ischool.parent.ChildInfo;
import tw.com.ischool.parent.R;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class BannerHandler implements IContactHandler {

	private ChildInfo _child;

	public BannerHandler(ChildInfo child) {
		_child = child;
	}

	@Override
	public View inflate(LayoutInflater inflater, View convertView) {
		convertView = inflater.inflate(R.layout.item_contact_school, null);
		TextView txtView = (TextView) convertView
				.findViewById(R.id.txtSchoolName);
		txtView.setText(_child.getAccessable().getTitle());

		TextView txtClassView = (TextView) convertView
				.findViewById(R.id.txtClassName);
		txtClassView.setText(_child.getClassName());
		return convertView;
	}

	@Override
	public void onSelected() {

	}
}
