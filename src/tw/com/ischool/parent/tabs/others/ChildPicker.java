package tw.com.ischool.parent.tabs.others;

import ischool.utilities.StringUtil;

import java.util.List;

import tw.com.ischool.parent.ChildInfo;
import tw.com.ischool.parent.MainActivity;
import tw.com.ischool.parent.R;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChildPicker {
	private static final String PREF_SELECTED_CHILD = "SelectedChild";
	private static final String PREF_SELECTED_NAME = "StudentName";

	private List<ChildInfo> mChildren;
	private ChildInfo mSelectedChild;
	private ChildAdapter mChildAdapter;
	private onChildSelectedListener mChildChangedListener;
	private SharedPreferences mSettings;

	public ChildPicker(Context context, ActionBar actionBar,
			onChildSelectedListener listener) {

		mSettings = context.getSharedPreferences(PREF_SELECTED_CHILD, 0);

		mChildChangedListener = listener;

		actionBar.setDisplayHomeAsUpEnabled(true);

		mChildren = MainActivity.getChildren().getChildren();
		if (mChildren.size() > 1) {
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			mChildAdapter = new ChildAdapter(context);
			actionBar.setListNavigationCallbacks(mChildAdapter,
					new OnNavigationListener() {

						@Override
						public boolean onNavigationItemSelected(
								int itemPosition, long itemId) {
							mSelectedChild = mChildren.get(itemPosition);
							Editor editor = mSettings.edit();
							editor.putString(PREF_SELECTED_NAME,
									mSelectedChild.getStudentName());
							editor.commit();
							
							if (mChildChangedListener != null) {

								mChildChangedListener.onChildSelected(
										mSelectedChild, mChildren.size());
								// return
								// mChildChangedListener.onNavigationItemSelected(itemPosition,
								// itemId);
							}

							return false;
						}
					});

			String selectedName = mSettings.getString(PREF_SELECTED_NAME,
					StringUtil.EMPTY);
			
			if (!StringUtil.isNullOrWhitespace(selectedName)) {
				int position = 0;
				for(ChildInfo child : mChildren){
					if(child.getStudentName().equals(selectedName)){
						break;
					}
					position++;
				}
				actionBar.setSelectedNavigationItem(position);
			}
		} else if (mChildren.size() == 1) {
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			mSelectedChild = mChildren.get(0);
			if (mChildChangedListener != null) {
				// mChildChangedListener.onNavigationItemSelected(0, 0);
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

	class ChildAdapter extends BaseAdapter {

		private LayoutInflater _inflater;

		@Override
		public int getCount() {
			return mChildren.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mChildren.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		ChildAdapter(Context context) {
			_inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ChildInfo child = mChildren.get(position);

			ViewHolder holder = null;
			if (convertView == null) {
				convertView = _inflater.inflate(R.layout.item_spinner, null);

				holder = new ViewHolder();
				holder.textView = (TextView) convertView
						.findViewById(R.id.textView);
				;

				convertView.setTag(holder);
			}

			holder = (ViewHolder) convertView.getTag();
			holder.textView.setText(child.getStudentName());

			return convertView;
		}

	}

	static class ViewHolder {
		TextView textView;
	}
}
