import client.OrderClient;
import client.RequestSpec;
import client.UserClient;
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

import java.util.ArrayList;
import java.util.List;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

@RunWith(Parameterized.class)
public class CreateOrderTest {
    private final int fromIndex;
    private final int toIndex;
    private final int statusCode;
    private OrderClient orderClient;
    private UserClient userClient;
    private LoginUser testLoginUser;
    private User testUser;
    private String accessToken;

    public CreateOrderTest(int fromIndex, int toIndex, int statusCode){
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.statusCode = statusCode;
    }

    @Parameterized.Parameters
    public static Object[][] getData() {
        return new Object[][]{
                {0, 1, SC_OK},
                {0, 7, SC_OK},
                {8, 13, SC_OK},
                {0, 0, SC_BAD_REQUEST},
        };
    }

    @Before
    public void setUp() {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        orderClient = new OrderClient();
        userClient = new UserClient();
        testUser  = new User("carljohnsoncj@yandex.ru", "55555", "CJ");
        userClient.create(testUser);
    }

    @Test
    @DisplayName("Проверка создания заказа авторизованного пользователя")
    public void createOrderWithAuthorization() {
        testLoginUser = new LoginUser("carljohnsoncj@yandex.ru", "55555");
        Response responseLogin = userClient.login(testLoginUser);
        accessToken = responseLogin.as(UserAccessToken.class).getAccessToken();
        System.out.println("Access Token: " + accessToken);

        Response responseGetIngredient = orderClient.getIngredient();

        List<String> ingredients = new ArrayList<>(responseGetIngredient
                .then()
                .statusCode(SC_OK)
                .extract()
                .path("data._id"));

        Order order = new Order(ingredients.subList(fromIndex, toIndex));

        Response responseCreate = orderClient.createOrder(order, accessToken);

        if (statusCode == SC_OK) {
            responseCreate
                    .then()
                    .body("order._id", notNullValue())
                    .body("success", equalTo(true));
        } else if (statusCode == SC_BAD_REQUEST) {
            responseCreate
                    .then()
                    .body("success", equalTo(false))
                    .body("message", equalTo("Ingredient ids must be provided"));
        }
    }

    @Test
    @DisplayName("Проверка создания заказа не авторизованного пользователя")
    public void createOrderWithoutAuthorization(){
        Response responseGetIngredient = orderClient.getIngredient();

        List<String> ingredients = new ArrayList<>(responseGetIngredient
                .then()
                .statusCode(SC_OK)
                .extract()
                .path("data._id"));

        Order order = new Order(ingredients.subList(fromIndex, toIndex));

        Response responseCreate = orderClient.createOrderWithoutAuthorization(order);

        responseCreate
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @After
    public void deleteUser (){
        if (accessToken != null) {
            Response responseDelete = userClient.deleteUser (accessToken);
            responseDelete
                    .then()
                    .statusCode(SC_ACCEPTED)
                    .body("success", equalTo(true));
        }
    }
}