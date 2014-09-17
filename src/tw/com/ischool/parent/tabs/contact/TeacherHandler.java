package tw.com.ischool.parent.tabs.contact;

import tw.com.ischool.parent.ChildInfo;
import tw.com.ischool.parent.R;
import tw.com.ischool.parent.TeacherInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class TeacherHandler implements IContactHandler {

	private ChildInfo _child;
	private Context _context;

	public TeacherHandler(ChildInfo child, Context context) {
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

		TeacherInfo teacher = _child.getTeacher();
		txtView.setText(teacher.getName() + " ( "
				+ _context.getString(R.string.homeroom_teacher) + " ) ");

		Bitmap bitmap = teacher.getPhotoBitmap();

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
