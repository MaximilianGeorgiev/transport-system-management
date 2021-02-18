package com.tuvarna.transportsystem.services;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.tuvarna.transportsystem.dao.UserDAO;
import com.tuvarna.transportsystem.entities.Location;
import com.tuvarna.transportsystem.entities.Ticket;
import com.tuvarna.transportsystem.entities.Trip;
import com.tuvarna.transportsystem.entities.User;
import com.tuvarna.transportsystem.interfaces.ICustomerFunctionality;
import com.tuvarna.transportsystem.utils.ValidationUtils;
import com.tuvarna.transportsystem.utils.ViewResponse;

import javafx.scene.control.TextField;

public class CustomerService implements ICustomerFunctionality {
	private UserService userService;
	private static final Logger logger = LogManager.getLogger(CustomerService.class.getName());

	public CustomerService() {
		this.userService = new UserService();
		PropertyConfigurator.configure("log4j.properties"); // configure log4j
	}

	@Override
	public List<Trip> getMatchingTrips(String departureLoc, String arrivalLoc, TextField date, String quantity,
			String time) throws ParseException {
		ViewResponse response = ValidationUtils.validateUserPanelGetMatchingTrips(departureLoc, arrivalLoc, date,
				quantity, time);

		if (!response.isValid()) {
			return null;
		}

		response.setValid(false);

		TripService tripService = new TripService();
		List<Trip> fullTrips = tripService.getAll();

		/*
		 * Uses local machine's format and since it contains HH:MM:SS as well, it is
		 * splitted and only the date is taken.
		 */
		String dateFormatPattern = new SimpleDateFormat().toLocalizedPattern().split(" ")[0];
		DateFormat formatDepartureDate = new SimpleDateFormat(dateFormatPattern);

		Date dateDeparture = formatDepartureDate.parse(date.getText());

		List<Trip> filteredTrips = new ArrayList<>();

		/* Iterate through the trips and validate all the fields */
		for (Trip trip : fullTrips) {
			Date dbDate = trip.getTripDepartureDate();

			/*
			 * Probably the least intuitive approach to fix the difference in formats
			 * between the current machine and postgresql date but it works and is universal
			 */
			boolean matchesDates = dbDate.getYear() == dateDeparture.getYear()
					&& dbDate.getMonth() == dateDeparture.getMonth() && dbDate.getDay() == dateDeparture.getDay();
			boolean checkQuantity = Integer.parseInt(quantity) <= trip.getMaxTicketsPerUser();
			boolean matchesTime = trip.getTripDepartureHour().contentEquals(time.trim());
			boolean tripEndPointsSearched = trip.getRoute().getRouteDepartureLocation().getLocationName().equals(
					departureLoc) && trip.getRoute().getRouteArrivalLocation().getLocationName().equals(arrivalLoc);
			boolean tripDepartureMiddlePointSearched = false;
			boolean tripArrivalMiddlePointSearched = false;
			boolean endPointToMiddlePointSearched = false;
			boolean middlePointToEndPointSearched = false;

			RouteService routeService = new RouteService();
			List<Location> attachmentLocations = routeService
					.getAttachmentLocationsInRouteById(trip.getRoute().getRouteId());

			/*
			 * Scenario: departure station matches start location of the route but the
			 * arrival station searched doesn't match the end of the route. We are searching
			 * for: an attachment location with the searched arrival location
			 */
			if (trip.getRoute().getRouteDepartureLocation().getLocationName().equals(departureLoc)
					&& (!trip.getRoute().getRouteArrivalLocation().getLocationName().equals(arrivalLoc))) {

				for (Location location : attachmentLocations) {
					if (location.getLocationName().equals(arrivalLoc)) {
						endPointToMiddlePointSearched = true;
						break;
					}
				}
			}
			/*
			 * Scenario: Arrival station matches the end point of the route but the
			 * departure station is probably a middle point. Check it.
			 */
			if ((!trip.getRoute().getRouteDepartureLocation().getLocationName().equals(departureLoc))
					&& (trip.getRoute().getRouteArrivalLocation().getLocationName().equals(arrivalLoc))) {

				for (Location attachmentLocation : attachmentLocations) {
					if (attachmentLocation.getLocationName().equals(departureLoc)) {
						middlePointToEndPointSearched = true;

						int routeId = trip.getRoute().getRouteId();
						int locationId = attachmentLocation.getLocationId();

						/*
						 * Initially, matchesTime compares the start point of the route with the
						 * searched time. If we are buying a ticket from a middle point, it takes time
						 * until the bus reaches that location and we are searching from another hour.
						 * In RouteAttachment it is logged when the bus arrives at the middle point.
						 */
						matchesTime = time.equals(routeService.getArrivalHourAtAttachmentLocation(routeId, locationId));

						break;
					}
				}
			}

			/*
			 * Scenario: We are searching for a trip between 2 middle points. Check if the
			 * departure location is present in the RouteAttachment table
			 */
			for (Location attachmentLocation : attachmentLocations) {
				if (attachmentLocation.getLocationName().equals(departureLoc)) {
					tripDepartureMiddlePointSearched = true;

					int routeId = trip.getRoute().getRouteId();
					int locationId = attachmentLocation.getLocationId();

					matchesTime = time.equals(routeService.getArrivalHourAtAttachmentLocation(routeId, locationId));

					break;
				}
			}

			/* Same for arrival */
			for (Location attachmentLocation : attachmentLocations) {
				if (attachmentLocation.getLocationName().equals(arrivalLoc)) {
					tripArrivalMiddlePointSearched = true;
					break;
				}
			}

			/* If all the criteria matches check if there are enough available tickets */
			if (matchesDates && checkQuantity && matchesTime) {

				/*
				 * If either the customer chose the start and end point of the route (Sofia -
				 * Varna) or they chose two valid middle points from the RouteAttachment table
				 * (for example Shumen - Veliko Tarnovo) then a purchase can proceed.
				 * 
				 * Also, if the customer chose (Sofia - Veliko Tarnovo) (start of route - middle
				 * point) or they chose (Veliko Tarnovo - Varna) (middle point - end of route)
				 * this is also a valid search
				 */
				if (tripEndPointsSearched || (tripDepartureMiddlePointSearched && tripArrivalMiddlePointSearched)
						|| (endPointToMiddlePointSearched || middlePointToEndPointSearched)) {

					int ticketsToPurchase = Integer.parseInt(quantity);

					/* If there are enough tickets substitute the bought tickets */
					if (trip.getTripTicketAvailability() >= ticketsToPurchase) {
						filteredTrips.add(trip);
					}
				}
			}
		}

		return filteredTrips;
	}

	@Override
	public ViewResponse buyTicket(Trip trip, User customer, String departureLoc, String arrivalLoc,
			String ticketsToPurchaseInput) {
		ViewResponse response = new ViewResponse();
		response.setValid(false);

		if (trip == null) {
			logger.info("CONSTRAINT FAILURE: No trip selected.");

			response.setMessage("Please select a trip.");
			return response;
		}

		TripService tripService = new TripService();
		LocationService locationService = new LocationService();

		int ticketsToPurchase = Integer.parseInt(ticketsToPurchaseInput);

		tripService.updateTripTicketAvailability(trip, trip.getTripTicketAvailability() - ticketsToPurchase);
		logger.info("Updated trip ticket availability after purchase.");

		Location departureLocation = locationService.getByName(departureLoc).get();
		Location arrivalLocation = locationService.getByName(arrivalLoc).get();

		TicketService ticketService = new TicketService();
		Ticket ticket = new Ticket(new Date(System.currentTimeMillis()), trip, departureLocation, arrivalLocation);
		ticketService.save(ticket);
		logger.info("Ticket created and persisted to database.");

		UserService userService = new UserService();
		userService.addTicket(customer, ticket);
		logger.info("Inserted into UsersTicket table.");

		// For every 5 purchased tickets, the user gains a rating of 0.2
		if (customer.getTickets().size() % 5 == 0) {
			customer.getUserProfile().setUserProfileRating(customer.getUserProfile().getUserProfileRating() + 0.2);
			logger.info("Customer gained a rating of 0.2");
		}

		response.setValid(true);
		return response;
	}

}
