package tw.com.ischool.parent.util;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class SimpleArrayAdapter<T> extends BaseAdapter {

	protected LayoutInflater _inflater;
	protected List<T> _array;
		
	public SimpleArrayAdapter(Context context, List<T> array) {
		super();
		_array = array;
		_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return _array.size();
	}

	@Override
	public Object getItem(int position) {
		return _array.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {		
		return null;
	}
	
}
