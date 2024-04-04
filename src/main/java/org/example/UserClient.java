package org.example;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class UserClient {

    //метод для POST-запроса на ручку /api/auth/register (регистрация нового пользователя)
    public Response create(UserData userData) {
        return given()
                .header("Content-type", "application/json")
                .body(userData)
                .when()
                .post("/api/auth/register");
    }
    //метод для POST-запроса на ручку /api/auth/login (логин пользователя)
    public Response login(UserData userData) {
        return given()
                .header("Content-type", "application/json")
                .body(userData)
                .when()
                .post("/api/auth/login");
    }
    //метод для PATCH-запроса на ручку /api/auth/user (обновление данных о пользователе)
    public Response updateData(String accessToken, UserData userData) {
        return given()
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-type", "application/json")
                .body(userData)
                .when()
                .patch("/api/auth/user");
    }
    //метод для PATCH-запроса на ручку /api/auth/user (обновление данных о пользователе без авторизации)
    public Response updateDataNoAuth(UserData userData) {
        return given()
                .header("Content-type", "application/json")
                .body(userData)
                .when()
                .patch("/api/auth/user");
    }
    //метод для DELETE-запроса на ручку /api/auth/user (удаление пользователя)
    public Response delete(String accessToken) {
        return given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .delete("/api/auth/user");
    }
}
