package tw.com.ischool.parent.tabs.others.discipline;

import tw.com.ischool.parent.R;
import tw.com.ischool.parent.tabs.others.IGridItemHandler;
import android.content.Context;
import android.content.Intent;

public class DisciplineItemHandler implements IGridItemHandler {

	private Context _context;

	public DisciplineItemHandler(Context context) {
		_context = context;
	}

	@Override
	public int getDrawableId() {
		return R.drawable.discipline;
	}

	@Override
	public int getTitleId() {
		return R.string.btn_discipline;
	}

	@Override
	public void onClick() {
		Intent intent = new Intent(_context, DisciplineActivity.class);
		_context.startActivity(intent);
	}

}
