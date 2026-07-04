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
@Profile("!test")
@Slf4j
public class BookingHoldExpirationListener
        extends KeyExpirationEventMessageListener {

    private final ReservationRepository reservationRepository;

    public BookingHoldExpirationListener(
            RedisMessageListenerContainer listenerContainer,
            ReservationRepository reservationRepository) {

        super(listenerContainer);

        this.reservationRepository = reservationRepository;
    }

    @Override
    public void onMessage(
            Message message,
            byte[] pattern) {

        // Expired Redis key (Reservation Id)
        String expiredKey =
                message.toString();

        System.out.println(expiredKey);

        try {

            // Reservation id is used as the Redis key
            Long reservationId =
                    Long.parseLong(expiredKey);

            Reservation reservation =
                    reservationRepository.findById(
                                    reservationId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Reservation not found with id: "
                                                    + reservationId));

            // Expire only reservations waiting for payment
            if (reservation.getReservationStatus()
                    == ReservationStatus.PENDING) {

                reservation.setReservationStatus(
                        ReservationStatus.EXPIRED);

                reservationRepository.save(
                        reservation);

                log.info(
                        "Reservation {} expired after booking hold timeout.",
                        reservationId);
            }

        } catch (NumberFormatException exception) {

            // Ignore Redis keys that are not reservation ids
            log.debug(
                    "Ignoring expired Redis key: {}",
                    expiredKey);
        }
    }
}