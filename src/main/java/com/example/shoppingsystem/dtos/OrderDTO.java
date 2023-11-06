package com.example.shoppingsystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private Long order_id;
    private AddressInfoDTO address_info;
    private Date order_date;
    private String order_status;
    private String totalBill;
    private List<OrderDetailDTO> order_detail;
}
