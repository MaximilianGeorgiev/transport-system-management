package com.tuvarna.transportsystem.interfaces;

import com.tuvarna.transportsystem.utils.ViewResponse;

public interface IUserFunctionality {

	public ViewResponse register(String fullname, String username, String password, String userLocation);

	public ViewResponse login(String username, String password);

}
