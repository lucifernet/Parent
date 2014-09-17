package tw.com.ischool.parent.tabs.contact;

import tw.com.ischool.parent.ChildInfo;
import tw.com.ischool.parent.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ChildHandler implements IContactHandler {

	private ChildInfo _child;
	private Context _context;

	public ChildHandler(ChildInfo child, Context context) {
		_child = child;
		_context = context;
	}

	@Override
	public View inflate(LayoutInflater inflater, View convertView) {
		convertView = inflater.inflate(R.layout.item_contact_person, null);
		TextView txtView = (TextView) convertView
				.findViewById(R.id.txtPersonName);
		ImageView imgView = (ImageView) convertView
				.findViewById(R.id.imgPersonHead);

		txtView.setText(_child.getStudentName());

		Bitmap bitmap = _child.getPhotoBitmap();

		if (bitmap == null) {
			imgView.setImageResource(R.drawable.no_photo);
		} else {
			imgView.setImageBitmap(bitmap);
		}
		return convertView;
	}

	@Override
	public void onSelected() {

	}

}
