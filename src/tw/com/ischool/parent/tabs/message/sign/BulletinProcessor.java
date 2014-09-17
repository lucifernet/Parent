package tw.com.ischool.parent.tabs.message.sign;

import ischool.dsa.utility.XmlUtil;
import ischool.utilities.StringUtil;

import org.w3c.dom.Element;

import tw.com.ischool.parent.R;

public class BulletinProcessor extends SignProcessor {

	@Override
	int getColorId() {
		return R.color.bg_school;
	}

	@Override
	String getWord(Element rawElement) {
		Element content = XmlUtil.selectElement(rawElement, "Content");
		content = XmlUtil.selectElement(content, "Message");
		Element from = XmlUtil.selectElement(content, "From");
		String school = XmlUtil.getElementText(from, "School");
		if (StringUtil.isNullOrWhitespace(school))
			return "校";
		else
			return school.substring(0, 1);
	}

}
