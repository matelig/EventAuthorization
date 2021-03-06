package oauth2;

import helper.Common;
import helper.KeysCoder;
import helper.TokenData;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("/accessToken")
public class AccessTokenEndpoint {

    @Inject
    private TokenStorageDatabase database;

    public static final String INVALID_CLIENT_DESCRIPTION = "Client authentication failed (e.g., unknown client, no client authentication included, or unsupported authentication method).";

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response authorize(@Context HttpServletRequest request) throws OAuthSystemException {
        try {
            OAuthTokenRequest oauthRequest = new OAuthTokenRequest(request);

            if (!checkClientId(oauthRequest.getClientId())) {
                return buildInvalidClientIdResponse();
            }

            if (!checkClientSecret(oauthRequest.getClientSecret())) {
                return buildInvalidClientSecretResponse();
            }

            if (!oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.PASSWORD.toString())) {
                buildInvalidUserPassResponse();
            }

            TokenData tokenData = new TokenData();
            Long currentDateTime = System.currentTimeMillis();
            tokenData.setAccessToken(KeysCoder.shared.generateAccessToken(oauthRequest.getUsername()));
            tokenData.setRefreshToken(KeysCoder.shared.generateRefreshToken());
            tokenData.setAccessTokenCreationTime(currentDateTime);
            tokenData.setRefreshTokenCreationTime(currentDateTime);
            database.addToken(tokenData);

            OAuthResponse response = OAuthASResponse
                    .tokenResponse(HttpServletResponse.SC_OK)
                    .setAccessToken(tokenData.getAccessToken())
                    .setRefreshToken(tokenData.getRefreshToken())
                    .setTokenType("Bearer")
                    .setExpiresIn("3600")
                    .buildJSONMessage();
            return Response.status(response.getResponseStatus()).entity(response.getBody()).build();

        } catch (OAuthProblemException e) {
            OAuthResponse res = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).error(e)
                    .buildJSONMessage();
            return Response.status(res.getResponseStatus()).entity(res.getBody()).build();
        }
    }

    private Response buildInvalidClientIdResponse() throws OAuthSystemException {
        OAuthResponse response =
                OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT)
                        .setErrorDescription(INVALID_CLIENT_DESCRIPTION)
                        .buildJSONMessage();
        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
    }

    private Response buildInvalidClientSecretResponse() throws OAuthSystemException {
        OAuthResponse response =
                OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                        .setError(OAuthError.TokenResponse.UNAUTHORIZED_CLIENT).setErrorDescription(INVALID_CLIENT_DESCRIPTION)
                        .buildJSONMessage();
        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
    }

    private Response buildBadAuthCodeResponse() throws OAuthSystemException {
        OAuthResponse response = OAuthASResponse
                .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                .setError(OAuthError.TokenResponse.INVALID_GRANT)
                .setErrorDescription("invalid authorization code")
                .buildJSONMessage();
        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
    }

    private Response buildInvalidUserPassResponse() throws OAuthSystemException {
        OAuthResponse response = OAuthASResponse
                .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                .setError(OAuthError.TokenResponse.INVALID_GRANT)
                .setErrorDescription("invalid username or password")
                .buildJSONMessage();
        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
    }

    private boolean checkClientId(String clientId) {
        return true;
    }

    private boolean checkClientSecret(String secret) {
        return true;
    }

    private boolean checkAuthCode(String authCode) {
        //return database.isValidAuthCode(authCode);
        return true;
    }

    private boolean checkUserPass(String user, String pass) {
        return Common.PASSWORD.equals(pass) && Common.USERNAME.equals(user);
    }
}
