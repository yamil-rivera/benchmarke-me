package stress;

import org.rapidoid.setup.App;
import org.rapidoid.setup.On;

public class Main {

  private static final int serverPort = 8080;

  public static void main(String[] args) {
  	App.bootstrap(args);
  	On.address("0.0.0.0").port(serverPort);
  }

}
