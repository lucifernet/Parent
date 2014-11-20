package tw.com.ischool.parent.tabs.others.eval_score;

import ischool.dsa.client.OnReceiveListener;
import ischool.dsa.utility.Converter;
import ischool.dsa.utility.DSResponse;
import ischool.dsa.utility.XmlUtil;
import ischool.dsa.utility.http.Cancelable;
import ischool.utilities.StringUtil;

import java.math.BigDecimal;
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

public class JHEvalScoreFragment extends Fragment {

	private Activity mActivity;

	private ListView mListView;
	private Spinner mSpinnerSemester;
	private Spinner mSpinnerExam;
	private List<Semester> mSemesters;
	private SemesAdapter mAdapter;
	private ChildInfo mSelectedChild;

	private List<Exam> mExamList;
	private ExamAdapter mExamAdapter;

	private List<ScoreInfo> mScoreInfos;
	private ScoreAdapter mScoreAdapter;

	private int mDomainScale = 0;
	private int mSubjectScale = 2;
	private int mDomainRoundingMode = BigDecimal.ROUND_HALF_UP;
	private int mSubjectRoundingMode = BigDecimal.ROUND_HALF_UP;;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_jh_eval_score,
				container, false);

		mListView = (ListView) view.findViewById(R.id.listView);

		mSpinnerSemester = (Spinner) view.findViewById(R.id.spinnerSemester);
		mSpinnerExam = (Spinner) view.findViewById(R.id.spinnerExam);

		mScoreInfos = new ArrayList<ScoreInfo>();
		mSemesters = new ArrayList<Semester>();
		mExamList = new ArrayList<Exam>();

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mActivity = getActivity();

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

		mExamAdapter = new ExamAdapter();
		mSpinnerExam.setAdapter(mExamAdapter);
		mSpinnerExam.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				Exam s = mExamList.get(position);
				onExamChanged(s);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		mSelectedChild = (ChildInfo) getArguments().getSerializable(
				EvalScoreActivity.PARAM_SELECTED_CHILD);

		mScoreAdapter = new ScoreAdapter();
		mListView.setAdapter(mScoreAdapter);

		loadSemester();

		// getScores();
	}

	private void loadSemester() {
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
		XmlUtil.addElement(content, "StudentID", mSelectedChild.getStudentId());

		mSelectedChild.callService(Parent.CONTRACT_PARENT,
				Parent.SERVICE_JH_EVAL_SCORE_GET_SEMESTER, content,
				new OnReceiveListener<DSResponse>() {

					@Override
					public void onReceive(DSResponse result) {
						onSemesterReceived(result, dialog, cancelable);
						// dialog.dismiss();
					}

					@Override
					public void onError(Exception ex) {
						Toast.makeText(mActivity, ex.toString(),
								Toast.LENGTH_LONG).show();
						dialog.dismiss();
					}
				}, cancelable);
	}

	/**
	 * 取回學期後發生
	 * **/
	private void onSemesterReceived(DSResponse result, ProgressDialog dialog,
			Cancelable cancelable) {
		mSemesters.clear();

		Element content = result.getContent();
		for (Element e : XmlUtil.selectElements(content, "Semester")) {
			String schoolYear = e.getAttribute("SchoolYear");
			String semester = e.getAttribute("Semester");
			String examid = e.getAttribute("ExamID");
			String examName = e.getAttribute("ExamName");
			String examOrder = e.getAttribute("ExamDisplayOrder");

			Exam exam = new Exam();
			exam.id = examid;
			exam.name = examName;
			exam.order = Converter.toInteger(examOrder);
			exam.schoolYear = schoolYear;
			exam.semester = semester;

			Semester targetSemester = null;
			for (Semester s : mSemesters) {
				if (s.schoolYear.equals(schoolYear)
						&& s.semester.equals(semester)) {
					targetSemester = s;
					break;
				}
			}

			if (targetSemester == null) {
				targetSemester = new Semester();
				targetSemester.schoolYear = schoolYear;
				targetSemester.semester = semester;
				mSemesters.add(targetSemester);
			}

			boolean found = false;
			for(Exam each : targetSemester.exams){
				if(each.id.equals(exam.id)) {
					found = true;
					break;
				}
			}
			
			if(!found)
				targetSemester.exams.add(exam);
		}

		// sort
		Collections.sort(mSemesters, new Comparator<Semester>() {

			@Override
			public int compare(Semester lhs, Semester rhs) {
				int ly = Converter.toInteger(lhs.schoolYear);
				int ry = Converter.toInteger(rhs.schoolYear);

				if (ly > ry)
					return -1;
				if (ly < ry)
					return 1;

				int ls = Converter.toInteger(lhs.semester);
				int rs = Converter.toInteger(rhs.semester);

				if (ls > rs)
					return -1;
				if (ls < rs)
					return 1;

				return 0;
			}
		});

		for (Semester s : mSemesters) {
			Collections.sort(s.exams, new Comparator<Exam>() {

				@Override
				public int compare(Exam lhs, Exam rhs) {
					int lo = lhs.order;
					int ro = rhs.order;
					if (lo > ro)
						return 1;
					if (lo < ro)
						return -1;
					return 0;
				}
			});
		}

		mAdapter.notifyDataSetChanged();

		loadScoreCalcRule(dialog, cancelable);
	}

	/**
	 * 取得成績計算規則
	 * **/
	private void loadScoreCalcRule(final ProgressDialog dialog,
			final Cancelable cancelable) {
		Element request = XmlUtil.createElement("Request");		
		XmlUtil.addElement(request, "StudentID",
				mSelectedChild.getStudentId());

		dialog.setMessage("取得成績計算規則");

		mSelectedChild.callService(Parent.CONTRACT_PARENT,
				Parent.SERVICE_JH_EVAL_SCORE_GET_CALC_RULE, request,
				new OnReceiveListener<DSResponse>() {

					@Override
					public void onReceive(DSResponse result) {
						Element content = result.getContent();
						String subjectScale = XmlUtil.getElementText(content, "SubjectScales");
						String subjectRule = XmlUtil.getElementText(content, "SubjectRoundRule");
						String domainScale = XmlUtil.getElementText(content, "DomainScales");
						String domainRule = XmlUtil.getElementText(content, "DomainRoundRule");
						
						mSubjectScale = Converter.toInteger(subjectScale);
						mDomainScale = Converter.toInteger(domainScale);
						
						content = XmlUtil.selectElement(content, "Content");
						content = XmlUtil.selectElement(content, "成績計算規則");
						content = XmlUtil.selectElement(content, "各項成績計算位數");


						if (domainRule.equals("無條件進位")) {
							mDomainRoundingMode = BigDecimal.ROUND_UP;
						} else if (domainRule.equals("無條件捨去")) {
							mDomainRoundingMode = BigDecimal.ROUND_DOWN;
						} else {
							mDomainRoundingMode = BigDecimal.ROUND_HALF_UP;
						}

						if (subjectRule.equals("無條件進位")) {
							mDomainRoundingMode = BigDecimal.ROUND_UP;
						} else if (subjectRule.equals("無條件捨去")) {
							mDomainRoundingMode = BigDecimal.ROUND_DOWN;
						} else {
							mDomainRoundingMode = BigDecimal.ROUND_HALF_UP;
						}
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

	/**
	 * 學期改變時
	 * **/
	private void onSemesterChanged(Semester s) {
		int examSelection = mSpinnerExam.getSelectedItemPosition();

		mExamList.clear();
		mExamList.addAll(s.exams);

		mExamAdapter.notifyDataSetChanged();
		if (mExamList.size() > 0 && examSelection != 0)
			mSpinnerExam.setSelection(0);
		else if (examSelection == 0) {
			Exam exam = mExamList.get(0);
			onExamChanged(exam);
		}

	}

	/**
	 * 考試改變時
	 * **/
	private void onExamChanged(Exam exam) {		
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

		Element request = XmlUtil.createElement("Request");
		Element condition = XmlUtil.addElement(request, "Condition");
		XmlUtil.addElement(condition, "StudentID",
				mSelectedChild.getStudentId());
		XmlUtil.addElement(condition, "SchoolYear", exam.schoolYear);
		XmlUtil.addElement(condition, "Semester", exam.semester);
		XmlUtil.addElement(condition, "ExamID", exam.id);

		mSelectedChild.callParentService(
				Parent.SERVICE_JH_EVAL_SCORE_GET_EXAM_SCORE, request,
				new OnReceiveListener<DSResponse>() {

					@Override
					public void onReceive(DSResponse result) {
						onExamScoreReceived(result);
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

	private void onExamScoreReceived(DSResponse result) {
		
		TextView emptyText = (TextView) mActivity
				.findViewById(R.id.empty_view_jh_eval_score);
		mListView.setEmptyView(emptyText);
		
		mScoreInfos.clear();

		Element content = result.getContent();
		Element seme = XmlUtil.selectElement(content, "Seme");

		ArrayList<DomainScore> domains = new ArrayList<DomainScore>();

		for (Element course : XmlUtil.selectElements(seme, "Course")) {
			String domainName = course.getAttribute("Domain");

			DomainScore domain = null;
			for (DomainScore d : domains) {
				if (d.name.equals(domainName)) {
					domain = d;
					break;
				}
			}

			if (domain == null) {
				domain = new DomainScore(domainName);
				domains.add(domain);
			}

			int weight = Converter.toInteger(course.getAttribute("Credit"), 1);
			Element fixTime = XmlUtil.selectElement(course, "FixTime");
			Element fixTimeExt = XmlUtil.selectElement(fixTime, "Extension");
			int regularPercent = Converter.toInteger(XmlUtil.getElementText(
					fixTimeExt, "ScorePercentage"));
			int usualPercent = 100 - regularPercent;

			Element exam = XmlUtil.selectElement(course, "Exam");
			Element detail = XmlUtil.selectElement(exam, "ScoreDetail");
			Element extension = XmlUtil.selectElement(detail, "Extension");
			extension = XmlUtil.selectElement(extension, "Extension");

			String regScore = XmlUtil.getElementText(extension, "Score");

			String usuScore = XmlUtil.getElementText(extension,
					"AssignmentScore");

			String textScore = XmlUtil.getElementText(extension, "Text");

			SubjectScore subject = new SubjectScore();
			subject.name = course.getAttribute("Subject");
			subject.regScore = StringUtil.isNullOrWhitespace(regScore) ? null
					: new BigDecimal(regScore);
			subject.usuScore = StringUtil.isNullOrWhitespace(usuScore) ? null
					: new BigDecimal(usuScore);
			subject.textScore = textScore;
			subject.usuScorePer = new BigDecimal(usualPercent);
			subject.regScorePer = new BigDecimal(regularPercent);
			subject.weight = new BigDecimal(weight);

			domain.subjects.add(subject);
		}

		for (DomainScore d : domains) {
			addToSchoolInfo(d);
		}

		mScoreAdapter.notifyDataSetChanged();
	}

	/**
	 * 將 double 值轉為字串, 這裡要依據成績計算成績計算規則設定的四捨五入來轉化
	 * **/
	private String getDoubleString(BigDecimal b) {
		if (b == null)
			return StringUtil.EMPTY;

		return b.toString();
	}

	/**
	 * 將 domain 內的資訊加入 mScoreInfos 裡
	 * **/
	private void addToSchoolInfo(DomainScore domain) {
		ScoreInfo score = new ScoreInfo();
		score.isDomain = true;
		score.regularScore = "";
		score.improve = 0;
		score.subjectName = domain.name;
		score.textScore = "";
		BigDecimal domainWeightScore = domain.calcWeightScore();
		score.totalScore = getDoubleString(domainWeightScore);
		score.usualScore = "";
		score.weight = "1";
		score.textScore = domain.textScore;
		
		mScoreInfos.add(score);

		for (SubjectScore subject : domain.subjects) {
			score = new ScoreInfo();
			score.isDomain = false;
			score.regularScore = getDoubleString(subject.regScore);
			score.improve = 0;
			score.subjectName = subject.name;
			score.textScore = subject.textScore;
			score.totalScore = getDoubleString(subject.calcTotalScore());
			score.usualScore = getDoubleString(subject.usuScore);
			score.weight = subject.weight.toString();
			mScoreInfos.add(score);
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
			String title = String.format(tmp, s.schoolYear, s.semester);
			holder.textView.setText(title);
			return convertView;
		}

	}

	class ViewHolder {
		TextView textView;
	}

	private class Semester {
		private String schoolYear;
		private String semester;
		private List<Exam> exams;

		public Semester() {
			exams = new ArrayList<Exam>();
		}
	}

	private class Exam {
		private String id;
		private int order;
		private String name;
		private String schoolYear;
		private String semester;
	}

	private class ExamAdapter extends BaseAdapter {

		private LayoutInflater _inflater;

		ExamAdapter() {
			_inflater = (LayoutInflater) mActivity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mExamList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mExamList.get(arg0);
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

			Exam s = mExamList.get(index);
			holder.textView.setText(s.name);
			return convertView;
		}

	}

	private class ScoreInfo {
		private boolean isDomain;
		private int improve = 0;
		private String regularScore;
		private String usualScore;
		private String totalScore;
		private String weight;
		private String subjectName;
		private String textScore;
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
						R.layout.item_jh_eval_score_domain, parent, false);

				TextView txtWeightScore = (TextView) convertView
						.findViewById(R.id.txtWeightScore);
				txtWeightScore.setText(s.totalScore);
			} else {
				convertView = _inflater.inflate(R.layout.item_jh_eval_score,
						parent, false);

				TextView txtRegularScore = (TextView) convertView
						.findViewById(R.id.txtRegularScore);
				txtRegularScore.setText(s.regularScore);
			}

			// 名稱
			TextView textView = (TextView) convertView
					.findViewById(R.id.txtSubject);
			textView.setText(s.subjectName);

			// 權重
			TextView txtWeight = (TextView) convertView
					.findViewById(R.id.txtWeight);
			if (txtWeight != null)
				txtWeight.setText(String.valueOf(s.weight));

			// 文字評量
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

			// 進退步
			ImageView imgImprove = (ImageView) convertView
					.findViewById(R.id.imgImprove);
			if (s.improve == -1) {
				imgImprove.setImageResource(R.drawable.down);
			} else if (s.improve == 1) {
				imgImprove.setImageResource(R.drawable.up);
			} else {
				imgImprove.setVisibility(View.GONE);
			}

			// 平時成績
			TextView txtUauslScore = (TextView) convertView
					.findViewById(R.id.txtUauslScore);
			if (txtUauslScore != null)
				txtUauslScore.setText(s.usualScore);

			// 定期評量
			TextView txtRegularScore = (TextView) convertView
					.findViewById(R.id.txtRegularScore);
			if (txtRegularScore != null)
				txtRegularScore.setText(s.regularScore);

			// 總成績
			TextView txtTotalScore = (TextView) convertView
					.findViewById(R.id.txtTotalScore);
			if (txtTotalScore != null)
				txtTotalScore.setText(s.totalScore);

			return convertView;
		}

	}

	/**
	 * 處理領域成績物件
	 * **/
	private class DomainScore {
		private String name;
		private List<SubjectScore> subjects;
		private String textScore;

		private DomainScore(String domainName) {
			subjects = new ArrayList<JHEvalScoreFragment.SubjectScore>();

			this.name = domainName;
		}

		/**
		 * 計算領域加權成績
		 * **/
		private BigDecimal calcWeightScore() {
			BigDecimal totalWeight = new BigDecimal(0);
			BigDecimal totalScore = new BigDecimal(0);

			for (SubjectScore s : subjects) {
				BigDecimal score = s.calcTotalScore();
				if (score == null)
					return null;

				score = score.multiply(s.weight);
				totalScore = totalScore.add(score);
				totalWeight = totalWeight.add(s.weight);
			}

			if (totalWeight.compareTo(BigDecimal.ZERO) == 0)
				return null;

			BigDecimal result = totalScore.divide(totalWeight, mDomainScale,
					mDomainRoundingMode);
			return result;
		}
	}

	/**
	 * 處理科目成績物件
	 * **/
	private class SubjectScore {
		private String name;
		private BigDecimal regScore;
		private BigDecimal usuScore;
		private BigDecimal weight;
		private BigDecimal regScorePer;
		private BigDecimal usuScorePer;
		private String textScore;

		private BigDecimal calcedTotalScore = null;

		/**
		 * 計算總成績
		 * **/
		private BigDecimal calcTotalScore() {
			if (calcedTotalScore == null) {
				// calcedTotalScore = ((regScore * regScorePer) + (usuScore *
				// usuScorePer))
				// / (usuScorePer + regScorePer);

				if (regScore == null || usuScore == null)
					calcedTotalScore = null;
				else {
					BigDecimal reg = regScore.multiply(regScorePer);
					BigDecimal usu = usuScore.multiply(usuScorePer);
					BigDecimal per = usuScorePer.add(regScorePer);

					calcedTotalScore = reg.add(usu).divide(per, mSubjectScale,
							mSubjectRoundingMode);
				}
			}
			return calcedTotalScore;
		}
	}
}
