import client.OrderClient;
import client.RequestSpec;
import client.UserClient;
import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.LoginUser;
import models.Order;
import models.User;
import models.UserAccessToken;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.hamcrest.CoreMatchers.equalTo;

@RunWith(Parameterized.class)
public class InvalidHashOrderTest {
    private final List<String> ingredients;
    private OrderClient orderClient;
    private UserClient userClient;
    private LoginUser testLoginUser;
    private Order order;
    private User testUser;
    private String accessToken;

    public InvalidHashOrderTest(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    @Parameterized.Parameters
    public static Object[][] getData(){
        Faker faker = new Faker();
        return new Object[][]{
                {List.of(faker.number().digits(7))},
                {List.of(faker.number().digits(23))},
                {List.of(faker.number().digits(25))}
        };
    }

    @Before
    public void setUp()  {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        orderClient = new OrderClient();
        userClient = new UserClient();
        testUser  = new User("carljohnsoncj@yandex.ru", "55555", "CJ");
        userClient.create(testUser);
        testLoginUser = new LoginUser("carljohnsoncj@yandex.ru", "55555");
        Response responseLogin = userClient.login(testLoginUser);
        accessToken = responseLogin.as(UserAccessToken.class).getAccessToken();
        System.out.println("Access Token: " + accessToken);
        order = new Order(ingredients);
    }

    @Test
    @DisplayName("Проверка создания заказа с неверным хэшем")
    public void createOrderInvalidHash(){
        Response responseCreate = orderClient.createOrder(order, accessToken);

        responseCreate
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
    }

    @After
    public void deleteUser(){
        if (accessToken != null) {
            Response responseDelete = userClient.deleteUser(accessToken);
            responseDelete
                    .then()
                    .statusCode(SC_ACCEPTED)
                    .body("success", equalTo(true));
        }
    }
}
