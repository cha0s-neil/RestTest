package com.rest.test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class MainTest {

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "http://localhost:8085"; // Correct port from documentation
    }

    // 1. Verify GET /books returns 200 OK with valid structure
    @Test
    public void verifyGetBooksEndpoint() {
        given()
                .auth().preemptive().basic("user", "password")
                .contentType(ContentType.JSON)
                .when()
                .get("/books")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(2))) // Ensures at least 2 books are returned
                .body("[0].id", notNullValue()) // Ensures each book has an ID
                .body("[0].name", notNullValue()) // Ensures each book has a name
                .body("[0].author", notNullValue()) // Ensures each book has an author
                .body("[0].price", notNullValue()); // Ensures each book has a price
    }


    // 2. Verify POST /books creates new resource
    @Test
    public void verifyBookCreation() {
        String bookJson = "{ \"name\": \"Clean Code\", \"author\": \"Santideva\", \"price\": 45.99 }";

        given()
                .auth().preemptive().basic("admin", "password")
                .contentType(ContentType.JSON)
                .body(bookJson)
                .when()
                .post("/books")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Clean Code"))
                .body("price", equalTo(45.99f));
    }

    // 3. Verify GET /books/{id} returns correct book
    @Test
    public void verifyGetBookById() {
        given()
                .auth().preemptive().basic("user", "password")
                .contentType(ContentType.JSON)
                .when()
                .get("/books/2")
                .then()
                .statusCode(200)
                .body("id", equalTo(2))
                .body("name", containsString("The Life-Changing Magic of Tidying Up"))
                .body("author", equalTo("Marie Kondo"));
    }

    // 4. Verify PUT /books/{id} updates resource
    @Test
    public void verifyBookUpdate() {
        String jsonString = "{ \"id\": 1, \"name\": \"A to the Bodhisattva Way of Life\", \"author\": \"Santideva\", \"price\": 15.41 }";

        given()
                .auth().preemptive().basic("admin", "password")
                .contentType(ContentType.JSON)
                .body(jsonString)
                .when()
                .put("/books/1")
                .then()
                .statusCode(200)
                .body("name", equalTo("A to the Bodhisattva Way of Life"));
    }

    // 5. Verify DELETE /books/{id} removes resource
    @Test
    public void verifyBookDeletion() {
        given()
                .auth().preemptive().basic("admin", "password")
                .contentType(ContentType.JSON)
                .when()
                .delete("/books/1")
                .then()
                .statusCode(500);

        // Verify deletion
        given()
                .auth().preemptive().basic("admin", "password")
                .when()
                .get("/books/1")
                .then()
                .statusCode(404);
    }

    // 6. Verify unauthorized access to protected endpoints
    @Test
    public void verifyUnauthorizedAccess() {
        given()
                .auth().preemptive().basic("invalid", "wrongpass")
                .contentType(ContentType.JSON)
                .when()
                .get("/books")
                .then()
                .statusCode(401);
    }

    // 7. Verify user role cannot create books
    @Test
    public void verifyUserRoleRestrictions() {
        String bookJson = "{ \"name\": \"Unauthorized Book\", \"price\": 10 }";

        given()
                .auth().preemptive().basic("user", "password")
                .contentType(ContentType.JSON)
                .body(bookJson)
                .when()
                .post("/books")
                .then()
                .statusCode(403);
    }

    // 8. Verify invalid book ID returns 404
    @Test
    public void verifyInvalidBookId() {
        given()
                .auth().preemptive().basic("admin", "password")
                .when()
                .get("/books/9999")
                .then()
                .statusCode(404);
    }

    // 9. Verify validation for required fields
    @Test
    public void verifyMissingRequiredFields() {
        String invalidJson = "{ \"author\": \"Incomplete Author\" }";

        given()
                .auth().preemptive().basic("admin", "password")
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post("/books")
                .then()
                .statusCode(400);
    }

    // 10. Verify invalid data type handling
    @Test
    public void verifyInvalidDataTypeHandling() {
        String invalidJson = "{ \"price\": \"forty-five\" }";

        given()
                .auth().preemptive().basic("admin", "password")
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .put("/books/1")
                .then()
                .statusCode(400);
    }
}