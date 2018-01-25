package com.creative.fines.app.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.creative.fines.app.R;

import java.util.ArrayList;
import java.util.HashMap;


public class WorkAdapter extends BaseAdapter{

	private LayoutInflater inflater;
	private ArrayList<HashMap<String,Object>> workMenuList;
	private ViewHolder viewHolder;
	private Context con;


	public WorkAdapter(Context con , ArrayList<HashMap<String,Object>> array){
		inflater = LayoutInflater.from(con);
		workMenuList = array;
		this.con = con;
	}

	@Override
	public int getCount() {
		return workMenuList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(final int position, final View convertview, ViewGroup parent) {

		View v = convertview;

		if(v == null){
			viewHolder = new ViewHolder();

			v = inflater.inflate(R.layout.list_item_green, parent,false);
			viewHolder.menu_title = (TextView)v.findViewById(R.id.textView1);

			v.setTag(viewHolder);

		}else {
			viewHolder = (ViewHolder)v.getTag();
		}
		viewHolder.menu_title.setText(workMenuList.get(position).get("title").toString());

		return v;
	}

	
	public void setArrayList(ArrayList<HashMap<String,Object>> arrays){
		this.workMenuList = arrays;
	}
	
	public ArrayList<HashMap<String,Object>> getArrayList(){
		return workMenuList;
	}
	
	
	/*
	 * ViewHolder
	 */
	class ViewHolder{
		TextView menu_title;
	}
	

}







