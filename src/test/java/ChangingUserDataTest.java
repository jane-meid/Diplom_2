import client.RequestSpec;
import client.UserClient;
import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.LoginUser;
import models.User;
import models.UserAccessToken;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;

@RunWith(Parameterized.class)
public class ChangingUserDataTest{
    private final String email;
    private final String newEmail;
    private final String password;
    private final String newPassword;
    private final String name;
    private final String newName;
    private UserClient userClient;
    private String accessToken;

    public ChangingUserDataTest(String email, String newEmail, String password, String newPassword, String name, String newName) {
        this.email = email;
        this.newEmail = newEmail;
        this.password = password;
        this.newPassword = newPassword;
        this.name = name;
        this.newName = newName;
    }

    @Before
    public void setUp() {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        userClient = new UserClient();
        Response responseCreate = userClient.create(new User(email, password, name));
        accessToken = responseCreate.as(UserAccessToken.class).getAccessToken();
        System.out.println("Access Token: " + accessToken);
    }

    @Parameterized.Parameters
    public static Object[][] getData() {
        Faker faker = new Faker();
        return new Object[][]{
                {"carljohnsoncj@yandex.com", faker.internet().emailAddress(), "55555", faker.internet().password(), "CJ", faker.name().firstName()},
                {"carljohnsoncj@yandex.com", faker.internet().emailAddress(), "55555", "55555", "CJ", "CJ"},
                {"carljohnsoncj@yandex.com", "carljohnsoncj@yandex.com", "55555", faker.internet().password(), "CJ", "CJ"},
                {"carljohnsoncj@yandex.com", "carljohnsoncj@yandex.com", "55555", "55555", "CJ", faker.name().firstName()}
        };
    }
    @Test
    @DisplayName("Проверка изменения данных авторизованного пользователя")
    public void changingUserDataTest() {
        Response responseLogin = userClient.login(new LoginUser(email, password));

        Response responseGetUser = userClient.getUser(accessToken);
        responseGetUser
                .then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("user.email", equalTo(email))
                .body("user.name", equalTo(name));

        Response responseUpdate = userClient.updateUser(new User(newEmail, newPassword, newName), accessToken);
        responseUpdate
                .then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("user.email", equalTo(newEmail))
                .body("user.name", equalTo(newName));

        Response responseGetUserAfterUpdate = userClient.getUser(accessToken);
        responseGetUserAfterUpdate
                .then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("user.email", equalTo(newEmail))
                .body("user.name", equalTo(newName));

        Response responseLoginAfterUpdate = userClient.login(new LoginUser(newEmail, newPassword));
        responseLoginAfterUpdate
                .then()
                .statusCode(SC_OK);
    }
    @Test
    @DisplayName("Проверка изменения данных неавторизованного пользователя")
    public void changingUserDataWithoutAuthorizationTest() {
        Response responseUpdateWithoutAuthorization = userClient.updateUserWithoutAuthorization(new User(newEmail, newPassword, newName));
        responseUpdateWithoutAuthorization
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
    @After
    public void deleteUser () {
        if (accessToken != null) {
            Response responseDelete = userClient.deleteUser(accessToken);
            responseDelete
                    .then()
                    .statusCode(SC_ACCEPTED)
                    .body("success", equalTo(true));
        }
    }
}
