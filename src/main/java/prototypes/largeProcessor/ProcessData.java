package prototypes.largeProcessor;

import akka.actor.typed.ActorRef;

public class ProcessData {
    private ActorRef<ProcessData> caller;
    private String jsonData;


    public ProcessData(String jsonData) {
        this.jsonData = jsonData;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    public void setCaller(ActorRef<ProcessData> caller) {
        this.caller = caller;
    }
}
