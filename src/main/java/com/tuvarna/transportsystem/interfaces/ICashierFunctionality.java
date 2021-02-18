package com.tuvarna.transportsystem.interfaces;

import com.tuvarna.transportsystem.entities.Ticket;
import com.tuvarna.transportsystem.entities.Trip;
import com.tuvarna.transportsystem.entities.User;
import com.tuvarna.transportsystem.utils.ViewResponse;

import javafx.collections.ObservableList;

public interface ICashierFunctionality {

	public ObservableList<Trip> getScheduleForCashier(User cashier);

	public ObservableList<String> getDepLocationsForCashierBySelectedRow(User cashier, Trip trip);

	public ObservableList<String> getArrLocationsForCashierBySelectedRow(User cashier, Trip trip);

	public ViewResponse sellTicket(Trip trip, String departureLocation, String arrivalLocation, String quantity,
			boolean customerIsGuest, boolean customerIsRegistered, String usernameSelected, String guestUserFullName);

	public ObservableList<Ticket> cashierGetTicketsSold(User cashier);

}
