package tw.com.ischool.parent.tabs.others.sems_score;

import ischool.dsa.client.OnReceiveListener;
import ischool.dsa.utility.Converter;
import ischool.dsa.utility.DSResponse;
import ischool.dsa.utility.XmlHelper;
import ischool.dsa.utility.XmlUtil;
import ischool.dsa.utility.http.Cancelable;
import ischool.utilities.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.w3c.dom.Element;

import tw.com.ischool.parent.ChildInfo;
import tw.com.ischool.parent.Parent;
import tw.com.ischool.parent.R;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class JHSemScoreFragment extends Fragment {

	private static final String ATTR_SCORE = "Score"; // 成績
	private static final String ATTR_TEXT_SCORE = "TextScore"; // 文字描述
	private static final String ATTR_WEIGHT = "Weight"; // 權數
	private static final String ATTR_SUBJECT = "Subject"; // 科目
	private static final String ATTR_DEGREE = "Degree"; // 努力程度
	private static final String ATTR_PERIOD_COUNT = "PeriodCount"; // 節數
	private static final String ATTR_MEMO = "Memo"; // 註記
	private static final String ATTR_DOMAIN = "Domain"; // 領域

	private Activity mActivity;

	private ListView mListView;
	private TextView mTxtBadCount;
	private TextView mTxtCourseLearnScore;
	private TextView mTxtLearnDomainScore;
	private Spinner mSpinnerSemester;
	private List<Semester> mSemesters;
	private SemesAdapter mAdapter;
	private ChildInfo mSelectedChild;
	private List<ScoreInfo> mScoreInfos;
	private ScoreAdapter mScoreAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_jh_semsester_score,
				container, false);

		mListView = (ListView) view.findViewById(R.id.listView);
		mTxtBadCount = (TextView) view.findViewById(R.id.txtBadCount);
		mTxtCourseLearnScore = (TextView) view
				.findViewById(R.id.txtCourseLearnScore);
		mTxtLearnDomainScore = (TextView) view
				.findViewById(R.id.txtLearnDomainScore);
		mSpinnerSemester = (Spinner) view.findViewById(R.id.spinnerSemester);
		mSemesters = new ArrayList<Semester>();
		mScoreInfos = new ArrayList<JHSemScoreFragment.ScoreInfo>();

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mActivity = getActivity();

		TextView emptyText = (TextView) mActivity
				.findViewById(R.id.empty_view_jh_semester_score);
		mListView.setEmptyView(emptyText);
		
		mAdapter = new SemesAdapter();
		mSpinnerSemester.setAdapter(mAdapter);
		mSpinnerSemester
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						Semester s = mSemesters.get(position);
						onSemesterChanged(s);
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub

					}
				});

		mSelectedChild = (ChildInfo) getArguments().getSerializable(
				SemsScoreActivity.PARAM_SELECTED_CHILD);

		mScoreAdapter = new ScoreAdapter();
		mListView.setAdapter(mScoreAdapter);

		loadSemester();

		// getScores();
	}

	private void loadSemester() {
		// semesterScoreJH.GetChildSemsScore

		final ProgressDialog dialog = new ProgressDialog(mActivity);
		dialog.setTitle(mActivity.getString(R.string.progress_title));
		dialog.setMessage(mActivity
				.getString(R.string.jh_semester_score_semester_loading));
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

		Element content = XmlUtil.createElement("Request");
		XmlUtil.addElement(content, "RefStudentId",
				mSelectedChild.getStudentId());

		mSelectedChild.callService(Parent.CONTRACT_PARENT,
				Parent.SERVICE_JH_SEMESTER_SCORE, content,
				new OnReceiveListener<DSResponse>() {

					@Override
					public void onReceive(DSResponse result) {
						onSemesterReceived(result);
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

	private void getScores(final Semester semester) {
		final ProgressDialog dialog = new ProgressDialog(mActivity);
		dialog.setTitle(mActivity.getString(R.string.progress_title));
		dialog.setMessage(mActivity
				.getString(R.string.jh_semester_score_semester_loading));
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

		Element content = XmlUtil.createElement("Request");
		XmlUtil.addElement(content, "RefStudentId",
				mSelectedChild.getStudentId());
		XmlUtil.addElement(content, "SchoolYear", semester.getSchoolYear());
		XmlUtil.addElement(content, "Semester", semester.getSemester());
		XmlUtil.addElement(content, "All");

		mSelectedChild.callService(Parent.CONTRACT_PARENT,
				Parent.SERVICE_JH_SEMESTER_SCORE, content,
				new OnReceiveListener<DSResponse>() {

					@Override
					public void onReceive(DSResponse result) {
						Element rsp = result.getContent();
						Element sss = XmlUtil.selectElement(rsp,
								"SemsSubjScore");
						String scoreInfoString = XmlUtil.getElementText(sss,
								"ScoreInfo");

						scoreInfoString = scoreInfoString.replaceAll("努力程度",
								ATTR_DEGREE);
						scoreInfoString = scoreInfoString.replaceAll("成績",
								ATTR_SCORE);
						scoreInfoString = scoreInfoString.replaceAll("文字描述",
								ATTR_TEXT_SCORE);
						scoreInfoString = scoreInfoString.replaceAll("權數",
								ATTR_WEIGHT);
						scoreInfoString = scoreInfoString.replaceAll("註記",
								ATTR_MEMO);
						scoreInfoString = scoreInfoString.replaceAll("節數",
								ATTR_PERIOD_COUNT);
						scoreInfoString = scoreInfoString.replaceAll("領域",
								ATTR_DOMAIN);
						scoreInfoString = scoreInfoString.replaceAll("科目",
								ATTR_SUBJECT);

						Element scoreInfo = XmlHelper.parseXml("<Root>"
								+ scoreInfoString + "</Root>");

						semester.setScoreInfo(scoreInfo);

						onScoreReceived(scoreInfo);
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

	private void onSemesterReceived(DSResponse response) {
		Element content = response.getContent();
		mSemesters.clear();

		for (Element sss : XmlUtil.selectElements(content, "SemsSubjScore")) {

			Semester s = new Semester(sss);
			mSemesters.add(s);
		}

		Collections.sort(mSemesters, new Comparator<Semester>() {

			@Override
			public int compare(Semester lhs, Semester rhs) {
				int ly = Converter.toInteger(lhs.getSchoolYear());
				int ry = Converter.toInteger(rhs.getSchoolYear());
				
				if(ly > ry) return -1;
				if(ly < ry) return 1;
				
				int ls = Converter.toInteger(lhs.getSemester());
				int rs = Converter.toInteger(rhs.getSemester());
				
				if(ls > rs) return -1;
				if(ls < rs) return 1;
				
				return 0;
			}
		});

		mAdapter.notifyDataSetChanged();
	}

	private void onScoreReceived(Element scoreInfoElement) {
		mScoreInfos.clear();
		Element domains = XmlUtil.selectElement(scoreInfoElement, "Domains");
		Element sss = XmlUtil.selectElement(scoreInfoElement,
				"SemesterSubjectScoreInfo");

		int badCount = 0;
		for (Element domain : XmlUtil.selectElements(domains, "Domain")) {
			ScoreInfo si = new ScoreInfo();
			si.domain = domain.getAttribute(ATTR_DOMAIN);
			si.score = domain.getAttribute(ATTR_SCORE);
			si.subject = domain.getAttribute(ATTR_SUBJECT);
			si.textScore = domain.getAttribute(ATTR_TEXT_SCORE);
			si.weight = domain.getAttribute(ATTR_WEIGHT);
			si.isDomain = true;
			mScoreInfos.add(si);

			Double d = Double.parseDouble(si.score);
			if (d < 60)
				badCount++;

			for (Element subject : XmlUtil.selectElementsByAttribute(sss,
					"Subject", ATTR_DOMAIN, si.domain)) {
				ScoreInfo subjectScoreInfo = new ScoreInfo();
				subjectScoreInfo.domain = subject.getAttribute(ATTR_DOMAIN);
				subjectScoreInfo.score = subject.getAttribute(ATTR_SCORE);
				subjectScoreInfo.subject = subject.getAttribute(ATTR_SUBJECT);
				subjectScoreInfo.textScore = subject
						.getAttribute(ATTR_TEXT_SCORE);
				subjectScoreInfo.weight = subject.getAttribute(ATTR_WEIGHT);
				subjectScoreInfo.isDomain = false;
				mScoreInfos.add(subjectScoreInfo);
			}
		}

		mTxtBadCount.setText(String.valueOf(badCount));

		String ldScore = XmlUtil.getElementText(scoreInfoElement,
				"LearnDomainScore");
		mTxtLearnDomainScore.setText(ldScore);

		String clScore = XmlUtil.getElementText(scoreInfoElement,
				"CourseLearnScore");
		mTxtCourseLearnScore.setText(clScore);

		mScoreAdapter.notifyDataSetChanged();
	}

	private void onSemesterChanged(Semester s) {
		Element scoreInfo = s.getScoreInfos();

		if (scoreInfo == null) {
			getScores(s);
		} else {
			onScoreReceived(scoreInfo);
		}

	}

	private class SemesAdapter extends BaseAdapter {

		private LayoutInflater _inflater;

		SemesAdapter() {
			_inflater = (LayoutInflater) mActivity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mSemesters.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mSemesters.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int index, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = _inflater.inflate(R.layout.item_spinner, parent,
						false);
				holder = new ViewHolder();
				holder.textView = (TextView) convertView
						.findViewById(R.id.textView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			Semester s = mSemesters.get(index);
			String tmp = mActivity
					.getString(R.string.jh_semester_score_semester_temp);
			String title = String.format(tmp, s.getSchoolYear(),
					s.getSemester());
			holder.textView.setText(title);
			return convertView;
		}

	}

	class ViewHolder {
		TextView textView;
	}

	private class Semester {
		private String _schoolYear;
		private String _semester;
		private Element _scoreInfo;

		public Semester(Element sss) {
			String schoolYear = sss.getAttribute("SchoolYear");
			String semester = sss.getAttribute("Semester");
			_schoolYear = schoolYear;
			_semester = semester;
		}

		public String getSchoolYear() {
			return _schoolYear;
		}

		public String getSemester() {
			return _semester;
		}

		public Element getScoreInfos() {
			return _scoreInfo;
		}

		public void setScoreInfo(Element scoreInfo) {
			_scoreInfo = scoreInfo;
		}
	}
	
	private class ScoreInfo {
		String domain;
		String subject;
		String weight;
		String score;
		String textScore;
		boolean isDomain = false;

		boolean isPass() {
			double d = Double.parseDouble(score);
			if (d >= 60)
				return true;
			return false;
		}
	}

	private class ScoreAdapter extends BaseAdapter {

		private LayoutInflater _inflater;

		public ScoreAdapter() {
			_inflater = (LayoutInflater) mActivity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mScoreInfos.size();
		}

		@Override
		public Object getItem(int position) {
			return mScoreInfos.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ScoreInfo s = mScoreInfos.get(position);

			if (s.isDomain) {
				convertView = _inflater.inflate(
						R.layout.item_jh_sems_score_domain, parent, false);

				TextView textView = (TextView) convertView
						.findViewById(R.id.txtSubject);
				textView.setText(s.domain);

			} else {
				convertView = _inflater.inflate(R.layout.item_jh_sems_score,
						parent, false);

				TextView textView = (TextView) convertView
						.findViewById(R.id.txtSubject);
				textView.setText(s.subject);
			}

			TextView txtScore = (TextView) convertView
					.findViewById(R.id.txtScore);
			txtScore.setText(s.score);

			TextView txtWeight = (TextView) convertView
					.findViewById(R.id.txtWeight);
			txtWeight.setText(s.weight);

			LinearLayout layout = (LinearLayout) convertView
					.findViewById(R.id.layoutTextScore);
			TextView txtTextScore = (TextView) convertView
					.findViewById(R.id.txtTextScore);

			if (layout != null) {
				if (!StringUtil.isNullOrWhitespace(s.textScore)) {
					layout.setVisibility(View.VISIBLE);
					txtTextScore.setText(s.textScore);
				} else {
					layout.setVisibility(View.GONE);
					txtTextScore.setText(StringUtil.EMPTY);
				}
			}

			ImageView imgGot = (ImageView) convertView
					.findViewById(R.id.imgGot);

			double score = Double.parseDouble(s.score);
			if (score >= 60) {
				imgGot.setVisibility(View.VISIBLE);
			} else {
				imgGot.setVisibility(View.GONE);
			}
			return convertView;
		}

	}
}
