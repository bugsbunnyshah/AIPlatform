package io.bugsbunny;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.bugsbunny.dataIngestion.service.StreamIngester;
import io.bugsbunny.dataIngestion.service.StreamIngesterContext;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class Braineous {
    public static void main(String[] args) throws Exception
    {
        System.out.println("Braineous Started...");
        System.out.println("*******************************");
        System.out.println("STARTING_INGESTION");
        System.out.println("*******************************");
        JsonArray array = new JsonArray();
        array.add(new JsonObject());
        StreamIngesterContext.getStreamIngester().submit(null,null,array);


        Quarkus.run(args);
    }
}
