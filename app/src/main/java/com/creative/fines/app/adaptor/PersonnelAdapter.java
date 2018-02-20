package com.creative.fines.app.adaptor;

import android.content.Context;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.creative.fines.app.R;
import com.creative.fines.app.util.UtilClass;

import java.util.ArrayList;
import java.util.HashMap;


public class PersonnelAdapter extends BaseAdapter{

	private LayoutInflater inflater;
	private ArrayList<HashMap<String,String>> peopleList;
	private ViewHolder viewHolder;
	private Context con;


	public PersonnelAdapter(Context con , ArrayList<HashMap<String,String>> array){
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
			viewHolder.dataL1 = (TextView)v.findViewById(R.id.textView2);
			viewHolder.dataL2 = (TextView)v.findViewById(R.id.textView3);
			viewHolder.dataR1 = (TextView)v.findViewById(R.id.textView4);
			viewHolder.dataR2 = (TextView)v.findViewById(R.id.textView5);
			viewHolder.dataR3 = (TextView)v.findViewById(R.id.textView6);
			viewHolder.dataR4 = (TextView)v.findViewById(R.id.textView7);
			viewHolder.dataR5 = (TextView)v.findViewById(R.id.textView8);

			v.setTag(viewHolder);

		}else {
			viewHolder = (ViewHolder)v.getTag();
		}
		UtilClass.dataNullCheckZero(peopleList.get(position));
		byte[] byteArray =  Base64.decode(peopleList.get(position).get("user_pic").toString(), Base64.DEFAULT) ;
//		Bitmap bmp1 = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
		Glide.with(con).load(byteArray)
				.asBitmap()
//				.transform(new CropCircleTransformation(new CustomBitmapPool()))
				.error(R.drawable.no_img)
//				.signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
				.into(viewHolder.people_image);

		viewHolder.people_name.setText(peopleList.get(position).get("user_nm").toString());

		viewHolder.dataL1.setText(peopleList.get(position).get("L1").toString());
		viewHolder.dataL2.setText(peopleList.get(position).get("L2").toString());

		viewHolder.dataR1.setText(peopleList.get(position).get("R1").toString().trim());
		viewHolder.dataR2.setText(peopleList.get(position).get("R2").toString());
		viewHolder.dataR3.setText(peopleList.get(position).get("R3").toString());
		viewHolder.dataR4.setText(peopleList.get(position).get("R4").toString());
		viewHolder.dataR5.setText(peopleList.get(position).get("R5").toString());

		return v;
	}


	public void setArrayList(ArrayList<HashMap<String,String>> arrays){
		this.peopleList = arrays;
	}

	public ArrayList<HashMap<String,String>> getArrayList(){
		return peopleList;
	}


	/*
	 * ViewHolder
	 */
	class ViewHolder{
		ImageView people_image;
		TextView people_name;
		TextView dataL1;
		TextView dataL2;
		TextView dataR1;
		TextView dataR2;
		TextView dataR3;
		TextView dataR4;
		TextView dataR5;

	}


}







