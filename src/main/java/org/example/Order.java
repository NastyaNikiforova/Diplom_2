package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private Object[] ingredients;
    private String _id;
    private Owner owner;
    private String status;
    private String name;
    private Date createdAt;
    private Date updatedAt;
    private int number;
    private int price;
}


