import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.example.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

public class GetUserOrdersTest {
    private UserClient userClient;
    private OrderClient orderClient;
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
    @Step("Send POST request to /api/orders to create order ")
    public Response sendPostRequestWithAuthToCreateOrder(String accessToken, OrderData orderData) {
        return orderClient.createWithAuth(accessToken, orderData);
    }
    @Step("Send GET request to /api/orders with authorization to get user orders")
    public Response sendGetRequestWithAuthToGetOrders(String accessToken, UserData userData) {
        return orderClient.getOrdersWithAuth(accessToken, userData);
    }
    @Step("Send GET request to /api/orders without authorization to get user orders")
    public Response sendGetRequestNoAuthToGetOrders(UserData userData) {
        return orderClient.getOrdersNoAuth(userData);
    }
    @Step("Check 200 status code for successful getting orders")
    public void check200StatusCodeForSuccessfulGettingOrders(Response response) {
        response.then().statusCode(200)
                .and().assertThat().body("success", equalTo(true));
    }
    @Step("Check 401 status code for get user orders without authorization")
    public void check401StatusCodeForGetOrdersNoAuth(Response response) {
        response.then().statusCode(401)
                .and().assertThat().body("message", equalTo("You should be authorised"));
    }
    @Step("Check response body for get user orders with authorization")
    public void checkResponseBodyForGetOrdersWithAuth(Response response) {
        response.then()
                .body("$", hasKey("orders"))
                .body("$", hasKey("total"))
                .body("$", hasKey("totalToday"));
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
    @DisplayName("Get user orders with authorization and check 200 status code")
    public void getUserOrdersWithAuthAndCheck200StatusCode() {
        Response loginResponse = sendPostRequestToLoginUser();
        String accessToken = getAccessToken(loginResponse);
        orderData = new OrderData(new String[]{"61c0c5a71d1f82001bdaaa6d", "61c0c5a71d1f82001bdaaa71", "61c0c5a71d1f82001bdaaa72"});
        sendPostRequestWithAuthToCreateOrder(accessToken, orderData);
        Response response = sendGetRequestWithAuthToGetOrders(accessToken, userData);
        check200StatusCodeForSuccessfulGettingOrders(response);
        checkResponseBodyForGetOrdersWithAuth(response);
        System.out.println(response.asPrettyString());
    }
    @Test
    @DisplayName("Get user orders without authorization and check 401 status code")
    public void getUserOrdersNoAuthAndCheck401StatusCode() {
        Response loginResponse = sendPostRequestToLoginUser();
        String accessToken = getAccessToken(loginResponse);
        orderData = new OrderData(new String[]{"61c0c5a71d1f82001bdaaa6d", "61c0c5a71d1f82001bdaaa71", "61c0c5a71d1f82001bdaaa72"});
        sendPostRequestWithAuthToCreateOrder(accessToken, orderData);
        Response response = sendGetRequestNoAuthToGetOrders(userData);
        check401StatusCodeForGetOrdersNoAuth(response);
        System.out.println(response.asPrettyString());
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
