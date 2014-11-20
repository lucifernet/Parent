package tw.com.ischool.parent.tabs.message;

import ischool.dsa.utility.XmlHelper;
import ischool.dsa.utility.XmlUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.w3c.dom.Element;

import tw.com.ischool.parent.MainActivity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MessageDataSource {

	private SQLiteDatabase database;
	private MessageHelper dbHelper;
	private String[] allColumns = { MessageHelper.COLUMN_ID,
			MessageHelper.COLUMN_SERVER_UID, MessageHelper.COLUMN_RAW_CONTENT,
			MessageHelper.COLUMN_TIMESTAMP, MessageHelper.COLUMN_DID_READ };

	private SimpleDateFormat formatter;

	public MessageDataSource(Context context) {
		dbHelper = new MessageHelper(context);
		formatter = new SimpleDateFormat(Message.FORMAT_DATETIME,
				Locale.getDefault());
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public Cursor getAllMessages(String account) {

		Cursor mCursor = database.query(MessageHelper.TABLE_MESSAGE,
				allColumns, MessageHelper.COLUMN_ACCOUNT + "=?",
				new String[] { account }, null, null,
				MessageHelper.COLUMN_TIMESTAMP + " desc");

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public long insertMessage(String school, String account, Element message) {
		Calendar now = Calendar.getInstance(Locale.getDefault());

		String serverUid = XmlUtil.getElementText(message, "Uid");

		ContentValues initialValues = new ContentValues();
		initialValues.put(MessageHelper.COLUMN_TIMESTAMP,
				formatter.format(now.getTime()));
		initialValues.put(MessageHelper.COLUMN_RAW_CONTENT,
				XmlHelper.convertToString(message, true));
		initialValues.put(MessageHelper.COLUMN_SERVER_UID, serverUid);
		initialValues.put(MessageHelper.COLUMN_DID_READ, "-1");
		initialValues.put(MessageHelper.COLUMN_SERVER, school);
		initialValues.put(MessageHelper.COLUMN_ACCOUNT, account);

		Cursor cursor = database.query(MessageHelper.TABLE_MESSAGE,
				new String[] { MessageHelper.COLUMN_ID },
				MessageHelper.COLUMN_SERVER_UID + "=?",
				new String[] { serverUid }, null, null, null);

		long id = -1;
		if (cursor != null && cursor.moveToFirst()) {
			id = cursor.getLong(cursor.getColumnIndex(MessageHelper.COLUMN_ID));

			database.update(MessageHelper.TABLE_MESSAGE, initialValues,
					MessageHelper.COLUMN_ID + "=?",
					new String[] { String.valueOf(id) });
			return id;
		} else {
			return database.insert(MessageHelper.TABLE_MESSAGE, null,
					initialValues);
		}
	}

	public long insertMessages(String school, String account, Element content) {
		long id = -1;
		for (Element element : XmlUtil.selectElements(content, "Message"))
			id = insertMessage(school, account, element);

		return id;
	}

	/**
	 * 取回指定學校最後一筆 UID
	 * 
	 * @param school
	 *            : 指定學校
	 * **/
	public long getLastUid(String school, String account) {
		Cursor mCursor = database.query(MessageHelper.TABLE_MESSAGE,
				new String[] { MessageHelper.COLUMN_SERVER_UID },
				MessageHelper.COLUMN_SERVER + "=? and "
						+ MessageHelper.COLUMN_ACCOUNT + "=?", new String[] {
						school, account }, null, null,
				MessageHelper.COLUMN_SERVER_UID + " desc", "1");

		if (mCursor != null && mCursor.moveToFirst()) {
			return mCursor.getLong(0);
		}
		return -1;
	}

	/**
	 * 依 sqlite 中的 _id 欄位取回指定的訊息
	 * **/
	public Message getMessage(long id) {
		Cursor mCursor = database.query(MessageHelper.TABLE_MESSAGE,
				allColumns, MessageHelper.COLUMN_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, "1");

		if (mCursor != null && mCursor.moveToFirst()) {
			return new Message(mCursor);
		}
		return null;
	}

	/**
	 * 將指定訊息設定為已讀
	 * **/
	public void setDidRead(long id) {
		setMessageStatus(id, 1);
	}

	/**
	 * 設定訊息狀態
	 * 
	 * @param id
	 *            : 訊息在手機內的編號
	 * @param status
	 *            : 訊息狀態, -1未通知, 0未讀, 1已讀
	 * **/
	public void setMessageStatus(long id, int status) {
		ContentValues values = new ContentValues();
		values.put(MessageHelper.COLUMN_DID_READ, status);
		long count = database.update(MessageHelper.TABLE_MESSAGE, values,
				MessageHelper.COLUMN_ID + "=?",
				new String[] { String.valueOf(id) });
		Log.d(MainActivity.TAG, "set Did Read count : " + count);

	}

	/**
	 * 取得未通知訊息筆數
	 * **/
	public int getUnnotifyCount() {
		String sql = "select count(*) from " + MessageHelper.TABLE_MESSAGE
				+ " where " + MessageHelper.COLUMN_DID_READ + "=?";
		Cursor c = database.rawQuery(sql, new String[] { "-1" });
		c.moveToFirst();
		int count = c.getInt(0);
		c.close();
		Log.d(MainActivity.TAG, "get unread count : " + count);

		return count;
	}

	/**
	 * 將所有未通知訊息設為未讀
	 * **/
	public void setAllMessageNotified() {
		ContentValues values = new ContentValues();
		values.put(MessageHelper.COLUMN_DID_READ, 0);
		database.update(MessageHelper.TABLE_MESSAGE, values,
				MessageHelper.COLUMN_DID_READ + "=?",
				new String[] { String.valueOf(-1) });

	}

	/**
	 * 取出第一筆未通知訊息
	 * **/
	public Message getUnnotifyMessage() {
		Cursor mCursor = database.query(MessageHelper.TABLE_MESSAGE,
				allColumns, MessageHelper.COLUMN_DID_READ + "=?",
				new String[] { String.valueOf("-1") }, null, null, null, "1");

		if (mCursor != null && mCursor.moveToFirst()) {
			return new Message(mCursor);
		}
		return null;
	}

	public List<Message> getUnnotifyMessages() {
		ArrayList<Message> messages = new ArrayList<Message>();
		Cursor mCursor = database.query(MessageHelper.TABLE_MESSAGE,
				allColumns, MessageHelper.COLUMN_DID_READ + "=?",
				new String[] { String.valueOf("-1") }, null, null, null, "5");

		if (mCursor != null) {
			mCursor.moveToPrevious();
			while(mCursor.moveToNext()){
				Message m = new Message(mCursor);
				messages.add(m);
			}
		}
		
		mCursor.close();
		return messages;
	}
}
