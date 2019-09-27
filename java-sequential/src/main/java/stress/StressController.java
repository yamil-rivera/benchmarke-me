package stress;

import org.rapidoid.annotation.GET;
import org.rapidoid.annotation.Controller;
import org.rapidoid.log.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.Random;

import static stress.Configuration.jdbcClient;

@Controller
public class StressController {

  private static final int NUMBER_OF_HASHES_PER_OPERATION = 1000;
  private static final short NUMBER_OF_BYTES_IN_SHA256_BLOCK = (256/8);

  @GET("/stress/{loopCount}")
  public String stress(int loopCount) {

    for (int i = 0; i < loopCount; i++) {
    	ioWorkOut(cpuWorkOut());
    }
    return "I worked out " + loopCount + " times!";

  }

  private byte[] cpuWorkOut() {
	  byte[] token = new byte[NUMBER_OF_BYTES_IN_SHA256_BLOCK];
	  new Random().nextBytes(token);
	  try {
		  for (int j = 0; j < NUMBER_OF_HASHES_PER_OPERATION; j++) {
			  MessageDigest digest = MessageDigest.getInstance("SHA-256");
			  token = digest.digest(token);
		  }
	  } catch (NoSuchAlgorithmException ex) {

	  	Log.error("NoSuchAlgorithmException: " + ex.getMessage());
	  }
	  return token;
  }

  private void ioWorkOut(byte[] hashedToken) {
	  String encodedToken = Base64.getEncoder().encodeToString(hashedToken);
	  try (Connection dbConnection = jdbcClient.getConnection();
		   Statement statement = dbConnection.createStatement()) {
		  statement.executeUpdate(
				  "INSERT INTO tokens(encoded_token) VALUES ('" + encodedToken + "');");
	  } catch (SQLException ex) {
		  Log.error("SQLException: " + ex.getMessage());
	  }
  }

}
