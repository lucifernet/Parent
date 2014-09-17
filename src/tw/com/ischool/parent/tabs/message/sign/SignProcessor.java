package tw.com.ischool.parent.tabs.message.sign;

import org.w3c.dom.Element;

import android.content.Context;
import android.widget.TextView;

public abstract class SignProcessor implements ISignProcessor {

	@Override
	public void process(Context context, TextView txtSign, Element rawElement) {
		String word = getWord(rawElement);
		setValue(context, txtSign, word);
	}

	abstract int getColorId();

	abstract String getWord(Element rawElement);

	protected void setValue(Context context, TextView txtSign, String word) {
		int color = context.getResources().getColor(getColorId());
		txtSign.setBackgroundColor(color);
		txtSign.setText(word);
	}

	public static ISignProcessor getInstanct(String actionType) {
		if (actionType.equalsIgnoreCase("bulletin"))
			return new BulletinProcessor();
		return new BulletinProcessor();
	}

}
