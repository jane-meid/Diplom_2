package client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.Order;

import static io.restassured.RestAssured.given;

public class OrderClient {

    private static final String BASE_URL = "/api/orders";
    private static final String GET_URL = "/api/ingredients";

    @Step("Получить данные об ингредиентах")
    public Response getIngredient() {
        return given()
                .get(GET_URL);
    }

    @Step("Создать заказ с авторизацией")
    public Response createOrder(Order order, String token) {
        return given()
                .header("Authorization", token) // Устанавливаем токен авторизации
                .body(order)
                .when()
                .post(BASE_URL);
    }

    @Step("Создать заказ без авторизации")
    public Response createOrderWithoutAuthorization(Order order) {
        return given()
                .body(order)
                .when()
                .post(BASE_URL);
    }

    @Step("Получить заказы авторизованного пользователя")
    public Response getOrderUser(String token) {
        return given()
                .header("Authorization", token)
                .get(BASE_URL);
    }

    @Step("Получить заказы не авторизованного пользователя")
    public Response getOrderUserWithoutAuthorization() {
        return given()
                .get(BASE_URL);
    }
}