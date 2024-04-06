package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Owner {
    private String name;
    private String email;
    private Date createdAt;
    private Date updatedAt;

}
