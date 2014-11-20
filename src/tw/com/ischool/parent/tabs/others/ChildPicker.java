package tw.com.ischool.parent.tabs.others;

import ischool.utilities.StringUtil;

import java.util.ArrayList;

import tw.com.ischool.parent.ChildInfo;
import tw.com.ischool.parent.Parent;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.ArrayAdapter;

public class ChildPicker {
	private static final String PREF_SELECTED_CHILD = "SelectedChild";
	private static final String PREF_SELECTED_NAME = "StudentName";

	private ChildInfo mSelectedChild;
	private onChildSelectedListener mChildChangedListener;
	private SharedPreferences mSettings;

	public ChildPicker(Context context, ActionBar actionBar,
			onChildSelectedListener listener) {

		mSettings = context.getSharedPreferences(PREF_SELECTED_CHILD, 0);

		mChildChangedListener = listener;

		actionBar.setDisplayHomeAsUpEnabled(true);

		
		if (Parent.getChildren().getChildren().size() > 1) {
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			ArrayList<String> childNames = new ArrayList<String>();
			for(ChildInfo child : Parent.getChildren().getChildren()){
				childNames.add(child.getStudentName());
			}
			
			ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, android.R.id.text1, childNames);
			
			actionBar.setListNavigationCallbacks(mAdapter,
					new OnNavigationListener() {

						@Override
						public boolean onNavigationItemSelected(
								int itemPosition, long itemId) {
							mSelectedChild = Parent.getChildren().getChildren().get(
									itemPosition);
							Editor editor = mSettings.edit();
							editor.putString(PREF_SELECTED_NAME,
									mSelectedChild.getStudentName());
							editor.commit();

							if (mChildChangedListener != null) {

								mChildChangedListener.onChildSelected(
										mSelectedChild, Parent.getChildren().getChildren()
												.size());
							}

							return false;
						}
					});

			String selectedName = mSettings.getString(PREF_SELECTED_NAME,
					StringUtil.EMPTY);

			if (!StringUtil.isNullOrWhitespace(selectedName)) {
				int position = 0;
				for (String childName : childNames) {
					if (childName.equals(selectedName)) {
						break;
					}
					position++;
				}
				if(position < childNames.size())
					actionBar.setSelectedNavigationItem(position);
				else
					actionBar.setSelectedNavigationItem(0);
			}
		} else if (Parent.getChildren().getChildren().size() == 1) {
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			mSelectedChild = Parent.getChildren().getChildren().get(0);
			if (mChildChangedListener != null) {
				mChildChangedListener.onChildSelected(mSelectedChild, 1);
			}
		}
	}

	public ChildInfo getSelectedChild() {
		return mSelectedChild;
	}

	public interface onChildSelectedListener {
		void onChildSelected(ChildInfo child, int childCount);
	}
}
