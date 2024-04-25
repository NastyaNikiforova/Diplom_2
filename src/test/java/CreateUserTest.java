import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.example.UserData;
import org.example.UserClient;
import org.example.UserRegResponse;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CreateUserTest {
    private UserData userData;
    private UserClient userClient;
    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        userClient = new UserClient();
        userData = new UserData("magenta@yandex.ru", "yellow", "Alex");
    }
    @Step("Send POST request to /api/auth/register to create new user with all required parameters")
    public Response sendPostRequestToCreateUserAllParams() {
        return userClient.create(userData);
    }
    @Step("Send POST request to /api/auth/register to create new user without a required parameter")
    public Response sendPostRequestToCreateUserNotAllParams() {
        userData = new UserData("magenta@yandex.ru", "", "Alex");
        return userClient.create(userData);
    }
    @Step("Send POST request to /api/auth/login to login user")
    public Response sendPostRequestToLoginUser() {
        return userClient.login(userData);
    }
    @Step("Check 200 status code for successful create user")
    public void check200StatusCode(Response response) {
        response.then().statusCode(200)
                .and().assertThat().body("success", equalTo(true));
    }
    @Step("Check response body for successful create user")
    public void checkResponseBodyForSuccessfulCreateUser(Response response) {
        UserRegResponse userRegResponse = response.body().as(UserRegResponse.class);
        MatcherAssert.assertThat(userRegResponse, notNullValue());
    }
    @Step("Check 403 status code and response body for an existing user ")
    public void check403StatusCodeForAnExistingUser(Response response) {
        response.then().statusCode(403)
                .and().assertThat().body("message", equalTo("User already exists"));
    }
    @Step("Check 403 status code and response body for user without a required parameter")
    public void check403StatusCodeForUserNotAllParams(Response response) {
        response.then().statusCode(403)
                .and().assertThat().body("message", equalTo("Email, password and name are required fields"));
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
    @DisplayName("Create new user with all required parameters and check 200 status code")
    public void createNewUserAndCheck200StatusCode() {
        Response response = sendPostRequestToCreateUserAllParams();
        check200StatusCode(response);
        checkResponseBodyForSuccessfulCreateUser(response);
    }
    @Test
    @DisplayName("Create a user who already exists and check 403 status code")
    public void createUserWhoAlreadyExistsAndCheck403StatusCode() {
        sendPostRequestToCreateUserAllParams();
        Response response = sendPostRequestToCreateUserAllParams();
        check403StatusCodeForAnExistingUser(response);
    }
    @Test
    @DisplayName("Create new user without a required parameter and check 403 status code")
    public void createUserWithoutRequiredParamAndCheck403StatusCode() {
        Response response = sendPostRequestToCreateUserNotAllParams();
        check403StatusCodeForUserNotAllParams(response);
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
