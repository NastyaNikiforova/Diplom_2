import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.example.UserData;
import org.example.UserClient;
import org.example.UserLoginResponse;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class LoginUserTest {
    private UserData userData;
    private UserClient userClient;
    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        userClient = new UserClient();
        userData = new UserData("magenta@yandex.ru", "yellow", "Alex");
        userClient.create(userData);
    }
    @Step("Send POST request to /api/auth/login to login existing user")
    public Response sendPostRequestToLoginExistingUser() {
        return userClient.login(userData);
    }
    @Step("Send POST request to /api/auth/login to login with an invalid data")
    public Response sendPostRequestToLoginWithInvalidData() {
        userData = new UserData("magenta@yandex.ru", "", "Alex");
        return userClient.login(userData);
    }
    @Step("Check 200 status code for successful login")
    public void check200StatusCodeForSuccessfulLogin(Response response) {
        response.then().statusCode(200)
                .and().assertThat().body("success", equalTo(true));
    }
    @Step("Check response body for successful login")
    public void checkResponseBodyForSuccessfulLogin(Response response) {
        UserLoginResponse userLoginResponse = response.body().as(UserLoginResponse.class);
        MatcherAssert.assertThat(userLoginResponse, notNullValue());
    }
    @Step("Check 401 status code and response body for failed login")
    public void check401StatusCodeForFailedLogin(Response response) {
        response.then().statusCode(401)
                .and().assertThat().body("message", equalTo("email or password are incorrect"));
    }
    @Step("Get accessToken from the response")
    public String getAccessToken(Response response) {
        String token = response.then().extract().path("accessToken");
        StringBuilder builder = new StringBuilder(token);
        String accessToken = builder.substring(7);
        return accessToken;
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
    @DisplayName("Login existing user and check 200 status code")
    public void loginExistingUserAndCheck200StatusCode() {
        Response response = sendPostRequestToLoginExistingUser();
        check200StatusCodeForSuccessfulLogin(response);
        checkResponseBodyForSuccessfulLogin(response);
    }
    @Test
    @DisplayName("Login with an invalid data and check 401 status code")
    public void loginWithInvalidDataAndCheck401StatusCode() {
        Response response = sendPostRequestToLoginWithInvalidData();
        check401StatusCodeForFailedLogin(response);
    }

    @After
    public void deleteUser() {
        Response loginResponse = sendPostRequestToLoginExistingUser();
        if (loginResponse.getStatusCode() == 200) {
            String accessToken = getAccessToken(loginResponse);
            Response deleteResponse = deleteUser(accessToken);
            check202StatusCodeAfterDeleteUser(deleteResponse);
        }
    }
}
