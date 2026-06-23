package com.infotact.project1.dto.request;

import lombok.Data;

@Data
public class PaymentRequestDTO {

    private Long reservationId;

    private String paymentMethod;
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