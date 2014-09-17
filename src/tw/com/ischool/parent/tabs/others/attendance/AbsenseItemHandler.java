package tw.com.ischool.parent.tabs.others.attendance;

import tw.com.ischool.parent.R;
import tw.com.ischool.parent.tabs.others.IGridItemHandler;
import android.content.Context;
import android.content.Intent;

public class AbsenseItemHandler implements IGridItemHandler {

	private Context _context;

	public AbsenseItemHandler(Context context) {
		_context = context;
	}

	@Override
	public int getDrawableId() {
		return R.drawable.red_chair;
	}

	@Override
	public int getTitleId() {
		return R.string.btn_absense;
	}

	@Override
	public void onClick() {		
		Intent intent = new Intent(_context, AttendanceActivity.class);
		_context.startActivity(intent);
	}

}
