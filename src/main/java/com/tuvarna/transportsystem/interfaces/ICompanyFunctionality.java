package com.tuvarna.transportsystem.interfaces;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import com.tuvarna.transportsystem.entities.Request;
import com.tuvarna.transportsystem.entities.Route;
import com.tuvarna.transportsystem.entities.Trip;
import com.tuvarna.transportsystem.utils.ViewResponse;

import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;

public interface ICompanyFunctionality {

	public ViewResponse addTrip(Route routeCreated, String ticketsAvailability, String seatsCapacity, String duration,
			String price, TextField departureDate, TextField arrivalDate, Object maxTicketsPerUser,
			String hourOfDeparture, String departureLocation, String arrivalLocation, Toggle selectedTripType,
			Toggle selectedBusType) throws ParseException;

	public Route createGlobalTrip(String departureLocation, String arrivalLocation); // check

	public void addTripHandleAttachmentLocations(String departureLocation, String arrivalLocation, Route route); // check

	public ViewResponse addAttachmentLocations(List<String> locations, List<String> hours, Route route);

	public ViewResponse acceptRequest(Request request);

	public ViewResponse rejectRequest(Request request);

	public ViewResponse scheduleShowAttachments(Trip trip) throws IOException;

	public ViewResponse cancelTrip(Trip trip);
}
