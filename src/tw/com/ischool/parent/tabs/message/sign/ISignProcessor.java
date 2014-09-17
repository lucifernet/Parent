package tw.com.ischool.parent.tabs.message.sign;

import org.w3c.dom.Element;

import android.content.Context;
import android.widget.TextView;

public interface ISignProcessor {
	void process(Context context, TextView txtSign, Element rawElement);
}
