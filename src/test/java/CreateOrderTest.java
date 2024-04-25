import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.example.*;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

public class CreateOrderTest {
    private OrderClient orderClient;
    private UserClient userClient;
    private UserData userData;
    private OrderData orderData;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        orderClient = new OrderClient();
        userClient = new UserClient();
        userData = new UserData("magenta@yandex.ru", "yellow", "Alex");
        userClient.create(userData);
    }
    @Step("Send POST request to /api/auth/login to login user")
    public Response sendPostRequestToLoginUser() {
        return userClient.login(userData);
    }
    @Step("Get accessToken from the response")
    public String getAccessToken(Response response) {
        String token = response.then().extract().path("accessToken");
        StringBuilder builder = new StringBuilder(token);
        String accessToken = builder.substring(7);
        return accessToken;
    }
    @Step("Send POST request to /api/orders with authorization and with ingredients to create order")
    public Response sendPostRequestWithAuthToCreateOrder(String accessToken, OrderData orderData) {
        return orderClient.createWithAuth(accessToken, orderData);
    }
    @Step("Send POST request to /api/orders without authorization and with ingredients to create order")
    public Response sendPostRequestWithoutAuthToCreateOrder(OrderData orderData) {
        return orderClient.createNoAuth(orderData);
    }
    @Step("Send POST request to /api/orders with authorization and without ingredients to create order")
    public Response sendPostRequestWithAuthNoIngredientsToCreateOrder(String accessToken, OrderData orderData) {
        return orderClient.createWithAuth(accessToken, orderData);
    }
    @Step("Send POST request to /api/orders without authorization and without ingredients to create order")
    public Response sendPostRequestWithoutAuthNoIngredientsToCreateOrder(OrderData orderData) {
        return orderClient.createNoAuth(orderData);
    }
    @Step("Send POST request to /api/orders with authorization and with invalid hash of ingredients")
    public Response sendPostRequestWithAuthAndInvalidHashIngredientsToCreateOrder(String accessToken, OrderData orderData) {
        return orderClient.createWithAuth(accessToken, orderData);
    }
    @Step("Check 200 status code for successful create order")
    public void check200StatusCodeForSuccessfulCreateOrder(Response response) {
        response.then().statusCode(200)
                .and().assertThat().body("success", equalTo(true));
    }
    @Step("Check 400 status code for create order without ingredients")
    public void check400StatusCodeForCreateOrderWithoutIngredients(Response response) {
        response.then().statusCode(400)
                .and().assertThat().body("message", equalTo("Ingredient ids must be provided"));
    }
    @Step("Check 500 status code for create order with invalid hash of ingredients")
    public void check500StatusCodeForCreateOrderWithInvalidHashIngredients(Response response) {
        response.then().statusCode(500);
    }
    @Step("Check response body for create order with authorization")
    public void checkResponseBodyForCreateOrderWithAuth(Response response) {
        OrderResponse orderResponse = response.body().as(OrderResponse.class);
        MatcherAssert.assertThat(orderResponse, notNullValue());
    }
    @Step("Check response body for create order without authorization")
     public void checkResponseBodyForCreateOrderWithoutAuth(Response response) {
        response.then()
                .body("$", hasKey("name"))
                .body("$", hasKey("order"));
    }
    @Step("Send DELETE request to delete user")
    public Response deleteUser(String accessToken) {
        return userClient.delete(accessToken);
    }
    @Step("Check 202 status code and response body after delete user")
    public void check202StatusCodeAfterDeleteUser(Response response) {
        response.then().statusCode(202)
                .and().assertThat().body("message", equalTo("User successfully removed"));
    }
    @Test
    @DisplayName("Create order with authorization and with ingredients and check 200 status code")
    public void createOrderWithAuthAndIngredientsAndCheck200StatusCode() {
        Response loginResponse = sendPostRequestToLoginUser();
        String accessToken = getAccessToken(loginResponse);
        orderData = new OrderData(new String[]{"61c0c5a71d1f82001bdaaa6d", "61c0c5a71d1f82001bdaaa71", "61c0c5a71d1f82001bdaaa72"});
        Response orderResponse = sendPostRequestWithAuthToCreateOrder(accessToken, orderData);
        check200StatusCodeForSuccessfulCreateOrder(orderResponse);
        checkResponseBodyForCreateOrderWithAuth(orderResponse);
        System.out.println(orderResponse.asPrettyString());
    }
    @Test
    @DisplayName("Create order without authorization and with ingredients and check 200 status code")
    public void createOrderWithoutAuthWithIngredientsAndCheck200StatusCode() {
        orderData = new OrderData(new String[]{"61c0c5a71d1f82001bdaaa6d", "61c0c5a71d1f82001bdaaa71", "61c0c5a71d1f82001bdaaa72"});
        Response orderResponse = sendPostRequestWithoutAuthToCreateOrder(orderData);
        check200StatusCodeForSuccessfulCreateOrder(orderResponse);
        checkResponseBodyForCreateOrderWithoutAuth(orderResponse);
        System.out.println(orderResponse.asPrettyString());
    }
    @Test
    @DisplayName("Create order with authorization and without ingredients and check 400 status code")
    public void createOrderWithAuthNoIngredientsAndCheck400StatusCode() {
        Response loginResponse = sendPostRequestToLoginUser();
        String accessToken = getAccessToken(loginResponse);
        orderData = new OrderData(new String[]{});
        Response orderResponse = sendPostRequestWithAuthNoIngredientsToCreateOrder(accessToken, orderData);
        check400StatusCodeForCreateOrderWithoutIngredients(orderResponse);
        System.out.println(orderResponse.asPrettyString());
    }
    @Test
    @DisplayName("Create order without authorization and without ingredients and check 400 status code")
    public void createOrderWithoutAuthNoIngredientsAndCheck400StatusCode() {
        orderData = new OrderData(new String[]{});
        Response orderResponse = sendPostRequestWithoutAuthNoIngredientsToCreateOrder(orderData);
        check400StatusCodeForCreateOrderWithoutIngredients(orderResponse);
        System.out.println(orderResponse.asPrettyString());
    }
    @Test
    @DisplayName("Create order with authorization and with invalid hash of ingredients and check 500 status code")
    public void createOrderWithAuthWithInvalidHashIngredientsAndCheck500StatusCode() {
        Response loginResponse = sendPostRequestToLoginUser();
        String accessToken = getAccessToken(loginResponse);
        orderData = new OrderData(new String[]{"61c075738", "610000000000", "61c088888872"});
        Response orderResponse = sendPostRequestWithAuthAndInvalidHashIngredientsToCreateOrder(accessToken, orderData);
        check500StatusCodeForCreateOrderWithInvalidHashIngredients(orderResponse);
        System.out.println(orderResponse.asPrettyString());
    }

    @After
    public void deleteUser() {
        Response loginResponse = sendPostRequestToLoginUser();
        if (loginResponse.getStatusCode() == 200) {
            String accessToken = getAccessToken(loginResponse);
            Response deleteResponse = deleteUser(accessToken);
            check202StatusCodeAfterDeleteUser(deleteResponse);
        }
    }
}
