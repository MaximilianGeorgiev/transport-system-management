package com.tuvarna.transportsystem.interfaces;

import java.text.ParseException;
import java.util.List;

import com.tuvarna.transportsystem.entities.Trip;
import com.tuvarna.transportsystem.entities.User;
import com.tuvarna.transportsystem.utils.ViewResponse;

import javafx.scene.control.TextField;

public interface ICustomerFunctionality {

	public List<Trip> getMatchingTrips(String departureLoc, String arrivalLoc, TextField date, String quantity,
			String time) throws ParseException;

	public ViewResponse buyTicket(Trip trip, User customer, String departureLoc, String arrivalLoc,
			String ticketsToPurchaseInput);

}
