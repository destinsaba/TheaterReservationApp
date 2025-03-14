package com.project.java_backend.service;

import com.project.java_backend.exception.SeatNotAvailableException;
import com.project.java_backend.model.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PurchaseTicketService extends PaymentService{

    @Autowired
    private TicketService ticketService;

    @Autowired
    private SeatAvailabilityService seatAvailabilityService;

    @Autowired
    private SeatService seatService;

    @Autowired
    private ShowtimeService showtimeService;

    // Purchase 1 or more tickets for a single showtime
    public List<Ticket> purchaseTickets(String email, Long registeredUserId, Long showtimeId, List<Long> seatIds, String cardNumber, Double price) {

        //Ensure all seats are available
        for (int i = 0; i < seatIds.size(); i++) {
            if (!seatAvailabilityService.isSeatAvailable(seatIds.get(i), showtimeId)) {
                throw new SeatNotAvailableException("Seat is already reserved");
            }
        }

        // Validate restriction on 10% early bookings
        if (!showtimeService.getShowtimeById(showtimeId).getMovie().isPublic() && 
            seatAvailabilityService.isTenPercentOrMoreBooked(showtimeId, seatIds.size())) {
                throw new IllegalStateException("Only 10% of seats can be booked by registered users before public release.");
            }

        // Make payment before continuing
        if (cardNumber == null || price == null || price == 0) {
            //For payment covered completely by coupon
            makePayment(price, email);
        } else {
            makePayment(price,  cardNumber, email);
        }

        // Reserve the seats and create tickets
        List<Ticket> tickets = new ArrayList<Ticket>();
        for (int i = 0; i < seatIds.size(); i++) {
            seatAvailabilityService.reserveSeat(seatIds.get(i), showtimeId);
            tickets.add(ticketService.createTicket(seatService.getSeatById(seatIds.get(i)).getPrice(), 
                                                    email, 
                                                    registeredUserId,
                                                    showtimeService.getShowtimeById(showtimeId), 
                                                    seatService.getSeatById(seatIds.get(i))));
        }

        // Send tickets via email
        emailService.sendSimpleEmail(email, "Your Tickets", buildTicketEmail(tickets));

        return tickets;
    }

    @Override
    protected String buildEmailReceipt(Double amount, String paymentMethod) {
        return super.buildEmailReceipt(amount, paymentMethod) + "Item purchased: Ticket\n"
                                                                + "You will receive your tickets by email shortly\n";
    }

    private String buildTicketEmail(List<Ticket> tickets) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
        String emailbody = "Your tickets are attached below. Enjoy!\n\n" +
                            "Showtime: " + tickets.get(0).getShowtime().getStartTime().format(formatter) + "\n" +
                            "Theater: " + tickets.get(0).getSeat().getTheater().getName() + "\n\n";
        for (int i = 0; i < tickets.size(); i++) {
            emailbody = emailbody.concat(
                "Ticket " + (i+1) + "\n" +
                "\tTicket Code: " + tickets.get(i).getCode() + "\n" +
                "\tSeat: " + tickets.get(i).getSeat().getSeatNumber() + "\n\n"
            );
        } 
        return emailbody;
    }
}
