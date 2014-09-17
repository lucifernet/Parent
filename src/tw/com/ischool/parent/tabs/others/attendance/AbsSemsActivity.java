package tw.com.ischool.parent.tabs.others.attendance;

import ischool.dsa.client.OnReceiveListener;
import ischool.dsa.utility.DSRequest;
import ischool.dsa.utility.DSResponse;
import ischool.dsa.utility.XmlUtil;
import ischool.dsa.utility.http.Cancelable;

import java.util.ArrayList;

import org.w3c.dom.Element;

import tw.com.ischool.parent.ChildInfo;
import tw.com.ischool.parent.Parent;
import tw.com.ischool.parent.R;
import tw.com.ischool.parent.tabs.others.ChildPicker;
import tw.com.ischool.parent.tabs.others.ChildPicker.onChildSelectedListener;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class AbsSemsActivity extends Activity {

	private ChildPicker mChildPicker;
	private ChildInfo mSelectedChild;

	private Spinner mSemsSpinner;
	private SemesAdapter mSemesAdapter;
	private ArrayList<Element> mSemesList;

	private ArrayList<Element> mAbsList;
	//private AbsAdapter mAbsAdapter;
	private ListView mAbsListView;
	                            
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_abs_sems);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		setTitle(R.string.title_activity_abs_sems);

		mChildPicker = new ChildPicker(this, actionBar, new onChildSelectedListener() {
			
			@Override
			public void onChildSelected(ChildInfo child, int count) {
				mSelectedChild = child;
				if (count == 1) {							
					String title = getString(R.string.title_activity_abs_sems)
							+ "(" + child.getStudentName() + ")";
					setTitle(title);
				}
				
				onChildChanged();
			}
		});

		

		mSemsSpinner = (Spinner) this.findViewById(R.id.spinnerSemester);
		mSemesList = new ArrayList<Element>();
		mSemesAdapter = new SemesAdapter(this);
		mSemsSpinner.setAdapter(mSemesAdapter);
		mSemsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Element data = mSemesList.get(position);
				String schoolYear = XmlUtil.getElementText(data, "school_year");
				String semester = XmlUtil.getElementText(data, "semester");
				getAbsence(schoolYear, semester);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});	
	}

	private void getAbsence(String schoolYear, String semester) {
		DSRequest request = new DSRequest();
		Element body = request.getBody();
		XmlUtil.addElement(body, "id", mSelectedChild.getStudentId());
		XmlUtil.addElement(body, "school_year", schoolYear);
		XmlUtil.addElement(body, "semester", semester);

		mSelectedChild.callService(Parent.CONTRACT_PARENT,
				Parent.SERVICE_GET_ATTENDANCE, request,
				new OnReceiveListener<DSResponse>() {

					@Override
					public void onReceive(DSResponse result) {
						bindAbsense(result);
					}

					@Override
					public void onError(Exception ex) {
						// TODO Auto-generated method stub

					}

				}, new Cancelable());
	}

	private void bindAbsense(DSResponse result) {

		Element body = result.getBody();
		
		
	}

	private void bindSemester(DSResponse result) {
		Element body = result.getBody();
		mSemesList.clear();
		mSemesList.addAll(XmlUtil.selectElements(body, "data"));
		mSemesAdapter.notifyDataSetChanged();
	}

	private void onChildChanged() {
		DSRequest request = new DSRequest();
		Element xml = XmlUtil.createElement("id");
		xml.setTextContent(mSelectedChild.getStudentId());
		request.setContent(xml);

		mSelectedChild.callService(Parent.CONTRACT_PARENT,
				Parent.SERVICE_GET_SEMESTERS, request,
				new OnReceiveListener<DSResponse>() {

					@Override
					public void onReceive(DSResponse result) {
						bindSemester(result);
					}

					@Override
					public void onError(Exception ex) {
						// TODO Auto-generated method stub

					}
				}, new Cancelable());

	}

	class SemesAdapter extends BaseAdapter {

		private LayoutInflater _inflater;

		@Override
		public int getCount() {
			return mSemesList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mSemesList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		SemesAdapter(Context context) {
			_inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Element data = mSemesList.get(position);
			String schoolYear = XmlUtil.getElementText(data, "school_year");
			String semester = XmlUtil.getElementText(data, "semester");

			ViewHolder holder = null;
			if (convertView == null) {
				convertView = _inflater.inflate(R.layout.item_spinner, null);

				holder = new ViewHolder();
				holder.textView = (TextView) convertView
						.findViewById(R.id.textView);
				;

				convertView.setTag(holder);
			}

			holder = (ViewHolder) convertView.getTag();
			String str = "%s 學年度第 %s 學期";
			str = String.format(str, schoolYear, semester);
			holder.textView.setTag(data);
			holder.textView.setText(str);

			return convertView;
		}

	}

	static class ViewHolder {
		TextView textView;
	}
}
