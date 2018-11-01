package helper;

import io.jsonwebtoken.Jwts;
import javax.enterprise.context.ApplicationScoped;
import java.util.Date;

@ApplicationScoped
public class KeysCoder {
    public static KeysCoder shared = new KeysCoder();

    private KeysCoder() {}

    public String generateAccessToken(String username) {
        Date expirationDate = new Date(System.currentTimeMillis()+Common.accessTokenExpirationTime);
        //should also contain clientSecret ?
        return  Jwts.builder().setExpiration(expirationDate).setSubject(username).signWith(Common.key).compact();
    }

    public String generateRefreshToken() {
        Date expirationDate = new Date(System.currentTimeMillis()+Common.refreshTokenExpirationTime);
        return  Jwts.builder().setExpiration(expirationDate).signWith(Common.key).compact();
    }

    public String decodeAccessToken(String jwtToken) {
        return Jwts.parser().setSigningKey(Common.key).parseClaimsJws(jwtToken).getBody().getSubject(); // should return user email
    }
}
