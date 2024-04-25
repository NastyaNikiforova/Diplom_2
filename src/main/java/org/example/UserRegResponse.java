package org.example;

import lombok.Data;

@Data
public class UserRegResponse {
    private boolean success;
    private User user;
    private String accessToken;
    private String refreshToken;
}
