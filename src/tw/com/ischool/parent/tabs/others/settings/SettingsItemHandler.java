package tw.com.ischool.parent.tabs.others.settings;

import tw.com.ischool.parent.R;
import tw.com.ischool.parent.tabs.others.IGridItemHandler;
import android.content.Context;
import android.content.Intent;

public class SettingsItemHandler implements IGridItemHandler {

	private Context _context;

	public SettingsItemHandler(Context context) {
		_context = context;
	}
	
	@Override
	public int getDrawableId() {
		return R.drawable.settings;
	}

	@Override
	public int getTitleId() {
		return R.string.title_activity_settings;
	}

	@Override
	public void onClick() {
		Intent intent = new Intent(_context, SettingsActivity.class);
		_context.startActivity(intent);
	}

}
