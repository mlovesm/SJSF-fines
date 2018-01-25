package com.creative.fines.app.bean;

import java.io.Serializable;

public class BoardClass implements Serializable{
	private String board_id;
	private String board_title;
	private String board_name;
	private String board_date;

	public BoardClass(String board_id, String board_title, String board_name, String board_date) {
		this.board_id = board_id;
		this.board_title = board_title;
		this.board_name = board_name;
		this.board_date = board_date;
	}

	public BoardClass(String board_title, String board_name, String board_date) {
		this.board_title = board_title;
		this.board_name = board_name;
		this.board_date = board_date;
	}

	public String getBoard_id() {
		return board_id;
	}

	public void setBoard_id(String board_id) {
		this.board_id = board_id;
	}

	public String getBoard_title() {
		return board_title;
	}

	public void setBoard_title(String board_title) {
		this.board_title = board_title;
	}

	public String getBoard_name() {
		return board_name;
	}

	public void setBoard_name(String board_name) {
		this.board_name = board_name;
	}

	public String getBoard_date() {
		return board_date;
	}

	public void setBoard_date(String board_date) {
		this.board_date = board_date;
	}

	@Override
	public String toString() {
		return "BoardClass{" +
				"board_id=" + board_id +
				", board_title='" + board_title + '\'' +
				", board_name='" + board_name + '\'' +
				", board_date='" + board_date + '\'' +
				'}';
	}
}
