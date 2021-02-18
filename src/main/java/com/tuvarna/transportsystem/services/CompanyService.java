package com.tuvarna.transportsystem.services;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.tuvarna.transportsystem.controllers.CompanyLoadStationsController;
import com.tuvarna.transportsystem.controllers.CompanyShowRouteAttachmentsController;
import com.tuvarna.transportsystem.dao.UserDAO;
import com.tuvarna.transportsystem.entities.Location;
import com.tuvarna.transportsystem.entities.Request;
import com.tuvarna.transportsystem.entities.Route;
import com.tuvarna.transportsystem.entities.Ticket;
import com.tuvarna.transportsystem.entities.TransportType;
import com.tuvarna.transportsystem.entities.Trip;
import com.tuvarna.transportsystem.entities.TripType;
import com.tuvarna.transportsystem.entities.User;
import com.tuvarna.transportsystem.interfaces.ICompanyFunctionality;
import com.tuvarna.transportsystem.utils.DatabaseUtils;
import com.tuvarna.transportsystem.utils.NotificationUtils;
import com.tuvarna.transportsystem.utils.ValidationUtils;
import com.tuvarna.transportsystem.utils.ViewResponse;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.stage.Stage;

public class CompanyService implements ICompanyFunctionality {
	private UserService userService;
	private static final Logger logger = LogManager.getLogger(CompanyService.class.getName());

	public CompanyService() {
		this.userService = new UserService();
		PropertyConfigurator.configure("log4j.properties"); // configure log4j
	}

	@Override
	public ViewResponse addTrip(Route routeCreated, String ticketsAvailability, String seatsCapacity, String duration,
			String price, TextField departureDate, TextField arrivalDate, Object maxTicketsPerUser,
			String hourOfDeparture, String departureLocation, String arrivalLocation, Toggle selectedTripType,
			Toggle selectedBusType) throws ParseException {

		ViewResponse response = ValidationUtils.validateCompanyAddTrip(routeCreated, ticketsAvailability, seatsCapacity,
				duration, price, departureDate, arrivalDate, maxTicketsPerUser, hourOfDeparture, departureLocation,
				arrivalLocation, selectedTripType, selectedBusType);

		if (!response.isValid()) {
			return response;
		}

		response.setValid(false);

		String transportType = selectedBusType.toString().substring(selectedBusType.toString().indexOf('\'') + 1,
				selectedBusType.toString().lastIndexOf('\''));
		String tripType = selectedTripType.toString().substring(selectedTripType.toString().indexOf('\'') + 1,
				selectedTripType.toString().lastIndexOf('\''));

		TransportType transportTypeInstance = new TransportTypeService().getByName(transportType).get();
		TripType tripTypeInstance = new TripTypeService().getByName(tripType).get();
		int tripDuration = Integer.parseInt(duration.trim());
		double tripPrice = Double.parseDouble(price.trim());
		int tripTicketAvailability = Integer.parseInt(ticketsAvailability.trim());
		int tripCapacity = Integer.parseInt(seatsCapacity.trim());
		int tripMaxTicketsPerUser = Integer.parseInt(maxTicketsPerUser.toString().trim());

		if (routeCreated == null) {
			logger.info("CONSTRAINT FAILED: No stations added.");

			response.setMessage("Please add stations first.");
			return response;
		}

		String departure = departureDate.getText();
		// DateFormat formatDepartureDate = new SimpleDateFormat("MM/dd/yyyy");

		String dateFormatPattern = new SimpleDateFormat().toLocalizedPattern().split(" ")[0];
		DateFormat formatDepartureDate = new SimpleDateFormat(dateFormatPattern);
		Date dateDeparture = formatDepartureDate.parse(departure);

		// arrival date
		String arrival = arrivalDate.getText();
		// DateFormat formatArrivalDate = new SimpleDateFormat("MM/dd/yyyy");
		DateFormat formatArrivalDate = new SimpleDateFormat(dateFormatPattern);
		Date dateArrival = formatArrivalDate.parse(arrival);
		// SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH).parse(arrival);

		Trip newTrip = new Trip(tripTypeInstance, routeCreated, dateDeparture, dateArrival, tripCapacity,
				transportTypeInstance, tripMaxTicketsPerUser, tripTicketAvailability, tripPrice, tripDuration,
				hourOfDeparture);
		TripService tripService = new TripService();
		tripService.save(newTrip);
		logger.info("Constraint validation passed, persisting new trip to database.");

		/*
		 * In the UserTrip table a new entry will be added with the logged in user
		 * (owner in this case) and the newly created trip
		 */
		new UserService().addTrip(DatabaseUtils.currentUser, newTrip);
		logger.info("Inserting to UsersTrip table (Associating transport company to this trip.");

		/* Distributor gets a notification */
		NotificationUtils.generateNewTripNotification(newTrip);

		response.setValid(true);
		return response;
	}

	@Override
	public Route createGlobalTrip(String departureLocation, String arrivalLocation) {
		ViewResponse constraintCheck = ValidationUtils.validateCompanyAddAttachmentLocationsEndPoint(departureLocation,
				arrivalLocation);

		if (!constraintCheck.isValid()) {
			return null;
		}

		// creating the route
		LocationService locationService = new LocationService();
		Location locationDeparture = locationService.getByName(departureLocation).get();
		Location locationArrival = locationService.getByName(arrivalLocation).get();
		RouteService routeService = new RouteService();
		Route route = new Route(locationDeparture, locationArrival);

		routeService.save(route);

		return route;
	}

	@Override
	public void addTripHandleAttachmentLocations(String departureLocation, String arrivalLocation, Route route) {
		try {
			Stage stage = new Stage();
			FXMLLoader userPanel = new FXMLLoader(getClass().getResource("/views/CompanyLoadStations.fxml"));
			DialogPane root = (DialogPane) userPanel.load();

			// sending the route and stations to other controller
			CompanyLoadStationsController controller = (CompanyLoadStationsController) userPanel.getController();
			controller.getRouteLocations(arrivalLocation, departureLocation, route);

			Scene adminScene = new Scene(root);
			stage.setScene(adminScene);
			stage.setTitle("Transport Company");
			stage.showAndWait();

		} catch (Exception e) {
			System.out.println("Problem");

		}
	}

	@Override
	public ViewResponse addAttachmentLocations(List<String> locations, List<String> hours, Route route) {
		ViewResponse response = ValidationUtils.validateCompanyAddAttachmentLocations(locations, hours, route);

		if (!response.isValid()) {
			return response;
		}

		RouteService routeService = new RouteService();
		LocationService locationService = new LocationService();

		List<String> nonNullLocations = locations.stream().filter(l -> l != null).collect(Collectors.toList());
		List<String> nonNullHours = hours.stream().filter(h -> h != null).collect(Collectors.toList());

		for (int i = 0; i < nonNullLocations.size(); i++) {
			Location location = locationService.getByName(nonNullLocations.get(i)).get();
			routeService.addAttachmentLocation(route, location, nonNullHours.get(i));
		}

		response.setValid(true);
		return response;
	}

	@Override
	public ViewResponse acceptRequest(Request request) {
		ViewResponse response = ValidationUtils.validateCompanyAcceptRequest(request);

		if (!response.isValid()) {
			return response;
		}

		int tickets = request.getTrip().getTripTicketAvailability();
		int requestedTickets = request.getTicketsQuantity();
		int newTickets = tickets + requestedTickets;

		Trip trip = request.getTrip();
		TripService tripService = new TripService();
		tripService.updateTripTicketAvailability(trip, newTickets);
		logger.info("Request accepted: updating tickets availability for the trip.");

		RequestService requestService = new RequestService();
		requestService.updateStatus(request, DatabaseUtils.REQUEST_STATUSACCEPTED);
		logger.info("Request was accepted.");

		return response;
	}

	@Override
	public ViewResponse rejectRequest(Request request) {
		ViewResponse response = ValidationUtils.validateCompanyRejectRequest(request);

		if (!response.isValid()) {
			return response;
		}

		RequestService requestService = new RequestService();
		requestService.updateStatus(request, DatabaseUtils.REQUEST_STATUSREJECTED);
		logger.info("Request status updated: REJECTED");

		return response;
	}

	@Override
	public ViewResponse scheduleShowAttachments(Trip trip) throws IOException {
		ViewResponse response = ValidationUtils.validateCompanyScheduleShowAttachments(trip);

		if (!response.isValid()) {
			return response;
		}

		Stage stage = new Stage();
		FXMLLoader userPanel = new FXMLLoader(getClass().getResource("/views/CompanyShowRouteAttachments.fxml"));
		DialogPane root = (DialogPane) userPanel.load();
		// send trip to other controller
		CompanyShowRouteAttachmentsController controller = (CompanyShowRouteAttachmentsController) userPanel
				.getController();
		controller.getTrip(trip);

		Scene adminScene = new Scene(root);
		stage.setScene(adminScene);
		stage.setTitle("Transport Company");
		stage.showAndWait();

		logger.info("Switched to attachment locations view.");

		return response;
	}

	@Override
	public ViewResponse cancelTrip(Trip trip) {
		ViewResponse response = ValidationUtils.validateCompanyCancelTrip(trip);

		if (!response.isValid()) {
			return response;
		}

		response.setValid(false);

		int tripId = trip.getTripId();

		/*
		 * Couldn't make a working REMOVE cascade no matter what, that's why I need to
		 * manually cascade everything... 1. Remove every ticket from the UsersTicket
		 * table 2. Remove every ticket associated with the trip from Ticket table 3.
		 * Remove the trip from UsersTrip (Company - trip) 4. Remove all requests for
		 * this trip 5. Delete the trip itself
		 */

		TripService tripService = new TripService();
		TicketService ticketService = new TicketService();
		UserService userService = new UserService();
		RequestService requestService = new RequestService();

		User company = userService.getUserByTripId(tripId).get();

		if (!company.equals(DatabaseUtils.currentUser)) {
			logger.info("Logged in user doesn't own trip.");

			response.setMessage("Unable to cancel trip due to security reasons.");
			return response;
		}

		List<Ticket> tickets = ticketService.getByTrip(tripId);
		List<User> users = userService.getAll();

		try {
			users.forEach(u -> {
				List<Ticket> userTickets = u.getTickets();

				userTickets.forEach(t -> {
					if (t.getTrip().getTripId() == tripId) {
						userService.removeTicket(u, t);
					}
				});
			});

			tickets.forEach(t -> {
				if (t.getTrip().getTripId() == tripId) {
					ticketService.deleteById(t.getTicketId());
				}
			});

			tickets.forEach(t -> ticketService.deleteById(t.getTicketId()));
			userService.removeTrip(company, trip);
			requestService.deleteByTripId(tripId);

			logger.info("Trip deletion successfully cascaded");

			/* Inform distributor for the cancellation before it is deleted */
			NotificationUtils.generateCancelledTripNotification(trip);

			tripService.deleteById(tripId);
			logger.info("Trip deleted.");
		} catch (Exception e) {
			logger.error("Unable to delete trip. Most likely cascading failed at some point.");

			response.setMessage("Unable to delete trip. Most likely cascading failed at some point.");
			return response;
		}

		response.setValid(true);
		return response;
	}
}
