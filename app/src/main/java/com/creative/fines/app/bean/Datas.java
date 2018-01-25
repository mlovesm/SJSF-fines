package com.creative.fines.app.bean;

import java.util.ArrayList;
import java.util.HashMap;

public class Datas {
	ArrayList<HashMap<String,Object>> list;
	String count;
	String status;

	public ArrayList<HashMap<String, Object>> getList() {
		return list;
	}

	public void setList(ArrayList<HashMap<String, Object>> list) {
		this.list = list;
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
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
				"list=" + list +
				", count='" + count + '\'' +
				", status='" + status + '\'' +
				'}';
	}
}
