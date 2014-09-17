package tw.com.ischool.parent.tabs.others.sems_score;

import ischool.dsa.client.OnReceiveListener;
import ischool.dsa.utility.DSResponse;
import ischool.dsa.utility.XmlHelper;
import ischool.dsa.utility.XmlUtil;
import ischool.dsa.utility.http.Cancelable;
import ischool.utilities.StringUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import tw.com.ischool.parent.ChildInfo;
import tw.com.ischool.parent.Parent;
import tw.com.ischool.parent.R;
import tw.com.ischool.parent.util.SimpleArrayAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ScoreFragment extends Fragment {

	private static final String PARAM_SCORE = "score";
	// private static final String PARAM_SCHOOL_YEAR = "schoolyear";
	private static final String PARAM_SEMESTER = "semester";
	// private static final String PARAM_SCHOOL_YEAR_INDEX = "schoolyearIndex";
	private static final String PARAM_SEMESTER_INDEX = "semesterIndex";

	private static final String NOT_CREDIT = "NotCredit";
	private static final String NOT_COMMENT = "NotComment";
	private static final String NECESSARY = "Necessary";
	private static final String DEPT_OR_SCHOOL = "DeptOrSchool";
	private static final String ORIGINAL_SCORE = "OriginalScore";
	private static final String MODIFY_SCORE = "ModifyScore";
	private static final String BEST_SCORE = "BestScore";
	private static final String GOT_CREDIT = "GotCredit";
	private static final String SUBJECT = "Subject";
	private static final String SUBJECT_GRADE_YEAR = "SubjectGrade";
	private static final String RETEST_SCORE = "RetestScore";
	private static final String RESTUDY_SCORE = "RestudyScore";
	private static final String OPEN_TYPE = "Type";
	private static final String CREDIT_COUNT = "Credit";

	// private Spinner mSpinnerSchoolYear;
	private Spinner mSpinnerSemester;
	private SemesterList mSemesterList = new SemesterList();
	// private ArrayList<String> mSchoolYearList = new ArrayList<String>();
	// private ArrayAdapter<String> mSchoolYearAdapter;
	private SemesAdapter mSemesterAdapter;

	private TextView mTxtStudy;
	private TextView mTxtGot;
	private TextView mTxtNecessary;
	private TextView mTxtNecessaryDept;
	private TextView mTxtNecessarySchool;
	private TextView mTxtUnnecessary;
	private TextView mTxtUnnecessaryDept;
	private TextView mTxtUnnecessarySchool;
	private TextView mTxtIntern;
	// private PullToRefreshLayout mPullToRefreshLayout;
	private ListView mListView;
	private boolean mActivityCreated = false;
	private Element mSourceElement;
	// private Element mStudentElement;
	private ChildInfo mSelectedChild;
	private Activity mActivity;

	private List<Element> mAllElementList = new ArrayList<Element>();
	private ArrayList<SubjectInfo> mDisplayList = new ArrayList<SubjectInfo>();
	private ScoreAdapter mAdapter;

	private OnItemSelectedListener mSemesterSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long itemId) {

			Semester s = mSemesterList.get(position);

			filterSemester(s.getSchoolYear(), s.getSemester());
			mAdapter.notifyDataSetChanged();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	// private OnRefreshListener mRefreshListener = new OnRefreshListener() {
	//
	// @Override
	// public void onRefreshStarted(View view) {
	// if (mSpinnerSchoolYear.getSelectedItem() == null
	// || mSpinnerSemester.getSelectedItem() == null) {
	// mPullToRefreshLayout.setRefreshComplete();
	// return;
	// }
	//
	// String studentId = XmlUtil.getElementText(mStudentElement,
	// "StudentID");
	//
	// Element request = XmlUtil.createElement("Request");
	// XmlUtil.addElement(request, "RefStudentId", studentId);
	// XmlUtil.addElement(request, "All");
	// XmlUtil.addElement(request, "SchoolYear", mSpinnerSchoolYear
	// .getSelectedItem().toString());
	// XmlUtil.addElement(request, "Semester", mSpinnerSemester
	// .getSelectedItem().toString());
	//
	// DSS.sendRequest(mActivity, "student.GetSemsSubjectScore", request,
	// new OnReceiveListener<DSResponse>() {
	//
	// @Override
	// protected void onReceive(DSResponse result) {
	// onLoadScoreSucceed(result);
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_student_score,
				container, false);

		mListView = (ListView) view.findViewById(R.id.lvScore);
		mTxtStudy = (TextView) view.findViewById(R.id.txtStudy);
		mTxtGot = (TextView) view.findViewById(R.id.txtGot);
		mTxtIntern = (TextView) view.findViewById(R.id.txtIntern);
		mTxtNecessary = (TextView) view.findViewById(R.id.txtNecessary);
		mTxtNecessaryDept = (TextView) view.findViewById(R.id.txtNecessaryDept);
		mTxtNecessarySchool = (TextView) view
				.findViewById(R.id.txtNecessarySchool);
		mTxtUnnecessary = (TextView) view.findViewById(R.id.txtUnnecessary);
		mTxtUnnecessaryDept = (TextView) view
				.findViewById(R.id.txtUnnecessaryDept);
		mTxtUnnecessarySchool = (TextView) view
				.findViewById(R.id.txtUnnecessarySchool);
		mSpinnerSemester = (Spinner) view.findViewById(R.id.spinnerSemester);
		// mSpinnerSchoolYear = (Spinner) view
		// .findViewById(R.id.spinnerSchoolYear);

		// Retrieve the PullToRefreshLayout from the content view
		// mPullToRefreshLayout = (PullToRefreshLayout) view
		// .findViewById(R.id.ptr_score);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mActivity = getActivity();

		// mSchoolYearAdapter = new ArrayAdapter<String>(mActivity,
		// android.R.layout.simple_spinner_item, android.R.id.text1,
		// mSchoolYearList);
		// mSchoolYearAdapter
		// .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// mSpinnerSchoolYear.setAdapter(mSchoolYearAdapter);
		// mSpinnerSchoolYear.setOnItemSelectedListener(mSemesterSelectedListener);

		// TODO
		mSemesterAdapter = new SemesAdapter(mActivity, mSemesterList);		
		mSpinnerSemester.setAdapter(mSemesterAdapter);
		mSpinnerSemester.setOnItemSelectedListener(mSemesterSelectedListener);

		mAdapter = new ScoreAdapter();
		mListView.setAdapter(mAdapter);

		// Now setup the PullToRefreshLayout
		// ActionBarPullToRefresh.from(mActivity).allChildrenArePullable()
		// .listener(mRefreshListener).setup(mPullToRefreshLayout);

		if (savedInstanceState != null) {
			mSelectedChild = (ChildInfo) savedInstanceState
					.getSerializable(SemsScoreActivity.PARAM_SELECTED_CHILD);
			mSourceElement = XmlHelper.parseXml(savedInstanceState
					.getString(PARAM_SCORE));

			// mSchoolYearList.clear();
			// mSemesterList.clear();
			//
			// mSchoolYearList.addAll(savedInstanceState
			// .getStringArrayList(PARAM_SCHOOL_YEAR));

			SemesterList list = (SemesterList) savedInstanceState
					.getSerializable(PARAM_SEMESTER);
			mSemesterList.addAll(list);

			// mSchoolYearAdapter.notifyDataSetChanged();
			mSemesterAdapter.notifyDataSetChanged();
			// mSpinnerSchoolYear.setSelection(savedInstanceState
			// .getInt(PARAM_SCHOOL_YEAR_INDEX));
			mSpinnerSemester.setSelection(savedInstanceState
					.getInt(PARAM_SEMESTER_INDEX));
		} else {
			mSelectedChild = (ChildInfo) getArguments().getSerializable(
					SemsScoreActivity.PARAM_SELECTED_CHILD);

			mActivityCreated = true;
			loadSemesters();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(PARAM_SCORE,
				XmlHelper.convertToString(mSourceElement));
		outState.putSerializable(SemsScoreActivity.PARAM_SELECTED_CHILD,
				mSelectedChild);
		// outState.putStringArrayList(PARAM_SCHOOL_YEAR, mSchoolYearList);
		outState.putSerializable(PARAM_SEMESTER, mSemesterList);
		// outState.putInt(PARAM_SCHOOL_YEAR_INDEX,
		// mSpinnerSchoolYear.getSelectedItemPosition());
		outState.putInt(PARAM_SEMESTER_INDEX,
				mSpinnerSemester.getSelectedItemPosition());
		super.onSaveInstanceState(outState);
	}

	public void loadSemesters() {
		if (!mActivityCreated || mSelectedChild == null)
			return;

		Element request = XmlUtil.createElement("Request");
		XmlUtil.addElement(request, "RefStudentId",
				mSelectedChild.getStudentId());

		final ProgressDialog dialog = new ProgressDialog(mActivity);
		dialog.setTitle(mActivity.getString(R.string.progress_title));
		dialog.setMessage(mActivity.getString(R.string.score_loading_semester));
		dialog.setCancelable(false);

		// final ProgressDialog progress = ProgressDialog.show(mActivity,
		// mActivity.getString(R.string.progress_title),
		// mActivity.getString(R.string.score_loading_semester));

		final Cancelable cancelable = new Cancelable();

		dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
				getString(R.string.cancel), new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						cancelable.setCancel(true);
						dialog.dismiss();
					}
				});

		dialog.show();

		mSelectedChild.callParentService(Parent.SERVICE_GET_SH_SEMES_SCORE,
				request, new OnReceiveListener<DSResponse>() {

					@Override
					public void onReceive(DSResponse result) {
						onLoadSemesterSucceed(result);
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

	private void onLoadSemesterSucceed(DSResponse result) {
		mSourceElement = result.getContent();

		mAllElementList = XmlUtil.selectElements(mSourceElement,
				"SemsSubjScore");

		// mSchoolYearList.clear();
		mSemesterList.clear();

		for (Element e : mAllElementList) {
			Semester semester = new Semester(e);
			mSemesterList.add(semester);
		}

		// mSchoolYearAdapter.notifyDataSetChanged();
		mSemesterAdapter.notifyDataSetChanged();

		if (mSemesterList.size() > 0) {
			Semester s = mSemesterList.get(0);
			filterSemester(s.getSchoolYear(), s.getSemester());
		} else {
			TextView emptyText = (TextView) mActivity
					.findViewById(R.id.empty_view_score);
			mListView.setEmptyView(emptyText);
		}
	}

	private void filterSemester(String schoolYear, String semester) {

		String studentId = mSelectedChild.getStudentId();

		Element request = XmlUtil.createElement("Request");
		XmlUtil.addElement(request, "RefStudentId", studentId);
		XmlUtil.addElement(request, "All");
		XmlUtil.addElement(request, "SchoolYear", schoolYear);
		XmlUtil.addElement(request, "Semester", semester);

		// final ProgressDialog progress = ProgressDialog.show(mActivity,
		// mActivity.getString(R.string.progress_title),
		// mActivity.getString(R.string.score_loading));

		final ProgressDialog dialog = new ProgressDialog(mActivity);
		dialog.setTitle(mActivity.getString(R.string.progress_title));
		dialog.setMessage(mActivity.getString(R.string.score_loading));
		dialog.setCancelable(false);

		final Cancelable cancelable = new Cancelable();

		dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
				getString(R.string.cancel), new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						cancelable.setCancel(true);
						dialog.dismiss();
					}
				});

		dialog.show();

		mSelectedChild.callService(Parent.CONTRACT_PARENT,
				Parent.SERVICE_GET_SH_SEMES_SCORE, request,
				new OnReceiveListener<DSResponse>() {

					@Override
					public void onReceive(DSResponse result) {
						onLoadScoreSucceed(result);
						dialog.dismiss();
					}

					@Override
					public void onError(Exception ex) {
						Toast.makeText(mActivity, ex.toString(),
								Toast.LENGTH_LONG).show();

						mAdapter.notifyDataSetChanged();
						dialog.dismiss();
					}

				}, cancelable);
	}

	private void onLoadScoreSucceed(DSResponse result) {
		TextView emptyText = (TextView) mActivity
				.findViewById(R.id.empty_view_score);
		mListView.setEmptyView(emptyText);

		Element rsp = result.getContent();
		Element e = XmlUtil.selectElement(rsp, "SemsSubjScore");
		String scoreInfo = XmlUtil.getElementText(e, "ScoreInfo");
		scoreInfo = scoreInfo.replace("不計學分", NOT_CREDIT)
				.replace("不需評分", NOT_COMMENT).replace("修課必選修", NECESSARY)
				.replace("修課校部訂", DEPT_OR_SCHOOL)
				.replace("原始成績", ORIGINAL_SCORE)
				.replace("學年調整成績", MODIFY_SCORE).replace("擇優採計成績", BEST_SCORE)
				.replace("是否取得學分", GOT_CREDIT)
				.replace("科目級別", SUBJECT_GRADE_YEAR).replace("科目", SUBJECT)
				.replace("補考成績", RETEST_SCORE).replace("重修成績", RESTUDY_SCORE)
				.replace("開課分項類別", OPEN_TYPE).replace("開課學分數", CREDIT_COUNT);

		mDisplayList.clear();

		Element fieldElement = XmlHelper.parseXml(scoreInfo);
		int totalCount = 0, gotCount = 0, necessaryCount = 0, deptNecessaryCount = 0, schoolNecessaryCount = 0, unnecessaryCount = 0, deptUnnecessaryCount = 0, schoolUnnecessaryCount = 0, internCount = 0;
		for (Element subjectElement : XmlUtil.selectElements(fieldElement,
				"Subject")) {
			SubjectInfo info = new SubjectInfo(subjectElement);
			mDisplayList.add(new SubjectInfo(subjectElement));

			if (!info.countOn)
				continue;

			int credit = info.credit;
			totalCount += credit;

			if (info.gotCredit) {
				gotCount += credit;

				if (info.openType.equals("實習科目")) {
					internCount += credit;
				}

				if (info.necessary.equals("必修")) {
					necessaryCount += credit;

					if (info.deptOrSchool.equals("部訂")) {
						deptNecessaryCount += credit;
					} else {
						schoolNecessaryCount += credit;
					}
				} else if (info.necessary.equals("選修")) {
					unnecessaryCount += credit;

					if (info.deptOrSchool.equals("部訂")) {
						deptUnnecessaryCount += credit;
					} else {
						schoolUnnecessaryCount += credit;
					}
				}
			}
		}

		mTxtStudy.setText(String.valueOf(totalCount));
		mTxtGot.setText(String.valueOf(gotCount));
		mTxtNecessary.setText(String.valueOf(necessaryCount));
		mTxtNecessaryDept.setText(String.valueOf(deptNecessaryCount));
		mTxtNecessarySchool.setText(String.valueOf(schoolNecessaryCount));
		mTxtUnnecessary.setText(String.valueOf(unnecessaryCount));
		mTxtUnnecessaryDept.setText(String.valueOf(deptUnnecessaryCount));
		mTxtUnnecessarySchool.setText(String.valueOf(schoolUnnecessaryCount));
		mTxtIntern.setText(String.valueOf(internCount));

		mAdapter.notifyDataSetChanged();
	}

	private class ScoreAdapter extends ArrayAdapter<SubjectInfo> {

		private LayoutInflater _inflater;

		public ScoreAdapter() {
			super(mActivity, R.layout.item_score, mDisplayList);
			_inflater = (LayoutInflater) mActivity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = _inflater.inflate(R.layout.item_score, null);
				holder = new ViewHolder();

				holder.txtCredit = (TextView) convertView
						.findViewById(R.id.txtCredit);
				holder.txtDeptOrSchool = (TextView) convertView
						.findViewById(R.id.txtDeptOrSchool);
				holder.txtNecessary = (TextView) convertView
						.findViewById(R.id.txtNecessary);
				holder.txtScore = (TextView) convertView
						.findViewById(R.id.txtScore);
				holder.txtSubject = (TextView) convertView
						.findViewById(R.id.txtSubject);
				holder.imgGot = (ImageView) convertView
						.findViewById(R.id.imgGot);
				holder.labelCredit = (TextView) convertView
						.findViewById(R.id.labelCredit);
				holder.labelScore = (TextView) convertView
						.findViewById(R.id.labelScore);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			SubjectInfo info = mDisplayList.get(position);
			holder.txtSubject.setText(info.subject);
			holder.txtDeptOrSchool.setText(info.deptOrSchool);
			holder.txtNecessary.setText(info.necessary);
			holder.txtScore.setText(info.getScoreValue());
			holder.txtCredit.setText(String.valueOf(info.credit));
			holder.imgGot.setVisibility(info.gotCredit ? View.VISIBLE
					: View.GONE);

			int color = info.gotCredit ? mActivity.getResources().getColor(
					android.R.color.black) : Color.RED;

			holder.txtSubject.setTextColor(color);
			holder.txtDeptOrSchool.setTextColor(color);
			holder.txtNecessary.setTextColor(color);
			holder.txtScore.setTextColor(color);
			holder.txtCredit.setTextColor(color);
			holder.labelCredit.setTextColor(color);
			holder.labelScore.setTextColor(color);

			return convertView;
		}

		@Override
		public boolean isEnabled(int position) {
			return false;
		}
	}

	private class ViewHolder {
		TextView txtSubject, txtScore, txtDeptOrSchool, txtNecessary,
				txtCredit, labelCredit, labelScore;
		ImageView imgGot;
	}

	private class SubjectInfo {
		String subject, deptOrSchool, necessary, openType;
		double score;
		int credit;
		boolean gotCredit, countOn;

		public SubjectInfo(Element e) {
			subject = e.getAttribute(SUBJECT) + " "
					+ e.getAttribute(SUBJECT_GRADE_YEAR);
			credit = StringUtil.convertToInt(e.getAttribute(CREDIT_COUNT));

			deptOrSchool = e.getAttribute(DEPT_OR_SCHOOL);
			necessary = e.getAttribute(NECESSARY);

			double oriScore = StringUtil.convertToDouble(
					e.getAttribute(ORIGINAL_SCORE), -1);
			double bestScore = StringUtil.convertToDouble(
					e.getAttribute(BEST_SCORE), -1);
			double reScore = StringUtil.convertToDouble(
					e.getAttribute(RESTUDY_SCORE), -1);
			double retestScore = StringUtil.convertToDouble(
					e.getAttribute(RETEST_SCORE), -1);
			score = Math.max(oriScore,
					Math.max(bestScore, Math.max(reScore, retestScore)));

			String gc = e.getAttribute(GOT_CREDIT);
			gotCredit = gc.equals("是");
			countOn = e.getAttribute(NOT_CREDIT).equals("否");
			openType = e.getAttribute(OPEN_TYPE);
		}

		public String getScoreValue() {
			String value = score == -1 ? "--" : String.valueOf(score);
			return value;
		}
	}

	class SemesAdapter extends SimpleArrayAdapter<Semester> {

		public SemesAdapter(Context context, List<Semester> objects) {
			super(context,objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = _inflater.inflate(
						R.layout.item_spinner, null);
				holder = new ViewHolder();
				holder.text1 = (TextView) convertView
						.findViewById(R.id.textView);				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			Semester s = _array.get(position);

			String str = "%s 學年度 %s 學期";
			str = String.format(str, s.getSchoolYear(), s.getSemester());
			holder.text1.setText(str);
			return convertView;
		}

		private class ViewHolder {
			TextView text1;
		}
	}

	class Semester implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String schoolYear;
		private String semester;

		public Semester(Element e) {
			schoolYear = e.getAttribute("SchoolYear");
			semester = e.getAttribute("Semester");
		}

		public String getSchoolYear() {
			return schoolYear;
		}

		public String getSemester() {
			return semester;
		}
	}

	class SemesterList extends ArrayList<Semester> implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	}
}
