package tw.com.ischool.parent.tabs.others.sems_score;

import tw.com.ischool.parent.R;
import tw.com.ischool.parent.tabs.others.IGridItemHandler;
import tw.com.ischool.parent.tabs.others.discipline.DisciplineActivity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class SemScoreItemHandler implements IGridItemHandler {

	private Context _context;

	public SemScoreItemHandler(Context context) {
		_context = context;
	}

	@Override
	public int getDrawableId() {
		return R.drawable.test_paper;
	}

	@Override
	public int getTitleId() {
		return R.string.btn_sem_score;
	}

	@Override
	public void onClick() {
		Intent intent = new Intent(_context, SemsScoreActivity.class);
		_context.startActivity(intent);
	}

}
