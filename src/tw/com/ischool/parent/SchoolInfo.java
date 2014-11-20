package tw.com.ischool.parent;

import java.io.Serializable;

import ischool.dsa.utility.XmlUtil;
import ischool.utilities.StringUtil;

import org.w3c.dom.Element;

public class SchoolInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final int JUNIOR_HIGH_SCHOOL = 5;
	public static final int SENIOR_HIGH_SCHOOL = 3;

	public static final String TYPE_JH_1 = "新竹國中";
	public static final String TYPE_JH_2 = "新竹國中";
	public static final String TYPE_SH = "高中";
	
	private String _chineseName;
	private String _englishName;
	private String _address;
	private String _englishAddress;
	private String _code;
	private String _fax;
	private String _telephone;
	private String _chancellorChineseName; // 校長中文名
	private String _chancellorEnglishName; // 校長英文名
	private String _eduDirectorName;
	private String _stuDirectorName;
	private String _scoreType;
	
	public SchoolInfo(Element schoolInfo) {
		String chineseName = XmlUtil.getElementText(schoolInfo, "ChineseName");
		this.setChineseName(chineseName);

		String englishName = XmlUtil.getElementText(schoolInfo, "EnglishName");
		this.setEnglishName(englishName);

		String address = XmlUtil.getElementText(schoolInfo, "Address");
		this.setAddress(address);

		String englishAddress = XmlUtil.getElementText(schoolInfo,
				"EnglishAddress");
		this.setEnglishAddress(englishAddress);

		String code = XmlUtil.getElementText(schoolInfo, "Code");
		this.setCode(code);

		String fax = XmlUtil.getElementText(schoolInfo, "Fax");
		this.setFax(fax);

		String telephone = XmlUtil.getElementText(schoolInfo, "Telephone");
		this.setTelephone(telephone);

		String chancellorChineseName = XmlUtil.getElementText(schoolInfo,
				"ChancellorChineseName");
		this.setChancellorChineseName(chancellorChineseName);

		String chancellorEnglishName = XmlUtil.getElementText(schoolInfo,
				"ChancellorEnglishName");
		this.setChancellorEnglishName(chancellorEnglishName);

		String eduDirectorName = XmlUtil.getElementText(schoolInfo,
				"EduDirectorName");
		this.setEduDirectorName(eduDirectorName);

		String stuDirectorName = XmlUtil.getElementText(schoolInfo,
				"StuDirectorName");
		this.setStuDirectorName(stuDirectorName);
	}

	public String getChineseName() {
		return _chineseName;
	}

	private void setChineseName(String chineseName) {
		_chineseName = chineseName;
	}

	public String getEnglishName() {
		return _englishName;
	}

	private void setEnglishName(String englishName) {
		_englishName = englishName;
	}

	public String getAddress() {
		return _address;
	}

	private void setAddress(String address) {
		_address = address;
	}

	public String getEnglishAddress() {
		return _englishAddress;
	}

	private void setEnglishAddress(String englishAddress) {
		_englishAddress = englishAddress;
	}

	public String getFax() {
		return _fax;
	}

	private void setFax(String fax) {
		_fax = fax;
	}

	public String getCode() {
		return _code;
	}

	private void setCode(String code) {
		_code = code;
	}

	public String getChancellorChineseName() {
		return _chancellorChineseName;
	}

	private void setChancellorChineseName(String chancellorChineseName) {
		_chancellorChineseName = chancellorChineseName;
	}

	public String getTelephone() {
		return _telephone;
	}

	private void setTelephone(String telephone) {
		_telephone = telephone;
	}

	public String getChancellorEnglishName() {
		return _chancellorEnglishName;
	}

	private void setChancellorEnglishName(String chancellorEnglishName) {
		_chancellorEnglishName = chancellorEnglishName;
	}

	public String getEduDirectorName() {
		return _eduDirectorName;
	}

	private void setEduDirectorName(String eduDirectorName) {
		_eduDirectorName = eduDirectorName;
	}

	public String getStuDirectorName() {
		return _stuDirectorName;
	}

	private void setStuDirectorName(String stuDirectorName) {
		_stuDirectorName = stuDirectorName;
	}

	/**
	 * 取得學校成績類別
	 * **/
	public String getSchoolType() {
//		if (StringUtil.isNullOrWhitespace(_code))
//			return SENIOR_HIGH_SCHOOL;
//
//		if (_code.length() < 6)
//			return SENIOR_HIGH_SCHOOL;
//
//		char c = _code.charAt(3);
//
//		if (c == '3')
//			return SENIOR_HIGH_SCHOOL;
//		if (c == '5')
//			return JUNIOR_HIGH_SCHOOL;
//		
//		return SENIOR_HIGH_SCHOOL;
		if(!StringUtil.isNullOrWhitespace(_scoreType))
			return _scoreType;
		return TYPE_SH;
	}

	public void setSchoolType(String scoreType) {
		_scoreType = scoreType;
	}

}
