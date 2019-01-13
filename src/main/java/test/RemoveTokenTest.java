package test;

import helper.TokenData;
import oauth2.TokenStorageDatabase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class RemoveTokenTest {

    @InjectMocks
    private TokenStorageDatabase database;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void addNewTokensThenTryToRemoveIt() {
        TokenData tokenData = new TokenData();
        tokenData.setAccessToken("accessToken");
        tokenData.setAccessTokenCreationTime(System.currentTimeMillis());
        tokenData.setRefreshToken("refreshToken");
        tokenData.setRefreshTokenCreationTime(System.currentTimeMillis());
        database.addToken(tokenData);

        assertTrue(database.isValidToken("accessToken"));

        database.removeTokenData("accessToken");

        assertFalse(database.isValidToken("accessToken"));
    }
}
