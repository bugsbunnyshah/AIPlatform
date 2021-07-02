package prototypes.largeProcessor;

import akka.actor.typed.ActorSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataProcessRunner {
  private static Logger logger = LoggerFactory.getLogger(DataProcessRunner.class);

  public static void main(String[] args)
  {
    final ActorSystem<ProcessData> system =
            ActorSystem.create(DataProcessor.create(3), "DataProcessor");

    system.tell(new ProcessData("{'blah0':'blah0'}"));
    system.tell(new ProcessData("{'blah1':'blah1'}"));
    system.tell(new ProcessData("{'blah2':'blah2'}"));
    system.tell(new ProcessData("{'blah3':'blah3'}"));
  }
}
