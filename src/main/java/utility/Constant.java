package utility;

/**
 * The Constant class provides a set of static constants used throughout the application.
 * These constants include configurations for tunnels, common string values, HTTP request types, and more.
 */
public class Constant {

    /**
     * Default constructor for the Constant class.
     * Initializes an instance of Constant with default settings.
     */
    public Constant() {
        super();
    }

    // TUNNEL CONFIGURATIONS

    /**
     * The directory path for tunnel resources.
     */
    protected static final String TUNNEL_DIRECTORY = "./src/main/resources/tunnel/";

    /**
     * The directory path for tunnel log files.
     */
    protected static final String TUNNEL_LOG_DIRECTORY = "/logs/tunnel/";

    /**
     * The directory path for Mac-specific tunnel resources.
     */
    protected static final String TUNNEL_DIRECTORY_MAC = TUNNEL_DIRECTORY + "LT_Mac";

    /**
     * The directory path for Windows-specific tunnel resources.
     */
    protected static final String TUNNEL_DIRECTORY_WINDOWS = TUNNEL_DIRECTORY + "LT_Win";

    /**
     * The directory path for Linux-specific tunnel resources.
     */
    protected static final String TUNNEL_DIRECTORY_LINUX = TUNNEL_DIRECTORY + "LT_Linux";

    /**
     * Supported tunnel modes, such as "tcp" and "ssh".
     */
    protected static final String[] TUNNEL_MODES = new String[]{"tcp", "ssh"};


    // STRING AND BOOLEAN CONSTANTS

    /**
     * Newline character used for line breaks in text.
     */
    public static final String NEW_LINE = "\n";

    /**
     * Key for retrieving the operating system name property.
     */
    protected static final String OS_NAME = "os.name";

    /**
     * Key for retrieving the user directory property.
     */
    protected static final String USER_DIR = "user.dir";

    /**
     * String representation of a true boolean value.
     */
    protected static final String TRUE_STRING = "true";

    /**
     * String representation of a false boolean value.
     */
    protected static final String FALSE_STRING = "true";


    // HTTP REQUEST TYPES

    /**
     * HTTPS protocol prefix.
     */
    public static final String HTTPS = "https://";

    /**
     * HTTP protocol prefix.
     */
    public static final String HTTP = "http://";

    /**
     * HTTP GET method.
     */
    public static final String GET = "GET";

    /**
     * HTTP GET method with redirect handling.
     */
    public static final String GET_REDIRECT = "GET_REDIRECT";

    /**
     * HTTP GET method without status code verification.
     */
    public static final String GET_WITHOUT_STATUS_CODE_VERIFICATION = "GET_WITHOUT_STATUS_CODE_VERIFICATION";

    /**
     * HTTP POST method.
     */
    public static final String POST = "POST";

    /**
     * HTTP PUT method.
     */
    public static final String PUT = "PUT";

    /**
     * HTTP PATCH method.
     */
    public static final String PATCH = "PATCH";

    /**
     * HTTP DELETE method.
     */
    public static final String DELETE = "DELETE";

    /**
     * HTTP header for authorization.
     */
    public static final String AUTHORIZATION = "Authorization";
}
