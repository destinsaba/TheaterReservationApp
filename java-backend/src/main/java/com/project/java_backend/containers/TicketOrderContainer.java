package com.project.java_backend.containers;

import java.util.List;

// Container for sending ticket order info via JSON
public class TicketOrderContainer {
	private Long registeredUserId;
	private String email;
	private String cardNumber;
	private Long showtimeId;
	private List<Long> seatIds;
	private Double price;

	public TicketOrderContainer() {
		// Default constructor
	}

	// Getters and setters

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Long getRegisteredUserId() {
		return registeredUserId;
	}

	public void setRegisteredUserId(Long registeredUserId) {
		this.registeredUserId = registeredUserId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public Long getShowtimeId() {
		return showtimeId;
	}

	public void setShowtimeId(Long showtimeId) {
		this.showtimeId = showtimeId;
	}

	public List<Long> getSeatIds() {
		return seatIds;
	}

	public void setSeatIds(List<Long> seatIds) {
		this.seatIds = seatIds;
	}
}
