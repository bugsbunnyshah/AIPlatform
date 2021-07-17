package prototypes.largeProcessor;

import akka.actor.typed.ActorSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DataProcessRunner {
  private static Logger logger = LoggerFactory.getLogger(DataProcessRunner.class);

  public static void main(String[] args)
  {
    final ActorSystem<ProcessData> system =
            ActorSystem.create(DataProcessor.create(3), "DataProcessor");

    Thread[] threads = new Thread[10];
    for(int i=0; i<10; i++) {
      Thread t = new Thread(() -> {
        String threadName = "THREAD//"+ UUID.randomUUID();
        system.tell(new ProcessData("{\""+threadName+"\":'blah0'}"));
        system.tell(new ProcessData("{\""+threadName+"\":'blah1'}"));
        system.tell(new ProcessData("{\""+threadName+"\":'blah2'}"));
        system.tell(new ProcessData("{\""+threadName+"\":'blah3'}"));
      });
      threads[i] = t;
    }

    int counter =1;
    for(Thread t:threads){
      try {
        Thread.sleep(counter*100);
      }
      catch (Exception e){
        e.printStackTrace();
      }
      t.start();

      counter++;
    }
  }
}
