package com.creative.fines.app.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.creative.fines.app.R;

import java.util.ArrayList;
import java.util.HashMap;


public class BaseAdapter extends android.widget.BaseAdapter {

	private LayoutInflater inflater;
	private ArrayList<HashMap<String,Object>> RowList;
	private ViewHolder viewHolder;
	private Context con;


	public BaseAdapter(Context con , ArrayList<HashMap<String,Object>> array){
		inflater = LayoutInflater.from(con);
		RowList = array;
		this.con = con;
	}

	@Override
	public int getCount() {
		return RowList.size();
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

			v = inflater.inflate(R.layout.search_list_row, parent,false);
			viewHolder.nm = (TextView)v.findViewById(R.id.textView1);
			viewHolder.kind = (TextView)v.findViewById(R.id.textView2);

			v.setTag(viewHolder);

		}else {
			viewHolder = (ViewHolder)v.getTag();
		}
		viewHolder.nm.setText(RowList.get(position).get("data1").toString());
		viewHolder.kind.setText(RowList.get(position).get("data2").toString());

		return v;
	}

	
	public void setArrayList(ArrayList<HashMap<String,Object>> arrays){
		this.RowList = arrays;
	}
	
	public ArrayList<HashMap<String,Object>> getArrayList(){
		return RowList;
	}
	
	
	/*
	 * ViewHolder
	 */
	class ViewHolder{
		TextView nm;
		TextView kind;
	}
	

}







