package tw.com.ischool.parent;

import ischool.dsa.client.OnReceiveListener;
import ischool.dsa.utility.DSRequest;
import ischool.dsa.utility.DSResponse;
import ischool.dsa.utility.XmlUtil;
import ischool.dsa.utility.http.Cancelable;
import ischool.utilities.StringUtil;

import java.io.InputStream;
import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;

import tw.com.ischool.account.login.Accessable;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;

public class ChildInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String _studentId;
	private String _studentName;
	private String _studentNumber;
	private String _seatNo;
	private String _gender;
	private String _linkAccount;
	private String _classId;
	private String _gradeYear;
	private String _className;
	private Accessable _accessable;
	private String _photo;
	private TeacherInfo _teacher;
	private SchoolInfo _schoolInfo;

	public ChildInfo(Accessable accessable, Element source) {
		setAccessable(accessable);

		setStudentId(XmlUtil.getElementText(source, "StudentId"));
		setStudentNumber(XmlUtil.getElementText(source, "StudentNumber"));
		setStudentName(XmlUtil.getElementText(source, "StudentName"));
		setSeatNo(XmlUtil.getElementText(source, "SeatNo"));
		setGender(XmlUtil.getElementText(source, "Gender"));
		setLinkAccount(XmlUtil.getElementText(source, "LinkAccount"));
		setClassId(XmlUtil.getElementText(source, "ClassId"));
		setGradeYear(XmlUtil.getElementText(source, "GradeYear"));
		setClassName(XmlUtil.getElementText(source, "ClassName"));
		setPhoto(XmlUtil.getElementText(source, "StudentPhoto"));

		_teacher = new TeacherInfo();
		_teacher.setLoginId(XmlUtil.getElementText(source, "TeacherLoginId"));
		_teacher.setName(XmlUtil.getElementText(source, "TeacherName"));
		_teacher.setPhone(XmlUtil.getElementText(source, "TeacherPhone"));
		_teacher.setPhoto(XmlUtil.getElementText(source, "TeacherPhoto"));
	}

	public void getSchoolInfo(final Context context, final OnReceiveListener<SchoolInfo> listener) {
		if (listener == null)
			return;

		if (_schoolInfo != null) {
			listener.onReceive(_schoolInfo);
			return;
		}

		DSRequest request = new DSRequest();
		this.callService(Parent.CONTRACT_PARENT,
				Parent.SERVICE_GET_SCHOOL_INFO, request, new OnReceiveListener<DSResponse>() {
					
					@Override
					public void onReceive(DSResponse result) {
						Element content = result.getContent();
						_schoolInfo = new SchoolInfo(content);
						
						//TODO 這裡的 school type 先改成從檔案裡取得
						try {
							Resources res = context.getResources();
							InputStream in_s = res.openRawResource(R.raw.school_list);

							byte[] b = new byte[in_s.available()];
							in_s.read(b);
							String rawString = new String(b);

							JSONArray array = new JSONArray(rawString);
							JSONObject school = null;
							for(int i=0;i<array.length();i++){
								JSONObject json = array.getJSONObject(i);
								String dsns = json.getString("dsnsName");
								if(dsns.equals(_accessable.getAccessPoint())){
									school = json;
									break;
								}									
							}
							
							if(school != null){
								String scoreType = school.getString("scoreType");
								_schoolInfo.setSchoolType(scoreType);
							}
						} catch (Exception e) {

						}
						
						listener.onReceive(_schoolInfo);
					}
					
					@Override
					public void onError(Exception ex) {
						listener.onError(ex);						
					}
				},
				new Cancelable());
	}

	public String getStudentId() {
		return _studentId;
	}

	private void setStudentId(String studentId) {
		_studentId = studentId;
	}

	public String getStudentName() {
		return _studentName;
	}

	private void setStudentName(String studentName) {
		_studentName = studentName;
	}

	public String getStudentNumber() {
		return _studentNumber;
	}

	private void setStudentNumber(String studentNumber) {
		_studentNumber = studentNumber;
	}

	public String getSeatNo() {
		return _seatNo;
	}

	private void setSeatNo(String seatNo) {
		_seatNo = seatNo;
	}

	public String getGender() {
		return _gender;
	}

	private void setGender(String gender) {
		_gender = gender;
	}

	public String getLinkAccount() {
		return _linkAccount;
	}

	private void setLinkAccount(String linkAccount) {
		_linkAccount = linkAccount;
	}

	public String getClassId() {
		return _classId;
	}

	private void setClassId(String classId) {
		_classId = classId;
	}

	public String getGradeYear() {
		return _gradeYear;
	}

	private void setGradeYear(String gradeYear) {
		_gradeYear = gradeYear;
	}

	public String getClassName() {
		return _className;
	}

	private void setClassName(String className) {
		_className = className;
	}

	public Accessable getAccessable() {
		return _accessable;
	}

	private void setAccessable(Accessable accessable) {
		_accessable = accessable;
	}

	public String getPhoto() {
		return _photo;
	}

	public void setPhoto(String photo) {
		_photo = photo;
	}

	public TeacherInfo getTeacher() {
		return _teacher;
	}

	public Bitmap getPhotoBitmap() {
		return StringUtil.parseBase64ToBitmap(getPhoto());
	}

	public void callService(String contract, String serviceName,
			DSRequest request, OnReceiveListener<DSResponse> listener,
			Cancelable cancelable) {
		Parent.getConnectionHelper().callService(_accessable, contract,
				serviceName, request, listener, cancelable);

	}

	public void callService(String contract, String serviceName,
			Element request, OnReceiveListener<DSResponse> listener,
			Cancelable cancelable) {

		DSRequest req = new DSRequest();
		req.setContent(request);

		callService(contract, serviceName, req, listener, cancelable);

	}

	public void callParentService(String serviceName, Element request,
			OnReceiveListener<DSResponse> listener, Cancelable cancelable) {

		DSRequest req = new DSRequest();
		req.setContent(request);

		callService(Parent.CONTRACT_PARENT, serviceName, req, listener,
				cancelable);

	}

	public void callParentService(String serviceName, DSRequest request,
			OnReceiveListener<DSResponse> listener, Cancelable cancelable) {

		callService(Parent.CONTRACT_PARENT, serviceName, request, listener,
				cancelable);

	}
}
