package tw.com.ischool.parent.tabs.others.eval_score;

import ischool.dsa.client.OnReceiveListener;
import ischool.dsa.utility.Converter;
import ischool.dsa.utility.DSResponse;
import ischool.dsa.utility.XmlUtil;
import ischool.dsa.utility.http.Cancelable;

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
import android.graphics.Color;
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

public class SHEvalScoreFragment extends Fragment {
	private Activity mActivity;
	private ListView mListView;
	private Spinner mSpinnerSemester;	
	private List<Semester> mSemesters;
	private SemesAdapter mAdapter;
	private ChildInfo mSelectedChild;

	private List<Exam> mExamList;
	// private ExamAdapter mExamAdapter;

	private List<ScoreInfo> mScoreInfos;
	private ScoreAdapter mScoreAdapter;
	private Element mRawScoreElement;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_sh_eval_score,
				container, false);

		mListView = (ListView) view.findViewById(R.id.listView);

		mSpinnerSemester = (Spinner) view.findViewById(R.id.spinnerSemester);

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

		// mExamAdapter = new ExamAdapter();
		// mSpinnerExam.setAdapter(mExamAdapter);
		// mSpinnerExam.setOnItemSelectedListener(new OnItemSelectedListener() {
		//
		// @Override
		// public void onItemSelected(AdapterView<?> parent, View view,
		// int position, long id) {
		// Exam s = mExamList.get(position);
		// onExamChanged(s);
		// }
		//
		// @Override
		// public void onNothingSelected(AdapterView<?> parent) {
		// // TODO Auto-generated method stub
		//
		// }
		// });

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
				Parent.SERVICE_SH_EVAL_SCORE_GET_SEMESTER, content,
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

			Semester s = new Semester();
			s.schoolYear = schoolYear;
			s.semester = semester;
			mSemesters.add(s);
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

		mAdapter.notifyDataSetChanged();

		dialog.dismiss();
	}

	/**
	 * 學期改變時
	 * **/
	private void onSemesterChanged(final Semester s) {
		// 取回所有該學期評量成績
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
		XmlUtil.addElement(condition, "SchoolYear", s.schoolYear);
		XmlUtil.addElement(condition, "Semester", s.semester);

		mSelectedChild.callService(Parent.CONTRACT_PARENT,
				Parent.SERVICE_SH_EVAL_SCORE_GET_SCORE, request,
				new OnReceiveListener<DSResponse>() {

					@Override
					public void onReceive(DSResponse result) {
						onScoreReceived(result, s);
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

	private void onScoreReceived(DSResponse result, Semester s) {
		TextView emptyText = (TextView) mActivity
				.findViewById(R.id.empty_view_sh_eval_score);
		mListView.setEmptyView(emptyText);

		mRawScoreElement = result.getContent();

		Element targetElement = null;
		for (Element seme : XmlUtil.selectElements(mRawScoreElement, "Seme")) {
			String schoolYear = seme.getAttribute("SchoolYear");
			String semester = seme.getAttribute("Semester");

			if (schoolYear.equals(s.schoolYear) && semester.equals(s.semester)) {
				targetElement = seme;
				break;
			}
		}

		mScoreInfos.clear();

		if (targetElement == null)
			return;

		mExamList.clear();

		for (Element course : XmlUtil.selectElements(targetElement, "Course")) {

			ScoreInfo info = new ScoreInfo();
			info.subjectName = course.getAttribute("Subject");

			for (Element e : XmlUtil.selectElements(course, "Exam")) {
				Exam exam = new Exam();
//				exam.id = e.getAttribute("ExamID");
				exam.name = e.getAttribute("ExamName");
//				exam.order = Converter.toInteger(e
//						.getAttribute("ExamDisplayOrder"));

				// TODO
				ExamScore es = new ExamScore();
				es.exam = exam;

				Element detail = XmlUtil.selectElement(e, "ScoreDetail");
				es.score = detail.getAttribute("Score");				
				es.color = Color.DKGRAY;
				info.scores.add(es);
				
				if (es.score.equals("缺")) {
					es.color = Color.BLUE;
				} else {
					Double score;
					try {
						score = Double.parseDouble(es.score);
						if (score < 60) {
							es.color = Color.RED;
						}
					} catch (NumberFormatException ex) {

					}
				}
				
				//計算進退步, 小於2表示只有一筆, 那就不用比了
				if(info.scores.size() < 2)
					continue;
				
				double currentScore = Converter.toDouble(es.score, -1);
				if(currentScore == -1){
					es.improve = 0;
					continue;
				}
				
				ExamScore lastES = info.scores.get(info.scores.size()-2);
			
				double lastScore = Converter.toDouble(lastES.score, 0);
				if(currentScore > lastScore)
					es.improve = 1;
				else if (currentScore < lastScore)
					es.improve = -1;
				
				
			}

			mScoreInfos.add(info);
		}

		mScoreAdapter.notifyDataSetChanged();
	}

	private class Semester {
		private String schoolYear;
		private String semester;
	}

	private class Exam {
//		private String id;
//		private int order;
		private String name;
	}

	private class ScoreInfo {
		private String subjectName;

		ArrayList<ExamScore> scores;

		public ScoreInfo() {
			scores = new ArrayList<ExamScore>();
		}
	}

	private class ExamScore {
		private Exam exam;
		private String score;
		private int color;
		private int improve = 0;
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

			ScoreViewHolder holder = null;

			if (convertView == null) {
				convertView = _inflater.inflate(R.layout.item_sh_eval_score,
						parent, false);

				holder = new ScoreViewHolder();
				holder.txtSubject = (TextView) convertView
						.findViewById(R.id.txtSubject);

				holder.layouts.add((LinearLayout) convertView
						.findViewById(R.id.layoutScore1));
				holder.txtExamNames.add((TextView) convertView
						.findViewById(R.id.txtExamName1));
				holder.txtExamScores.add((TextView) convertView
						.findViewById(R.id.txtExamScore1));
				holder.txtImproves.add((ImageView) convertView
						.findViewById(R.id.txtImprove1));

				holder.layouts.add((LinearLayout) convertView
						.findViewById(R.id.layoutScore2));
				holder.txtExamNames.add((TextView) convertView
						.findViewById(R.id.txtExamName2));
				holder.txtExamScores.add((TextView) convertView
						.findViewById(R.id.txtExamScore2));
				holder.txtImproves.add((ImageView) convertView
						.findViewById(R.id.txtImprove2));

				holder.layouts.add((LinearLayout) convertView
						.findViewById(R.id.layoutScore3));
				holder.txtExamNames.add((TextView) convertView
						.findViewById(R.id.txtExamName3));
				holder.txtExamScores.add((TextView) convertView
						.findViewById(R.id.txtExamScore3));
				holder.txtImproves.add((ImageView) convertView
						.findViewById(R.id.txtImprove3));

				holder.layouts.add((LinearLayout) convertView
						.findViewById(R.id.layoutScore4));
				holder.txtExamNames.add((TextView) convertView
						.findViewById(R.id.txtExamName4));
				holder.txtExamScores.add((TextView) convertView
						.findViewById(R.id.txtExamScore4));
				holder.txtImproves.add((ImageView) convertView
						.findViewById(R.id.txtImprove4));

				holder.layouts.add((LinearLayout) convertView
						.findViewById(R.id.layoutScore5));
				holder.txtExamNames.add((TextView) convertView
						.findViewById(R.id.txtExamName5));
				holder.txtExamScores.add((TextView) convertView
						.findViewById(R.id.txtExamScore5));
				holder.txtImproves.add((ImageView) convertView
						.findViewById(R.id.txtImprove5));

				holder.layouts.add((LinearLayout) convertView
						.findViewById(R.id.layoutScore6));
				holder.txtExamNames.add((TextView) convertView
						.findViewById(R.id.txtExamName6));
				holder.txtExamScores.add((TextView) convertView
						.findViewById(R.id.txtExamScore6));
				holder.txtImproves.add((ImageView) convertView
						.findViewById(R.id.txtImprove6));

				holder.layouts.add((LinearLayout) convertView
						.findViewById(R.id.layoutScore7));
				holder.txtExamNames.add((TextView) convertView
						.findViewById(R.id.txtExamName7));
				holder.txtExamScores.add((TextView) convertView
						.findViewById(R.id.txtExamScore7));
				holder.txtImproves.add((ImageView) convertView
						.findViewById(R.id.txtImprove7));

				holder.layouts.add((LinearLayout) convertView
						.findViewById(R.id.layoutScore8));
				holder.txtExamNames.add((TextView) convertView
						.findViewById(R.id.txtExamName8));
				holder.txtExamScores.add((TextView) convertView
						.findViewById(R.id.txtExamScore8));
				holder.txtImproves.add((ImageView) convertView
						.findViewById(R.id.txtImprove8));

				holder.layouts.add((LinearLayout) convertView
						.findViewById(R.id.layoutScore9));
				holder.txtExamNames.add((TextView) convertView
						.findViewById(R.id.txtExamName9));
				holder.txtExamScores.add((TextView) convertView
						.findViewById(R.id.txtExamScore9));
				holder.txtImproves.add((ImageView) convertView
						.findViewById(R.id.txtImprove9));

				holder.layouts.add((LinearLayout) convertView
						.findViewById(R.id.layoutScore10));
				holder.txtExamNames.add((TextView) convertView
						.findViewById(R.id.txtExamName10));
				holder.txtExamScores.add((TextView) convertView
						.findViewById(R.id.txtExamScore10));
				holder.txtImproves.add((ImageView) convertView
						.findViewById(R.id.txtImprove10));

				holder.layouts.add((LinearLayout) convertView
						.findViewById(R.id.layoutScore11));
				holder.txtExamNames.add((TextView) convertView
						.findViewById(R.id.txtExamName11));
				holder.txtExamScores.add((TextView) convertView
						.findViewById(R.id.txtExamScore11));
				holder.txtImproves.add((ImageView) convertView
						.findViewById(R.id.txtImprove11));

				holder.layouts.add((LinearLayout) convertView
						.findViewById(R.id.layoutScore12));
				holder.txtExamNames.add((TextView) convertView
						.findViewById(R.id.txtExamName12));
				holder.txtExamScores.add((TextView) convertView
						.findViewById(R.id.txtExamScore12));
				holder.txtImproves.add((ImageView) convertView
						.findViewById(R.id.txtImprove12));

				holder.layouts.add((LinearLayout) convertView
						.findViewById(R.id.layoutScore13));
				holder.txtExamNames.add((TextView) convertView
						.findViewById(R.id.txtExamName13));
				holder.txtExamScores.add((TextView) convertView
						.findViewById(R.id.txtExamScore13));
				holder.txtImproves.add((ImageView) convertView
						.findViewById(R.id.txtImprove13));
				convertView.setTag(holder);
			} else {
				holder = (ScoreViewHolder) convertView.getTag();
			}

			holder.txtSubject.setText(s.subjectName);

			for (int i = 0; i < holder.layouts.size(); i++) {
				LinearLayout layout = holder.layouts.get(i);

				if (i >= s.scores.size()) {
					if (layout.getVisibility() == View.VISIBLE)
						layout.setVisibility(View.GONE);
					else
						break;
				} else {
					if (layout.getVisibility() == View.GONE)
						layout.setVisibility(View.VISIBLE);

					TextView txtExamName = holder.txtExamNames
							.get(i);
					TextView txtExamScore = holder.txtExamScores
							.get(i);
					ImageView imgImprove = holder.txtImproves.get(i);
					
					ExamScore es = s.scores.get(i);
					txtExamName.setText(es.exam.name);
					txtExamScore.setText(es.score);
										
					int color = txtExamScore.getCurrentTextColor();
					
					if(color != es.color){
						txtExamScore.setTextColor(es.color);
						txtExamName.setTextColor(es.color);
					}	
					
					if(imgImprove.getVisibility() == View.INVISIBLE && es.improve == 0){
						continue;
					}
					
					
					if(imgImprove.getTag() == null){
						setImprove(imgImprove, 0, es.improve);						
					} else {
						int original = (Integer)imgImprove.getTag();
						setImprove(imgImprove, original, es.improve);	
					}
				}
			}

			return convertView;
		}

		private void setImprove(ImageView imgImprove, int original, int improve){			
			imgImprove.setTag(improve);
			if(original == improve) return;
			
			if(improve == 0 && imgImprove.getVisibility() != View.INVISIBLE){
				imgImprove.setVisibility(View.INVISIBLE);
				return;
			}
				
			if(imgImprove.getVisibility() == View.INVISIBLE){
				imgImprove.setVisibility(View.VISIBLE);
			}
			
			if(improve == 1){
				imgImprove.setImageResource(R.drawable.up);							
			} else if (improve == -1){
				imgImprove.setImageResource(R.drawable.down);
			}
		}
	}

	static class ScoreViewHolder {
		TextView txtSubject;
		List<LinearLayout> layouts;
		List<TextView> txtExamNames;
		List<TextView> txtExamScores;
		List<ImageView> txtImproves;
		ScoreViewHolder() {
			layouts = new ArrayList<LinearLayout>();
			txtExamNames = new ArrayList<TextView>();
			txtExamScores = new ArrayList<TextView>();
			txtImproves = new ArrayList<ImageView>();
		}
	}
}
