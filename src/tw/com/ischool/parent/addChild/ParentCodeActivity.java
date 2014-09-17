package tw.com.ischool.parent.addChild;

import ischool.dsa.client.ContractConnection;
import ischool.dsa.client.OnReceiveListener;
import ischool.dsa.utility.DSRequest;
import ischool.dsa.utility.DSResponse;
import ischool.dsa.utility.XmlUtil;
import ischool.dsa.utility.http.Cancelable;
import ischool.utilities.JSONUtil;
import ischool.utilities.StringUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;

import tw.com.ischool.parent.MainActivity;
import tw.com.ischool.parent.Parent;
import tw.com.ischool.parent.R;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;

public class ParentCodeActivity extends Activity {

	public static final int REQUEST_CODE_PARENT_CODE = 1002;
	
	private SchoolList mSchools;
//	private SchoolList mFilteredSchools;
	private ArrayList<String> mFilterSchoolNames;
	private AutoCompleteAdapter mAdapter;
	private AutoCompleteTextView mTxtSchool;
	private EditText mTxtParentCode;
	private EditText mTxtRelation;
	private Button mBtnOK;

	private String mDsnsName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parent_code);

		setTitle("輸學家長代碼");
		
		mTxtSchool = (AutoCompleteTextView) findViewById(R.id.txtSchool);
		mTxtRelation = (EditText) findViewById(R.id.txtRelation);
		mTxtParentCode = (EditText) findViewById(R.id.txtParentCode);
		mBtnOK = (Button) findViewById(R.id.btnOK);
//		mFilteredSchools = new SchoolList();
		mFilterSchoolNames = new ArrayList<String>();

		// mAdapter = new ArrayAdapter<String>(ParentCodeActivity.this,
		// android.R.layout.simple_spinner_item, mFilterSchoolNames);

		// mTxtSchool.setFilters(new InputFilter[]);

		// 載入學校, 目前還沒有很好的 service, 先寫死在這
		loadSchools(new OnReceiveListener<SchoolList>() {

			@Override
			public void onReceive(SchoolList result) {
				mSchools = result;

				for (SchoolInfo school : mSchools)
					mFilterSchoolNames.add(school.schoolName);

				// mAdapter.notifyDataSetChanged();

				mAdapter = new AutoCompleteAdapter(ParentCodeActivity.this,
						android.R.layout.simple_dropdown_item_1line,
						android.R.id.text1, mFilterSchoolNames);
				mTxtSchool.setAdapter(mAdapter);
			}

			@Override
			public void onError(Exception ex) {
				// TODO Auto-generated method stub

			}
		});

		mBtnOK.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				submit();
			}
		});
	}

	private void loadSchools(OnReceiveListener<SchoolList> listener) {
		SchoolList list = new SchoolList();

		try {
			Resources res = getResources();
			InputStream in_s = res.openRawResource(R.raw.school_list);

			byte[] b = new byte[in_s.available()];
			in_s.read(b);
			String rawString = new String(b);

			JSONArray jsonArray = new JSONArray(rawString);
			for (int i = 0; i < jsonArray.length(); i++) {
				SchoolInfo school = new SchoolInfo();
				JSONObject json = jsonArray.getJSONObject(i);

				school.schoolName = JSONUtil.getString(json, "schoolName");
				school.dsnsName = JSONUtil.getString(json, "dsnsName");
				school.city = JSONUtil.getString(json, "city");
				list.add(school);
			}
		} catch (Exception e) {

		}

		if (listener != null)
			listener.onReceive(list);
	}

	private void submit() {
		mTxtSchool.setError(null);

		String schoolName = mTxtSchool.getText().toString();
		mDsnsName = null;
		for (SchoolInfo school : mSchools) {
			if (schoolName.equalsIgnoreCase(school.schoolName))
				mDsnsName = school.dsnsName;
		}

		if (StringUtil.isNullOrWhitespace(schoolName)) {
			mTxtSchool.setError("學校不可空白");
			return;
		}
		
		if(StringUtil.isNullOrWhitespace(mDsnsName)){
			mDsnsName = mTxtSchool.getText().toString();
		}

		ContractConnection greening = MainActivity.getConnectionHelper()
				.getGreening();
		greening.connectAnotherByPassportAsync(mDsnsName, Parent.CONTRACT_JOIN, true, new OnReceiveListener<ContractConnection>() {
			
			@Override
			public void onReceive(ContractConnection result) {
				joinParent(result);
			}
			
			@Override
			public void onError(Exception ex) {
				mTxtSchool.setError("連結學校時發生錯誤 : " + ex.getMessage());
			}
		});		
	}
	
	private void joinParent(ContractConnection connection){
		DSRequest request = new DSRequest();
		Element content = XmlUtil.createElement("Request");
		XmlUtil.addElement(content, "ParentCode", mTxtParentCode.getText()
				.toString());
		XmlUtil.addElement(content, "Relationship", mTxtRelation.getText()
				.toString());
		request.setContent(content);

		connection.sendAsyncRequest(Parent.SERVICE_JOIN_PARENT, request,
				new OnReceiveListener<DSResponse>() {

					@Override
					public void onReceive(DSResponse result) {
						addApplicationRef();
					}

					@Override
					public void onError(Exception ex) {
						mTxtParentCode.setError(ex.getMessage());
					}
				}, new Cancelable());
	}

	private void addApplicationRef(){
		DSRequest request = new DSRequest();
		Element content = XmlUtil.createElement("Request");
		Element applications = XmlUtil.addElement(content, "Applications");
		Element application = XmlUtil.addElement(applications, "Application");
		XmlUtil.addElement(application, "AccessPoint",mDsnsName);
		XmlUtil.addElement(application, "Type","dynpkg");		
		request.setContent(content);
		
		ContractConnection greening = MainActivity.getConnectionHelper()
				.getGreening();
		greening.sendAsyncRequest(Parent.SERVICE_ADD_APPLICATION_REF,request , new OnReceiveListener<DSResponse>() {
			
			@Override
			public void onReceive(DSResponse result) {
				setResult(RESULT_OK);
				finish();
			}
			
			@Override
			public void onError(Exception ex) {
				setResult(RESULT_OK);
				finish();
			}
		}, new Cancelable());
	}
	
	class SchoolList extends ArrayList<SchoolInfo> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	}

	class SchoolInfo {
		String dsnsName;
		String schoolName;
		String city;

		boolean match(String inputText) {
			if (dsnsName.contains(inputText))
				return true;
			if (schoolName.contains(inputText))
				return true;
			return false;
		}
	}

	class AutoCompleteAdapter extends ArrayAdapter<String> implements
			Filterable {

		private ArrayList<String> fullList;
		private ArrayList<String> mOriginalValues;
		private ArrayFilter mFilter;

		public AutoCompleteAdapter(Context context, int resource,
				int textViewResourceId, List<String> objects) {

			super(context, resource, textViewResourceId, objects);
			fullList = (ArrayList<String>) objects;
			mOriginalValues = new ArrayList<String>(fullList);

		}

		@Override
		public int getCount() {
			return fullList.size();
		}

		@Override
		public String getItem(int position) {
			return fullList.get(position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return super.getView(position, convertView, parent);
		}

		@Override
		public Filter getFilter() {
			if (mFilter == null) {
				mFilter = new ArrayFilter();
			}
			return mFilter;
		}

		private class ArrayFilter extends Filter {
			private Object lock;

			@Override
			protected FilterResults performFiltering(CharSequence prefix) {
				FilterResults results = new FilterResults();

				if (mOriginalValues == null) {
					synchronized (lock) {
						mOriginalValues = new ArrayList<String>(fullList);
					}
				}

				if (prefix == null || prefix.length() == 0) {
					synchronized (lock) {
						ArrayList<String> list = new ArrayList<String>(
								mOriginalValues);
						results.values = list;
						results.count = list.size();
					}
				} else {
					final String prefixString = prefix.toString().toLowerCase();

					ArrayList<String> values = mOriginalValues;
					int count = values.size();

					ArrayList<String> newValues = new ArrayList<String>(count);

					for (int i = 0; i < count; i++) {
						String item = values.get(i);
						if (item.toLowerCase().contains(prefixString)) {
							newValues.add(item);
						}

					}

					results.values = newValues;
					results.count = newValues.size();
				}

				return results;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {

				if (results.values != null) {
					fullList = (ArrayList<String>) results.values;
				} else {
					fullList = new ArrayList<String>();
				}
				if (results.count > 0) {
					notifyDataSetChanged();
				} else {
					notifyDataSetInvalidated();
				}
			}
		}
	}
}
