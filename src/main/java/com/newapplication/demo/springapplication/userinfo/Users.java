package com.newapplication.demo.springapplication.userinfo;

public class Users {
	public static User getFirstUser() {
		User user1 = new User("abc", "abc", "New York", "June 1");
		return user1;
	}
	
	public static User getSecondUser() {
		User user1 = new User("def", "def", "New York", "June 10");
		return user1;
	}
	
	public static User getThirdUser() {
		User user1 = new User("ghi", "ghi", "New York", "June 11");
		return user1;
	}
	
	public static User getFourthUser() {
		User user1 = new User("lmn", "lmn", "New York", "June 12");
		return user1;
	}
}
