package com.infotact.project1.repository;

import com.infotact.project1.model.BookingHold;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository

// CrudRepository provides Redis CRUD operations
public interface BookingHoldRepository
        extends CrudRepository<BookingHold, String> {

}