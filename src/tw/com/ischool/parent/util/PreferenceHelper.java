package tw.com.ischool.parent.util;

import ischool.utilities.StringUtil;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferenceHelper {

	public static final String PREF_ACCOUNT = "PreferenceAccount";
	public static final String KEY_CURRENT_ACCOUNT = "CurrentAccount";
	
	public static String getCacheUseAccount(Context context) {
		String value = getCacheValue(context, PREF_ACCOUNT, KEY_CURRENT_ACCOUNT);
		return value;
	}
	
	public static void cacheUseAccount(Context context, String account){
		cache(context, PREF_ACCOUNT, KEY_CURRENT_ACCOUNT, account);
	}
	
	private static String getCacheValue(Context context, String prefName, String propertyName){
		SharedPreferences pref = context.getSharedPreferences(prefName, 0);
		String value = pref.getString(propertyName,
				StringUtil.EMPTY);
		return value;
	}
	
	private static void cache(Context context, String prefName, String propertyName, String value){
		SharedPreferences pref = context.getSharedPreferences(prefName, 0);
		Editor edit = pref.edit();
		edit.putString(propertyName, value);
		edit.commit();
	}
}
