package com.creative.fines.app.retrofit;

import java.util.ArrayList;
import java.util.HashMap;

public class Datas {
	ArrayList<HashMap<String,String>> datas;
	int count;
	String status;

	public ArrayList<HashMap<String, String>> getList() {
		return datas;
	}

	public void setList(ArrayList<HashMap<String, String>> datas) {
		this.datas = datas;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}


	@Override
	public String toString() {
		return "Datas{" +
				"datas=" + datas +
				", count='" + count + '\'' +
				", status='" + status + '\'' +
				'}';
	}
}
