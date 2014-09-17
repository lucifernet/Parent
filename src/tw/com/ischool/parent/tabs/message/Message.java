package tw.com.ischool.parent.tabs.message;

import ischool.dsa.utility.XmlHelper;
import ischool.utilities.StringUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.w3c.dom.Element;

import android.database.Cursor;

public class Message {
	public static final String FORMAT_DATETIME = "yyyy/MM/dd HH:mm:sss";

	private String mId;
	private String mRawContent;
	private Calendar mTimestamp;
	private String mTimeStampString;
	private boolean mDidRead;

	public Message(Cursor cursor) {
		mId = cursor.getString(cursor.getColumnIndex(MessageHelper.COLUMN_ID));
		mRawContent = cursor.getString(cursor
				.getColumnIndex(MessageHelper.COLUMN_RAW_CONTENT));

		setTimeStampString(cursor.getString(cursor
				.getColumnIndex(MessageHelper.COLUMN_TIMESTAMP)));
		int didRead = cursor.getInt(cursor
				.getColumnIndex(MessageHelper.COLUMN_DID_READ));
		mDidRead = didRead == 1;

	}

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		mId = id;
	}

	public String getRawContent() {
		return mRawContent;
	}

	public void setRawContent(String rawContent) {
		mRawContent = rawContent;
	}

	public Element getRawElement() {
		if (StringUtil.isNullOrWhitespace(mRawContent))
			return null;
		try {
			return XmlHelper.parseXml(mRawContent);
		} catch (Exception ex) {
			return null;
		}
	}

	public boolean isRead() {
		return mDidRead;
	}

	public void setRead(boolean didRead) {
		mDidRead = didRead;
	}

	@Override
	public String toString() {
		return mRawContent;
	}

	public String getTimeStampString() {
		return mTimeStampString;
	}

	public void setTimeStampString(String timeStampString) {
		mTimeStampString = timeStampString;
	}

	public Calendar getTimestamp() {
		if (mTimestamp != null)
			return mTimestamp;

		SimpleDateFormat format = new SimpleDateFormat(FORMAT_DATETIME,
				Locale.getDefault());
		try {
			Date d = format.parse(mTimeStampString);
			mTimestamp = Calendar.getInstance(Locale.getDefault());
			mTimestamp.setTime(d);
		} catch (ParseException e) {
			return Calendar.getInstance(Locale.getDefault());
		}

		return mTimestamp;
	}
}
