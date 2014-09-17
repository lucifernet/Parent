package tw.com.ischool.parent.util;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.Display;

public class ScreenHelper {
	// private Activity mActivity;
	private float mWidthDP;
	private float mHeightDP;
	private float mDensity;
	private int mWidthPixels;
	private int mHeightPixels;

	public ScreenHelper(Activity activity) {
		// mActivity = activity;

		Display display = activity.getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);

		mDensity = activity.getResources().getDisplayMetrics().density;
		setWidthPixels(outMetrics.widthPixels);
		setHeightPixels(outMetrics.heightPixels);

		setHeightDP(mHeightPixels / mDensity);
		setWidthDP(mWidthPixels / mDensity);
	}

	public int toPixelInt(int dp) {
		return (int) (dp * mDensity);
	}

	public float toPixelFloat(int dp) {
		return dp * mDensity;
	}

	public int toDp(int pixel) {
		return (int) (pixel / mDensity);
	}

	public float getWidthDP() {
		return mWidthDP;
	}

	public float getHeightDP() {
		return mHeightDP;
	}

	public int getWidthPixels() {
		return mWidthPixels;
	}

	public int getHeightPixels() {
		return mHeightPixels;
	}

	private void setHeightDP(float heightDP) {
		mHeightDP = heightDP;
	}

	private void setWidthDP(float widthDP) {
		mWidthDP = widthDP;
	}

	private void setWidthPixels(int widthPixels) {
		mWidthPixels = widthPixels;
	}

	private void setHeightPixels(int heightPixels) {
		mHeightPixels = heightPixels;
	}
}
