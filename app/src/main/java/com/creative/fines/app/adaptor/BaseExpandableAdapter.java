package com.creative.fines.app.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.creative.fines.app.R;


public class BaseExpandableAdapter extends BaseExpandableListAdapter {

	private String[] groupList = null;
	private String[][] childList = null;
	private LayoutInflater inflater = null;
	private ViewHolder viewHolder = null;

	public BaseExpandableAdapter(Context c, String[] groupList, String[][] childList){
		super();
		this.inflater = LayoutInflater.from(c);
		this.groupList = groupList;
		this.childList = childList;
	}

	// 그룹 포지션을 반환한다.
	@Override
	public String getGroup(int groupPosition) {
		return groupList[groupPosition];
	}

	// 그룹 사이즈를 반환한다.
	@Override
	public int getGroupCount() {
		return groupList.length;
	}

	// 그룹 ID를 반환한다.
	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	// 그룹뷰 각각의 ROW 
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
							 View convertView, ViewGroup parent) {

		View v = convertView;

		if(v == null){
			viewHolder = new ViewHolder();
			v = inflater.inflate(R.layout.expand_list_gitem, parent, false);
			viewHolder.tv_groupName = (TextView) v.findViewById(R.id.textView1);
			v.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)v.getTag();
		}

		viewHolder.tv_groupName.setText(getGroup(groupPosition));

		return v;
	}

	// 차일드뷰를 반환한다.
	@Override
	public String getChild(int groupPosition, int childPosition) {
		return childList[groupPosition][childPosition];
	}

	// 차일드뷰 사이즈를 반환한다.
	@Override
	public int getChildrenCount(int groupPosition) {
		return childList[groupPosition].length;
	}

	// 차일드뷰 ID를 반환한다.
	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	// 차일드뷰 각각의 ROW
	@Override
	public View getChildView(int groupPosition, int childPosition,
							 boolean isLastChild, View convertView, ViewGroup parent) {

		View v = convertView;

		if(v == null){
			viewHolder = new ViewHolder();
			v = inflater.inflate(R.layout.expand_list_citem, null);
			viewHolder.tv_childName = (TextView) v.findViewById(R.id.textView1);
//			viewHolder.iv_image = (ImageView) v.findViewById(R.id.imageView1);

			v.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)v.getTag();
		}

		viewHolder.tv_childName.setText(getChild(groupPosition, childPosition));

		return v;
	}

	@Override
	public boolean hasStableIds() { return true; }

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) { return true; }

	class ViewHolder {
		public ImageView iv_image;
		public TextView tv_groupName;
		public TextView tv_childName;
	}

}







