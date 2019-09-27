package stress;

import org.rapidoid.annotation.GET;
import org.rapidoid.annotation.Controller;
import org.rapidoid.ctx.Contextual;
import org.rapidoid.http.Req;
import org.rapidoid.log.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.*;
import java.util.Arrays;

import static stress.Configuration.jdbcClient;

@Controller
public class StressController {

  private BlockingQueue<byte[]> processingQueue;
  private static final int NUMBER_OF_HASHES_PER_OPERATION = 1000;
  private static final short NUMBER_OF_BYTES_IN_SHA256_BLOCK = (256/8);
  private static final byte[] POISON_PILL = new byte[] {0, 0, 0, 0, 0, 0, 0, 0,
		  										 0, 0, 0, 0, 0, 0, 0, 0,
		  										 0, 0, 0, 0, 0, 0, 0, 0,
  												 0, 0, 0, 0, 0, 0, 0, 0};

  @GET("/stress/{loopCount}")
  public String stress(int loopCount) {

  	Log.error("---------- STARTING ----------");

  	Req req = Contextual.request();
  	Log.error(req.sessionId());

    processingQueue = new LinkedBlockingDeque<>();

    ArrayList<CompletableFuture<Void>> cpuWorkOutList = new ArrayList<>();
    for (int i = 0; i < loopCount; i++) {
	    CompletableFuture<Void> cpuWorkOutFuture = CompletableFuture.runAsync(cpuWorkOut);
        cpuWorkOutList.add(cpuWorkOutFuture);
    }
    Log.error("joining cpu");
    for (CompletableFuture<Void> future : cpuWorkOutList) {
		future.join();
	}
	try {
		Log.error("poisoning the queue");
		processingQueue.put(POISON_PILL);
		Log.error("queue poisoned");
	} catch (InterruptedException ex) {
		Log.error("InterruptedException: " + ex.getMessage());
	}

	ArrayList<CompletableFuture<Void>> ioWorkOutList = new ArrayList<>();
    for (int i = 0; i < loopCount; i++) {
      CompletableFuture<Void> ioWorkOutFuture =
          CompletableFuture.runAsync(ioWorkOut, Configuration.ioExecutor);
      ioWorkOutList.add(ioWorkOutFuture);
    }

    Log.error("joining io");
    for (CompletableFuture<Void> future : ioWorkOutList) {
    	future.join();
    }

    Log.error("---------- RETURNING ----------");
    return "I worked out " + loopCount + " times!";
  }

  private Runnable cpuWorkOut = () -> {
	  byte[] token = new byte[NUMBER_OF_BYTES_IN_SHA256_BLOCK];
	  new Random().nextBytes(token);
	  try {
		  for (int j = 0; j < NUMBER_OF_HASHES_PER_OPERATION; j++) {
			  MessageDigest digest = MessageDigest.getInstance("SHA-256");
			  token = digest.digest(token);
		  }
		  Log.error("putting into queue");
		  processingQueue.put(token);
		  Log.error("put into queue");
	  } catch (NoSuchAlgorithmException ex) {
		  Log.error("NoSuchAlgorithmException: " + ex.getMessage());
	  } catch (InterruptedException ex) {
		  Log.error("InterruptedException: " + ex.getMessage());
	  }
  };

  private Runnable ioWorkOut =
  () -> {
		byte[] hashedToken = null;
		try {
			Log.error("getting from queue");
			hashedToken = processingQueue.take();
			Log.error("gotten from queue");
			if (Arrays.equals(hashedToken, POISON_PILL)) {
				return;
			}
		} catch (InterruptedException ex) {
			Log.error("InterruptedException: " + ex.getMessage());
		}
		String encodedToken = Base64.getEncoder().encodeToString(hashedToken);
		Log.error("locking");
		synchronized (this) {
			Log.error("locked");
			try (Connection dbConnection = jdbcClient.getConnection();
			  Statement statement = dbConnection.createStatement()) {
			  statement.executeUpdate(
				"INSERT INTO tokens(encoded_token) VALUES ('" + encodedToken + "');");

		} catch (SQLException ex) {
			Log.error("SQLException: " + ex.getMessage());
		}
		Log.error("Unlock");
	  }
	  Log.error("Unlocked");
  };
}
