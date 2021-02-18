package com.tuvarna.transportsystem.interfaces;

import java.io.IOException;

import com.tuvarna.transportsystem.entities.Trip;
import com.tuvarna.transportsystem.utils.ViewResponse;

public interface IDistributorFunctionality {

	public ViewResponse addCashier(String fullname, String location, String company);

	public ViewResponse makeRequest(Trip trip, String requiredTickets);

	public ViewResponse loadAttachmentsInSchedule(Trip trip) throws IOException;

	public ViewResponse assignCashier(Trip trip, String location, String cashier);
}
