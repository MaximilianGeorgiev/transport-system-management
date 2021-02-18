package com.tuvarna.transportsystem.services;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.tuvarna.transportsystem.controllers.DistributorShowRouteAttachmentsController;
import com.tuvarna.transportsystem.dao.UserDAO;
import com.tuvarna.transportsystem.entities.Location;
import com.tuvarna.transportsystem.entities.Request;
import com.tuvarna.transportsystem.entities.Trip;
import com.tuvarna.transportsystem.entities.User;
import com.tuvarna.transportsystem.entities.UserProfile;
import com.tuvarna.transportsystem.interfaces.IDistributorFunctionality;
import com.tuvarna.transportsystem.utils.DatabaseUtils;
import com.tuvarna.transportsystem.utils.ValidationUtils;
import com.tuvarna.transportsystem.utils.ViewResponse;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class DistributorService implements IDistributorFunctionality {
	private UserService userService;
	private static final Logger logger = LogManager.getLogger(DistributorService.class.getName());

	public DistributorService() {
		this.userService = new UserService();
		PropertyConfigurator.configure("log4j.properties"); // configure log4j
	}

	@Override
	public ViewResponse addCashier(String fullname, String location, String company) {
		ViewResponse response = ValidationUtils.validateDistributorAddCashier(fullname, location, company);

		if (!response.isValid()) {
			return response;
		}

		Location userLocation = new LocationService().getByName(location).get();
		User userCompany = userService.getByFullName(company).get(0); // only

		/* Create a unique User Profile associated with this user */
		UserProfileService userProfileService = new UserProfileService();
		UserProfile profile = new UserProfile();
		userProfileService.save(profile);

		String username = DatabaseUtils.generateUserName(fullname);
		String password = DatabaseUtils.generatePassword();

		User cashier = new User(fullname, username, password, profile, DatabaseUtils.USERTYPE_CASHIER, userLocation);

		userService.save(cashier);
		userService.addRole(cashier, DatabaseUtils.ROLE_USER);
		userService.addCashierToTransportCompany(userCompany, cashier);
		logger.info("User successfully created. Assigned role 'user' and inserted into CompanyCashier table.");

		StringBuilder outputString = new StringBuilder();
		outputString.append(" Username: ").append(username).append("\n Password: ").append(password);

		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("User succesfully created.");
		alert.setHeaderText("Please provide login credentials to the user!");
		alert.setContentText(outputString.toString());

		alert.showAndWait();

		return response;
	}

	@Override
	public ViewResponse makeRequest(Trip trip, String requiredTickets) {
		ViewResponse response = ValidationUtils.validateDistributorMakeRequest(trip, requiredTickets);

		if (!response.isValid()) {
			return response;
		}

		RequestService requestService = new RequestService();
		Request request = new Request(Integer.parseInt(requiredTickets), trip, DatabaseUtils.REQUEST_STATUSPENDING);
		requestService.save(request);

		logger.info("Request passed all constraints checks and was successfully created.");
		return response;
	}

	@Override
	public ViewResponse loadAttachmentsInSchedule(Trip trip) throws IOException {
		ViewResponse response = new ViewResponse();
		response.setValid(false);

		if (trip == null) {
			logger.info("CONSTRAINT FAILURE: No trip selected.");

			response.setMessage("Select trip first!");
			return response;
		}

		Stage stage = new Stage();
		FXMLLoader userPanel = new FXMLLoader(getClass().getResource("/views/DistributorShowRouteAttachments.fxml"));
		DialogPane root = (DialogPane) userPanel.load();
		// send trip to other controller
		DistributorShowRouteAttachmentsController controller = (DistributorShowRouteAttachmentsController) userPanel
				.getController();
		controller.getTrip(trip);

		Scene adminScene = new Scene(root);
		stage.setScene(adminScene);
		stage.setTitle("Transport Company");
		stage.showAndWait();

		response.setValid(true);
		return response;
	}

	@Override
	public ViewResponse assignCashier(Trip trip, String location, String cashier) {
		ViewResponse response = ValidationUtils.validateDistributorAssignCashier(trip, location, cashier);
		UserService userService = new UserService();

		if (!response.isValid()) {
			return response;
		}

		response.setValid(false);

		TripService tripService = new TripService();
		RouteService routeService = new RouteService();

		String refactoredCashierName = cashier.split("\\(")[0]; // name comes as username(location)
		User cashierInstance = userService.getByName(refactoredCashierName).get();

		/*
		 * A cashier can only be assigned for a location which exists in the trip route
		 * and is the location the user is registered with
		 */

		List<Location> attachments = routeService.getAttachmentLocationsInRouteById(trip.getRoute().getRouteId());

		boolean tripAttachmentLocationsContainsUserLocation = attachments.contains(cashierInstance.getUserLocation());
		boolean tripEndPointsContainsUserLocation = trip.getRoute().getRouteDepartureLocation().getLocationName()
				.equals(cashierInstance.getUserLocation().getLocationName())
				|| trip.getRoute().getRouteArrivalLocation().getLocationName()
						.equals(cashierInstance.getUserLocation().getLocationName());

		Location locationSelected = new LocationService().getByName(location).get();

		/*
		 * Since the locations are not prepopulated with valid ones only, we check if
		 * the selected location is a valid attachment location or a valid end point
		 */

		boolean selectedLocationIsValidAttachment = attachments.contains(locationSelected);
		boolean selectedLocationIsValidEndPoint = trip.getRoute().getRouteDepartureLocation().getLocationName()
				.equals(locationSelected.getLocationName())
				|| trip.getRoute().getRouteArrivalLocation().getLocationName()
						.equals(locationSelected.getLocationName());

		if (tripAttachmentLocationsContainsUserLocation || tripEndPointsContainsUserLocation) {
			if (!(selectedLocationIsValidAttachment || selectedLocationIsValidEndPoint)) {
				response.setMessage("Selected location is unrelated to this trip.");
				return response;
			}

			if (trip.getCashiers().contains(cashierInstance)) {
				response.setMessage("Selected user is already assigned for this trip.");
				return response;
			}

			// cashier location is in an attachment point but we are trying to assign him to
			// an end point that doesn't match his location
			if (selectedLocationIsValidEndPoint) {
				if (!locationSelected.getLocationName().equals(cashierInstance.getUserLocation().getLocationName())) {
					if (!locationSelected.getLocationName()
							.equals(cashierInstance.getUserLocation().getLocationName())) {
						response.setMessage("Cashier cannot operate in selected location.");
						return response;
					}

				} else if (!locationSelected.getLocationName()
						.equals(cashierInstance.getUserLocation().getLocationName())) {
					if (!locationSelected.getLocationName()
							.equals(cashierInstance.getUserLocation().getLocationName())) {
						response.setMessage("Cashier cannot operate in selected location.");
						return response;
					}
				}
			}

			tripService.addCashierForTrip(trip, cashierInstance);
			logger.info(
					"Cashier is eligible to be a cashier for this trip. Added in TripsCashier table and assigned for the trip.");

			response.setValid(true);
			return response;
		}

		response.setMessage("Failed to assign cashier for selected location.");
		return response;
	}
}
