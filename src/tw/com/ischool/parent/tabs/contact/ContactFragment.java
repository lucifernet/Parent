package tw.com.ischool.parent.tabs.contact;

import java.util.ArrayList;

import tw.com.ischool.parent.ChildInfo;
import tw.com.ischool.parent.Children;
import tw.com.ischool.parent.MainActivity;
import tw.com.ischool.parent.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class ContactFragment extends Fragment {

	private ListView mListView;
	private int mReceiveCount = 0;
	private ArrayList<IContactHandler> mContacts;
	private ProgressDialog mDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_contact, container,
				false);

		mListView = (ListView) rootView.findViewById(R.id.lvContact);

		getContact();

		return rootView;
	}

	private void getContact() {
		mContacts = new ArrayList<IContactHandler>();
		Children children = MainActivity.getChildren();
		for(ChildInfo child : children.getChildren()){
			BannerHandler banner = new BannerHandler(child);
			mContacts.add(banner);
			
			ChildHandler ch = new ChildHandler(child, getActivity());
			mContacts.add(ch);
			
			TeacherHandler th = new TeacherHandler(child, getActivity());
			mContacts.add(th);
		}

		bindContacts();
	}

	// 把資料放在畫面上
	private void bindContacts() {
		ContactAdapter adapter = new ContactAdapter(getActivity());
		mListView.setAdapter(adapter);
	}

	private class ContactAdapter extends BaseAdapter {

		private LayoutInflater _inflater;

		ContactAdapter(Context context) {
			_inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mContacts.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mContacts.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			IContactHandler contact = mContacts.get(position);
			convertView = contact.inflate(_inflater, convertView);
			return convertView;
		}

	}
}
