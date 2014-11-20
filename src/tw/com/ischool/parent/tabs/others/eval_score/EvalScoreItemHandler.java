package tw.com.ischool.parent.tabs.others.eval_score;

import tw.com.ischool.parent.R;
import tw.com.ischool.parent.tabs.others.IGridItemHandler;
import android.content.Context;
import android.content.Intent;

public class EvalScoreItemHandler implements IGridItemHandler {

	private Context _context;

	public EvalScoreItemHandler(Context context) {
		_context = context;
	}

	@Override
	public int getDrawableId() {
		return R.drawable.eval_score;
	}

	@Override
	public int getTitleId() {
		return R.string.btn_eval_score;
	}

	@Override
	public void onClick() {
		Intent intent = new Intent(_context, EvalScoreActivity.class);
		_context.startActivity(intent);
	}

}
