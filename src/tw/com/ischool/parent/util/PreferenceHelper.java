package tw.com.ischool.parent.util;

import ischool.dsa.utility.XmlHelper;
import ischool.dsa.utility.XmlUtil;
import ischool.utilities.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.w3c.dom.Element;

import tw.com.ischool.account.login.Accessable;
import tw.com.ischool.parent.Children;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;

public class PreferenceHelper {

	public static final String PREF_ACCOUNT = "PreferenceAccount";
	public static final String KEY_CURRENT_ACCOUNT = "CurrentAccount";

	public static final String PREF_LOGIN = "ParentLogin";
	public static final String KEY_CHILDREN_OF = "children_of_";

	private static final String PREF_CON_HELPER = "pref_connection_helper";
	private static final String PREF_PARAM_ACCESSABLES_FOR = "accessables_for_";

	public static String getCacheUseAccount(Context context) {
		String value = getCacheValue(context, PREF_ACCOUNT, KEY_CURRENT_ACCOUNT);
		return value;
	}

	public static void cacheUseAccount(Context context, String account) {
		cache(context, PREF_ACCOUNT, KEY_CURRENT_ACCOUNT, account);
	}

	public static Children getCacheChildren(Context context) {
		String name = getCacheUseAccount(context);
		String contentString = getCacheValue(context, PREF_LOGIN,
				KEY_CHILDREN_OF + name);

		if (!StringUtil.isNullOrWhitespace(contentString)) {
			return parseChildren(contentString);
		}

		return new Children();
	}

	public static void cacheChildren(Context context, Children mChildren) {
		String childrenString = convertChildToString(mChildren);
		cache(context, PREF_LOGIN, KEY_CHILDREN_OF
				+ getCacheUseAccount(context), childrenString);
	}

	public static List<Accessable> getAccessables(Context context) {
		String sourceString = getCacheValue(context, PREF_CON_HELPER,
				PREF_PARAM_ACCESSABLES_FOR + getCacheUseAccount(context));
		List<Accessable> result = new ArrayList<Accessable>();

		if (!StringUtil.isNullOrWhitespace(sourceString)) {
			Element content = XmlHelper.parseXml(sourceString);
			result.addAll(parseAccessable(content));
		}

		return result;
	}

	public static void cacheAccessables(Context context,
			List<Accessable> accessables) {
		Element root = XmlUtil.createElement("User");

		for (Accessable a : accessables) {
			XmlUtil.appendElement(root, a.toElement());
		}

		String content = XmlHelper.convertToString(root, true);
		cache(context, PREF_CON_HELPER, PREF_PARAM_ACCESSABLES_FOR
				+ getCacheUseAccount(context), content);
	}

	public static void clearCaches(Context context){
		//clearPreference(context, PREF_)
	}
	
	private static String getCacheValue(Context context, String prefName,
			String propertyName) {
		SharedPreferences pref = context.getSharedPreferences(prefName, 0);
		String value = pref.getString(propertyName, StringUtil.EMPTY);
		return value;
	}

	private static void cache(Context context, String prefName,
			String propertyName, String value) {
		SharedPreferences pref = context.getSharedPreferences(prefName, 0);
		Editor edit = pref.edit();
		edit.putString(propertyName, value);
		edit.commit();
	}

	private static Children parseChildren(String content) {
		byte[] bytes = Base64.decode(content, Base64.DEFAULT);

		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		ObjectInputStream is = null;
		Children children;

		try {
			is = new ObjectInputStream(in);
			Object obj = is.readObject();
			children = (Children) obj;
		} catch (Exception ex) {
			children = new Children();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				in.close();
			} catch (IOException e) {
			}
		}
		return children;
	}

	private static String convertChildToString(Children children) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		String base64String = StringUtil.EMPTY;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(children);
			byte[] yourBytes = bos.toByteArray();

			base64String = Base64.encodeToString(yourBytes, Base64.DEFAULT);
		} catch (IOException ex) {
			base64String = StringUtil.EMPTY;
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException ex) {
				// ignore close exception
			}
			try {
				bos.close();
			} catch (IOException ex) {
				// ignore close exception
			}
		}
		return base64String;
	}

	private static List<Accessable> parseAccessable(Element content) {
		ArrayList<Accessable> accessables = new ArrayList<Accessable>();

		HashSet<String> set = new HashSet<String>();

		Element element = XmlUtil.selectElement(content, "Domain");
		for (Element item : XmlUtil.selectElements(element, "App")) {
			Accessable accessable = new Accessable(item);
			if (set.contains(accessable.getAccessPoint()))
				continue;

			accessables.add(accessable);
		}

		element = XmlUtil.selectElement(content, "User");
		for (Element item : XmlUtil.selectElements(element, "App")) {
			Accessable accessable = new Accessable(item);
			if (set.contains(accessable.getAccessPoint()))
				continue;

			accessables.add(accessable);
		}

		return accessables;
	}

//	private static void clearPreference(Context context, String prefName) {
//		// TODO Auto-generated method stub
//		SharedPreferences pref = context.getSharedPreferences(prefName, 0);
//		Editor edit = pref.edit();
//		edit.clear();
//		edit.commit();
//	}
}
