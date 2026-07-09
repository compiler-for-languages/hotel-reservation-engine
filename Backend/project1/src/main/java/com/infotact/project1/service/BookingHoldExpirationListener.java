package com.infotact.project1.service;

import com.infotact.project1.enums.ReservationStatus;
import com.infotact.project1.model.Reservation;
import com.infotact.project1.repository.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

@Service
//@Profile("!test")
@Slf4j
public class BookingHoldExpirationListener
        extends KeyExpirationEventMessageListener {

    private final ReservationRepository reservationRepository;

    public BookingHoldExpirationListener(
            RedisMessageListenerContainer listenerContainer,
            ReservationRepository reservationRepository) {

        super(listenerContainer);

        this.reservationRepository = reservationRepository;

        System.out.println("======================================================================BookingHoldExpirationListener Loaded====================================================================");
    }

    @Override
    public void onMessage(
            Message message,
            byte[] pattern) {

        System.out.println("================================================");
        System.out.println("Redis Expiration Event Received");
        System.out.println("Key : " + message.toString());
        System.out.println("================================================");

        // Expired Redis key
        String expiredKey = message.toString();

        try {

            // Expected format: bookingHold:51
            if (!expiredKey.startsWith("bookingHold:")) {
                return;
            }

            // Extract reservation id (long)
            String reservationIdString =
                    expiredKey.substring("bookingHold:".length());

            Long reservationId =
                    Long.parseLong(reservationIdString);

            System.out.println("Reservation Id : " + reservationId);

            Reservation reservation =
                    reservationRepository.findById(reservationId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Reservation not found with id: "
                                                    + reservationId));

            // Expire only pending reservations
            //This check creates an exception for confirmed payments
            if (reservation.getReservationStatus()
                    == ReservationStatus.PENDING) {

                reservation.setReservationStatus(
                        ReservationStatus.EXPIRED);

                reservationRepository.save(reservation);

                System.out.println(
                        "Reservation " + reservationId + " marked as EXPIRED.");

                log.info(
                        "Reservation {} expired after booking hold timeout.",
                        reservationId);
            }

        } catch (Exception exception) {

            exception.printStackTrace();

            log.error(
                    "Failed to process Redis expiration event for key: {}",
                    expiredKey,
                    exception);
        }
    }

}