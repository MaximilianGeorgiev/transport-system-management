package com.tuvarna.transportsystem.services;

import java.util.List;
import java.util.Optional;

import com.tuvarna.transportsystem.dao.RequestDAO;
import com.tuvarna.transportsystem.entities.Request;
import com.tuvarna.transportsystem.interfaces.ICrudOperations;

public class RequestService implements ICrudOperations<Request> {
	private RequestDAO requestDAO;

	public RequestService() {
		this.requestDAO = new RequestDAO();
	}
	
	public void updateStatus(Request request, String newStatus) {
		requestDAO.updateStatus(request, newStatus);
	}
	
	public void deleteByTripId(int tripId) {
		requestDAO.deleteByTripId(tripId);
	}

	@Override
	public Optional<Request> getById(int id) {
		return requestDAO.getById(id);
	}

	@Override
	public List<Request> getAll() {
		return requestDAO.getAll();
	}

	@Override
	public void save(Request request) {
		requestDAO.save(request);
	}

	@Override
	public void deleteById(int id) {
		requestDAO.deleteById(id);
	}

	@Deprecated
	@Override
	public Optional<Request> getByName(String name) {
		return null;
	}

	@Deprecated
	@Override
	public void updateName(Request request, String newValue) {
	}

	@Deprecated
	@Override
	public void deleteByName(String name) {
	}

	@Deprecated
	@Override
	public void update(Request request, String[] newValues) {
	}
}
