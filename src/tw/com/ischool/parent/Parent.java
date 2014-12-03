package tw.com.ischool.parent;

import java.util.ArrayList;
import java.util.List;

import tw.com.ischool.account.login.Accessable;
import tw.com.ischool.account.login.ConnectionHelper;

public class Parent {
	public static final String CLIENT_ID = "e6228b759e6ca00c620a1f9a1171745d";
	
	public static final String CLIENT_SEC = "070575826e01ae4396d244b2ebb463491c447634068657eb3cb20d01a3b96fdd";
	
	public static final String CONTRACT_PARENT = "ischool.parent.app";
	
	public static final String SERVICE_GET_MESSAGE = "im.GetMessage";

	public static final String SERVICE_GET_MY_CHILD = "main.GetMyChildren";

	public static final String SERVICE_GET_SEMESTERS = "absence.GetSchoolYearSemester";

	public static final String SERVICE_GET_ATTENDANCE = "absence.GetChildAttendance";

	public static final String SERVICE_GET_DISCIPLINE = "discipline.GetChildDiscipline";

	public static final String SERVICE_GET_SH_SEMES_SCORE = "semesterScoreSH.GetChildSemsScore";

	public static final String SERVICE_JOIN_PARENT = "Join.AsParent";

	public static final String CONTRACT_JOIN = "auth.guest";

	public static final String SERVICE_ADD_APPLICATION_REF = "AddApplicationRef";

	public static final String SERVICE_GET_SCHOOL_INFO = "main.GetSchoolInfo";

	public static final String SERVICE_JH_SEMESTER_SCORE = "semesterScoreJH.GetChildSemsScore";

	public static final String SERVICE_JH_EVAL_SCORE_GET_SEMESTER = "evaluateScoreJH.GetSemesters";

	public static final String SERVICE_JH_EVAL_SCORE_GET_EXAM_SCORE = "evaluateScoreJH.GetExamScore";

	public static final String SERVICE_JH_EVAL_SCORE_GET_CALC_RULE = "evaluateScoreJH.GetScoreCalcRule";

	public static final String SERVICE_SH_EVAL_SCORE_GET_SCORE = "evaluateScoreSH.GetExamScore";

	public static final String SERVICE_SH_EVAL_SCORE_GET_SEMESTER = "evaluateScoreSH.GetSemesters";

	public static final String SERVICE_REMOVE_CHILD = "main.RemoveChild";


	private static ConnectionHelper sConnectionHelper;
//	private static List<Accessable> sAccessables;
	private static Children sChildren;
	
	public static ConnectionHelper getConnectionHelper(){
		return sConnectionHelper;
	}
	
	public static List<Accessable> getAccessables(){		
		if(sConnectionHelper == null)
			return new ArrayList<Accessable>();
		if(sConnectionHelper.getAccessables() == null)
			return new ArrayList<Accessable>();
		return sConnectionHelper.getAccessables();		
	}
	
	public static Children getChildren(){
		if(sChildren == null)
			sChildren = new Children();
		return sChildren;
	}
	
	public static void setConnectionHelper(ConnectionHelper connectionHelper){
		sConnectionHelper = connectionHelper;
	}
	
	public static void setChildren(Children children){
		sChildren = children;
	}
	
}
