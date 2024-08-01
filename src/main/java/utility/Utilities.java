package utility;

import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

/**
 * The Utilities class provides a collection of common utility methods that can be used across the application.
 * This includes methods for retry logic, encoding credentials, generating random values, and networking utilities.
 */
public class Utilities {
    private static final Logger ltLogger = LogManager.getLogger(Utilities.class);

    /**
     * Default constructor for the Utilities class.
     * Initializes an instance of Utilities, providing a base for utility methods and properties
     * that can be used throughout the application.
     */
    public Utilities() {
        super();
    }

    /**
     * Retries the execution of a Runnable up to a specified number of times in case of failure.
     *
     * @param maxRetriesOnFailure The maximum number of retry attempts.
     * @param r                   The Runnable to execute.
     * @return true if the execution was successful within the allowed attempts, false otherwise.
     */
    public boolean retry(int maxRetriesOnFailure, Runnable r) {
        int tries = 0;
        while (tries < maxRetriesOnFailure) {
            try {
                ltLogger.info("TRIES {}/{}", tries + 1, maxRetriesOnFailure);
                r.run();
                return true;
            } catch (Exception e) {
                ltLogger.error("Exception :- {}", e.toString());
                tries++;
            }
        }
        return false;
    }


    /**
     * Generates a Base64 encoded authentication token using the provided username and password.
     *
     * @param userName The username.
     * @param passWord The password.
     * @return A Base64 encoded string representing the credentials.
     */
    public String generateBase64EncodedAuthToken(String userName, String passWord) {
        return Base64.getEncoder().encodeToString((userName + ":" + passWord).getBytes());
    }


    /**
     * Creates a new instance of Random.
     *
     * @return A new Random object.
     */
    public Random newRandom() {
        return new Random();
    }


    /**
     * Generates a random alphanumeric string of the specified size.
     *
     * @param size The desired length of the random string.
     * @return A random alphanumeric string of the given size.
     */
    public String getRandomAlphaNumericString(int size) {
        byte[] byteArray = new byte[256];
        newRandom().nextBytes(byteArray);
        String randomStr = new String(byteArray, StandardCharsets.UTF_8);
        StringBuilder strBuilder = new StringBuilder();
        String alphaNumericStr = randomStr.replaceAll("[^A-Za-z0-9]", "");
        for (int i = 0; i < alphaNumericStr.length(); i++) {
            if (size > 0 && (Character.isLetter(alphaNumericStr.charAt(i)) || Character.isDigit(alphaNumericStr.charAt(i)))) {
                strBuilder.append(alphaNumericStr.charAt(i));
            }
            size--;
        }
        return strBuilder.toString();
    }


    /**
     * Finds and returns an open port number available on the host machine.
     *
     * @return The open port number as a string.
     */
    @SneakyThrows
    public String getOpenPort() {
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            int port = serverSocket.getLocalPort();
            serverSocket.close();
            return String.valueOf(port);
        } catch (IOException e) {
            ltLogger.error("Failed to get an open port :- %s", e);
            throw new IOException(e);
        }
    }
}
