package example.cards;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

import java.io.File;

/**
 * Application which starts up a Jetty container.
 */
public class Application {
    public static void main(String[] args) throws Exception {
        System.out.println("Running Jetty server...");
        startJetty(8080, "jetty");
        System.out.println("Jetty server started...");
    }

    public static Server startJetty(int port, String relativeResourceBase) throws Exception {
        Server server = new Server(port);
        WebAppContext context = new WebAppContext();
        context.addServlet(HttpServletDispatcher.class, "/*");
        context.addEventListener(new GuiceResteasyBootstrapServletContextListener());
        context.setResourceBase(new File(relativeResourceBase).getAbsolutePath());
        context.setInitParameter("resteasy.guice.modules", ServiceModule.class.getName());
        context.setServer(server);
        server.setHandler(context);
        server.start();
        return server;
    }
}
