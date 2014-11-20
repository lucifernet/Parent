package tw.com.ischool.parent.tabs.message;

import ischool.dsa.utility.XmlUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;

import org.w3c.dom.Element;

import tw.com.ischool.parent.R;
import tw.com.ischool.parent.tabs.message.sign.BulletinProcessor;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MessageContentActivity extends Activity {

	// public static final String PARAM_RAW = "raw";
	public static final String PARAM_MESSAGE_ID = "messageid";
	public static final int RESULT_DID_READ = 0;

	private MessageDataSource mMessageSource;
	private TextView mTxtSubject;
	private TextView mTxtFromSchool;
	private TextView mTxtFromUnit;
	private TextView mTxtContent;
	private TextView mTxtTime;
	private TextView mTxtSign;
	private LinearLayout mLayoutAttachements;
	private ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_content);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();

		mMessageSource = new MessageDataSource(this);
		mMessageSource.open();

		// String rawString = bundle.getString(PARAM_RAW);

		long id = bundle.getLong(PARAM_MESSAGE_ID);
		Message msg = mMessageSource.getMessage(id);

		mTxtSubject = (TextView) findViewById(R.id.txtSubject);
		mTxtFromSchool = (TextView) findViewById(R.id.txtFromSchool);
		mTxtFromUnit = (TextView) findViewById(R.id.txtFromUnit);
		mTxtContent = (TextView) findViewById(R.id.txtContent);
		mTxtTime = (TextView) findViewById(R.id.txtTime);
		mTxtSign = (TextView) findViewById(R.id.txtSign);
		mLayoutAttachements = (LinearLayout) findViewById(R.id.layoutAttachments);

		Element raw = msg.getRawElement();
		String actionType = XmlUtil.getElementText(raw, "ActionType");
		Element content = XmlUtil.selectElement(raw, "Content");
		Element contentMessage = XmlUtil.selectElement(content, "Message");
		String subject = XmlUtil.getElementText(contentMessage, "Subject");

		Element from = XmlUtil.selectElement(contentMessage, "From");
		String school = XmlUtil.getElementText(from, "School");
		String unit = XmlUtil.getElementText(from, "Unit");
		String message = XmlUtil.getElementText(contentMessage, "Content");
		String timeString = XmlUtil.getElementText(raw, "LastUpdate");

		mTxtContent.setText(message);
		mTxtFromSchool.setText(school);
		mTxtFromUnit.setText(unit);
		mTxtSubject.setText(subject);
		mTxtTime.setText(timeString);

		mMessageSource.setDidRead(id);

		BulletinProcessor.getInstanct(actionType).process(this, mTxtSign, raw);

		// 附加檔案
		// Log.d(MainActivity.TAG, XmlHelper.convertToString(contentMessage,
		// true));
		Element attachments = XmlUtil.selectElement(contentMessage,
				"Attachments");
		List<Element> urls = XmlUtil.selectElements(attachments, "URL");
		if (urls.size() == 0) {
			mLayoutAttachements.setVisibility(View.GONE);
		} else {
			mLayoutAttachements.setVisibility(View.VISIBLE);

			// instantiate it within the onCreate method
			mProgressDialog = new ProgressDialog(MessageContentActivity.this);
			mProgressDialog
					.setMessage(getString(R.string.message_content_downloading));
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setCancelable(true);

			for (Element url : urls) {
				final String urlString = url.getTextContent();
				String[] s = urlString.split("/");
				String fname = urlString;
				if (s.length > 0) {
					fname = s[s.length - 1];
					try {
						fname = URLDecoder.decode(fname, "UTF-8");
					} catch (UnsupportedEncodingException e) {

					}
				}
				final String fileName = fname;
				View child = getLayoutInflater().inflate(
						R.layout.item_attachments, null);

				TextView txtFileName = (TextView) child
						.findViewById(R.id.txtFileName);
				txtFileName.setText(fileName);

				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				layoutParams.setMargins(10, 10, 10, 10);
				mLayoutAttachements.addView(child, layoutParams);

				child.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						// execute this when the downloader must be fired
						final DownloadTask downloadTask = new DownloadTask(
								MessageContentActivity.this);
						downloadTask.execute(urlString);

						mProgressDialog
								.setOnCancelListener(new DialogInterface.OnCancelListener() {
									@Override
									public void onCancel(DialogInterface dialog) {
										downloadTask.cancel(true);
									}
								});
					}
				});
			}
		}

		setResult(RESULT_DID_READ, intent);
	}

	@Override
	protected void onDestroy() {
		if (mMessageSource != null)
			mMessageSource.close();
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// usually, subclasses of AsyncTask are declared inside the activity class.
	// that way, you can easily modify the UI thread from here
	private class DownloadTask extends AsyncTask<String, Integer, String> {

		private Context context;
		private PowerManager.WakeLock mWakeLock;
		private String mFileName;

		public DownloadTask(Context context) {
			this.context = context;
		}

		@Override
		protected String doInBackground(String... sUrl) {
			InputStream input = null;
			OutputStream output = null;
			HttpURLConnection connection = null;

			try {
				URL url = new URL(sUrl[0]);
				connection = (HttpURLConnection) url.openConnection();
				connection.connect();

				// expect HTTP 200 OK, so we don't mistakenly save error report
				// instead of the file
				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					return "Server returned HTTP "
							+ connection.getResponseCode() + " "
							+ connection.getResponseMessage();
				}

				// this will be useful to display download percentage
				// might be -1: server did not report the length
				int fileLength = connection.getContentLength();

				// download the file
				input = connection.getInputStream();

				String[] s = sUrl[0].split("/");
				String fname = s[s.length - 1];
				if (s.length > 0) {
					fname = s[s.length - 1];
					fname = URLDecoder.decode(fname, "UTF-8");
				}
				File file = new File(
						Environment
								.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
						fname);
				mFileName = file.getAbsolutePath();
				output = new FileOutputStream(file);

				byte data[] = new byte[4096];
				long total = 0;
				int count;
				while ((count = input.read(data)) != -1) {
					// allow canceling with back button
					if (isCancelled()) {
						input.close();
						return null;
					}
					total += count;
					// publishing the progress....
					if (fileLength > 0) // only if total length is known
						publishProgress((int) (total * 100 / fileLength));
					output.write(data, 0, count);
				}
			} catch (Exception e) {
				return e.toString();
			} finally {
				try {
					if (output != null)
						output.close();
					if (input != null)
						input.close();
				} catch (IOException ignored) {
				}

				if (connection != null)
					connection.disconnect();
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// take CPU lock to prevent CPU from going off if the user
			// presses the power button during download
			PowerManager pm = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					getClass().getName());
			mWakeLock.acquire();
			mProgressDialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			// if we get here, length is known, now set indeterminate to false
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.setMax(100);
			mProgressDialog.setProgress(progress[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			mWakeLock.release();
			mProgressDialog.dismiss();
			if (result != null)
				Toast.makeText(context, "Download error: " + result,
						Toast.LENGTH_LONG).show();
			else {
				Toast.makeText(context, "檔案儲存於 : " + mFileName,
						Toast.LENGTH_SHORT).show();
				openFile(mFileName);
			}
		}
	}

	private void openFile(String fileName) {
		MimeTypeMap myMime = MimeTypeMap.getSingleton();

		Intent newIntent = new Intent(android.content.Intent.ACTION_VIEW);

		File file = new File(fileName);

		// Intent newIntent = new Intent(Intent.ACTION_VIEW);
		String mimeType = myMime.getMimeTypeFromExtension(fileExt(fileName)
				.substring(1));
		newIntent.setDataAndType(Uri.fromFile(file), mimeType);
		newIntent.setFlags(newIntent.FLAG_ACTIVITY_NEW_TASK);
		try {
			startActivity(newIntent);
		} catch (android.content.ActivityNotFoundException e) {
			Toast.makeText(this, "無開啟此檔案的應用程式", 4000).show();
		}
	}

	private String fileExt(String url) {
		if (url.indexOf("?") > -1) {
			url = url.substring(0, url.indexOf("?"));
		}
		if (url.lastIndexOf(".") == -1) {
			return null;
		} else {
			String ext = url.substring(url.lastIndexOf("."));
			if (ext.indexOf("%") > -1) {
				ext = ext.substring(0, ext.indexOf("%"));
			}
			if (ext.indexOf("/") > -1) {
				ext = ext.substring(0, ext.indexOf("/"));
			}
			return ext.toLowerCase();

		}
	}
}
