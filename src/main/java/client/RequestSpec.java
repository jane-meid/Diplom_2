package client;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;


public class RequestSpec {

    private static final String baseURI = "https://stellarburgers.nomoreparties.site";

    public static RequestSpecification requestSpecification() {

        return given()
                .log()
                .all()
                .contentType(ContentType.JSON)
                .baseUri(baseURI);
    }
}
