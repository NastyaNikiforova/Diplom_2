import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.example.UserClient;
import org.example.UserData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;

public class UpdateUserDataTest {
    private UserClient userClient;
    private UserData userData;
    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
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
    @Step("Send PATCH request to /api/auth/user with authorization to update user data")
    public Response sendPatchRequestWithAuthToUpdateUserData(String accessToken) {
        userData = new UserData("garden@yandex.ru","cucumber", "MARFA");
        return userClient.updateData(accessToken, userData);
    }
    @Step("Send PATCH request to /api/auth/user with authorization and exists email")
    public Response sendPatchRequestWithAuthAndExistsEmail(String accessToken) {
        userData = new UserData("","rabbit", "Anna");
        return userClient.updateData(accessToken, userData);
    }
    @Step("Send PATCH request to /api/auth/user without authorization")
    public Response sendPatchRequestWithoutAuth() {
        userData = new UserData("magenta@yandex.ru","063pancake063", "MARFA");
        return userClient.updateDataNoAuth(userData);
    }
    @Step("Check 200 status code for successful update")
    public void check200StatusCodeForSuccessfulUpdate(Response response) {
        response.then().statusCode(200)
                .and().assertThat().body("success", equalTo(true));
    }
    @Step("Check 403 status code for update with exists email")
    public void check403StatusCodeForUpdateWithExistsEmail(Response response) {
        response.then().statusCode(403)
                .and().assertThat().body("message", equalTo("User with such email already exists"));
    }
    @Step("Check 401 status code for update without authorization")
    public void check401StatusCodeForUpdateWithoutAuth(Response response) {
       response.then().statusCode(401)
               .and().assertThat().body("message", equalTo("You should be authorised"));
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
    @DisplayName("Update user data with authorization and check 200 status code")
    public void updateUserDataWithAuthAndCheck200StatusCode() {
        Response loginResponse = sendPostRequestToLoginUser();
        String accessToken = getAccessToken(loginResponse);
        Response updateResponse = sendPatchRequestWithAuthToUpdateUserData(accessToken);
        System.out.println(updateResponse.asPrettyString());
        check200StatusCodeForSuccessfulUpdate(updateResponse);
    }

    @Test
    @DisplayName("Update user data with authorization and exists email and check 403 status code")
    public void updateUserDataWithSameEmailAndCheck403StatusCode() {
        Response loginResponse = sendPostRequestToLoginUser();
        String accessToken = getAccessToken(loginResponse);
        Response updateResponse = sendPatchRequestWithAuthAndExistsEmail(accessToken);
        System.out.println(updateResponse.asPrettyString());
        check403StatusCodeForUpdateWithExistsEmail(updateResponse);
    }

    @Test
    @DisplayName("Update user data without authorization and check 401 status code")
    public void updateUserDataWithoutAuthAndCheck401StatusCode() {
        sendPostRequestToLoginUser();
        Response updateResponse = sendPatchRequestWithoutAuth();
        System.out.println(updateResponse.asPrettyString());
        check401StatusCodeForUpdateWithoutAuth(updateResponse);
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
