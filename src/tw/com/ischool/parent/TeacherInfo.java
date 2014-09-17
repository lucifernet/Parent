package tw.com.ischool.parent;

import java.io.Serializable;

import ischool.utilities.StringUtil;
import android.graphics.Bitmap;

public class TeacherInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String _name;
	private String _phone;
	private String _loginId;
	private String _photo;

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public String getPhone() {
		return _phone;
	}

	public void setPhone(String phone) {
		_phone = phone;
	}

	public String getLoginId() {
		return _loginId;
	}

	public void setLoginId(String loginId) {
		_loginId = loginId;
	}

	public String getPhoto() {
		return _photo;
	}

	public void setPhoto(String photo) {
		_photo = photo;
	}

	public Bitmap getPhotoBitmap() {
		return StringUtil.parseBase64ToBitmap(getPhoto());
	}
}
