package com.tuvarna.transportsystem.services;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.tuvarna.transportsystem.dao.UserDAO;
import com.tuvarna.transportsystem.entities.Location;
import com.tuvarna.transportsystem.entities.Ticket;
import com.tuvarna.transportsystem.entities.Trip;
import com.tuvarna.transportsystem.entities.User;
import com.tuvarna.transportsystem.entities.UserProfile;
import com.tuvarna.transportsystem.interfaces.ICashierFunctionality;
import com.tuvarna.transportsystem.utils.DatabaseUtils;
import com.tuvarna.transportsystem.utils.ValidationUtils;
import com.tuvarna.transportsystem.utils.ViewResponse;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CashierService implements ICashierFunctionality {
	private UserService userService;
	private static final Logger logger = LogManager.getLogger(CashierService.class.getName());

	public CashierService() {
		this.userService = new UserService();
		PropertyConfigurator.configure("log4j.properties"); // configure log4j
	}

	@Override
	public ObservableList<Trip> getScheduleForCashier(User cashier) {
		ObservableList<Trip> tripList = FXCollections.observableArrayList();
		TripService tripService = new TripService();

		/*
		 * Cashier accesses trips if they are the cashier assigned for it (exists in
		 * TripCashier table)
		 */
		List<Trip> eList = tripService.getAll();
		for (Trip ent : eList) {
			if (ent.getCashiers().contains(cashier)) {
				tripList.add(ent);
			}
		}
		return tripList;
	}

	@Override
	public ObservableList<String> getDepLocationsForCashierBySelectedRow(User cashier, Trip trip) {
		ObservableList<String> departureLocations = FXCollections.observableArrayList();

		if (trip.equals(null)) {
			departureLocations.add("No route selected.");
			return departureLocations;
		}

		int routeId = trip.getRoute().getRouteId();

		RouteService routeService = new RouteService();

		/*
		 * A cashier can sell a ticket departuring from the location they work in.
		 * That's why we filter it. Most likely it will return 1 result
		 */

		/*
		 * DEBUG, correct code is below List<Location> departureLocation =
		 * routeService.getAttachmentLocationsInRouteById(routeId);
		 */
		List<Location> departureLocation = routeService.getAttachmentLocationsInRouteById(routeId).stream()
				.filter(l -> l.getLocationName().equals(cashier.getUserLocation().getLocationName()))
				.collect(Collectors.toList());

		ObservableList<String> locationList = FXCollections.observableArrayList();
		departureLocation.forEach(l -> locationList.add(l.getLocationName()));
		locationList.add(trip.getRoute().getRouteDepartureLocation().getLocationName());

		return locationList;
	}

	@Override
	public ObservableList<String> getArrLocationsForCashierBySelectedRow(User cashier, Trip trip) {
		ObservableList<String> locationList = FXCollections.observableArrayList();

		if (trip.equals(null)) {
			locationList.add("No route selected.");
			return locationList;
		}

		int routeId = trip.getRoute().getRouteId();

		RouteService routeService = new RouteService();

		/*
		 * The arrival location cannot be the same as the current location of the
		 * cashier, so fetch everything else
		 */
		List<Location> departureLocation = routeService.getAttachmentLocationsInRouteById(routeId).stream()
				.filter(l -> !(l.getLocationName().equals(cashier.getUserLocation().getLocationName())))
				.collect(Collectors.toList());

		departureLocation.add(trip.getRoute().getRouteArrivalLocation());
		departureLocation.forEach(l -> locationList.add(l.getLocationName()));

		return locationList;
	}

	@Override
	public ViewResponse sellTicket(Trip trip, String departureLocation, String arrivalLocation, String quantity,
			boolean customerIsGuest, boolean customerIsRegistered, String usernameSelected, String guestUserFullName) {
		ViewResponse response = ValidationUtils.validateCashierSellTicket(trip, departureLocation, arrivalLocation,
				quantity, customerIsGuest, customerIsRegistered, usernameSelected, guestUserFullName);

		if (!response.isValid()) {
			return response; // display constraint failure to user
		}

		User customer = null;
		response.setValid(false);

		if (customerIsRegistered) {
			customer = userService.getByName(usernameSelected).get();
		} else if (customerIsGuest) {

			UserProfileService userProfileService = new UserProfileService();
			UserProfile userProfile = new UserProfile();
			userProfileService.save(userProfile);

			Location userLocation = new LocationService().getByName(departureLocation).get();

			customer = new User(guestUserFullName, DatabaseUtils.generateUserName(guestUserFullName),
					DatabaseUtils.generatePassword(), userProfile, DatabaseUtils.USERTYPE_USER, userLocation);
		}

		int ticketsToPurchase = Integer.parseInt(quantity);

		LocationService locationService = new LocationService();
		Location departureLocationInstance = locationService.getByName(departureLocation).get();
		Location arrivalLocationInstance = locationService.getByName(arrivalLocation).get();

		TripService tripService = new TripService();
		tripService.updateTripTicketAvailability(trip, trip.getTripTicketAvailability() - ticketsToPurchase);
		logger.info("Updated tickets availability for trip.");

		TicketService ticketService = new TicketService();
		Ticket ticket = new Ticket(new Date(System.currentTimeMillis()), trip, departureLocationInstance,
				arrivalLocationInstance);
		ticketService.save(ticket);
		logger.info("Ticket succesfully created.");

		userService.addTicket(customer, ticket);
		logger.info("Inserted into UsersTicket table.");

		// For every 5 purchased tickets, the user gains a rating of 0.2
		if (DatabaseUtils.currentUser.getTickets().size() % 5 == 0) {
			new UserProfileService().increaseRating(DatabaseUtils.currentUser.getUserProfile(), 0.2);
			logger.info("Cashier rating increased by 0.2");
		}

		User company = userService.getUserByTripId(trip.getTripId()).get();

		/*
		 * Iterate through the tickets, check if the trip they belong to matches the
		 * user id of this company's id. Basically, get all tickets sold by this
		 * company.
		 */
		List<Ticket> ticketsSoldByCompany = ticketService.getAll().stream().filter(
				t -> userService.getUserByTripId(t.getTrip().getTripId()).get().getUserId() == company.getUserId())
				.collect(Collectors.toList());

		if (ticketsSoldByCompany.size() % 5 == 0) {
			new UserProfileService().increaseRating(company.getUserProfile(), 0.1);
			logger.info("Company rating increased by 0.1");
		}

		response.setValid(true);
		return response;
	}

	@Override
	public ObservableList<Ticket> cashierGetTicketsSold(User cashier) {
		ObservableList<Ticket> ticketList = FXCollections.observableArrayList();

		TicketService ticketService = new TicketService();

		List<Ticket> allTickets = ticketService.getAll();

		for (Ticket ticket : allTickets) {
			if (ticket.getTrip().getCashiers().contains(cashier)) {
				ticketList.add(ticket);
			}
		}

		return ticketList;
	}
}
