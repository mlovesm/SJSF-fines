package com.creative.fines.app.adaptor;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.creative.fines.app.R;
import com.creative.fines.app.util.CustomBitmapPool;
import com.creative.fines.app.util.UtilClass;

import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.glide.transformations.CropCircleTransformation;


public class PersonnelAdapter extends BaseAdapter{

	private LayoutInflater inflater;
	private ArrayList<HashMap<String,Object>> peopleList;
	private ViewHolder viewHolder;
	private Context con;


	public PersonnelAdapter(Context con , ArrayList<HashMap<String,Object>> array){
		inflater = LayoutInflater.from(con);
		peopleList = array;
		this.con = con;
	}

	@Override
	public int getCount() {
		return peopleList.size();
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

			v = inflater.inflate(R.layout.people_list_item, parent,false);
			viewHolder.people_image = (ImageView) v.findViewById(R.id.imageView1);
			viewHolder.people_name = (TextView)v.findViewById(R.id.textView1);
			viewHolder.people_sabun = (TextView)v.findViewById(R.id.textView2);
			viewHolder.people_phone = (TextView)v.findViewById(R.id.textView3);
			viewHolder.people_email = (TextView)v.findViewById(R.id.textView4);

			v.setTag(viewHolder);

		}else {
			viewHolder = (ViewHolder)v.getTag();
		}
		byte[] byteArray =  Base64.decode(peopleList.get(position).get("user_pic").toString(), Base64.DEFAULT) ;
//		Bitmap bmp1 = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
		Glide.with(con).load(byteArray)
				.asBitmap()
				.transform(new CropCircleTransformation(new CustomBitmapPool()))
				.error(R.drawable.no_img)
//				.signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
				.into(viewHolder.people_image);

		viewHolder.people_name.setText(peopleList.get(position).get("user_nm").toString());
		String user_cell= peopleList.get(position).get("user_cell").toString();
		if(TextUtils.isEmpty((CharSequence) peopleList.get(position).get("user_cell"))){
			viewHolder.people_phone.setText("");
		}else{
			viewHolder.people_phone.setText(user_cell);
		}
		viewHolder.people_sabun.setText(peopleList.get(position).get("user_no").toString());
		viewHolder.people_email.setText(peopleList.get(position).get("user_email").toString());

		return v;
	}


	public void setArrayList(ArrayList<HashMap<String,Object>> arrays){
		this.peopleList = arrays;
	}

	public ArrayList<HashMap<String,Object>> getArrayList(){
		return peopleList;
	}


	/*
	 * ViewHolder
	 */
	class ViewHolder{
		ImageView people_image;
		TextView people_name;
		TextView people_phone;
		TextView people_email;
		TextView people_sabun;

	}


}







