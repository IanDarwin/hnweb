package webtier;

import java.io.IOException;
import java.io.InputStream;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import action.LoginAction;

/**
 * Simple Page View filter - adapts a tiny bit of Seam2
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

public class PageViewFilter implements Filter {
	
	private final boolean DEBUG = false;
	
	/** Communication between the parser and the mainline */
	static class PageInfo {
		boolean loginRequired;
		String restriction;
	}
	final static ThreadLocal<PageInfo> tl = new ThreadLocal<>();
	
	final static DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();

	@Override
	public void init(FilterConfig config) throws ServletException {
		System.out.println("PageViewFilter.init()");
	}

	/** Allow the request if the user is logged in and isn't blocked by <restrict>.
	 * Called before every request, so keep it light weight.
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain filterChain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		final ServletContext servletContext = request.getServletContext();

		final String requestURI = request.getRequestURI();
		if (DEBUG) {
			System.out.printf("PageViewFilter.doFilter(): requestURI = %s; ", requestURI);
		}

		if (requestURI.indexOf("../")) { // Red Alert!
			System.out.println("Red Alert: Attacking URL is " + requestURI);
			throw new SecurityException("Not allowed");
		}
		
		try {
			final HttpSession session = request.getSession();
			// These are asked for by JSF frameworks.
			if (requestURI.startsWith("/javax.faces.resource/")) {
				filterChain.doFilter(req, resp);
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
					// No page.xml, so the request is allowed
					System.err.println("No page.xml " + pagePath + " for " + requestURI + " --> allow");
					filterChain.doFilter(req, resp);
					return;
				} else {
					System.err.println("FOUND .page.xml " + pagePath + " for " + requestURI + " --> allow");
				}

				try {
					process(inputStrm);	// Side effect: calls tl.set() with a new PageInfo.
				} catch (Exception e) {
					e.printStackTrace();
					response.sendRedirect(contextPath + LoginConstants.PAGE_500);
				}
				// identity may be null!
				LoginAction identity = (LoginAction)session.getAttribute(LoginConstants.LOGIN_FLAG);

				PageInfo pageInfo = tl.get();
				if (pageInfo.loginRequired && identity == null) {
					response.sendRedirect(contextPath + LoginConstants.LOGIN_PAGE);
					return;
				}
				FacesContext fContext = getFacesContext(request, response);
				if (pageInfo.restriction != null && !evalAsBool(fContext, pageInfo.restriction)) {
					response.sendRedirect(contextPath + LoginConstants.PAGE_NON_PRIV);
					return;
				}
			} 
			
		} finally {
			// set this empty after use!
			tl.set(null);
		}
		filterChain.doFilter(req, resp);				
	}
	
	/** Read the XML document */
	static void process(InputStream is) throws Exception {
		System.out.println("PageViewFilter.process()");
		// Need to do the PageInfo get first, for testing
		PageInfo info = new PageInfo();
		tl.set(info);
		
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
		System.out.println("PageViewFilter.destroy()");
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

}
