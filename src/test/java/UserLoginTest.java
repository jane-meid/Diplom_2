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
import static org.hamcrest.CoreMatchers.notNullValue;

@RunWith(Parameterized.class)
public class UserLoginTest {
        private final String email;
        private final String password;
        private UserClient userClient;
        private User testUser;
        private String accessToken;
        public final int expectedStatusCode;
        public final boolean expectedSuccess;

    public UserLoginTest(String email, String password, int expectedStatusCode, boolean expectedSuccess) {
        this.email = email;
        this.password = password;
        this.expectedStatusCode = expectedStatusCode;
        this.expectedSuccess = expectedSuccess;
    }

    @Before
    public void setUp() {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        userClient = new UserClient();
        testUser  = new User("carljohnsoncj@yandex.ru", "55555", "CJ");
        userClient.create(testUser);
    }

    @Parameterized.Parameters
    public static Object[][] data() {
        Faker faker = new Faker();
        return new Object[][]{
                {"carljohnsoncj@yandex.ru", "55555", SC_OK, true},
                {"carljohnsoncj@yandex.ru", faker.internet().password(), SC_UNAUTHORIZED, false},
                {faker.internet().emailAddress(), "55555", SC_UNAUTHORIZED, false},
                {null, "55555", SC_UNAUTHORIZED, false},
                {"carljohnsoncj@yandex.ru", null, SC_UNAUTHORIZED, false}
        };
    }

    @Test
    @DisplayName("Проверка авторизации пользователя")
    public void testUserAuthorization() {
        Response response = userClient.login(new LoginUser(email, password));

        response
                .then()
                .statusCode(expectedStatusCode)
                .body("success", equalTo(expectedSuccess));

        if (expectedStatusCode == SC_UNAUTHORIZED) {
            response
                    .then()
                    .body("message", equalTo("email or password are incorrect"));

        } else if (expectedStatusCode == SC_OK) {
            response
                    .then()
                    .body("accessToken", notNullValue())
                    .body("refreshToken", notNullValue())
                    .body("user.email", equalTo(testUser.getEmail()))
                    .body("user.name", equalTo(testUser.getName()));
        }

        accessToken = response.as(UserAccessToken.class).getAccessToken();
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
