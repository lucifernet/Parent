package tw.com.ischool.parent.tabs.others;

public interface IGridItemHandler {

	/**
	 * 取得 icon id
	 * **/
	int getDrawableId();

	/**
	 * 取得 title id
	 * **/
	int getTitleId();

	/**
	 * 按下時觸發事件
	 * **/
	void onClick();
}
