package tw.com.ischool.parent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import tw.com.ischool.account.login.Accessable;


public class Children implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<ChildInfo> _children;

	public Children() {
		_children = new ArrayList<ChildInfo>();
	}

	public void addChild(ChildInfo child) {
		_children.add(child);
	}
	
	public void addChildren(Children children){
		_children.addAll(children.getChildren());
	}

	public void clear(){
		_children.clear();
	}
	
	public List<ChildInfo> findSchoolChild(Accessable accessable) {
		ArrayList<ChildInfo> children = new ArrayList<ChildInfo>();

		for (ChildInfo child : _children) {
			if (child.getAccessable() == accessable) {
				children.add(child);
			}
		}

		return children;
	}

	public List<ChildInfo> getChildren() {
		return _children;
	}

	public void remove(ChildInfo child) {
		int index = -1;
		for(int i=0;i<_children.size();i++){
			ChildInfo c = _children.get(i);
			if(c == child){
				index = i;
				break;
			}
			
			if(c.getStudentId().equals(child.getStudentId()) && c.getStudentName().equals(child.getStudentName())){
				index = i;
				break;
			}
		}
		
		if(index > -1){
			_children.remove(index);
		}		
	}
}
