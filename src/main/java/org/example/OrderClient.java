package org.example;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class OrderClient {
    //метод для POST-запроса на ручку /api/orders (создание заказа c авторизацией)
    public Response createWithAuth(String accessToken, OrderData orderData) {
        return given()
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-type", "application/json")
                .body(orderData)
                .when()
                .post("/api/orders");
    }
    //метод для POST-запроса на ручку /api/orders (создание заказа без авторизации)
    public Response createNoAuth(OrderData orderData) {
        return given()
                .header("Content-type", "application/json")
                .body(orderData)
                .when()
                .post("/api/orders");
    }
    //метод для GET-запроса на ручку /api/orders (получение заказов авторизованного пользователя)
    public Response getOrdersWithAuth(String accessToken, UserData userData) {
        return given()
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-type", "application/json")
                .body(userData)
                .when()
                .get("/api/orders");
    }
    //метод для GET-запроса на ручку /api/orders (получение заказов неавторизованного пользователя)
    public Response getOrdersNoAuth(UserData userData) {
        return given()
                .header("Content-type", "application/json")
                .body(userData)
                .when()
                .get("/api/orders");
    }
}
