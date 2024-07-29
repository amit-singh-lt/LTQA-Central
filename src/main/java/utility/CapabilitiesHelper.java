package utility;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The CapabilitiesHelper class assists in setting up and managing capabilities
 * for Selenium WebDriver instances. This class provides methods to configure
 * browser capabilities such as browser type, version, platform, and more.
 * It extends the WaitConstant class, allowing the use of predefined wait time constants.
 */
public class CapabilitiesHelper extends WaitConstant {
    private final Logger ltLogger = LogManager.getLogger(Capabilities.class);

    URI uri;
    String userName;
    String accessKey;
    String gridUrl;
    Map<String, Object> ltOptions = new HashMap<>();
    DesiredCapabilities dc = new DesiredCapabilities();

    /**
     * Default constructor for the CapabilitiesHelper class.
     * Initializes an instance of CapabilitiesHelper with default settings.
     */
    public CapabilitiesHelper() {
        super();
    }


    /**
     * Constructs a CapabilitiesHelper object with the specified credentials and URL.
     *
     * @param userName The username for authentication.
     * @param accessKey The access key for authentication.
     * @param gridUrl The URL of the Selenium grid.
     */
    public CapabilitiesHelper(String userName, String accessKey, String gridUrl) {
        this.userName = userName;
        this.accessKey = accessKey;
        this.gridUrl = gridUrl;
    }


    /**
     * Converts a comma-separated list of key=value pairs into a HashMap.
     *
     * @param capabilitiesArrayPair the comma-separated list of key=value pairs
     * @return a Map containing the key-value pairs, or an empty map if the input is null or empty
     */
    public Map<String, Object> getHashMapFromString(String[] capabilitiesArrayPair) {
        for (String pair : capabilitiesArrayPair) {
            String[] keyValue = pair.split("=");
            if (keyValue.length < 2) {
                ltLogger.warn("Either key or Value is missing and the length is !2, hence skipping this iteration");
                continue;
            }

            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            if (TRUE_STRING.equalsIgnoreCase(value) || FALSE_STRING.equalsIgnoreCase(value)) {
                ltOptions.put(key, Boolean.parseBoolean(value)); // boolean capabilities
            } else {
                ltOptions.put(key, value); // others
            }
        }

        return ltOptions;
    }


    /**
     * Appends dynamic capabilities from system properties to the provided capabilities string.
     *
     * @param capabilities the initial capabilities as a comma-separated list of key=value pairs
     * @return a Map containing the combined key-value pairs from the input capabilities and system properties
     */
    public Map<String, Object> appendDynamicCapability(String capabilities) {
        String cliCaps = System.getProperty("CAPS", "");
        if (cliCaps.contains("=")) {
            ltLogger.info("Caps passed from CLI :- {}", cliCaps);
            capabilities = capabilities + ";" + cliCaps;
        } else {
            if (capabilities == null || capabilities.isEmpty()) {
                ltLogger.warn("userCapability received in parameter is null, and returning empty lt:options response.");
                return Collections.emptyMap();
            }
        }
        return getHashMapFromString(capabilities.split(";"));
    }


    /**
     * Handles exceptions that occur during driver creation.
     * Logs the error, sets the driver creation time to -1, and rethrows the exception.
     *
     * @param e The exception that occurred during driver creation.
     * @throws Exception The rethrown exception with additional context information.
     */
    private void handleDriverCreationException(Exception e) throws Exception {
        ltLogger.error("[DRIVER CREATION ERROR] Driver was not created");
        ltLogger.error(e);
        throw new Exception("[DRIVER CREATION ERROR] Driver was not created" + NEW_LINE + "Exception :- " + e + NEW_LINE + "Capabilities :- " + ltOptions + NEW_LINE);
    }


    /**
     * Creates a RemoteWebDriver instance with the specified capabilities.
     * Logs the capabilities, constructs the URI, measures driver creation time, and sets up implicit wait timeout.
     *
     * @param mapCapabilities A map of capabilities to be set for the driver.
     * @return A RemoteWebDriver instance or null if an exception occurs.
     * @throws Exception If driver creation fails, the exception is handled and rethrown.
     */
    public RemoteWebDriver driverCreate(Map<String, Object> mapCapabilities) throws Exception {
        ltLogger.info("lt:options :- {}", mapCapabilities);
        dc.setCapability("lt:options", mapCapabilities);
        ltLogger.info("Capabilities :- {}", dc);
        try {
            uri = new URI(HTTPS + userName + ":" + accessKey + "@" + gridUrl);
            ltLogger.info("URI :- {}", uri);
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            RemoteWebDriver testDriver = new RemoteWebDriver(uri.toURL(), dc);
            stopWatch.stop();
            int driverCreationTime = (int) stopWatch.getTime();
            ltLogger.info("Driver Creation Time :- {} seconds", driverCreationTime);
            String sessionId = testDriver.getSessionId().toString();
            ltLogger.info("Session ID :- {}", sessionId);
            testDriver.manage().deleteAllCookies();
            testDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(SHORT_WAIT_TIME));
            testDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(LONG_WAIT_TIME));
            String platformName = ltOptions.get("platformName").toString();
            if(!(platformName.equalsIgnoreCase("android") || platformName.equalsIgnoreCase("ios"))) {
                testDriver.manage().window().maximize();
            }
            return testDriver;
        } catch (Exception e) {
            handleDriverCreationException(e);
        }
        return null;
    }


    /**
     * Quits the WebDriver session if it is not null.
     * Logs the appropriate message based on whether the driver was successfully closed or if it was null.
     *
     * @param driver The RemoteWebDriver instance to be closed.
     */
    public void quitDriver(RemoteWebDriver driver) {
        if (driver != null) {
            driver.quit();
            ltLogger.info("Driver closed");
        } else {
            ltLogger.error("Driver object received is null");
        }
    }
}
