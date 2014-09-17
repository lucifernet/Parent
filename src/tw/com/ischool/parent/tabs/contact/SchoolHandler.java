package tw.com.ischool.parent.tabs.contact;

import tw.com.ischool.account.login.Accessable;
import tw.com.ischool.parent.R;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class SchoolHandler implements IContactHandler {

	private Accessable _school;
	
	public SchoolHandler(Accessable school){
		_school = school;
	}

	private int getLayoutId() {
		return R.layout.item_contact_school; 
	}

	@Override
	public void onSelected() {
		// TODO Auto-generated method stub

	}

	@Override
	public View inflate(LayoutInflater inflater, View convertView) {
		
		convertView = inflater.inflate(getLayoutId(), null);
		TextView txtView = (TextView)convertView.findViewById(R.id.txtSchoolName);
		txtView.setText(_school.getTitle());
		return convertView;		
		
	}

}
