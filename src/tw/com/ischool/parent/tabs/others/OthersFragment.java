package tw.com.ischool.parent.tabs.others;

import java.util.ArrayList;
import java.util.List;

import tw.com.ischool.parent.R;
import tw.com.ischool.parent.tabs.others.attendance.AbsenseItemHandler;
import tw.com.ischool.parent.tabs.others.discipline.DisciplineItemHandler;
import tw.com.ischool.parent.tabs.others.eval_score.EvalScoreItemHandler;
import tw.com.ischool.parent.tabs.others.parent_code.ParentCodeItemHandler;
import tw.com.ischool.parent.tabs.others.sems_score.SemScoreItemHandler;
import tw.com.ischool.parent.tabs.others.settings.SettingsItemHandler;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class OthersFragment extends Fragment {

	private GridView mGridView;
	private GridAdapter mGridAdapter;

	private List<IGridItemHandler> mGridItems;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_others, container,
				false);

		mGridItems = new ArrayList<IGridItemHandler>();
		mGridItems.add(new AbsenseItemHandler(getActivity()));
		mGridItems.add(new DisciplineItemHandler(getActivity()));
		mGridItems.add(new SemScoreItemHandler(getActivity()));
		mGridItems.add(new EvalScoreItemHandler(getActivity()));
		mGridItems.add(new ParentCodeItemHandler(getActivity()));
		mGridItems.add(new SettingsItemHandler(getActivity()));
		
		//mGridItems.add(new EvalScoreItemHandler(getActivity()));
		//mGridItems.add(new StoreItemHandler(getActivity()));

		mGridAdapter = new GridAdapter(getActivity());
		mGridView = (GridView) rootView.findViewById(R.id.gridOthers);
		mGridView.setAdapter(mGridAdapter);
		mGridView.setOnItemClickListener(new GridOnClickListener());
		return rootView;
	}

	public class GridOnClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			IGridItemHandler item = mGridItems.get(position);
			item.onClick();
		}

	}

	private class GridAdapter extends BaseAdapter {

		private Context _context;
		private LayoutInflater _inflater;

		GridAdapter(Context context) {
			_context = context;
			_inflater = (LayoutInflater) _context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mGridItems.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mGridItems.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			IGridItemHandler item = mGridItems.get(position);
			ViewHolder holder;

			if (convertView == null) {
				convertView = _inflater
						.inflate(R.layout.item_grid_others, null);
				holder = new ViewHolder();
				holder.imgView = (ImageView) convertView
						.findViewById(R.id.imgOtherIcon);
				holder.txtView = (TextView) convertView
						.findViewById(R.id.txtOtherTitle);
				convertView.setTag(holder);
			}

			holder = (ViewHolder) convertView.getTag();
			holder.imgView.setImageResource(item.getDrawableId());
			holder.txtView.setText(item.getTitleId());
			return convertView;
		}

	}

	private static class ViewHolder {
		public ImageView imgView;
		public TextView txtView;
	}
}
