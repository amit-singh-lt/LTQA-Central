package utility;

import org.apache.logging.log4j.*;
import org.testng.asserts.SoftAssert;

/**
 * The CustomSoftAssert class extends SoftAssert to provide custom assertion functionality.
 */
public class CustomSoftAssert extends SoftAssert {
    private final Logger ltLogger = LogManager.getLogger(CustomSoftAssert.class);

    /**
     * Default constructor for the CustomSoftAssert class.
     * Initializes an instance of CustomSoftAssert with default settings.
     */
    public CustomSoftAssert() {
        super();
    }
}
