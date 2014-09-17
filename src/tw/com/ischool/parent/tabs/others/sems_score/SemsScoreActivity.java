package tw.com.ischool.parent.tabs.others.sems_score;

import tw.com.ischool.parent.ChildInfo;
import tw.com.ischool.parent.R;
import tw.com.ischool.parent.tabs.others.ChildPicker;
import tw.com.ischool.parent.tabs.others.ChildPicker.onChildSelectedListener;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

public class SemsScoreActivity extends Activity {

	public static final String PARAM_SELECTED_CHILD = "PARAM_SELECTED_CHILD";
	// public static final String
	private ChildPicker mChildPicker;
	private ChildInfo mSelectedChild;
	private FrameLayout mLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sems_score);

		mLayout = (FrameLayout) this.findViewById(R.id.layoutSemesScore);

		setTitle(R.string.title_activity_sems_score);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		mChildPicker = new ChildPicker(this, actionBar,
				new onChildSelectedListener() {

					@Override
					public void onChildSelected(ChildInfo child, int count) {
						mSelectedChild = child;

						if (count == 1) {							
							String title = getString(R.string.title_activity_sems_score)
									+ "(" + child.getStudentName() + ")";
							setTitle(title);
						}
						
						bindScore();
					}
				});

		// mChildPicker = new ChildPicker(this, actionBar,
		// new OnNavigationListener() {
		//
		// @Override
		// public boolean onNavigationItemSelected(int itemPosition,
		// long itemId) {
		// mSelectedChild = mChildPicker.getSelectedChild();
		// bindScore();
		// return true;
		// }
		// });

	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
			finish();

			// Toast.makeText(this, "home pressed", Toast.LENGTH_LONG).show();
			break;

		}

		return true;
	}

	private void bindScore() {
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Bundle bundle = new Bundle();
		bundle.putSerializable(PARAM_SELECTED_CHILD, mSelectedChild);
		Fragment fragment;
		// TODO 判斷國中還是高中, 但先只做高中的版本
		if (true) {
			fragment = new ScoreFragment();
		}

		fragment.setArguments(bundle);
		ft.replace(mLayout.getId(), fragment);
		ft.commitAllowingStateLoss();
	}

}
