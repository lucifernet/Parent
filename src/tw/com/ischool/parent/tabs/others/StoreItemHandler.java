package tw.com.ischool.parent.tabs.others;

import tw.com.ischool.parent.R;
import android.content.Context;
import android.widget.Toast;

public class StoreItemHandler implements IGridItemHandler {

	private Context _context;

	public StoreItemHandler(Context context) {
		_context = context;
	}

	@Override
	public int getDrawableId() {
		return android.R.drawable.ic_secure; 
	}

	@Override
	public int getTitleId() {
		return R.string.btn_store;
	}

	@Override
	public void onClick() {
		Toast.makeText(_context,
				_context.getString(getTitleId()) + " onClicked",
				Toast.LENGTH_SHORT).show();
	}

}
