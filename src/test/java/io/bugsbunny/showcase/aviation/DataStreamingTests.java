package io.bugsbunny.showcase.aviation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.StorageLevels;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.StreamingContext;
import org.apache.spark.streaming.dstream.DStream;
import org.apache.spark.streaming.receiver.Receiver;
import org.eclipse.microprofile.context.ThreadContext;
import org.junit.jupiter.api.Test;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.*;

import java.util.*;

public class DataStreamingTests {

    @Test
    public void transformTest() throws Exception{
        // Create a local StreamingContext with two working thread and batch interval of 1 second
        SparkConf sparkConf = new SparkConf().setAppName("JavaCustomReceiver").setMaster("local[2]");
        JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, new Duration(1000));

        JavaDStream<String> customReceiverStream = jssc.receiverStream(new JavaCustomReceiver(StorageLevels.MEMORY_AND_DISK_2));
        customReceiverStream.foreachRDD(new VoidFunction<JavaRDD<String>>() {
            @Override
            public void call(JavaRDD<String> stringJavaRDD) throws Exception {
                //stringJavaRDD.saveAsTextFile("streamTest/output");
                stringJavaRDD.foreach(s -> {
                    System.out.println(s);
                });
            }
        });


        jssc.start();
        jssc.awaitTermination();
    }


    private static class JavaCustomReceiver extends Receiver<String> {

        private JsonArray flightData;
        private int currentIndex;
        private int arraySize;

        public JavaCustomReceiver(StorageLevel storageLevel) {
            super(storageLevel);
        }

        @Override
        public void onStart() {
            try {
                String json = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("aviation/flights0.json"), "UTF-8");

                this.flightData = JsonParser.parseString(json).getAsJsonObject().get("data").getAsJsonArray();
                this.currentIndex = 0;
                this.arraySize = this.flightData.size();

                // Start the thread that receives data over a connection
                new Thread(this::receive).start();
            }
            catch (Exception e){
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onStop() {
            // There is nothing much to do as the thread calling receive()
            // is designed to stop by itself if isStopped() returns false
        }

        /** Create a socket connection and receive data until receiver is stopped */
        private void receive() {
            try {
                // Until stopped or connection broken continue reading
                while (!isStopped() && this.currentIndex < this.arraySize) {
                    store(this.flightData.get(this.currentIndex).getAsJsonObject().toString());
                    this.currentIndex++;
                }

                // Restart in an attempt to connect again when server is active again
                //restart("Trying to connect again");
                //this.stop("DONE.....");
            } catch(Throwable t) {
                // restart if there is any other error
                t.printStackTrace();
                restart("Error receiving data", t);
            }
        }
    }
}
