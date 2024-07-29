package utility;

/**
 * The Selectors class defines a set of constants representing common selector strategies
 * used for locating elements in a web page.
 */
public class Selectors {
    /**
     * Default constructor for the Selectors class.
     * Initializes an instance of Selectors with default settings.
     */
    public Selectors() {
        super();
    }

    /**
     * Selector strategy for locating elements by their class attribute.
     */
    public static final String CLASS = "class";

    /**
     * Selector strategy for locating elements by their unique ID attribute.
     */
    public static final String ID = "id";

    /**
     * Selector strategy for locating elements using CSS selectors.
     */
    public static final String CSS = "css";

    /**
     * Selector strategy for locating elements using XPath expressions.
     */
    public static final String XPATH = "xpath";

    /**
     * Selector strategy for locating elements by their name attribute.
     */
    public static final String NAME = "name";

    /**
     * Selector strategy for locating elements by their tag name.
     */
    public static final String TAGNAME = "tagname";
}
