package webtier;

public class LoginConstants {

	// Pages that we need to allow access to, and not store.
	public static final String HOME_PAGE = "/index.web";
	public static final String LOGIN_PAGE = "/Login.web";
	public static final String PAGE_403 = "/errors/403.jsp";
	public static final String PAGE_500 = "/errors/500.jsp";
	public static final String PAGE_NON_PRIV = "/errors/non-priv.jsp";
	public static final String WEB_IMAGES_DIR = "/images";
	public static final String WEB_TEMPLATE_DIR = "/template/";
	public static final String WEB_CSS_DIR = "/css/";
	public static final String WEB_JS_DIR = "/js/";
	public static final String JSF_RESOURCE_DIR = "/jakarta.faces.resource";
	public static final String FAVICON = "/favicon.ico";

	// Keys
	public static final String LOGIN_FLAG = "identity";
	public static final String TARGET_URI_KEY = "AFTER_LOGIN_GO_HERE";
}
