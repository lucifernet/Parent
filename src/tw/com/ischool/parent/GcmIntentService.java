package tw.com.ischool.parent;

import ischool.dsa.client.OnReceiveListener;
import ischool.dsa.utility.DSRequest;
import ischool.dsa.utility.DSResponse;
import ischool.dsa.utility.XmlUtil;
import ischool.dsa.utility.http.Cancelable;
import ischool.utilities.StringUtil;

import java.util.List;

import org.w3c.dom.Element;

import tw.com.ischool.account.login.Accessable;
import tw.com.ischool.parent.login.LoginHandler;
import tw.com.ischool.parent.tabs.message.Message;
import tw.com.ischool.parent.tabs.message.MessageContentActivity;
import tw.com.ischool.parent.tabs.message.MessageDataSource;
import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 5351316;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	private MessageDataSource mMessageSource;

	public GcmIntentService() {
		super("1086483844456");
	}

	private void initMessageSource() {
		if (mMessageSource == null) {
			mMessageSource = new MessageDataSource(this);
			mMessageSource.open();
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		initMessageSource();
		
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) { // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				// sendNotification("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				// sendNotification("Deleted messages on server: " +
				// extras.toString());
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {
				// This loop represents the service doing some work.
				// for (int i=0; i<5; i++) {
				// Log.i(MainActivity.TAG, "Working... " + (i+1)
				// + "/5 @ " + SystemClock.elapsedRealtime());
				// try {
				// Thread.sleep(5000);
				// } catch (InterruptedException e) {
				// }
				// }
				Log.i(MainActivity.TAG,
						"Completed work @ " + SystemClock.elapsedRealtime());
				// Post notification of received message.
				receiveMessage("Received: " + extras.toString(), extras);
				Log.i(MainActivity.TAG, "Received: " + extras.toString());
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void receiveMessage(String msg, Bundle data) {
		initMessageSource();
		
		String id = data.getString("id");
		final String dsns = data.getString("sender");

		if (Parent.getConnectionHelper() == null) {
			LoginHandler loginHandler = new LoginHandler(this);
			loginHandler.login(new LoginHandler.LoginListener() {

				@Override
				public void onReady() {
					getMessageFromSchool(dsns);
				}

				@Override
				public void onNotLogin() {
					// TODO Auto-generated method stub

				}
			});
		} else {
			getMessageFromSchool(dsns);
		}
	}

	private void getMessageFromSchool(String dsns) {
		Accessable accessable = null;
		for (Accessable a : Parent.getAccessables()) {
			if (a.getAccessPoint().equals(dsns)) {
				accessable = a;
				break;
			}
		}

		final String school = accessable.getAccessPoint();
		final String account = Parent.getConnectionHelper().getAccount().name;

		if (accessable != null) {
			List<ChildInfo> children = Parent.getChildren().findSchoolChild(
					accessable);

			DSRequest request = new DSRequest();
			Element content = XmlUtil.createElement("Request");

			long lastUid = mMessageSource.getLastUid(school, account);

			XmlUtil.addElement(content, "LastUid", lastUid + StringUtil.EMPTY);

			for (ChildInfo child : children) {
				XmlUtil.addElement(content, "ClassId", child.getClassId());
			}

			request.setContent(content);

			Parent.getConnectionHelper().callService(accessable,
					Parent.CONTRACT_PARENT, Parent.SERVICE_GET_MESSAGE,
					request, new OnReceiveListener<DSResponse>() {

						@Override
						public void onReceive(DSResponse result) {
							Element rsp = result.getContent();
							int count = XmlUtil.selectElements(rsp, "Message")
									.size();

							if (count > 0) {
								mMessageSource.insertMessages(school, account,
										rsp);
							}

							count = mMessageSource.getUnnotifyCount();
							
							if (count == 1) {
								notifyNewMessage();
							} else if (count > 1) {
								notifyNewMessages(count);
							}

						}

						@Override
						public void onError(Exception ex) {
							// TODO Auto-generated method stub

						}
					}, new Cancelable());
		}
	}

	private void notifyNewMessage() {
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Message msg = mMessageSource.getUnnotifyMessage();

		Intent intent = new Intent(this, MessageContentActivity.class);
		intent.putExtra(MessageContentActivity.PARAM_MESSAGE_ID, msg.getId());

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);

		String title = getString(R.string.gcm_title);
		title = String.format(title, "1");

		String subject = msg.getSubject();

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(title).setContentText(subject)
				.setSubText(msg.getFromSchool());

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void notifyNewMessages(int count) {
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		String title = getString(R.string.gcm_title);
		title = String.format(title, "1");

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

			List<Message> msgs = mMessageSource.getUnnotifyMessages();

			// 準備好 LargeIcon 的圖形物件
			Bitmap myBitmap = BitmapFactory.decodeResource(this.getResources(),
					R.drawable.ic_launcher);

			/* 準備 BigView 樣式 */
			Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
			String[] events = new String[msgs.size()];
			int index = 0;
			for (Message m : msgs) {
				events[index] = m.getSubject();
				index++;
			}

			// Sets a title for the Inbox style big view
			inboxStyle.setBigContentTitle(title);

			// Moves events into the big view
			for (int i = 0; i < events.length; i++) {

				inboxStyle.addLine(events[i]);
			}

			// 建立 Notification
			Notification noti = new Notification.Builder(this)
					.setSmallIcon(R.drawable.ic_launcher)
					.setLargeIcon(myBitmap).setContentIntent(pIntent)
					.setAutoCancel(true).setStyle(inboxStyle) // Moves the big
																// view style
																// object into
																// the
																// notification
																// object.
					.build();

			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

			notificationManager.notify(NOTIFICATION_ID, noti);
		} else {
			String subject = "按此處開啟程式";
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					this).setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle(title).setContentText(subject);

			mBuilder.setContentIntent(pIntent);
			mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
		}
	}
}
