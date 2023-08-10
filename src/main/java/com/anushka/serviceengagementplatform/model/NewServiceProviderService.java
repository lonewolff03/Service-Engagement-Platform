package com.anushka.serviceengagementplatform.model;

public class NewServiceProviderService {
	private int serviceId;

	public NewServiceProviderService(int serviceId) {
		super();
		this.serviceId = serviceId;
	}

	public NewServiceProviderService() {
		super();
		// TODO Auto-generated constructor stub
	}

	public int getServiceId() {
		return serviceId;
	}

	public void setServiceId(int serviceId) {
		this.serviceId = serviceId;
	}
}
