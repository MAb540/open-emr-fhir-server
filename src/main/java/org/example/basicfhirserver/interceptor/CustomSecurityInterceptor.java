package org.example.basicfhirserver.interceptor;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomSecurityInterceptor {

    // This hook runs the moment the servlet receives the request bytes
    @Hook(Pointcut.SERVER_INCOMING_REQUEST_POST_PROCESSED)
    public boolean incomingRequestBarrier(RequestDetails theRequestDetails,
                                          HttpServletRequest theServletRequest,
                                          HttpServletResponse theServletResponse) {

        // Read a raw cookie natively
        if (theServletRequest.getCookies() != null) {
            // Loop and process your cookies here
        }

        // Enforce global authorization rule
        String apiKey = theRequestDetails.getHeader("X-API-Key");
        System.out.println("apiKey  " + apiKey);
//        if (apiKey == null || !apiKey.equals("secret-key-123")) {
//            // HAPI automatically maps this exception to a clean FHIR HTTP 401 Unauthorized
//            throw new AuthenticationException("Missing or invalid X-API-Key header.");
//        }

        // Inject a tracking tracking cookie globally into the response header
        theServletResponse.addHeader("Set-Cookie", "server_session=active; Path=/; HttpOnly");

        return true; // Return true to let the request continue down to the provider layer
    }
}