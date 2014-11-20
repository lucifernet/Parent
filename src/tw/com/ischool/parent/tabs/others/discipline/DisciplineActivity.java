package tw.com.ischool.parent.tabs.others.discipline;

import ischool.dsa.client.OnReceiveListener;
import ischool.dsa.utility.DSRequest;
import ischool.dsa.utility.DSResponse;
import ischool.dsa.utility.XmlHelper;
import ischool.dsa.utility.XmlUtil;
import ischool.dsa.utility.http.Cancelable;
import ischool.utilities.StringUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.w3c.dom.Element;

import tw.com.ischool.parent.ChildInfo;
import tw.com.ischool.parent.Parent;
import tw.com.ischool.parent.R;
import tw.com.ischool.parent.tabs.others.ChildPicker;
import tw.com.ischool.parent.tabs.others.ChildPicker.onChildSelectedListener;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DisciplineActivity extends Activity {

	private static final String PARAM_ATTENDANCE = "attendance";
	private static final String PARAM_START_CALENDAR = "start";
	private static final String PARAM_END_CALENDAR = "end";
	private static final String PARAM_SELECTED_TYPE = "type";

	private ChildPicker mChildPicker;
	private ChildInfo mSelectedChild;
	// private ActivityHelper mActivityHelper;
	private Activity mActivity;
	private Element mAttendanceElement;
	private LinearLayout mContainerType;
	private ListView mListView;
	private AbsAdapter mAdapter;
	private Button mBtnStart;
	private Button mBtnEnd;
	private Button mClickedButton;
	private TextView mTxtCountAll;
	private View mContainerAll;
	private TextView mTxtCountMeritA;
	private View mContainerMeritA;
	private TextView mTxtCountMeritB;
	private View mContainerMeritB;
	private TextView mTxtCountMeritC;
	private View mContainerMeritC;
	private TextView mTxtCountDemeritA;
	private View mContainerDemeritA;
	private TextView mTxtCountDemeritB;
	private View mContainerDemeritB;
	private TextView mTxtCountDemeritC;
	private View mContainerDemeritC;
	private List<Element> mAllAbsenceElements = new ArrayList<Element>();
	private List<Element> mDateElements = new ArrayList<Element>();
	private List<Element> mDisplayElements = new ArrayList<Element>();
	private Calendar mStartCalender;
	private Calendar mEndCalender;
	private String mCurrentSelectedType;
	// private PullToRefreshLayout mPullToRefreshLayout;
	private String MERIT_ALL;
	private String MERIT_A;
	private String MERIT_B;
	private String MERIT_C;
	private String DEMERIT_A;
	private String DEMERIT_B;
	private String DEMERIT_C;

	private OnClickListener mTypeClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			changeSelectedType(view);
		}
	};

	private OnClickListener mBtnOnClickListener = new OnClickListener() {

		@Override
		public void onClick(final View v) {
			mClickedButton = (Button) v;
			Calendar c = (Calendar) mClickedButton.getTag();
			if (c == null) {
				c = Calendar.getInstance(Locale.getDefault());
			}

			final DatePickerDialog dialog = new DatePickerDialog(mActivity,
					mDateListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
					c.get(Calendar.DAY_OF_MONTH));

			dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
					mActivity.getString(R.string.attendance_unlimit),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface d, int which) {
							Button button = (Button) v;
							button.setTag(null);
							button.setText(R.string.attendance_unlimit);
							dialog.getDatePicker().setTag(
									R.string.attendance_unlimit);
							
							mStartCalender = (Calendar) mBtnStart.getTag();
							mEndCalender = (Calendar) mBtnEnd.getTag();

							filterAbsense(mStartCalender, mEndCalender);
						}
					});
			dialog.show();
		}
	};

	DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {

			if (view.getTag() == null) {
				Calendar c = Calendar.getInstance(Locale.getDefault());
				c.set(Calendar.YEAR, year);
				c.set(Calendar.MONTH, monthOfYear);
				c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

				Date date = c.getTime();
				String string = StringUtil.toDateString(date);
				mClickedButton.setText(string);
				mClickedButton.setTag(c);
			}

			view.setTag(null);

			mStartCalender = (Calendar) mBtnStart.getTag();
			mEndCalender = (Calendar) mBtnEnd.getTag();

			filterAbsense(mStartCalender, mEndCalender);
		}
	};

	// private OnRefreshListener mRefreshListener = new OnRefreshListener() {
	//
	// @Override
	// public void onRefreshStarted(View view) {
	// String studentId = XmlUtil.getElementText(mStudentElement,
	// "StudentID");
	//
	// Element request = XmlUtil.createElement("Request");
	// XmlUtil.addElement(request, "RefStudentId", studentId);
	//
	// DSS.sendRequest(mActivity, "student.GetDiscipline", request,
	// new OnReceiveListener<DSResponse>() {
	//
	// @Override
	// protected void onReceive(DSResponse result) {
	// mAttendanceElement = result.getContent();
	//
	// mAllAbsenceElements = XmlUtil.selectElements(
	// mAttendanceElement, "Discipline");
	//
	// filterAbsense(null, null);
	// mPullToRefreshLayout.setRefreshComplete();
	// }
	//
	// @Override
	// protected void onError(Exception e) {
	// Toast.makeText(mActivity, e.toString(),
	// Toast.LENGTH_LONG).show();
	// mPullToRefreshLayout.setRefreshComplete();
	// }
	// }, new Cancelable());
	// }
	// };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_discipline);
		mActivity = this;
		
		setTitle(R.string.title_activity_discipline);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		mChildPicker = new ChildPicker(this, actionBar, new onChildSelectedListener() {
			
			@Override
			public void onChildSelected(ChildInfo child, int count) {
				mSelectedChild = child;

				if (count == 1) {							
					String title = getString(R.string.title_activity_discipline)
							+ "(" + child.getStudentName() + ")";
					setTitle(title);
				}
				
				loadDiscipline();
			}
		});

		mListView = (ListView) this.findViewById(R.id.lvReason);
		mContainerType = (LinearLayout) this
				.findViewById(R.id.container_discipline_type);
		mBtnStart = (Button) this.findViewById(R.id.btnStart);
		mBtnEnd = (Button) this.findViewById(R.id.btnEnd);

		mTxtCountAll = (TextView) this.findViewById(R.id.txtCountAll);
		mTxtCountMeritA = (TextView) this.findViewById(R.id.txtMeritCountA);
		mTxtCountMeritB = (TextView) this.findViewById(R.id.txtMeritCountB);
		mTxtCountMeritC = (TextView) this.findViewById(R.id.txtMeritCountC);
		mTxtCountDemeritA = (TextView) this.findViewById(R.id.txtDemeritCountA);
		mTxtCountDemeritB = (TextView) this.findViewById(R.id.txtDemeritCountB);
		mTxtCountDemeritC = (TextView) this.findViewById(R.id.txtDemeritCountC);

		mContainerAll = this.findViewById(R.id.container_all);
		mContainerMeritA = this.findViewById(R.id.container_meritA);
		mContainerMeritB = this.findViewById(R.id.container_meritB);
		mContainerMeritC = this.findViewById(R.id.container_meritC);
		mContainerDemeritA = this.findViewById(R.id.container_demeritA);
		mContainerDemeritB = this.findViewById(R.id.container_demeritB);
		mContainerDemeritC = this.findViewById(R.id.container_demeritC);

		

		MERIT_ALL = mActivity.getString(R.string.discipline_all);
		MERIT_A = mActivity.getString(R.string.discipline_meritA);
		MERIT_B = mActivity.getString(R.string.discipline_meritB);
		MERIT_C = mActivity.getString(R.string.discipline_meritC);

		DEMERIT_A = mActivity.getString(R.string.discipline_demeritA);
		DEMERIT_B = mActivity.getString(R.string.discipline_demeritB);
		DEMERIT_C = mActivity.getString(R.string.discipline_demeritC);

		mAdapter = new AbsAdapter();
		mListView.setAdapter(mAdapter);

		mBtnStart.setOnClickListener(mBtnOnClickListener);
		mBtnEnd.setOnClickListener(mBtnOnClickListener);

		if (savedInstanceState != null) {

			mAttendanceElement = XmlHelper.parseXml(savedInstanceState
					.getString(PARAM_ATTENDANCE));
			mAllAbsenceElements = XmlUtil.selectElements(mAttendanceElement,
					"Discipline");

			mStartCalender = StringUtil.parseToCalendar(savedInstanceState
					.getString(PARAM_START_CALENDAR));
			mEndCalender = StringUtil.parseToCalendar(savedInstanceState
					.getString(PARAM_END_CALENDAR));

			mBtnStart.setTag(mStartCalender);
			mBtnEnd.setTag(mEndCalender);

			if (mStartCalender != null) {
				mBtnStart.setText(StringUtil.toDateString(mStartCalender));
			}

			if (mEndCalender != null) {
				mBtnEnd.setText(StringUtil.toDateString(mEndCalender));
			}

			filterAbsense(mStartCalender, mEndCalender);

			mCurrentSelectedType = savedInstanceState
					.getString(PARAM_SELECTED_TYPE);
			for (int i = 0; i < mContainerType.getChildCount(); i++) {
				View view = mContainerType.getChildAt(i);
				String type = (String) view.getTag();
				if (type.equalsIgnoreCase(mCurrentSelectedType)) {
					this.changeSelectedType(view);
					break;
				}
			}
		} else {
			loadDiscipline();
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
			finish();
			break;
		}

		return true;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(PARAM_ATTENDANCE,
				XmlHelper.convertToString(mAttendanceElement));
		outState.putString(PARAM_START_CALENDAR,
				StringUtil.toDateString(mStartCalender));
		outState.putString(PARAM_END_CALENDAR,
				StringUtil.toDateString(mEndCalender));
		outState.putString(PARAM_SELECTED_TYPE, mCurrentSelectedType);

		super.onSaveInstanceState(outState);
	}

	private void loadDiscipline() {
		if (mSelectedChild == null)
			return;

		String studentId = mSelectedChild.getStudentId();

		DSRequest req = new DSRequest();
		Element request = XmlUtil.createElement("Request");
		XmlUtil.addElement(request, "RefStudentId", studentId);
		req.setContent(request);

		// final ProgressDialog progress = ProgressDialog.show(mActivity,
		// mActivity.getString(R.string.progress_title),
		// mActivity.getString(R.string.discipline_loading));

		final ProgressDialog dialog = new ProgressDialog(mActivity);
		dialog.setTitle(mActivity.getString(R.string.progress_title));
		dialog.setMessage(mActivity.getString(R.string.discipline_loading));
		dialog.setCancelable(false);
		final Cancelable cancelable = new Cancelable();
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
				getString(R.string.cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						cancelable.setCancel(true);

						dialog.dismiss();
					}
				});

		dialog.show();

		mSelectedChild.callService(Parent.CONTRACT_PARENT,
				Parent.SERVICE_GET_DISCIPLINE, req,
				new OnReceiveListener<DSResponse>() {

					@Override
					public void onReceive(DSResponse result) {
						mAttendanceElement = result.getContent();

						mAllAbsenceElements = XmlUtil.selectElements(
								mAttendanceElement, "Discipline");

						filterAbsense(null, null);
						dialog.dismiss();
					}

					@Override
					public void onError(Exception ex) {
						Toast.makeText(mActivity, ex.toString(),
								Toast.LENGTH_LONG).show();
						dialog.dismiss();
					}

				}, cancelable);

	}

	private void filterAbsense(Calendar start, Calendar end) {
		TextView emptyText = (TextView) mActivity
				.findViewById(R.id.empty_view_discipline);
		mListView.setEmptyView(emptyText);

		mDisplayElements.clear();
		mDateElements.clear();

		for (Element e : mAllAbsenceElements) {
			String dateString = e.getAttribute("OccurDate");
			Calendar date = StringUtil.parseToCalendar(dateString);

			if (end != null && date.after(end))
				continue;
			if (start != null && date.before(start))
				continue;

			mDateElements.add(e);
			mDisplayElements.add(e);
		}
		mAdapter.notifyDataSetChanged();
		bindAbsense();
	}

	private void bindAbsense() {
		// mContainerType.removeAllViews();
		// if (mAllAbsenceElements == null || mAllAbsenceElements.size() == 0)
		// return;

		HashMap<String, Integer> absMap = new HashMap<String, Integer>();

		// absMap.put(MERIT_ALL, 0);
		absMap.put(MERIT_A, 0);
		absMap.put(MERIT_B, 0);
		absMap.put(MERIT_C, 0);
		absMap.put(DEMERIT_A, 0);
		absMap.put(DEMERIT_B, 0);
		absMap.put(DEMERIT_C, 0);

		for (Element e : mDisplayElements) {

			String flag = e.getAttribute("MeritFlag");

			// flag = 1 = 獎勵
			if (flag.equals("1")) {
				Element meritElement = XmlUtil.selectElement(e, "Merit");
				int a = StringUtil.convertToInt(meritElement.getAttribute("A"));
				if (a != 0) {
					int value = absMap.get(MERIT_A);
					absMap.put(MERIT_A, value + a);
				}

				a = StringUtil.convertToInt(meritElement.getAttribute("B"));
				if (a != 0) {
					int value = absMap.get(MERIT_B);
					absMap.put(MERIT_B, value + a);
				}

				a = StringUtil.convertToInt(meritElement.getAttribute("C"));
				if (a != 0) {
					int value = absMap.get(MERIT_C);
					absMap.put(MERIT_C, value + a);
				}
			} else { // flag = 0 = 懲
				Element meritElement = XmlUtil.selectElement(e, "Demerit");
				int a = StringUtil.convertToInt(meritElement.getAttribute("A"));
				if (a != 0) {
					int value = absMap.get(DEMERIT_A);
					absMap.put(DEMERIT_A, value + a);
				}

				a = StringUtil.convertToInt(meritElement.getAttribute("B"));
				if (a != 0) {
					int value = absMap.get(DEMERIT_B);
					absMap.put(DEMERIT_B, value + a);
				}

				a = StringUtil.convertToInt(meritElement.getAttribute("C"));
				if (a != 0) {
					int value = absMap.get(DEMERIT_C);
					absMap.put(DEMERIT_C, value + a);
				}
			}
		}

		int total = 0;
		for (String key : absMap.keySet()) {
			total += absMap.get(key);
		}

		mTxtCountAll.setText(String.valueOf(total));
		mContainerAll.setTag(MERIT_ALL);
		mContainerAll.setOnClickListener(mTypeClickListener);

		mTxtCountMeritA.setText(String.valueOf(absMap.get(MERIT_A)));
		mContainerMeritA.setTag(MERIT_A);
		mContainerMeritA.setOnClickListener(mTypeClickListener);

		mTxtCountMeritB.setText(String.valueOf(absMap.get(MERIT_B)));
		mContainerMeritB.setTag(MERIT_B);
		mContainerMeritB.setOnClickListener(mTypeClickListener);

		mTxtCountMeritC.setText(String.valueOf(absMap.get(MERIT_C)));
		mContainerMeritC.setTag(MERIT_C);
		mContainerMeritC.setOnClickListener(mTypeClickListener);

		mTxtCountDemeritA.setText(String.valueOf(absMap.get(DEMERIT_A)));
		mContainerDemeritA.setTag(DEMERIT_A);
		mContainerDemeritA.setOnClickListener(mTypeClickListener);

		mTxtCountDemeritB.setText(String.valueOf(absMap.get(DEMERIT_B)));
		mContainerDemeritB.setTag(DEMERIT_B);
		mContainerDemeritB.setOnClickListener(mTypeClickListener);

		mTxtCountDemeritC.setText(String.valueOf(absMap.get(DEMERIT_C)));
		mContainerDemeritC.setTag(DEMERIT_C);
		mContainerDemeritC.setOnClickListener(mTypeClickListener);

		changeSelectedType(mContainerAll);
		// int dp5 = mActivityHelper.getScreen().toPixelInt(5);
		//
		// LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		// LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		//
		// List<String> keys = new ArrayList<String>(absMap.keySet());
		// Collections.sort(keys, new Comparator<String>() {
		//
		// @Override
		// public int compare(String lhs, String rhs) {
		// if (lhs.equals(MERIT_ALL))
		// return -1;
		// if (rhs.equals(MERIT_ALL))
		// return 1;
		// return lhs.compareTo(rhs);
		// }
		// });
		//
		// for (String type : keys) {
		// TextView textView = new TextView(mActivity);
		// textView.setGravity(Gravity.CENTER);
		// textView.setMinimumWidth(mActivityHelper.getScreen()
		// .toPixelInt(100));
		// textView.setBackgroundResource(R.drawable.back);
		// textView.setPadding(dp5 * 2, dp5, dp5 * 2, dp5);
		//
		// int count = absMap.get(type);
		// String display = "%s ( %d )";
		// display = String.format(display, type, count);
		// textView.setText(display);
		// textView.setTag(type);
		//
		// textView.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// changeSelectedType(v);
		// }
		// });
		//
		// mContainerType.addView(textView, params);
		//
		// if (type.equals(MERIT_ALL)) {
		// changeSelectedType(textView);
		// }
		// }
	}

	private void changeSelectedType(View selectedView) {
		for (int i = 0; i < mContainerType.getChildCount(); i++) {
			View child = mContainerType.getChildAt(i);
			if (child != selectedView) {
				child.setBackgroundResource(R.drawable.back);
			} else {
				child.setBackgroundResource(R.drawable.back2);
			}
		}

		mCurrentSelectedType = (String) selectedView.getTag();
		final String allString = mActivity.getString(R.string.discipline_all);

		mDisplayElements.clear();
		if (mCurrentSelectedType.equals(allString)) {
			mDisplayElements.addAll(mDateElements);
		} else {
			for (Element dateElement : mDateElements) {
				Element meritElement = XmlUtil.selectElement(dateElement,
						"Merit");
				Element demeritElement = XmlUtil.selectElement(dateElement,
						"Demerit");

				int value = 0;
				if (mCurrentSelectedType.equals(MERIT_A)
						&& meritElement != null) {
					value = StringUtil.convertToInt(meritElement
							.getAttribute("A"));
				} else if (mCurrentSelectedType.equals(MERIT_B)
						&& meritElement != null) {
					value = StringUtil.convertToInt(meritElement
							.getAttribute("B"));
				} else if (mCurrentSelectedType.equals(MERIT_C)
						&& meritElement != null) {
					value = StringUtil.convertToInt(meritElement
							.getAttribute("C"));
				} else if (mCurrentSelectedType.equals(DEMERIT_A)
						&& demeritElement != null) {
					value = StringUtil.convertToInt(demeritElement
							.getAttribute("A"));
				} else if (mCurrentSelectedType.equals(DEMERIT_B)
						&& demeritElement != null) {
					value = StringUtil.convertToInt(demeritElement
							.getAttribute("B"));
				} else if (mCurrentSelectedType.equals(DEMERIT_C)
						&& demeritElement != null) {
					value = StringUtil.convertToInt(demeritElement
							.getAttribute("C"));
				}

				if (value != 0) {
					mDisplayElements.add(dateElement);
				}
			}
		}

		mAdapter.notifyDataSetChanged();
	}

	private class AbsAdapter extends ArrayAdapter<Element> {
		private LayoutInflater _inflater;

		public AbsAdapter() {
			super(mActivity, R.layout.item_attendance, mDisplayElements);

			_inflater = (LayoutInflater) mActivity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = _inflater.inflate(R.layout.item_discipline, null);
				holder.txtDate = (TextView) convertView
						.findViewById(R.id.txtDate);
				holder.txtDetail = (TextView) convertView
						.findViewById(R.id.txtDetail);
				holder.txtCountA = (TextView) convertView
						.findViewById(R.id.txtCountA);
				holder.txtCountB = (TextView) convertView
						.findViewById(R.id.txtCountB);
				holder.txtCountC = (TextView) convertView
						.findViewById(R.id.txtCountC);
				holder.txtNameA = (TextView) convertView
						.findViewById(R.id.txtNameA);
				holder.txtNameB = (TextView) convertView
						.findViewById(R.id.txtNameB);
				holder.txtNameC = (TextView) convertView
						.findViewById(R.id.txtNameC);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			Element dayElement = mDisplayElements.get(position);
			holder.txtDate.setText(dayElement.getAttribute("OccurDate"));
			holder.txtDetail.setText(XmlUtil.getElementText(dayElement,
					"Reason"));

			int flag = StringUtil.convertToInt(dayElement
					.getAttribute("MeritFlag"));

			Element meritElement;
			if (flag == 1) {
				meritElement = XmlUtil.selectElement(dayElement, "Merit");
				holder.txtNameA.setText(MERIT_A);
				holder.txtNameB.setText(MERIT_B);
				holder.txtNameC.setText(MERIT_C);
				holder.txtCountA.setBackgroundResource(R.drawable.merit_a);
				holder.txtCountB.setBackgroundResource(R.drawable.merit_b);
				holder.txtCountC.setBackgroundResource(R.drawable.merit_c);
			} else {
				meritElement = XmlUtil.selectElement(dayElement, "Demerit");
				holder.txtNameA.setText(DEMERIT_A);
				holder.txtNameB.setText(DEMERIT_B);
				holder.txtNameC.setText(DEMERIT_C);
				holder.txtCountA.setBackgroundResource(R.drawable.demerit_a);
				holder.txtCountB.setBackgroundResource(R.drawable.demerit_b);
				holder.txtCountC.setBackgroundResource(R.drawable.demerit_c);
			}

			holder.txtCountA.setText(meritElement.getAttribute("A"));
			holder.txtCountB.setText(meritElement.getAttribute("B"));
			holder.txtCountC.setText(meritElement.getAttribute("C"));
			return convertView;
		}

		@Override
		public boolean isEnabled(int position) {
			return false;
		}
	}

	class ViewHolder {
		TextView txtDate;
		TextView txtDetail;
		TextView txtCountA;
		TextView txtCountB;
		TextView txtCountC;
		TextView txtNameA;
		TextView txtNameB;
		TextView txtNameC;
	}
}
