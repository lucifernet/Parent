package tw.com.ischool.parent.tabs.message;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MessageHelper extends SQLiteOpenHelper {

	public static final String TABLE_MESSAGE = "messages";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_SERVER_UID = "uid";
	public static final String COLUMN_SERVER = "server";
	public static final String COLUMN_TIMESTAMP = "timestamp";
	public static final String COLUMN_RAW_CONTENT = "rawcontent";
	public static final String COLUMN_ACCOUNT = "account"; // 切換不同帳號時判斷用
	
	/**
	 * 訊息狀態, -1 表示未通知, 0 表示已通知未讀取, 1表示已讀
	 * **/
	public static final String COLUMN_DID_READ = "did_read";

	private static final String DATABASE_NAME = "message.db";
	private static final int DATABASE_VERSION = 2;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_MESSAGE + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_SERVER_UID
			+ " integer not null, " + COLUMN_SERVER + " text not null, "
			+ COLUMN_DID_READ + " integer not null, " + COLUMN_RAW_CONTENT
			+ " text not null, " + COLUMN_TIMESTAMP + " text not null, "
			+ COLUMN_ACCOUNT + " text not null);";

	public MessageHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO 這裡要依據不同的版本調整資料表內容
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGE);
		onCreate(db);
	}

}
