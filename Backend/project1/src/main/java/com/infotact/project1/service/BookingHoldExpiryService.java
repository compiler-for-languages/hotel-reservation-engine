package com.infotact.project1.service;

import com.infotact.project1.enums.BookingHoldStatus;
import com.infotact.project1.model.BookingHold;
import com.infotact.project1.repository.BookingHoldRepository;
import com.infotact.project1.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingHoldExpiryService {

    private final BookingHoldRepository bookingHoldRepository;

    private final ReservationRepository reservationRepository;

    // Release expired booking holds every minute
    @Scheduled(fixedRate = 60000)
    public void releaseExpiredBookingHolds() {
        for (BookingHold hold : bookingHoldRepository.findAll()) {
            if (hold.getStatus() == BookingHoldStatus.ACTIVE
                    && hold.getExpiresAt().isBefore(LocalDateTime.now())) {
                hold.setStatus(
                        BookingHoldStatus.EXPIRED);

                bookingHoldRepository.save(hold);

            }

        }

    } // Runs every 60 seconds

}
