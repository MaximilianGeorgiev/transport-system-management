package com.tuvarna.transportsystem.interfaces;

import java.util.List;

import com.tuvarna.transportsystem.entities.User;
import com.tuvarna.transportsystem.utils.ViewResponse;

public interface IAdminFunctionality {

	public ViewResponse editHonorarium(String username, String honorarium, String rating);

	public ViewResponse deleteUser(User user);

	public List<User> search(String keyword, String searchCriteria);

	public ViewResponse addUser(String fullname, boolean companySelected, boolean distributorSelected,
			String userLocation);
	
}
