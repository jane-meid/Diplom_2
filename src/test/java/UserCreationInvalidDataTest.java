import client.RequestSpec;
import client.UserClient;
import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.User;
import models.UserAccessToken;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.hamcrest.CoreMatchers.equalTo;

@RunWith(Parameterized.class)
public class UserCreationInvalidDataTest {
    private UserClient userClient;
    private String accessToken;
    private final User invalidUser;

    public UserCreationInvalidDataTest(User invalidUser) {
        this.invalidUser = invalidUser;
    }

    @Before
    public void setUp() {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        userClient = new UserClient();
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Faker faker = new Faker();
        return Arrays.asList(new Object[][]{
                {new User(null, faker.internet().password(), faker.name().firstName())},
                {new User(faker.internet().emailAddress(), null, faker.name().firstName())},
                {new User(faker.internet().emailAddress(), faker.internet().password(), null)}
        });
    }

    @Test
    @DisplayName("Проверка создания пользователя и не заполнения одного из обязательных полей")
    public void createUserWithMissingFields() {
        Response responseCreate = userClient.create(invalidUser);

        responseCreate
                .then()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));

        accessToken = responseCreate.as(UserAccessToken.class).getAccessToken();
        System.out.println("Access Token: " + accessToken);
    }

    @After
    public void deleteUser () {
        if (accessToken != null) {
            Response responseDelete = userClient.deleteUser (accessToken);
            responseDelete
                    .then()
                    .statusCode(SC_ACCEPTED)
                    .body("success", equalTo(true));
        }
    }
}
