package com.tuvarna.transportsystem.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.tuvarna.transportsystem.dao.UserDAO;
import com.tuvarna.transportsystem.entities.Location;
import com.tuvarna.transportsystem.entities.User;
import com.tuvarna.transportsystem.entities.UserProfile;
import com.tuvarna.transportsystem.entities.UserType;
import com.tuvarna.transportsystem.interfaces.IAdminFunctionality;
import com.tuvarna.transportsystem.utils.DatabaseUtils;
import com.tuvarna.transportsystem.utils.ValidationUtils;
import com.tuvarna.transportsystem.utils.ViewResponse;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;

public class AdminService implements IAdminFunctionality {
	private UserService userService;
	private static final Logger logger = LogManager.getLogger(AdminService.class.getName());

	public AdminService() {
		this.userService = new UserService();
		PropertyConfigurator.configure("log4j.properties"); // configure log4j
	}

	@Override
	public ViewResponse editHonorarium(String username, String honorarium, String rating) {
		ViewResponse response = ValidationUtils.validateAdminEditHonorarium(username, honorarium, rating);

		if (!response.isValid()) {
			return response; // return error message to view
		}

		response.setValid(false);

		boolean isChanged = false;

		User user = userService.getByName(username).get();
		UserProfile profile = user.getUserProfile();

		boolean ratingNull = rating == null || rating.equals(null) || rating.equals("");
		boolean honorariumNull = honorarium == null || honorarium.equals(null) || honorarium.equals("");

		if (ratingNull) {
			double honorariumValue = Double.parseDouble(honorarium.trim());

			/*
			 * Admin wants to change the horarium only but keep the rating the same. Check
			 * if the values differ
			 */
			if (profile.getUserProfileHonorarium() != honorariumValue) {
				profile.setUserProfileHonorarium(honorariumValue);
				userService.updateUserProfile(user, profile);

				logger.info("Successfully updated honorarium.");

				response.setMessage("Successfully updated honorarium.");
				return response;
			}

			logger.info("CONSTRAINT FAILED: New value matches old value for honorarium.");
			response.setMessage("New value matches old value for honorarium.");
			response.setValid(true);
			return response;
		} else if (honorariumNull) {
			double ratingValue = Double.parseDouble(rating.trim());

			if (profile.getUserProfileRating() != ratingValue) {
				profile.setUserProfileRating(ratingValue);
				userService.updateUserProfile(user, profile);

				logger.info("Successfully updated rating");
				response.setMessage("Successfully updated rating.");
				response.setValid(true);
				return response;
			}

			logger.info("CONSTRAINT FAILED: New value matches old value for rating.");
			response.setMessage("New value matches old value for rating.");
			return response;
		} else if (!(ratingNull) && !(honorariumNull)) {
			/* Both values should be changed */
			double honorariumValue = Double.parseDouble(honorarium.trim());
			double ratingValue = Double.parseDouble(rating.trim());

			boolean updatedHonorarium = false;
			boolean updatedRating = false;

			if (profile.getUserProfileHonorarium() != honorariumValue) {
				profile.setUserProfileHonorarium(honorariumValue);
				updatedHonorarium = true;
			}

			if (profile.getUserProfileRating() != ratingValue) {
				profile.setUserProfileRating(ratingValue);
				updatedRating = true;
			}

			if (updatedHonorarium || updatedRating) {
				userService.updateUserProfile(user, profile);
			}

			if (updatedHonorarium && updatedRating) {
				logger.info("Successfully updated both values.");
				response.setMessage("Successfully updated both values.");
				response.setValid(true);
				return response;
			}

			if (updatedHonorarium) {
				logger.info("Updated honorarium. Rating is the same value as before.");
				response.setMessage("Updated honorarium. Rating is the same value as before.");
				response.setValid(true);
				return response;
			}

			if (updatedRating) {
				logger.info("Updated rating. Honorarium is the same value as before.");
				response.setMessage("Updated rating. Honorarium is the same value as before.");
				response.setValid(true);
				return response;
			}
		}
		response.setMessage("Edit failed.");
		return response;
	}

	@Override
	public ViewResponse deleteUser(User user) {
		ViewResponse response = new ViewResponse();
		response.setValid(false);

		if (user == null) {
			response.setMessage("No user selected for deletion.");
			return response;
		}

		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Confirm deletion");
		alert.setHeaderText("Are you sure you would like to delete this user?");
		alert.setResizable(false);
		alert.setContentText("Please beaware that this user will be permanently deleted.");

		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == ButtonType.OK) {
			UserService userService = new UserService();

			if (DatabaseUtils.cascadeUserDeletion(user)) {
				logger.info("User successfully deleted.");
				response.setMessage("User successfully deleted.");
				response.setValid(true);
				return response;
			} else {
				response.setMessage("An error has occured while deleting user.");
				return response;
			}
		}
		response.setMessage("No actions taken.");
		return response; // user didnt select ok
	}

	@Override
	public List<User> search(String keyword, String searchCriteria) {
		List<User> returnList = new ArrayList<>();

		if (searchCriteria.equals("Search by user name")) {
			returnList.add(userService.getByName(keyword).get());
		} else if (searchCriteria.equals("Search by full name")) {
			returnList.addAll(userService.getByFullName(keyword));
		} else if (searchCriteria.equals("Search by location")) {
			returnList.addAll(userService.getByUserLocation(keyword));
		} else if (searchCriteria.equals("Search by user type")) {
			returnList.addAll(userService.getByUserType(keyword));
		}

		return returnList;
	}

	@Override
	public ViewResponse addUser(String fullname, boolean companySelected, boolean distributorSelected,
			String userLocation) {
		// error occured with the constraint validation, show in FXML
		ViewResponse response = ValidationUtils.validateAdminAddUser(fullname, companySelected, distributorSelected,
				userLocation);
		if (!response.isValid()) {
			return response;
		}

		response.setValid(false);

		// it is success, continue with the user creation
		LocationService locationService = new LocationService();

		if (!locationService.getByName(userLocation).isPresent()) {
			logger.error("Location not present in database.");
			response.setMessage("Location not present in database.");
			return response;
		}

		Location location = (Location) new LocationService().getByName(userLocation).get();

		/* Create a unique User Profile associated with this user */
		UserProfileService userProfileService = new UserProfileService();
		UserProfile profile = new UserProfile();
		userProfileService.save(profile);

		String username = DatabaseUtils.generateUserName(fullname);
		String password = DatabaseUtils.generatePassword();

		UserType userType = null;

		if (companySelected) {
			userType = DatabaseUtils.USERTYPE_COMPANY;
		} else if (distributorSelected) {
			userType = DatabaseUtils.USERTYPE_DISTRIBUTOR;
		}

		User user = new User(fullname, username, password, profile, userType, location);

		userService.save(user);
		userService.addRole(user, DatabaseUtils.ROLE_USER);

		StringBuilder outputString = new StringBuilder();
		outputString.append(" Username: ").append(username).append("\n Password: ").append(password);

		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("User succesfully created.");
		alert.setHeaderText("Please provide login credentials to the user!");
		alert.setContentText(outputString.toString());

		alert.showAndWait();
		logger.info("Successfully created user and persisted to database.");

		response.setValid(true);
		return response;
	}

}
