package com.tuvarna.transportsystem.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "\"TransportType\"", schema = "\"TransportSystem\"")
public class TransportType {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "transport_type_id")
	private int transportTypeId;

	@Column(name = "transport_type_name")
	private String transportTypeName;

	@OneToMany(mappedBy = "tripTransportType")
	private List<Trip> trips; // one transporttype can belong to multiple trips

	public TransportType() {

	}

	public TransportType(String transportTypeName) {
		this.transportTypeName = transportTypeName;
	}

	public int getTransportTypeId() {
		return transportTypeId;
	}

	public void setTransportTypeId(int transportTypeId) {
		this.transportTypeId = transportTypeId;
	}

	public String getTransportTypeName() {
		return transportTypeName;
	}

	public void setTransportTypeName(String transportTypeName) {
		this.transportTypeName = transportTypeName;
	}
}
