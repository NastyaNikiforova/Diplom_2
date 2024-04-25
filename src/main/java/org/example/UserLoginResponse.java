package org.example;

import lombok.Data;

@Data
public class UserLoginResponse {
    private boolean success;
    private String accessToken;
    private String refreshToken;
    private User user;
}
