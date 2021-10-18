package prototype.infrastructure;

import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.util.JsonUtil;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import javax.xml.bind.DatatypeConverter;

public class DataHistory {

    @Test
    public void detectUpdatesAll() throws Exception
    {
        Map<Integer,String> oids = new HashMap<>();
        oids.put(0,UUID.randomUUID().toString());
        oids.put(1,UUID.randomUUID().toString());
        oids.put(2,UUID.randomUUID().toString());

        JsonArray ingestion1 = new JsonArray();
        OffsetDateTime ingestion1Time = OffsetDateTime.now();
        for(int i=0; i<3; i++){
            JsonObject data = new JsonObject();
            ingestion1.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion1Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion1);

        JsonArray ingestion2 = new JsonArray();
        OffsetDateTime ingestion2Time = OffsetDateTime.now();
        ingestion2Time = ingestion2Time.plus(5, ChronoUnit.MINUTES);
        for(int i=0; i<3; i++){
            JsonObject data = new JsonObject();
            ingestion2.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion2Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion2);

        System.out.println(this.detectUpdates(ingestion1,ingestion2));
    }

    @Test
    public void detectUpdatesSubset() throws Exception
    {
        Map<Integer,String> oids = new HashMap<>();
        oids.put(0,UUID.randomUUID().toString());
        oids.put(1,UUID.randomUUID().toString());
        oids.put(2,UUID.randomUUID().toString());

        JsonArray ingestion1 = new JsonArray();
        OffsetDateTime ingestion1Time = OffsetDateTime.now();
        for(int i=0; i<3; i++){
            JsonObject data = new JsonObject();
            ingestion1.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion1Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion1);

        JsonArray ingestion2 = new JsonArray();
        OffsetDateTime ingestion2Time = OffsetDateTime.now();
        ingestion2Time = ingestion2Time.plus(5, ChronoUnit.MINUTES);
        for(int i=0; i<3; i++){
            JsonObject data = new JsonObject();
            if(i == 0) {
                ingestion2.add(ingestion1.get(0));
            }
            else
            {
                ingestion2.add(data);
                data.addProperty("oid",oids.get(i));
                data.addProperty("1", UUID.randomUUID().toString());
                data.addProperty("2",UUID.randomUUID().toString());
                data.addProperty("3", UUID.randomUUID().toString());
                String objectHash = this.getJsonHash(data);
                data.addProperty("timestamp",ingestion2Time.toEpochSecond());
                data.addProperty("objectHash",objectHash);
            }
        }
        JsonUtil.print(ingestion2);

        System.out.println(this.detectUpdates(ingestion1,ingestion2));
    }

    @Test
    public void detectUpdatesSubsetShuffle() throws Exception
    {
        Map<Integer,String> oids = new HashMap<>();
        oids.put(0,UUID.randomUUID().toString());
        oids.put(1,UUID.randomUUID().toString());
        oids.put(2,UUID.randomUUID().toString());

        JsonArray ingestion1 = new JsonArray();
        OffsetDateTime ingestion1Time = OffsetDateTime.now();
        for(int i=0; i<3; i++){
            JsonObject data = new JsonObject();
            ingestion1.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion1Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion1);

        JsonArray ingestion2 = new JsonArray();
        OffsetDateTime ingestion2Time = OffsetDateTime.now();
        ingestion2Time = ingestion2Time.plus(5, ChronoUnit.MINUTES);
        for(int i=0; i<3; i++){
            JsonObject data = new JsonObject();
            if(i == 0) {
                ingestion2.add(data);
                data.addProperty("oid",oids.get(i));
                data.addProperty("2", ingestion1.get(0).getAsJsonObject().get("2").getAsString());
                data.addProperty("3",ingestion1.get(0).getAsJsonObject().get("3").getAsString());
                data.addProperty("1", ingestion1.get(0).getAsJsonObject().get("1").getAsString());
                String objectHash = this.getJsonHash(data);
                data.addProperty("timestamp",ingestion2Time.toEpochSecond());
                data.addProperty("objectHash",objectHash);
            }
            else
            {
                ingestion2.add(data);
                data.addProperty("oid",oids.get(i));
                data.addProperty("1", UUID.randomUUID().toString());
                data.addProperty("2",UUID.randomUUID().toString());
                data.addProperty("3", UUID.randomUUID().toString());
                String objectHash = this.getJsonHash(data);
                data.addProperty("timestamp",ingestion2Time.toEpochSecond());
                data.addProperty("objectHash",objectHash);
            }
        }
        JsonUtil.print(ingestion2);

        System.out.println(this.detectUpdates(ingestion1,ingestion2));
    }

    @Test
    public void detectUpdatesMoreOnTop() throws Exception
    {
        Map<Integer,String> oids = new HashMap<>();
        oids.put(0,UUID.randomUUID().toString());
        oids.put(1,UUID.randomUUID().toString());
        oids.put(2,UUID.randomUUID().toString());
        oids.put(3,UUID.randomUUID().toString());
        oids.put(4,UUID.randomUUID().toString());

        JsonArray ingestion1 = new JsonArray();
        OffsetDateTime ingestion1Time = OffsetDateTime.now();
        for(int i=0; i<5; i++){
            JsonObject data = new JsonObject();
            ingestion1.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion1Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion1);

        JsonArray ingestion2 = new JsonArray();
        OffsetDateTime ingestion2Time = OffsetDateTime.now();
        ingestion2Time = ingestion2Time.plus(5, ChronoUnit.MINUTES);
        for(int i=0; i<3; i++){
            JsonObject data = new JsonObject();
            ingestion2.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion2Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion2);

        System.out.println(this.detectUpdates(ingestion1,ingestion2));
    }

    @Test
    public void detectUpdatesMoreOnTopSubset() throws Exception
    {
        Map<Integer,String> oids = new HashMap<>();
        oids.put(0,UUID.randomUUID().toString());
        oids.put(1,UUID.randomUUID().toString());
        oids.put(2,UUID.randomUUID().toString());
        oids.put(3,UUID.randomUUID().toString());
        oids.put(4,UUID.randomUUID().toString());

        JsonArray ingestion1 = new JsonArray();
        OffsetDateTime ingestion1Time = OffsetDateTime.now();
        for(int i=0; i<5; i++){
            JsonObject data = new JsonObject();
            ingestion1.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion1Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion1);

        JsonArray ingestion2 = new JsonArray();
        OffsetDateTime ingestion2Time = OffsetDateTime.now();
        ingestion2Time = ingestion2Time.plus(5, ChronoUnit.MINUTES);
        for(int i=0; i<3; i++){
            JsonObject data = new JsonObject();
            if(i == 0) {
                ingestion2.add(ingestion1.get(0));
            }
            else
            {
                ingestion2.add(data);
                data.addProperty("oid",oids.get(i));
                data.addProperty("1", UUID.randomUUID().toString());
                data.addProperty("2",UUID.randomUUID().toString());
                data.addProperty("3", UUID.randomUUID().toString());
                String objectHash = this.getJsonHash(data);
                data.addProperty("timestamp",ingestion2Time.toEpochSecond());
                data.addProperty("objectHash",objectHash);
            }
        }
        JsonUtil.print(ingestion2);

        System.out.println(this.detectUpdates(ingestion1,ingestion2));
    }

    @Test
    public void detectUpdatesMoreOnTopSubsetShuffle() throws Exception
    {
        Map<Integer,String> oids = new HashMap<>();
        oids.put(0,UUID.randomUUID().toString());
        oids.put(1,UUID.randomUUID().toString());
        oids.put(2,UUID.randomUUID().toString());
        oids.put(3,UUID.randomUUID().toString());
        oids.put(4,UUID.randomUUID().toString());

        JsonArray ingestion1 = new JsonArray();
        OffsetDateTime ingestion1Time = OffsetDateTime.now();
        for(int i=0; i<5; i++){
            JsonObject data = new JsonObject();
            ingestion1.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion1Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion1);

        JsonArray ingestion2 = new JsonArray();
        OffsetDateTime ingestion2Time = OffsetDateTime.now();
        ingestion2Time = ingestion2Time.plus(5, ChronoUnit.MINUTES);
        for(int i=0; i<3; i++){
            JsonObject data = new JsonObject();
            if(i == 0) {
                ingestion2.add(data);
                data.addProperty("oid",oids.get(i));
                data.addProperty("2", ingestion1.get(0).getAsJsonObject().get("2").getAsString());
                data.addProperty("3",ingestion1.get(0).getAsJsonObject().get("3").getAsString());
                data.addProperty("1", ingestion1.get(0).getAsJsonObject().get("1").getAsString());
                String objectHash = this.getJsonHash(data);
                data.addProperty("timestamp",ingestion2Time.toEpochSecond());
                data.addProperty("objectHash",objectHash);
            }
            else
            {
                ingestion2.add(data);
                data.addProperty("oid",oids.get(i));
                data.addProperty("1", UUID.randomUUID().toString());
                data.addProperty("2",UUID.randomUUID().toString());
                data.addProperty("3", UUID.randomUUID().toString());
                String objectHash = this.getJsonHash(data);
                data.addProperty("timestamp",ingestion2Time.toEpochSecond());
                data.addProperty("objectHash",objectHash);
            }
        }
        JsonUtil.print(ingestion2);

        System.out.println(this.detectUpdates(ingestion1,ingestion2));
    }

    @Test
    public void detectUpdatesMoreOnNext() throws Exception
    {
        Map<Integer,String> oids = new HashMap<>();
        oids.put(0,UUID.randomUUID().toString());
        oids.put(1,UUID.randomUUID().toString());
        oids.put(2,UUID.randomUUID().toString());
        oids.put(3,UUID.randomUUID().toString());
        oids.put(4,UUID.randomUUID().toString());

        JsonArray ingestion1 = new JsonArray();
        OffsetDateTime ingestion1Time = OffsetDateTime.now();
        for(int i=0; i<3; i++){
            JsonObject data = new JsonObject();
            ingestion1.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion1Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion1);

        JsonArray ingestion2 = new JsonArray();
        OffsetDateTime ingestion2Time = OffsetDateTime.now();
        ingestion2Time = ingestion2Time.plus(5, ChronoUnit.MINUTES);
        for(int i=0; i<5; i++){
            JsonObject data = new JsonObject();
            ingestion2.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion2Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion2);

        System.out.println(this.detectUpdates(ingestion1,ingestion2));
    }

    @Test
    public void detectUpdatesMoreOnNextSubset() throws Exception
    {
        Map<Integer,String> oids = new HashMap<>();
        oids.put(0,UUID.randomUUID().toString());
        oids.put(1,UUID.randomUUID().toString());
        oids.put(2,UUID.randomUUID().toString());
        oids.put(3,UUID.randomUUID().toString());
        oids.put(4,UUID.randomUUID().toString());

        JsonArray ingestion1 = new JsonArray();
        OffsetDateTime ingestion1Time = OffsetDateTime.now();
        for(int i=0; i<3; i++){
            JsonObject data = new JsonObject();
            ingestion1.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion1Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion1);

        JsonArray ingestion2 = new JsonArray();
        OffsetDateTime ingestion2Time = OffsetDateTime.now();
        ingestion2Time = ingestion2Time.plus(5, ChronoUnit.MINUTES);
        for(int i=0; i<5; i++){
            JsonObject data = new JsonObject();
            if(i == 0) {
                ingestion2.add(ingestion1.get(0));
            }
            else
            {
                ingestion2.add(data);
                data.addProperty("oid",oids.get(i));
                data.addProperty("1", UUID.randomUUID().toString());
                data.addProperty("2",UUID.randomUUID().toString());
                data.addProperty("3", UUID.randomUUID().toString());
                String objectHash = this.getJsonHash(data);
                data.addProperty("timestamp",ingestion2Time.toEpochSecond());
                data.addProperty("objectHash",objectHash);
            }
        }
        JsonUtil.print(ingestion2);

        System.out.println(this.detectUpdates(ingestion1,ingestion2));
    }

    @Test
    public void detectUpdatesMoreOnNextSubsetShuffle() throws Exception
    {
        Map<Integer,String> oids = new HashMap<>();
        oids.put(0,UUID.randomUUID().toString());
        oids.put(1,UUID.randomUUID().toString());
        oids.put(2,UUID.randomUUID().toString());
        oids.put(3,UUID.randomUUID().toString());
        oids.put(4,UUID.randomUUID().toString());

        JsonArray ingestion1 = new JsonArray();
        OffsetDateTime ingestion1Time = OffsetDateTime.now();
        for(int i=0; i<3; i++){
            JsonObject data = new JsonObject();
            ingestion1.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion1Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion1);

        JsonArray ingestion2 = new JsonArray();
        OffsetDateTime ingestion2Time = OffsetDateTime.now();
        ingestion2Time = ingestion2Time.plus(5, ChronoUnit.MINUTES);
        for(int i=0; i<5; i++){
            JsonObject data = new JsonObject();
            if(i == 0) {
                ingestion2.add(data);
                data.addProperty("oid",oids.get(i));
                data.addProperty("2", ingestion1.get(0).getAsJsonObject().get("2").getAsString());
                data.addProperty("3",ingestion1.get(0).getAsJsonObject().get("3").getAsString());
                data.addProperty("1", ingestion1.get(0).getAsJsonObject().get("1").getAsString());
                String objectHash = this.getJsonHash(data);
                data.addProperty("timestamp",ingestion2Time.toEpochSecond());
                data.addProperty("objectHash",objectHash);
            }
            else
            {
                ingestion2.add(data);
                data.addProperty("oid",oids.get(i));
                data.addProperty("1", UUID.randomUUID().toString());
                data.addProperty("2",UUID.randomUUID().toString());
                data.addProperty("3", UUID.randomUUID().toString());
                String objectHash = this.getJsonHash(data);
                data.addProperty("timestamp",ingestion2Time.toEpochSecond());
                data.addProperty("objectHash",objectHash);
            }
        }
        JsonUtil.print(ingestion2);

        System.out.println(this.detectUpdates(ingestion1,ingestion2));
    }

    @Test
    public void detectNewObjects() throws Exception
    {
        Map<Integer,String> oids = new HashMap<>();
        oids.put(0,UUID.randomUUID().toString());
        oids.put(1,UUID.randomUUID().toString());
        oids.put(2,UUID.randomUUID().toString());

        JsonArray ingestion1 = new JsonArray();
        OffsetDateTime ingestion1Time = OffsetDateTime.now();
        for(int i=0; i<3; i++){
            JsonObject data = new JsonObject();
            ingestion1.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion1Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion1);

        JsonArray ingestion2 = new JsonArray();
        OffsetDateTime ingestion2Time = OffsetDateTime.now();
        ingestion2Time = ingestion2Time.plus(5, ChronoUnit.MINUTES);
        for(int i=0; i<3; i++){
            JsonObject data = new JsonObject();
            ingestion2.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion2Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion2);

        System.out.println(this.detectAdds(ingestion1,ingestion2));
    }

    @Test
    public void detectNewObjectsMoreOnNext() throws Exception
    {
        Map<Integer,String> oids = new HashMap<>();
        oids.put(0,UUID.randomUUID().toString());
        oids.put(1,UUID.randomUUID().toString());
        oids.put(2,UUID.randomUUID().toString());
        oids.put(3,UUID.randomUUID().toString());
        oids.put(4,UUID.randomUUID().toString());

        JsonArray ingestion1 = new JsonArray();
        OffsetDateTime ingestion1Time = OffsetDateTime.now();
        for(int i=0; i<3; i++){
            JsonObject data = new JsonObject();
            ingestion1.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion1Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion1);

        JsonArray ingestion2 = new JsonArray();
        OffsetDateTime ingestion2Time = OffsetDateTime.now();
        ingestion2Time = ingestion2Time.plus(5, ChronoUnit.MINUTES);
        for(int i=0; i<5; i++){
            JsonObject data = new JsonObject();
            ingestion2.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion2Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion2);

        System.out.println(this.detectAdds(ingestion1,ingestion2));
    }

    @Test
    public void detectNewObjectsMoreOnTop() throws Exception
    {
        Map<Integer,String> oids = new HashMap<>();
        oids.put(0,UUID.randomUUID().toString());
        oids.put(1,UUID.randomUUID().toString());
        oids.put(2,UUID.randomUUID().toString());
        oids.put(3,UUID.randomUUID().toString());
        oids.put(4,UUID.randomUUID().toString());

        JsonArray ingestion1 = new JsonArray();
        OffsetDateTime ingestion1Time = OffsetDateTime.now();
        for(int i=0; i<5; i++){
            JsonObject data = new JsonObject();
            ingestion1.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion1Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion1);

        JsonArray ingestion2 = new JsonArray();
        OffsetDateTime ingestion2Time = OffsetDateTime.now();
        ingestion2Time = ingestion2Time.plus(5, ChronoUnit.MINUTES);
        for(int i=0; i<3; i++){
            JsonObject data = new JsonObject();
            ingestion2.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion2Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion2);

        System.out.println(this.detectAdds(ingestion1,ingestion2));
    }

    @Test
    public void detectDeletedObjects() throws Exception
    {
        Map<Integer,String> oids = new HashMap<>();
        oids.put(0,UUID.randomUUID().toString());
        oids.put(1,UUID.randomUUID().toString());
        oids.put(2,UUID.randomUUID().toString());

        JsonArray ingestion1 = new JsonArray();
        OffsetDateTime ingestion1Time = OffsetDateTime.now();
        for(int i=0; i<3; i++){
            JsonObject data = new JsonObject();
            ingestion1.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion1Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion1);

        JsonArray ingestion2 = new JsonArray();
        OffsetDateTime ingestion2Time = OffsetDateTime.now();
        ingestion2Time = ingestion2Time.plus(5, ChronoUnit.MINUTES);
        for(int i=0; i<3; i++){
            JsonObject data = new JsonObject();
            ingestion2.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion2Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion2);

        System.out.println(this.detectDeletes(ingestion1,ingestion2));
    }

    @Test
    public void detectDeletedObjectsMoreOnTop() throws Exception
    {
        Map<Integer,String> oids = new HashMap<>();
        oids.put(0,UUID.randomUUID().toString());
        oids.put(1,UUID.randomUUID().toString());
        oids.put(2,UUID.randomUUID().toString());
        oids.put(3,UUID.randomUUID().toString());
        oids.put(4,UUID.randomUUID().toString());

        JsonArray ingestion1 = new JsonArray();
        OffsetDateTime ingestion1Time = OffsetDateTime.now();
        for(int i=0; i<5; i++){
            JsonObject data = new JsonObject();
            ingestion1.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion1Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion1);

        JsonArray ingestion2 = new JsonArray();
        OffsetDateTime ingestion2Time = OffsetDateTime.now();
        ingestion2Time = ingestion2Time.plus(5, ChronoUnit.MINUTES);
        for(int i=0; i<3; i++){
            JsonObject data = new JsonObject();
            ingestion2.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion2Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion2);

        System.out.println(this.detectDeletes(ingestion1,ingestion2));
    }

    @Test
    public void detectDeletedObjectsMoreOnNext() throws Exception
    {
        Map<Integer,String> oids = new HashMap<>();
        oids.put(0,UUID.randomUUID().toString());
        oids.put(1,UUID.randomUUID().toString());
        oids.put(2,UUID.randomUUID().toString());
        oids.put(3,UUID.randomUUID().toString());
        oids.put(4,UUID.randomUUID().toString());

        JsonArray ingestion1 = new JsonArray();
        OffsetDateTime ingestion1Time = OffsetDateTime.now();
        for(int i=0; i<3; i++){
            JsonObject data = new JsonObject();
            ingestion1.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion1Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion1);

        JsonArray ingestion2 = new JsonArray();
        OffsetDateTime ingestion2Time = OffsetDateTime.now();
        ingestion2Time = ingestion2Time.plus(5, ChronoUnit.MINUTES);
        for(int i=0; i<5; i++){
            JsonObject data = new JsonObject();
            ingestion2.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion2Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion2);

        System.out.println(this.detectDeletes(ingestion1,ingestion2));
    }

    @Test
    public void createStateByTimeline() throws Exception
    {
        Map<Integer,String> oids = new HashMap<>();
        oids.put(0,UUID.randomUUID().toString());
        oids.put(1,UUID.randomUUID().toString());
        oids.put(2,UUID.randomUUID().toString());
        oids.put(3,UUID.randomUUID().toString());
        oids.put(4,UUID.randomUUID().toString());


        //timeline0
        JsonArray ingestion0 = new JsonArray();
        OffsetDateTime ingestion1Time = OffsetDateTime.now();
        for(int i=0; i<2; i++){
            JsonObject data = new JsonObject();
            ingestion0.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion1Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion0);

        //timeline1
        JsonArray ingestion1 = new JsonArray();
        OffsetDateTime ingestion2Time = OffsetDateTime.now();
        ingestion2Time = ingestion2Time.plus(5, ChronoUnit.MINUTES);
        for(int i=0; i<3; i++){
            JsonObject data = new JsonObject();
            ingestion1.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion2Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion1);

        //timeline2
        JsonArray ingestion2 = new JsonArray();
        OffsetDateTime ingestion3Time = OffsetDateTime.now();
        ingestion3Time = ingestion3Time.plus(5, ChronoUnit.MINUTES);
        for(int i=0; i<1; i++){
            JsonObject data = new JsonObject();
            ingestion2.add(data);
            data.addProperty("oid",oids.get(i));
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = this.getJsonHash(data);
            data.addProperty("timestamp",ingestion3Time.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        JsonUtil.print(ingestion2);

        Set<String> state = this.calculateState(ingestion0,ingestion1,ingestion2);
        System.out.println("********************");
        System.out.println(state);
    }

    private Set<String> calculateState(JsonArray start, JsonArray next, JsonArray last){
        Set<String> state = new HashSet<>();

        Set<String> adds = this.detectAdds(start,next);
        //List<String> deletes = this.detectDeletes(start,next);

        //start state
        Iterator<JsonElement> iterator = start.iterator();
        while(iterator.hasNext()){
            JsonObject jsonObject = iterator.next().getAsJsonObject();
            state.add(jsonObject.get("oid").getAsString());
        }

        //next
        state.addAll(adds);
        //state.removeAll(deletes);

        //currentState
        JsonArray currentState = new JsonArray();
        iterator = next.iterator();
        while(iterator.hasNext()){
            JsonObject jsonObject = iterator.next().getAsJsonObject();
            String oid = jsonObject.get("oid").getAsString();
            if(state.contains(oid)){
                currentState.add(jsonObject);
            }
        }

        //next-last
        adds = this.detectAdds(currentState,last);
        //deletes = this.detectDeletes(currentState,last);

        state.addAll(adds);
        //state.removeAll(deletes);

        return state;
    }

    private JsonObject findObject(String oid,JsonArray array){
        return null;
    }

    private Set<String> detectUpdates(JsonArray top, JsonArray next){
        Set<String> results = new HashSet<>();

        Map<String, Object> topMap = JsonFlattener.flattenAsMap(top.toString());
        Map<String, Object> nextMap = JsonFlattener.flattenAsMap(next.toString());

        int topArraySize = top.size();
        int nextArraySize = next.size();

        for(int i=0; i<topArraySize; i++){
            String currentOid = topMap.get("["+i+"].oid").toString();
            for(int j=0; j<nextArraySize; j++){
                String nextOid = nextMap.get("["+j+"].oid").toString();
                if(currentOid.equals(nextOid)){
                    String topObjectHash = topMap.get("["+i+"].objectHash").toString();
                    String nextObjectHash = nextMap.get("["+j+"].objectHash").toString();
                    if(!topObjectHash.equals(nextObjectHash)){
                       results.add(currentOid);
                    }
                }
            }
        }

        return results;
    }

    private Set<String> detectAdds(JsonArray top, JsonArray next){
        Set<String> results = new HashSet<>();

        Map<String, Object> topMap = JsonFlattener.flattenAsMap(top.toString());
        Map<String, Object> nextMap = JsonFlattener.flattenAsMap(next.toString());

        int topArraySize = top.size();
        int nextArraySize = next.size();

        for(int i=0; i<nextArraySize; i++){
            String currentOid = nextMap.get("["+i+"].oid").toString();
            boolean objectFound = false;
            for(int j=0; j<topArraySize; j++){
                String nextOid = topMap.get("["+j+"].oid").toString();
                if(currentOid.equals(nextOid)){
                    objectFound = true;
                    break;
                }
            }
            if(!objectFound) {
                results.add(currentOid);
            }
        }
        return results;
    }

    private Set<String> detectDeletes(JsonArray top, JsonArray next){
        Set<String> results = new HashSet<>();

        Map<String, Object> topMap = JsonFlattener.flattenAsMap(top.toString());
        Map<String, Object> nextMap = JsonFlattener.flattenAsMap(next.toString());

        int topArraySize = top.size();
        int nextArraySize = next.size();

        for(int i=0; i<topArraySize; i++){
            String currentOid = topMap.get("["+i+"].oid").toString();
            boolean objectFound = false;
            for(int j=0; j<nextArraySize; j++){
                String nextOid = nextMap.get("["+j+"].oid").toString();
                if(currentOid.equals(nextOid)){
                    objectFound = true;
                    break;
                }
            }
            if(!objectFound) {
                results.add(currentOid);
            }
        }
        return results;
    }

    private String getJsonHash(JsonObject jsonObject) throws NoSuchAlgorithmException {
        Map<String, Object> jsonMap = JsonFlattener.flattenAsMap(jsonObject.toString());
        Map<String,Object> sortedMap = new TreeMap<>();
        Set<Map.Entry<String,Object>> entrySet = jsonMap.entrySet();
        for(Map.Entry<String,Object> entry:entrySet){
            sortedMap.put(entry.getKey(),entry.getValue());
        }
        String jsonHashString = sortedMap.toString();
        return this.hash(jsonHashString);
    }

    private String hash(String original) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("md5");
        md5.update(original.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md5.digest();
        String myHash = DatatypeConverter
                .printHexBinary(digest).toUpperCase();
        return myHash;
    }
    //-----------------------------------------------------------------------------------------------
    public void jsonHash() throws Exception{
        JsonObject top = new JsonObject();
        top.addProperty("1", "1");
        top.addProperty("2","2");
        top.addProperty("3", "3");
        Map<String, Object> topMap = JsonFlattener.flattenAsMap(top.toString());
        System.out.println(topMap);

        JsonObject next = new JsonObject();
        next.addProperty("2", topMap.get("2").toString());
        next.addProperty("3",topMap.get("3").toString());
        next.addProperty("1", topMap.get("1").toString());
        Map<String, Object> nextMap = JsonFlattener.flattenAsMap(next.toString());
        System.out.println(nextMap);


        Map<String,Object> topSorted = new TreeMap<>();
        Map<String,Object> nextSorted = new TreeMap<>();

        Set<Map.Entry<String,Object>> entrySet = topMap.entrySet();
        for(Map.Entry<String,Object> entry:entrySet){
            topSorted.put(entry.getKey(),entry.getValue());
        }

        entrySet = nextMap.entrySet();
        for(Map.Entry<String,Object> entry:entrySet){
            nextSorted.put(entry.getKey(),entry.getValue());
        }

        System.out.println(topSorted);
        System.out.println(nextSorted);

        String topString = topSorted.toString();
        String nextString = nextSorted.toString();


        System.out.println(this.hash(topString));
        System.out.println(this.hash(nextString));
    }

    public void jsonHashReal() throws Exception{
        String topJson = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("prototype/top.json"),
                StandardCharsets.UTF_8);

        String nextJson = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("prototype/next.json"),
                StandardCharsets.UTF_8);

        JsonObject top = JsonParser.parseString(topJson).getAsJsonObject();
        Map<String, Object> topMap = JsonFlattener.flattenAsMap(top.toString());
        System.out.println(topMap);

        JsonObject next = JsonParser.parseString(nextJson).getAsJsonObject();
        Map<String, Object> nextMap = JsonFlattener.flattenAsMap(next.toString());
        System.out.println(nextMap);

        Map<String,Object> topSorted = new TreeMap<>();
        Map<String,Object> nextSorted = new TreeMap<>();

        Set<Map.Entry<String,Object>> entrySet = topMap.entrySet();
        for(Map.Entry<String,Object> entry:entrySet){
            topSorted.put(entry.getKey(),entry.getValue());
        }

        entrySet = nextMap.entrySet();
        for(Map.Entry<String,Object> entry:entrySet){
            nextSorted.put(entry.getKey(),entry.getValue());
        }

        System.out.println(topSorted);
        System.out.println(nextSorted);

        String topString = topSorted.toString();
        String nextString = nextSorted.toString();


        System.out.println(this.hash(topSorted.toString()));
        System.out.println(this.hash(nextSorted.toString()));
    }
}
