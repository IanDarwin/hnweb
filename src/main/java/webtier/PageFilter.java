package webtier;

import java.io.IOException;
import java.io.InputStream;

import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Application;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.FacesContextFactory;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.lifecycle.LifecycleFactory;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequestWrapper;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import action.LoginAction;

import org.xml.sax.SAXException;

/**
 * Combined plain-login filter AND Simple PageView filter - the latter a tiny remnant of Seam2.
 * Only implements login-required and role-required. Sample page.xml:
 * <pre>
 * &lt;page xmlns="http://jboss.com/products/seam/pages"
 *       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *       xsi:schemaLocation="http://jboss.com/products/seam/pages http://jboss.com/products/seam/pages-2.1.xsd"
 *         login-required='true'>
 * 
 *         &lt;restrict>
 *                 #{identity.hasRole('admin')}
 *         &lt;/restrict>
 * 
 *         &lt;action execute="#{bugList.bug.setApproved(false)}"/>
 * 
 * &lt;/page>
 * </pre>
 *
 * @author Ian Darwin
 */

public class PageFilter implements Filter {
	
	private final boolean DEBUG = false;
	
	/** Communication between the parser and the mainline;
	 * WARNING we only look at two things in the page.xml:
	 * loginRequired attribute of the <page> root element;
	 * restriction - content of <restrict> element
	 */
	static class PageInfo {
		boolean loginRequired;	// From root element.
		String restriction;		// e.g., #{identity.hasRole('fool')}
		String action;			// NOT USED YET, will be from: action execute attribute
	}

	final static DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();

	@Override
	public void init(FilterConfig config) throws ServletException {
		System.out.println("PageFilter.init()");
	}

	/** Allow the request if the user is logged in and isn't blocked by <restrict>.
	 * Called before every request, so keep it light weight.
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {

		try {

			final HttpServletRequest request = (HttpServletRequest) req;
			final HttpServletResponse response = (HttpServletResponse) resp;
			final HttpSession session = request.getSession();
			final ServletContext servletContext = request.getServletContext();

			final String requestURI = request.getRequestURI();
			if (DEBUG) {
				System.out.printf("PageFilter.doFilter(): requestURI = %s; ", requestURI);
			}

			if (requestURI.indexOf("../") >= 0) { // Red Alert!
				System.out.println("Red Alert: Attacking URL is " + requestURI);
				throw new LoginFailureException("Not allowed");
			}
			
			LoginAction identity = (LoginAction)session.getAttribute(LoginConstants.LOGIN_FLAG);

			// These pages are available to anybody
			if ((identity == null || !identity.isLoggedIn()) && (
				requestURI.indexOf(LoginConstants.HOME_PAGE) != -1 ||
				requestURI.indexOf(LoginConstants.LOGIN_PAGE) != -1 ||
				requestURI.indexOf(LoginConstants.WEB_IMAGES_DIR) != -1 ||
				requestURI.indexOf(LoginConstants.WEB_TEMPLATE_DIR) != -1 ||
				requestURI.indexOf(LoginConstants.JSF_RESOURCE_DIR) != -1 ||
				requestURI.indexOf(LoginConstants.WEB_CSS_DIR) != -1 ||
				requestURI.indexOf(LoginConstants.WEB_JS_DIR) != -1 ||
				requestURI.indexOf(LoginConstants.FAVICON) != -1)) {
				// User not logged in and trying to log in
				if (DEBUG) {
					System.out.println("(allowing, from list)");
				}
				chain.doFilter(req, resp);
				return;
			}
			
			final String JSF_PAGE_EXT = ".web";		// XXX get from JSF
			if (requestURI.endsWith(JSF_PAGE_EXT)) {

				String pagePath = requestURI.replace(JSF_PAGE_EXT, ".page.xml");
				final String contextPath = servletContext.getContextPath();
				pagePath = pagePath.replace(contextPath, "");
				if (DEBUG) {
					System.out.println("Look for page.xml file as " + pagePath);
				}
				InputStream inputStrm = servletContext.getResourceAsStream(pagePath);
				if (inputStrm == null) {
					// No page.xml, no restrictions, so the request is allowed
					System.err.println("No page.xml " + pagePath + " for " + requestURI + " --> allow");
					chain.doFilter(req, resp);
					return;
				}
				System.out.println("FOUND .page.xml " + pagePath + " for " + requestURI);

				PageInfo pageInfo = parsePageFile(inputStrm);
				if (pageInfo.loginRequired && identity == null) {
					// User not logged in and trying unauthorized access
					// Save where the user was trying to get to:
					session.setAttribute(LoginConstants.TARGET_URI_KEY, requestURI);
					response.sendRedirect(contextPath + LoginConstants.LOGIN_PAGE);
					return;
				}
				FacesContext fContext = getFacesContext(request, response);
				if (pageInfo.restriction != null && !evalAsBool(fContext, pageInfo.restriction)) {
					response.sendRedirect(contextPath + LoginConstants.PAGE_NON_PRIV);
					return;
				}
			} 
			
		} catch (IOException | ServletException e) {
			throw new LoginFailureException(e.toString(), e);
		}

		// LAST CALL - If we get here instead of hitting a "return" above, ALLOW THE PAGE
		chain.doFilter(req, resp);				
	}
	
	/** Read the XML document */
	static PageInfo parsePageFile(InputStream is) {
		System.out.println("PageFilter.parsePageFile()");

		try {
		// Need to do the PageInfo get first, for testing
		PageInfo info = new PageInfo();
		
		// So the file exists, let's parse it.
		DocumentBuilder parser = fact.newDocumentBuilder();
		Document doc = parser.parse(is);
		
		// Get out the info-bits we need
		final String loginRequiredString = doc.getDocumentElement().getAttribute("login-required");
		info.loginRequired = Boolean.parseBoolean(loginRequiredString);
		NodeList nameElements = doc.getElementsByTagName("restrict");
		final Node restrictNode = nameElements.item(0);
		if (restrictNode != null) {
			info.restriction = restrictNode.getTextContent().trim();
		}
		System.out.println("Results: login req'd: " + info.loginRequired + "; restriction: " + info.restriction);
		// parser.close(); // Doesn't have any cleanup method.
		return info;
		} catch (IOException | ParserConfigurationException | SAXException ex) {
			throw new LoginFailureException("Failure in page.xml processing", ex);
		}
	}

	/**
	 * Evaluate the "restrict" string as a JSF expression yielding boolean.
	 * @author Stephan Rauh, http://www.beyondjava.net/blog/how-to-evaluate-jsf-expression-language-el-expressions-in-a-bean/
	 * @param context 
	 */
	private boolean evalAsBool(FacesContext context, String p_expression) {
		final Application application = context.getApplication();
		ExpressionFactory expressionFactory = application.getExpressionFactory();
		ELContext elContext = context.getELContext();
		ValueExpression vex = expressionFactory.createValueExpression(elContext, p_expression, Boolean.class);
		Boolean result = (Boolean) vex.getValue(elContext);
		return result;
	}

	@Override
	public void destroy() {
		System.out.println("PageFilter.destroy()");
	}
	
	/**
	 * Creates a FacesContext even though we're not in a JSF Component.
	 * There should be an easier way, and this may be inefficient, but... "Works for me"
	 * @author https://cwiki.apache.org/confluence/display/MYFACES/Access+FacesContext+From+Servlet
	 * @param request The Request object
	 * @param response The Response object
	 * @return A faked-up FacesContext.
	 */
    protected FacesContext getFacesContext(HttpServletRequest request, HttpServletResponse response) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
 
            FacesContextFactory contextFactory  = (FacesContextFactory)FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
            LifecycleFactory lifecycleFactory = (LifecycleFactory)FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
            Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
 
            facesContext = contextFactory.getFacesContext(request.getSession().getServletContext(), request, response, lifecycle);
 
            // Set using our inner class
            InnerFacesContext.setFacesContextAsCurrentInstance(facesContext);
 
            // set a new viewRoot, otherwise context.getViewRoot returns null
            UIViewRoot view = facesContext.getApplication().getViewHandler().createView(facesContext, "");
            facesContext.setViewRoot(view);               
        }
        return facesContext;
    }

    public void removeFacesContext() {
        InnerFacesContext.setFacesContextAsCurrentInstance(null);
    }

    // You need an inner class to be able to call FacesContext.setCurrentInstance
    // since it's a protected method
    private abstract static class InnerFacesContext extends FacesContext {
        protected static void setFacesContextAsCurrentInstance(FacesContext facesContext) {
            FacesContext.setCurrentInstance(facesContext);
        }
    }    



	/**
	 * Delegating HTTPServletRequest that just overrides a few methods such as getRemoteUser(), since
	 * we are handling our own login...
	 */
	public class LoginFilterHTTPServletRequest extends HttpServletRequestWrapper {

		private String userName;

		public LoginFilterHTTPServletRequest(HttpServletRequest request, String userName) {
			super(request);
			this.userName = userName;
		}

		@Override
		public String getRemoteUser() {
			return this.userName;
		}
	}
}
