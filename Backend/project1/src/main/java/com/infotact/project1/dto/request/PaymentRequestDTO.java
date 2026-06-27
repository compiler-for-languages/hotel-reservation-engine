package com.infotact.project1.dto.request;

import com.infotact.project1.enums.PaymentMethod;
import lombok.Data;

@Data
public class PaymentRequestDTO {

    private Long reservationId;

    private PaymentMethod paymentMethod;
}


//Reservation already knows:
//
//Room Type
//Dates
// Price
//
//So backend can calculate:
//
//amount
//currency
//paymentGateway
//
//itself.
//
//Never trust client for amount.