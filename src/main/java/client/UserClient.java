package client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.LoginUser;
import models.User;

import static io.restassured.RestAssured.given;

public class UserClient {
    private static final String BASE_URL = "/api/auth/user";
    private static final String REGISTER_URL = "/api/auth/register";
    private static final String LOGIN_URL = "/api/auth/login";

    @Step("Создание пользователя")
    public Response create(User user) {
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(user)
                .when()
                .post(REGISTER_URL);
    }

    @Step("Авторизация пользователя")
    public Response login(LoginUser loginUser) {
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(loginUser)
                .when()
                .post(LOGIN_URL);
    }

    @Step("Получить информацию о пользователе")
    public Response getUser(String token) {
        return given()
                .header("Authorization", token)
                .when()
                .get(BASE_URL);

    }

    @Step("Обновление данных авторизованного пользователя")
    public Response updateUser(User user, String token) {
        return given()
                .header("Authorization", token)
                .header("Content-type", "application/json")
                .and()
                .body(user)
                .when()
                .patch(BASE_URL);
    }

    @Step("Обновление данных пользователя без авторизации")
    public Response updateUserWithoutAuthorization(User user){
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(user)
                .when()
                .patch(BASE_URL);
    }

    @Step("Удаление пользователя")
    public Response deleteUser(String token){
        return given()
                .header("Authorization", token)
                .delete(BASE_URL);
    }
}
