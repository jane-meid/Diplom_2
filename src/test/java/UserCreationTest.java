import client.RequestSpec;
import client.UserClient;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.User;
import models.UserAccessToken;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.github.javafaker.Faker;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;

public class UserCreationTest {
    private UserClient userClient;
    private String accessToken;

    @Before
    public void setUp() {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        userClient = new UserClient();
    }

    private User createRandomUser () {
        Faker faker = new Faker();
        return new User(faker.internet().emailAddress(), faker.internet().password(), faker.name().firstName());
    }

    @Test
    @DisplayName("Проверка создания пользователя")
    public void createUser () {
        User user = createRandomUser ();
        Response responseCreate = userClient.create(user);

        responseCreate
                .then()
                .statusCode(SC_OK)
                .body("success", equalTo(true));

        accessToken = responseCreate.as(UserAccessToken.class).getAccessToken();
        System.out.println("Access Token: " + accessToken);
    }

    @Test
    @DisplayName("Проверка создания уже существующего пользователя")
    public void createExistingUser () {
        User user = createRandomUser ();
        Response responseCreate = userClient.create(user);

        Response responseCreateAgain = userClient.create(user);

        responseCreateAgain
                .then()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));

        accessToken = responseCreate.as(UserAccessToken.class).getAccessToken();
        System.out.println("Access Token: " + accessToken);
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