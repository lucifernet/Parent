package tw.com.ischool.parent.tabs.others.parent_code;

import tw.com.ischool.parent.MainActivity;
import tw.com.ischool.parent.R;
import tw.com.ischool.parent.addChild.AddChildActivity;
import tw.com.ischool.parent.addChild.ParentCodeActivity;
import tw.com.ischool.parent.tabs.others.IGridItemHandler;
import android.app.Activity;
import android.content.Intent;

public class ParentCodeItemHandler implements IGridItemHandler{

	private Activity _context;

	public ParentCodeItemHandler(Activity context) {
		_context = context;
	}

	@Override
	public int getDrawableId() {
		return R.drawable.kid;
	}

	@Override
	public int getTitleId() {
		return R.string.btn_parent_code;
	}

	@Override
	public void onClick() {
		Intent intent = new Intent(_context, AddChildActivity.class);
		_context.startActivityForResult(intent, MainActivity.CODE_ADD_CHILD);
	}

}
