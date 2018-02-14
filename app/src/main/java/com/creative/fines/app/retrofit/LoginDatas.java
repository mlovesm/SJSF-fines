package com.creative.fines.app.retrofit;

public class LoginDatas {

	String status;
	String LATEST_APP_VER;
	int result;
	String sabun_no;
	int flag;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getLATEST_APP_VER() {
		return LATEST_APP_VER;
	}

	public void setLATEST_APP_VER(String LATEST_APP_VER) {
		this.LATEST_APP_VER = LATEST_APP_VER;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public String getSabun_no() {
		return sabun_no;
	}

	public void setSabun_no(String sabun_no) {
		this.sabun_no = sabun_no;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	@Override
	public String toString() {
		return "LoginDatas{" +
				"status='" + status + '\'' +
				", LATEST_APP_VER='" + LATEST_APP_VER + '\'' +
				", result=" + result +
				", sabun_no='" + sabun_no + '\'' +
				", flag=" + flag +
				'}';
	}
}
