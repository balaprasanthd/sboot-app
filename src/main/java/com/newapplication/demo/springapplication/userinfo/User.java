package com.newapplication.demo.springapplication.userinfo;

import org.springframework.data.annotation.Id;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;

@Container(containerName = "mycollection")
public class User {
	@Id
	private String id;
	@PartitionKey
	private String name;
	private String address;
	private String dob;
	public User(String id, String name, String address, String dob) {
		this.id = id;
		this.name = name;
		this.address = address;
		this.dob = dob;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getDob() {
		return dob;
	}
	public void setDob(String dob) {
		this.dob = dob;
	}
}
