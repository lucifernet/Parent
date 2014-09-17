package tw.com.ischool.parent.tabs.calendar;

import java.util.Calendar;
import java.util.Locale;

import tw.com.ischool.parent.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.ListView;
import android.widget.TextView;

public class CalendarFragment extends Fragment {

	private CalendarView mCalView;
	private ListView mListView;
	private TextView mEmptyView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// The last two arguments ensure LayoutParams are inflated
		// properly.
		View rootView = inflater.inflate(R.layout.fragment_calendar, container,
				false);

		mEmptyView = (TextView) rootView.findViewById(R.id.emptyView);
		mListView = (ListView) rootView.findViewById(R.id.lvSchedule);
		mListView.setEmptyView(mEmptyView);

		mCalView = (CalendarView) rootView.findViewById(R.id.calView);

		mCalView.setOnDateChangeListener(new OnDateChangeListener() {

			@Override
			public void onSelectedDayChange(CalendarView view, int year,
					int month, int dayOfMonth) {

				String dateString = year + "/" + month + "/" + dayOfMonth;
				mEmptyView.setText(dateString + "/n"
						+ getString(R.string.calendar_empty));
			}
		});

		Calendar minDate = Calendar.getInstance(Locale.getDefault());
		minDate.set(2014, 5, 1);
		mCalView.setMinDate(minDate.getTimeInMillis());

		return rootView;
	}

}
