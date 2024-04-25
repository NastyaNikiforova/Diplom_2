package org.example;

import lombok.Data;

@Data
public class OrderResponse {
    private boolean success;
    private String name;
    private Order order;

}
