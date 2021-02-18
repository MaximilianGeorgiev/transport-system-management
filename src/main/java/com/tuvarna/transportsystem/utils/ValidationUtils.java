package com.tuvarna.transportsystem.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.tuvarna.transportsystem.dao.UserDAO;
import com.tuvarna.transportsystem.entities.Location;
import com.tuvarna.transportsystem.entities.Request;
import com.tuvarna.transportsystem.entities.Route;
import com.tuvarna.transportsystem.entities.Trip;
import com.tuvarna.transportsystem.entities.UserType;
import com.tuvarna.transportsystem.services.LocationService;
import com.tuvarna.transportsystem.services.RouteService;
import com.tuvarna.transportsystem.services.UserService;

import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;

public class ValidationUtils {
	private static final Logger logger = LogManager.getLogger(ValidationUtils.class.getName());

	public static ViewResponse validateAdminEditHonorarium(String username, String honorarium, String rating) {
		ViewResponse response = new ViewResponse();
		UserService userService = new UserService();

		if (username.equals(null) || username == null || username.equals("")) {
			logger.info("CONSTRAINT FAILED: No username entered.");
			response.setValid(false);
			response.setMessage("No username entered.");

			return response;
		}

		if (!userService.getByName(username).isPresent()) {
			logger.info("CONSTRAINT FAILED: User not found in database.");
			response.setValid(false);
			response.setMessage("User not found in database.");

			return response;
		}

		boolean honorariumNull = false;
		boolean ratingNull = false;

		if (honorarium.equals(null) || honorarium == null || honorarium.equals("")) {
			honorariumNull = true;

			if (rating.equals(null) || rating == null || rating.equals("")) {
				logger.info("No changes to be made. Both values are null.");
				response.setValid(false);
				response.setMessage("No changes to be made."); // both null

				return response;
			}
		}

		if (rating.equals(null) || rating == null || rating.equals("")) {
			ratingNull = true;
		}

		if (!honorariumNull) {
			try {
				double tryParse = Double.parseDouble(honorarium);
			} catch (Exception e) {
				logger.info("CONSTRAINT FAILED: Invalid value for honorarium.");
				response.setValid(false);
				response.setMessage("Invalid value for honorarium.");

				return response;
			}
		}

		if (!ratingNull) {
			try {
				double tryParse = Double.parseDouble(rating);
			} catch (Exception e) {
				logger.info("CONSTRAINT FAILED: Invalid value for rating.");
				response.setValid(false);
				response.setMessage("Invalid value for rating.");

				return response;
			}
		}

		response.setValid(true);
		response.setMessage("Successfully updated user profile.");
		return response;
	}

	public static ViewResponse validateAdminSearch(String keyword, String searchCriteria) {
		ViewResponse response = new ViewResponse();
		UserDAO userDAO = new UserDAO();
		response.setValid(false);

		if (keyword == null || keyword.equals(null) || keyword.equals("")) {
			logger.info("CONSTRAINT FAILED: No keyword entered.");

			response.setMessage("Please enter keyword to search.");
			return response;
		}

		if (searchCriteria == null || searchCriteria.equals(null) || searchCriteria.equals("")) {
			logger.info("CONSTRAINT FAILED: No search criteria specified.");

			response.setMessage("Please select search criteria.");
			return response;
		}

		if (searchCriteria.equals("Search by user name")) {
			if (!userDAO.getByName(keyword).isPresent()) {
				logger.info("CONSTRAINT FAILED: User not found in database.");

				response.setMessage("User not found in database.");
				return response;
			}
		} else if (searchCriteria.equals("Search by full name")) {
			if (userDAO.getByFullName(keyword).isEmpty()) {
				response.setMessage("No users found.");
				return response;
			}
		} else if (searchCriteria.equals("Search by location")) {
			if (userDAO.getByUserLocation(keyword).isEmpty()) {
				response.setMessage("No users found.");
				return response;
			}
		} else if (searchCriteria.equals("Search by user type")) {
			if (userDAO.getByUserType(keyword).isEmpty()) {
				response.setMessage("No users found.");
				return response;
			}
		}

		response.setValid(true);
		response.setMessage(searchCriteria);
		return response;
	}

	public static ViewResponse validateAdminAddUser(String fullname, boolean companySelected,
			boolean distributorSelected, String location) {
		ViewResponse response = new ViewResponse();
		response.setValid(false);

		if (fullname.equals(null) || fullname == null || fullname.equals("")) {
			logger.info("CONSTRAINT FAILED: No fullname.");
			response.setMessage("Please provide a fullname for the user you wish to create.");
			return response;
		}

		UserType userType = null;

		if ((!companySelected) && (!distributorSelected)) {
			logger.info("CONSTRAINT FAILED: No usertype selected.");
			response.setMessage("Please specify the user type.");
			return response;
		}

		if (location == null) {
			logger.info("CONSTRAINT FAILED: No location selected.");
			response.setMessage("Please select a location.");
			return response;
		}

		response.setValid(true);
		return response;
	}

	public static ViewResponse validateCustomerRegistration(String fullname, String username, String password,
			String location) {
		ViewResponse response = new ViewResponse();
		response.setValid(false);

		if (fullname.trim().length() > 40 || fullname.trim().length() < 5) {
			logger.info("CONSTRAINT FAILED: Invalid fullname.");

			response.setMessage("Invalid fullname. Name must be between 5 and 40 characters.");
			return response;
		}

		if ((!Pattern.matches("^\\w+$", username)) || username.length() < 4 || username.length() > 20) {
			logger.info("CONSTRAINT FAILED: Invalid username.");

			response.setMessage("Invalid username. No spaces, special characters. Length: 4 - 20 characters.");
			return response;
		}

		if (password.length() < 5 || password.length() > 20) {
			logger.info("CONSTRAINT FAILED: Invalid password.");

			response.setMessage("Invalid password. Length: 5 - 20 characters.");
			return response;
		}

		if (location == null) {
			logger.info("CONSTRAINT FAILED: No location selected.");

			response.setMessage("No location selected.");
			return response;
		}

		LocationService locationService = new LocationService();

		if (!locationService.getByName(location).isPresent()) {
			logger.error("Location not present in database.");

			response.setMessage("Location not present in database.");
			return response;
		}

		response.setValid(true);
		return response;
	}

	public static ViewResponse validateCashierSellTicket(Trip trip, String departureLocation, String arrivalLocation,
			String quantity, boolean customerIsGuest, boolean customerIsRegistered, String usernameSelected,
			String guestUserfullName) {

		ViewResponse response = new ViewResponse();
		response.setValid(false);

		UserService userService = new UserService();

		if (trip == null) {
			logger.info("CONSTRAINT FAILED: No trip selected.");

			response.setMessage("Please select a trip.");
			return response;
		}

		if (departureLocation == null || departureLocation.equals(null) || departureLocation.equals("")) {
			logger.info("CONSTRAINT FAILED: Departure location not selected.");

			response.setMessage("Please select a departure location.");
			return response;
		}

		if (arrivalLocation == null || arrivalLocation.equals(null) || arrivalLocation.equals("")) {
			logger.info("CONSTRAINT FAILED: Arrival location not selected.");

			response.setMessage("Please select an arrival location.");
			return response;
		}

		if (quantity == null || quantity.equals(null) || quantity.equals("")) {
			logger.info("CONSTRAINT FAILED: No ticket quantity selected.");

			response.setMessage("Please select ticket quantity.");
			return response;
		}

		if (!(customerIsGuest) && !(customerIsRegistered)) {
			logger.info("CONSTRAINT FAILED: Customer type not specified.");

			response.setMessage("Please select if the customer is a guest or is registered.");
			return response;
		}

		if (customerIsRegistered) {
			if (usernameSelected == null || usernameSelected.equals(null) || usernameSelected.equals("")) {
				logger.info("CONSTRAINT FAILED: No customer selected.");

				response.setMessage("Please select a customer from the dropdown menu.");
				return response;
			}

			if (!userService.getByName(usernameSelected).isPresent()) {
				logger.info("CONSTRAINT FAILED: User not found in database.");

				response.setMessage("User not found in database.");
				return response;
			}
		} else if (customerIsGuest) {
			if (guestUserfullName == null || guestUserfullName.equals(null) || guestUserfullName.equals("")) {

				response.setMessage("Please enter the name of the customer.");
				return response;
			}
		}

		response.setValid(true);
		return response;
	}

	public static ViewResponse validateCompanyAddTrip(Route routeCreated, String ticketsAvailability,
			String seatsCapacity, String duration, String price, TextField departureDate, TextField arrivalDate,
			Object maxTicketsPerUser, String hourOfDeparture, String departureLocation, String arrivalLocation,
			Toggle selectedTripType, Toggle selectedBusType) throws java.text.ParseException {
		Pattern pattern = Pattern.compile("^\\d+$");
		ViewResponse response = new ViewResponse();
		response.setValid(false);

		/*
		 * Validate the text fields. In this case the pattern should be only a full
		 * number between 0-int.maxvalue
		 */
		if (!pattern.matcher(ticketsAvailability.trim()).matches()) {
			logger.info("CONSTRAINT FAILED: Invalid quantity.");

			response.setMessage("Invalid quantity.");
			return response;
		}

		if (maxTicketsPerUser == null) {
			logger.info("CONSTRAINT FAILED: Invalid quantity.");

			response.setMessage("Invalid quantity.");
			return response;
		}

		if (!pattern.matcher(maxTicketsPerUser.toString().trim()).matches()) {
			logger.info("CONSTRAINT FAILED: Invalid quantity.");

			response.setMessage("Invalid quantity.");
			return response;
		}

		if (!pattern.matcher(seatsCapacity.trim()).matches()) {
			logger.info("CONSTRAINT FAILED: Invalid seats capacity.");

			response.setMessage("Invalid seats capacity.");
			return response;
		}

		if (!pattern.matcher(duration.trim()).matches()) {
			logger.info("CONSTRAINT FAILED: Invalid duration.");

			response.setMessage("Invalid duration!");
			return response;
		}

		try {
			double tryParse = Double.parseDouble(price);
		} catch (Exception e) {
			logger.info("CONSTRAINT FAILED: Invalid price.");

			response.setMessage("Invalid price.");
			return response;
		}

		if (hourOfDeparture == null || hourOfDeparture.equals(null) || hourOfDeparture.equals("")) {
			logger.info("CONSTRAINT FAILED: Invalid hour of departure.");

			response.setMessage("Invalid hour of departure.");
			return response;
		}

		String departure = departureDate.getText();

		if (departure.equals(null) || departure.equals("") || departure == null) {
			logger.info("CONSTRAINT FAILED: Invalid departure date.");

			response.setMessage("Invalid departure date.");
			return response;
		}

		// DateFormat formatDepartureDate = new SimpleDateFormat("MM/dd/yyyy");

		String dateFormatPattern = new SimpleDateFormat().toLocalizedPattern().split(" ")[0];
		DateFormat formatDepartureDate = new SimpleDateFormat(dateFormatPattern);
		Date dateDeparture = formatDepartureDate.parse(departure);

		// arrival date
		String arrival = arrivalDate.getText();

		if (arrival.equals(null) || arrival.equals("") || arrival == null) {
			logger.info("CONSTRAINT FAILED: Invalid arrival date.");

			response.setMessage("Invalid arrival date.");
			return response;
		}
		// DateFormat formatArrivalDate = new SimpleDateFormat("MM/dd/yyyy");
		DateFormat formatArrivalDate = new SimpleDateFormat(dateFormatPattern);
		Date dateArrival = formatArrivalDate.parse(arrival);
		// SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH).parse(arrival);

		/* Date validation */
		if (dateDeparture.after(dateArrival) || dateDeparture.before(new Date(System.currentTimeMillis()))
				|| dateArrival.before(new Date(System.currentTimeMillis()))) {
			logger.info("CONSTRAINT FAILED: Invalid interval.");

			response.setMessage("Invalid interval!");
			return response;
		}

		if (departureLocation == null || departureLocation.equals(null) || departureLocation.equals("")) {
			logger.info("CONSTRAINT FAILED: Invalid departure location.");

			response.setMessage("Invalid departure location!");
			return response;
		}

		if (arrivalLocation == null || arrivalLocation.equals(null) || arrivalLocation.equals("")) {
			logger.info("CONSTRAINT FAILED: Invalid arrival location.");

			response.setMessage("Invalid arrival location!");
			return response;
		}

		if (departureLocation.equals(arrivalLocation)) {
			logger.info("CONSTRAINT FAILED: Locations match.");

			response.setMessage("Departure and arrival locations cannot match.");
			return response;
		}

		if (selectedBusType == null) {
			logger.info("CONSTRAINT FAILED: Bus type not specified.");

			response.setMessage("Please select bus type (Regular or Big bus)!");
			return response;
		}

		if (selectedTripType == null) {
			logger.info("CONSTRAINT FAILED: Trip type not specified.");

			response.setMessage("Please select trip type (Normal or Express)!");
			return response;
		}

		response.setValid(true);
		return response;
	}

	public static ViewResponse validateCompanyAddAttachmentLocationsEndPoint(String departureLocation,
			String arrivalLocation) {
		ViewResponse response = new ViewResponse();
		response.setValid(false);

		if (departureLocation == null || departureLocation.equals(null) || departureLocation.equals("")) {
			logger.info("CONSTRAINT FAILED: No departure station selected.");

			response.setMessage("Please select departure station!");
			return response;
		}

		if (arrivalLocation == null || arrivalLocation.equals(null) || arrivalLocation.equals("")) {
			logger.info("CONSTRAINT FAILED: No arrival station selected.");

			response.setMessage("Please select arrival station!");
			return response;
		}

		response.setValid(true);
		return response;
	}

	public static ViewResponse validateCompanyAddAttachmentLocations(List<String> locations, List<String> hours,
			Route route) {
		RouteService routeService = new RouteService();
		ViewResponse response = new ViewResponse();

		response.setValid(false);

		// JavaFX sends all choice boxes values (they can be null) and we have to filter
		// and check if they are nulls
		if (locations.stream().filter(l -> l != null).collect(Collectors.toList()).size() == 0) {
			logger.info("No locations were selected.");

			response.setMessage("No locations were selected.");
			return response;
		}

		if (hours.stream().filter(l -> l != null).collect(Collectors.toList()).size() == 0) {
			logger.info("No times were selected.");

			response.setMessage("No times were selected.");
			return response;
		}

		List<String> nonNullLocations = locations.stream().filter(l -> l != null).collect(Collectors.toList());
		List<String> nonNullHours = hours.stream().filter(h -> h != null).collect(Collectors.toList());

		if (locations.size() != hours.size()) {

			response.setMessage("Constraint mismatch.");
			return response;
		}

		if (locations.size() == 0) {
			logger.info("No locations have been added. Cannot proceed.");

			response.setMessage("No locations have been added. Cannot proceed.");
			return response;
		}

		int previousHour = 0;
		for (int i = 0; i < nonNullLocations.size(); i++) {
			String currentLocation = nonNullLocations.get(i);
			LocationService locationService = new LocationService();
			Location locationStation = locationService.getByName(currentLocation).get();
			String time = nonNullHours.get(i);

			if (i == 0) {
				previousHour = Integer.parseInt(time.split(":")[0]);
			}

			if (locationStation.getLocationName().equals(route.getRouteDepartureLocation().getLocationName())
					|| locationStation.getLocationName().equals(route.getRouteArrivalLocation().getLocationName())) {
				response.setMessage("Station " + i + " matches with arrival or departure location.");
				return response;
			}

			// following stations' times must be bigger than the first
			if (i != 0) {
				int currentHour = Integer.parseInt(hours.get(i).split(":")[0]);

				if (currentHour <= previousHour) {
					logger.info("Following arrival time must be larger than the previous one.");

					response.setMessage("Following arrival time must be larger than the previous one.");
					return response;
				}
				previousHour = currentHour;

			}

			if (routeService.getAttachmentLocationsInRouteById(route.getRouteId()).contains(locationStation)) {
				logger.info("Station already exists as an attachment location.");

				response.setMessage("Station already exists as an attachment location.");
				return response;
			}
		}

		response.setValid(true);
		return response;
	}

	public static ViewResponse validateCompanyAcceptRequest(Request request) {
		ViewResponse response = new ViewResponse();
		response.setValid(false);

		if (request == null) {
			logger.info("CONSTRAINT FAILURE: no request selected");

			response.setMessage("Please select request from table!");
			return response;
		}

		if (request.getStatus().equals(DatabaseUtils.REQUEST_STATUSREJECTED)) {
			logger.info("Cannot accept a previously rejected request.");

			response.setMessage("Cannot accept a previously rejected request.");
			return response;
		}

		if (request.getStatus().equals(DatabaseUtils.REQUEST_STATUSACCEPTED)) {
			logger.info("Request already accepted.");

			response.setMessage("Request already accepted.");
			return response;
		}

		response.setValid(true);
		return response;
	}

	public static ViewResponse validateCompanyRejectRequest(Request request) {
		ViewResponse response = new ViewResponse();
		response.setValid(false);

		if (request == null) {
			logger.info("CONSTRAINT FAILURE: No request selected.");

			response.setMessage("Please select request from table!");
			return response;
		}

		if (request.getStatus().equals(DatabaseUtils.REQUEST_STATUSACCEPTED)) {
			logger.info("Cannot accept a previously accepted request.");

			response.setMessage("Cannot accept a previously accepted request.");
			return response;
		}

		if (request.getStatus().equals(DatabaseUtils.REQUEST_STATUSREJECTED)) {
			logger.info("Request already rejected.");

			response.setMessage("Request already rejected.");
			return response;
		}

		response.setValid(true);
		return response;
	}

	public static ViewResponse validateCompanyScheduleShowAttachments(Trip trip) {
		ViewResponse response = new ViewResponse();
		response.setValid(false);

		if (trip == null) {
			logger.info("CONSTRAINT FAILURE: No trip selected.");
			response.setMessage("Select trip first!");
			return response;
		}

		response.setValid(true);
		return response;
	}

	public static ViewResponse validateCompanyCancelTrip(Trip trip) {
		ViewResponse response = new ViewResponse();
		response.setValid(false);

		if (trip == null) {
			logger.info("CONSTRAINT FAILURE: No trip selected.");

			response.setMessage("Please select a trip you would like to cancel.");
			return response;
		}

		if (trip.getTripDepartureDate().compareTo(new Date(System.currentTimeMillis())) <= 0) {
			logger.info("Cannot cancel a live trip or a completed trip.");

			response.setMessage("Cannot cancel a live trip or a completed trip.");
			return response;
		}

		response.setValid(true);
		return response;
	}

	public static ViewResponse validateDistributorAddCashier(String fullname, String location, String company) {
		ViewResponse response = new ViewResponse();
		response.setValid(false);

		if (fullname.length() < 4 || fullname.length() > 30) {
			logger.info("CONSTRAINT FAILURE: Invalid fullname.");

			response.setMessage("Invalid fullname. Length: 4 - 20");
			return response;
		}

		if (location == null || location.equals("") || location.equals(null)) {
			logger.info("CONSTRAINT FAILURE: No location selected.");

			response.setMessage("Please select a location!");
			return response;
		}

		if (company == null || company.equals("") || company.equals(null)) {
			logger.info("CONSTRAINT FAILURE: No company selected.");

			response.setMessage("Please select a company!");
			return response;
		}

		response.setValid(true);
		return response;
	}

	public static ViewResponse validateDistributorMakeRequest(Trip trip, String requiredTickets) {
		ViewResponse response = new ViewResponse();
		response.setValid(false);

		if (trip == null) {
			logger.info("CONSTRAINT FAILURE: No trip selected.");
			response.setMessage("Please select trip!");
			return response;
		}

		if (requiredTickets == null || requiredTickets.equals(null) || requiredTickets.equals("")) {
			logger.info("CONSTRAINT FAILURE: No tickets entered.");

			response.setMessage("Please enter required tickets!");
			return response;
		}

		try {
			int test = Integer.parseInt(requiredTickets);
		} catch (Exception e) {
			logger.info("CONSTRAINT FAILURE: Quantity NaN.");

			response.setMessage("Quantity must be a number!");
			return response;
		}

		int calCapacity = trip.getTripCapacity();
		int calTickets = trip.getTripTicketAvailability();
		int filCal = calCapacity - calTickets;

		if (Integer.parseInt(requiredTickets) > filCal) {
			logger.info("Not ENOUGH seats in the bus! Enter less tickets quantity!");

			response.setMessage("Not ENOUGH seats in the bus! Enter less tickets quantity!");
			return response;
		}

		response.setValid(true);
		return response;
	}

	public static ViewResponse validateDistributorAssignCashier(Trip trip, String location, String cashier) {
		ViewResponse response = new ViewResponse();
		response.setValid(false);

		if (trip == null) {
			logger.info("CONSTRAINT FAILURE: No trip selected.");

			response.setMessage("Please select trip!");
			return response;
		}

		if (location == null || location.equals("") || location.equals(null)) {
			logger.info("CONSTRAINT FAILURE: No location selected.");

			response.setMessage("Please select a location!");
			return response;
		}

		if (cashier == null || cashier.equals("") || cashier.equals(null)) {
			logger.info("CONSTRAINT FAILURE: No cashier selected.");

			response.setMessage("Please select a cashier!");
			return response;
		}

		response.setValid(true);
		return response;
	}

	public static ViewResponse validateUserPanelGetMatchingTrips(String departureLoc, String arrivalLoc, TextField date,
			String quantity, String time) {
		ViewResponse response = new ViewResponse();
		response.setValid(false);

		if (departureLoc == null || departureLoc.equals(null) || departureLoc.equals("")) {
			logger.info("CONSTRAINT FAILURE: No departure station selected.");

			response.setMessage("Please select departure station.");
			return response;
		}

		if (arrivalLoc == null || arrivalLoc.equals(null) || arrivalLoc.equals("")) {
			logger.info("CONSTRAINT FAILURE: No arrival station selected.");

			response.setMessage("Please select arrival station.");
			return response;
		}

		// text field right now
		if (date.getText() == null || date.getText().equals(null) || date.getText().equals("")) {
			logger.info("CONSTRAINT FAILURE: No date selected.");

			response.setMessage("Please select a date.");
			return response;
		}

		if (quantity == null || quantity.equals(null) || quantity.equals("")) {
			logger.info("CONSTRAINT FAILURE: No tickets quantity selected.");

			response.setMessage("Please select ticket quantity.");
			return response;
		}

		if (time == null || time.equals(null) || time.equals("")) {
			logger.info("CONSTRAINT FAILURE: No time selected.");

			response.setMessage("Please add time.");
			return response;
		}

		response.setValid(true);
		return response;
	}
}
