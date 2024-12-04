import client.OrderClient;
import client.RequestSpec;
import client.UserClient;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.LoginUser;
import models.User;
import models.UserAccessToken;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class GetOrderTest {
    private UserClient userClient;
    private OrderClient orderClient;
    private User testUser;
    private String accessToken;
    private LoginUser testLoginUser;

    @Before
    public void setUp() {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        orderClient = new OrderClient();
        userClient = new UserClient();
        testUser  = new User("carljohnsoncj@yandex.ru", "55555", "CJ");
        userClient.create(testUser);
    }

    @Test
    @DisplayName("Проверка получения заказов авторизованного пользователя")
    public void getOrderTest(){
        testLoginUser = new LoginUser("carljohnsoncj@yandex.ru", "55555");
        Response responseLogin = userClient.login(testLoginUser);
        accessToken = responseLogin.as(UserAccessToken.class).getAccessToken();
        System.out.println("Access Token: " + accessToken);

        Response responseGetOrder = orderClient.getOrderUser(accessToken);

        responseGetOrder
                .then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("orders.total", notNullValue());
    }

    @Test
    @DisplayName("Проверка получения заказов неавторизованного пользователя")
    public void getOrderWithoutAuthorization(){
        Response responseGetOrderWithoutAuthorization = orderClient.getOrderUserWithoutAuthorization();

        responseGetOrderWithoutAuthorization
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false)).
                body("message", equalTo("You should be authorised"));
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
