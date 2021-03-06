package com.tuvarna.transportsystem.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "\"UserType\"", schema = "\"TransportSystem\"")
public class UserType {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "usertype_id")
	private int userTypeId;

	@Column(name = "usertype_name")
	private String userTypeName;

	@OneToMany(mappedBy = "userType") /* No cascade: In the db you cant delete a usertype if it is mapped to a user */
	private List<User> users;

	public UserType() {

	}

	public UserType(String userTypeName) {
		this.userTypeName = userTypeName;
	}

	public int getUserTypeId() {
		return userTypeId;
	}

	public void setUserTypeId(int userTypeId) {
		this.userTypeId = userTypeId;
	}

	public String getUserTypeName() {
		return userTypeName;
	}

	public void setUserTypeName(String userTypeName) {
		this.userTypeName = userTypeName;

	}
}