package action;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class RestApplication extends Application {
    // Empty but must exist.
    public RestApplication() {
        System.out.println("RestApplication::init");
    }
}

