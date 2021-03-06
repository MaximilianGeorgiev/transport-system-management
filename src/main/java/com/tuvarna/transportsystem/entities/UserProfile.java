package com.tuvarna.transportsystem.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "\"UserProfile\"", schema = "\"TransportSystem\"")
public class UserProfile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "userprofile_id")
	private int userProfileId;

	@Column(name = "userprofile_rating")
	private double userProfileRating;

	@Column(name = "userprofile_honorarium")
	private double userProfileHonorarium;

	@OneToOne(mappedBy = "userProfile")
	private User user;

	public UserProfile() {
		this.userProfileRating = 0.0; // prevent exceptions in controllers, default values
		this.userProfileHonorarium = 0.0;
	}

	public UserProfile(double userProfileRating, double userProfileHonorarium) {

		this.userProfileRating = userProfileRating;
		this.userProfileHonorarium = userProfileHonorarium;
	}

	public int getUserProfileId() {
		return userProfileId;
	}

	public void setUserProfileId(int userProfileId) {
		this.userProfileId = userProfileId;
	}

	public double getUserProfileRating() {
		return userProfileRating;
	}

	public void setUserProfileRating(double userProfileRating) {
		this.userProfileRating = userProfileRating;
	}

	public double getUserProfileHonorarium() {
		return userProfileHonorarium;
	}

	public void setUserProfileHonorarium(double userProfileHonorarium) {
		this.userProfileHonorarium = userProfileHonorarium;
	}
}
