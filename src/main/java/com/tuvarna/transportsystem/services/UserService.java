package com.tuvarna.transportsystem.services;

import java.util.List;
import java.util.Optional;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.mindrot.jbcrypt.BCrypt;

import com.tuvarna.transportsystem.dao.UserDAO;
import com.tuvarna.transportsystem.entities.Location;
import com.tuvarna.transportsystem.entities.Role;
import com.tuvarna.transportsystem.entities.Ticket;
import com.tuvarna.transportsystem.entities.Trip;
import com.tuvarna.transportsystem.entities.User;
import com.tuvarna.transportsystem.entities.UserProfile;
import com.tuvarna.transportsystem.entities.UserType;
import com.tuvarna.transportsystem.interfaces.ICrudOperations;
import com.tuvarna.transportsystem.interfaces.IUserFunctionality;
import com.tuvarna.transportsystem.utils.DatabaseUtils;
import com.tuvarna.transportsystem.utils.ValidationUtils;
import com.tuvarna.transportsystem.utils.ViewResponse;

public class UserService implements ICrudOperations<User>, IUserFunctionality {
	private UserDAO userDAO;
	private static final Logger logger = LogManager.getLogger(UserService.class.getName());

	public UserService() {
		this.userDAO = new UserDAO();
		PropertyConfigurator.configure("log4j.properties"); // configure log4j
	}

	// if it redirects to login then there is a login error
	public boolean userLoginInputIncorrect(String output) {
		if (output.equals("/views/Login.fxml")) {
			return true;
		}
		return false;
	}

	// return view to FXML controller
	public String redirectLoginView(User user) {
		String userType = user.getUserType().getUserTypeName();

		if (userType.equals("Admin")) {
			return "/views/AdminPanel.fxml";
		} else if (userType.equals("User")) {
			return "/views/UserPanel.fxml";
		} else if (userType.equals("Transport Company")) {
			return "/views/CompanyAddTripPanel.fxml";
		} else if (userType.equals("Distributor")) {
			return "/views/DistributorAddPanel.fxml";
		} else if (userType.equals("Cashier")) {
			return "/views/CashierSchedulePanel.fxml";
		}

		// unknown type somehow
		return "/views/Login.fxml";
	}

	public boolean validateLogin(String username, String password) {
		if (username != null && username.trim().length() != 0 && password != null && password.length() != 0) {

			if (!this.getByName(username).isPresent()) {
				return false;
			}

			User userFound = this.getByName(username).get();

			// login success
			if (userFound.getUserLoginName().equals(username)
					&& BCrypt.checkpw(password, userFound.getUserPassword())) {
				DatabaseUtils.currentUser = userFound;
				return true;
			}
			return false;
		}

		return false;
	}

	public Optional<User> getUserByTripId(int tripId) {
		return userDAO.getUserByTripId(tripId);
	}

	/* Functionality for joined tables */
	public void addRole(User user, Role role) {
		userDAO.addRole(user, role);
	}

	public void addTrip(User user, Trip trip) {
		userDAO.addTrip(user, trip);
	}

	public void addTicket(User user, Ticket ticket) {
		userDAO.addTicket(user, ticket);
	}

	public void addCashierToTransportCompany(User company, User cashier) {
		userDAO.addCashierToTransportCompany(company, cashier);
	}

	public void removeRole(User user, Role role) {
		userDAO.removeRole(user, role);
	}

	public void removeTrip(User user, Trip trip) {
		userDAO.removeTrip(user, trip);
	}

	public void removeTicket(User user, Ticket ticket) {
		userDAO.removeTicket(user, ticket);
	}

	public void removeCashierFromTransportCompany(User company, User cashier) {
		userDAO.removeCashierFromCompany(company, cashier);
	}

	public void updateLocation(User user, Location location) {
		userDAO.updateLocation(user, location);
	}

	public void updateUserProfile(User user, UserProfile profile) {
		userDAO.updateUserProfile(user, profile);
	}

	public List<User> getByFullName(String name) {
		return userDAO.getByFullName(name);
	}

	public List<User> getByUserType(String type) {
		return userDAO.getByUserType(type);
	}

	public List<User> getByUserProfileId(int profileId) {
		return userDAO.getByUserProfileId(profileId);
	}

	public List<User> getByUserLocation(String location) {
		return userDAO.getByUserLocation(location);
	}

	@Override
	public Optional<User> getById(int id) {
		return userDAO.getById(id);
	}

	@Override
	public Optional<User> getByName(String name) {
		return userDAO.getByName(name);
	}

	@Override
	public List<User> getAll() {
		return userDAO.getAll();
	}

	@Override
	public void save(User user) {
		userDAO.save(user);
	}

	@Override
	public void updateName(User user, String newValue) {
		userDAO.updateName(user, newValue);
	}

	@Override
	public void deleteById(int id) {
		userDAO.deleteById(id);
	}

	@Override
	public void deleteByName(String name) {
		userDAO.deleteByName(name);
	}

	@Deprecated
	@Override
	public void update(User user, String[] newValues) {
	}

	@Override
	public ViewResponse register(String fullname, String username, String password, String userLocation) {
		ViewResponse validation = ValidationUtils.validateCustomerRegistration(fullname, username, password,
				userLocation);
		ViewResponse view = new ViewResponse();

		if (validation.isValid()) {
			view.setValid(false);
			LocationService locationService = new LocationService();
			Location location = locationService.getByName(userLocation).get();

			UserProfile profile = new UserProfile(0.0, 0.0);
			new UserProfileService().save(profile);
			UserType type = DatabaseUtils.USERTYPE_USER;
			UserService userService = new UserService();
			User user = new User(fullname, username, password, profile, type, location);
			userService.save(user);
			logger.info("Successfully persisted user to database.");

			userService.addRole(user, DatabaseUtils.ROLE_USER);
			logger.info("Assigned role user.");

			// registration success and return logged in view
			DatabaseUtils.currentUser = user;
			validation.setValid(true);
		}

		return validation; // returns error message to registration view and set information label
	}

	@Override
	public ViewResponse login(String username, String password) {
		if (validateLogin(username, password)) {
			return new ViewResponse(true,
					redirectLoginView(DatabaseUtils.currentUser)); /*
																	 * login success, load corresponding view
																	 */
		}

		return new ViewResponse(false, "/views/Login.fxml"); // unsuccessful login, redirect to login page
	}
}
