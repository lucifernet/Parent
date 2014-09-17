package tw.com.ischool.parent.tabs.message;

import ischool.dsa.client.OnReceiveListener;
import ischool.dsa.utility.DSRequest;
import ischool.dsa.utility.DSResponse;
import ischool.dsa.utility.XmlUtil;
import ischool.dsa.utility.http.Cancelable;
import ischool.utilities.StringUtil;

import java.util.List;

import org.w3c.dom.Element;

import tw.com.ischool.account.login.Accessable;
import tw.com.ischool.parent.ChildInfo;
import tw.com.ischool.parent.MainActivity;
import tw.com.ischool.parent.Parent;
import tw.com.ischool.parent.R;
import tw.com.ischool.parent.tabs.message.sign.ISignProcessor;
import tw.com.ischool.parent.tabs.message.sign.SignProcessor;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class MessageFragment extends Fragment {

	public static final String PREFERENCE_LAST_MESSAGE_TIME = "LastMessageTime";
	public static final String PREFERENCE_FILE_NAME = "tw.com.ischool.parent.tmp.preference";
	public static final int CODE_REQUEST = 101;

	private MessageDataSource mMessageSource;
	private CursorAdapter mMessageAdapter;
	private PullToRefreshListView mListView;
	private int mNewMessageReturned;
	private int mNewMessageCount = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// The last two arguments ensure LayoutParams are inflated
		// properly.
		View rootView = inflater.inflate(R.layout.fragment_message, container,
				false);

		mListView = (PullToRefreshListView) rootView
				.findViewById(R.id.lvMessage);
		mListView.setOnItemClickListener(new MessageClickListener());
		mListView.setMode(Mode.PULL_FROM_START);
		mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				if (refreshView.isHeaderShown()) {
					// 处理下拉刷新的业务
					requestNewMessage(new OnReceiveListener<Void>() {

						@Override
						public void onReceive(Void result) {
							mListView.onRefreshComplete();
						}

						@Override
						public void onError(Exception ex) {

						}
					});
				} else {
					// 处理上拉加载更多的业务
					// new GetMoreDataTask().execute();
				}

			}

		});

		mMessageSource = new MessageDataSource(getActivity());
		mMessageSource.open();

		getMessage();

		return rootView;
	}

	private void getMessage() {
		Cursor c = mMessageSource.getAllMessages();
		mMessageAdapter = new MessageAdapter(this.getActivity(), c);
		mListView.setAdapter(mMessageAdapter);

		requestNewMessage(null);
	}

	private void requestNewMessage(final OnReceiveListener<Void> listener) {
		// TODO 先取得最後一次取出訊息的 Uid
		Cancelable mCancelable = new Cancelable();
		mNewMessageReturned = 0;
		for (Accessable acc : MainActivity.getAccessables()) {
			final String school = acc.getAccessPoint();

			List<ChildInfo> children = MainActivity.getChildren()
					.findSchoolChild(acc);

			DSRequest request = new DSRequest();
			Element content = XmlUtil.createElement("Request");

			long lastUid = mMessageSource.getLastUid(school);

			XmlUtil.addElement(content, "LastUid", lastUid + StringUtil.EMPTY);

			for (ChildInfo child : children) {
				XmlUtil.addElement(content, "ClassId", child.getClassId());
			}

			request.setContent(content);

			mNewMessageCount = 0;

			MainActivity.getConnectionHelper().callService(acc,
					Parent.CONTRACT_PARENT, Parent.SERVICE_GET_MESSAGE,
					request, new OnReceiveListener<DSResponse>() {

						@Override
						public void onReceive(DSResponse result) {
							Element rsp = result.getContent();
							int count = XmlUtil.selectElements(rsp, "Message")
									.size();

							if (count > 0) {
								mMessageSource.insertMessages(school, rsp);
								mNewMessageCount += count;
							}

							mNewMessageReturned++;
							checkReceiveCompleted(listener);
						}

						@Override
						public void onError(Exception ex) {
							mNewMessageReturned++;
							checkReceiveCompleted(listener);
						}
					}, mCancelable);
		}
	}

	private void checkReceiveCompleted(OnReceiveListener<Void> listener) {
		if (mNewMessageReturned == MainActivity.getAccessables().size()) {
			if (listener != null)
				listener.onReceive(null);
		} else {
			return;
		}

		if (mNewMessageCount > 0) {
			Cursor cursor = mMessageSource.getAllMessages();
			mMessageAdapter.changeCursor(cursor);
			mMessageAdapter.notifyDataSetChanged();

			Toast.makeText(getActivity(), "取得 " + mNewMessageCount + " 筆新訊息",
					Toast.LENGTH_SHORT).show();
		}
	}

	private class MessageClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			ViewHolder holder = (ViewHolder) view.getTag();

			Intent intent = new Intent(getActivity(),
					MessageContentActivity.class);
			Bundle bundle = new Bundle();
			bundle.putLong(MessageContentActivity.PARAM_MESSAGE_ID, holder.id);
			intent.putExtras(bundle);
			startActivityForResult(intent, CODE_REQUEST);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode != MessageContentActivity.RESULT_DID_READ)
			return;

		Bundle bundle = data.getExtras();
		// long id = bundle.getLong(MessageContentActivity.PARAM_MESSAGE_ID);

		mMessageAdapter.notifyDataSetChanged();

		// 以下是傳說中更新一筆資料最有效率的方法
		// int start = mListView.getFirstVisiblePosition();
		// int end = mListView.getLastVisiblePosition();
		//
		// for (int i = start; i <= end; i++) {
		// View view = mListView.getChildAt(i - start);
		// ViewHolder holder = (ViewHolder) view.getTag();
		// if (holder.id == id) {
		// mListView.getAdapter().getView(i, view, mListView);
		// break;
		// }
		// }
	}

	private class MessageAdapter extends CursorAdapter {

		private LayoutInflater _inflater;

		public MessageAdapter(Context context, Cursor c) {
			super(context, c, true);

			_inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ViewHolder holder = (ViewHolder) view.getTag();

			Message msg = new Message(cursor);

			long id = cursor.getLong(cursor
					.getColumnIndex(MessageHelper.COLUMN_ID));

			Element raw = msg.getRawElement();
			Element content = XmlUtil.selectElement(raw, "Content");
			content = XmlUtil.selectElement(content, "Message");
			Element from = XmlUtil.selectElement(content, "From");

			String school = XmlUtil.getElementText(from, "School");
			String subject = XmlUtil.getElementText(content, "Subject");
			String unit = XmlUtil.getElementText(from, "Unit");

			int color = msg.isRead() ? 0xffe4e4e4 : 0xffffffff;
			view.setBackgroundColor(color);

			holder.txtFromSchool.setText(school);
			holder.txtFromUnit.setText(unit);
			holder.txtSubject.setText(subject);
			holder.id = id;

			// 設定看版
			String actionType = XmlUtil.getElementText(raw, "ActionType");
			ISignProcessor processor = SignProcessor.getInstanct(actionType);
			processor.process(getActivity(), holder.txtSign, raw);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			ViewHolder holder = new ViewHolder();

			View inflate = _inflater.inflate(R.layout.item_message, null);
			holder.txtSign = (TextView) inflate.findViewById(R.id.txtSign);
			holder.txtSubject = (TextView) inflate
					.findViewById(R.id.txtSubject);
			holder.txtFromSchool = (TextView) inflate
					.findViewById(R.id.txtFromSchool);
			holder.txtFromUnit = (TextView) inflate
					.findViewById(R.id.txtFromUnit);
			inflate.setTag(holder);
			return inflate;
		}

	}

	static class ViewHolder {
		public TextView txtSign;
		public TextView txtSubject;
		public TextView txtFromSchool;
		public TextView txtFromUnit;
		public long id;
	}
}
