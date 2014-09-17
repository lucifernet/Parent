package tw.com.ischool.parent.tabs.others;

import tw.com.ischool.parent.R;
import android.content.Context;
import android.widget.Toast;

public class EvalScoreItemHandler implements IGridItemHandler {

	private Context _context;

	public EvalScoreItemHandler(Context context) {
		_context = context;
	}

	@Override
	public int getDrawableId() {
		return android.R.drawable.ic_lock_lock;
	}

	@Override
	public int getTitleId() {
		return R.string.btn_eval_score;
	}

	@Override
	public void onClick() {
		Toast.makeText(_context,
				_context.getString(getTitleId()) + " onClicked",
				Toast.LENGTH_SHORT).show();
	}

}
