package com.tuvarna.transportsystem.utils;

/* this class sends the results of opertaions back to the view so they can be displayed to the user. It can display errors 
 * through constraint checks or inform the customer for a success.
*/

public class ViewResponse {
	private boolean isValid;
	private String message;

	public ViewResponse() {

	}

	public ViewResponse(boolean isValid, String message) {
		this.isValid = isValid;
		this.message = message;
	}

	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
