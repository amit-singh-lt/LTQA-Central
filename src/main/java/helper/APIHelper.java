package helper;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.*;

import java.util.Map;

/**
 * The APIHelper class provides methods for making HTTP requests using the RestAssured library.
 * It supports various HTTP methods and handles setting up request specifications, including headers,
 * query parameters, body, and content type. It also logs the details of the requests and responses.
 */
public class APIHelper {
    private final Logger ltLogger = LogManager.getLogger(APIHelper.class);

    /**
     * Default constructor for the APIHelper class.
     * Initializes an instance of the APIHelper with default settings.
     */
    public APIHelper() {
        super();
    }

    /**
     * Makes an HTTP request using the specified method, URI, body, content type, headers, and query parameters.
     * Logs the request details and verifies the response status code against the expected status.
     *
     * @param method          The HTTP method (GET, POST, PUT, DELETE, etc.).
     * @param uri             The URI of the API endpoint.
     * @param body            The request body as a string.
     * @param contentType     The content type of the request (e.g., JSON, XML).
     * @param headers         A map of headers to include in the request.
     * @param queryParam      A map of query parameters to include in the request.
     * @param expectedStatus  The expected HTTP status code of the response.
     * @return                The Response object containing the response data.
     */
    public Response httpMethod(String method,
                               String uri,
                               String body,
                               ContentType contentType,
                               Map<String, Object> headers,
                               Map<String, Object> queryParam,
                               int expectedStatus) {

        ltLogger.info("Hitting Method :- {} on URI :- {} with Body :- {}, Headers :- {}, Query Param :- {} and Content Type :- {} and Expected Status is :- {}",
                method, uri, body, headers, queryParam, contentType, expectedStatus);

        // Initialize the request specification
        RequestSpecification req = RestAssured.given();
        if (headers != null)
            req.headers(headers);
        if (queryParam != null)
            req.queryParams(queryParam);
        if (body != null) {
            req.body(body);
        }

        if (contentType != null) {
            req.contentType(contentType);
        }

        // Execute the request based on the method and verify the expected status code
        return switch (method) {
            case "GET" -> req.get(uri).then().statusCode(expectedStatus).extract().response();
            case "GET_REDIRECT" -> req.redirects().follow(false).get(uri).then().extract().response();
            case "GET_WITHOUT_STATUS_CODE_VERIFICATION" -> req.get(uri);
            case "POST" -> req.post(uri).then().statusCode(expectedStatus).extract().response();
            case "PUT" -> req.put(uri).then().statusCode(expectedStatus).extract().response();
            case "DELETE" -> req.delete(uri).then().statusCode(expectedStatus).extract().response();
            case "PATCH" -> req.patch(uri).then().statusCode(expectedStatus).extract().response();
            default -> null;
        };
    }
}
