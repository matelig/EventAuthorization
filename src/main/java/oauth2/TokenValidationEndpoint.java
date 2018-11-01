package oauth2;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("/validation")
public class TokenValidationEndpoint {

    @Inject
    private TokenStorageDatabase database;

    @GET
    @Produces("text/html")
    @Path("/accessToken")
    public Response validateAccessToken(@Context HttpServletRequest request) throws OAuthSystemException{

        try {
            OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest(request, ParameterStyle.HEADER);

            String token = oauthRequest.getAccessToken();

            if (!database.isValidToken(token)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            return Response.status(Response.Status.OK).build();

        } catch (OAuthProblemException e) {
            String errorCode = e.getError();
            if (OAuthUtils.isEmpty(errorCode)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        }
    }
}
