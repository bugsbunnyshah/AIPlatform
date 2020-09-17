package io.bugsbunny.datalake;

import com.mongodb.spark.MongoSpark;
import com.mongodb.spark.config.WriteConfig;

import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.*;
import org.apache.spark.sql.Dataset;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@QuarkusTest
public class DeltaLakeStartedTests implements Serializable
{
    private static Logger logger = LoggerFactory.getLogger(DeltaLakeStartedTests.class);

    //@Test
    public static void main(String[] args) throws Exception
    {
        String digitsCsv = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("dataLake/digits.csv"),
                StandardCharsets.UTF_8);
        logger.info(digitsCsv);

        /*SparkSession spark = SparkSession.builder()
                .master("local")
                .appName("AIPlatformDataLake")
                .config("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension")
                .config("spark.mongodb.input.uri", "mongodb://127.0.0.1/test.myCollection")
                .config("spark.mongodb.output.uri", "mongodb://127.0.0.1/test.myCollection")
                .getOrCreate();

        JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());
        Map<String, String> writeOverrides = new HashMap<String, String>();
        writeOverrides.put("collection", "spark");
        writeOverrides.put("writeConcern.w", "majority");
        WriteConfig writeConfig = WriteConfig.create(jsc).withOptions(writeOverrides);

        File file = new File(Thread.currentThread().getContextClassLoader().
                getResource("dataLake/digits.csv").toURI());
        //Dataset<Row> df = spark.readStream().csv(file.getPath());
        final Dataset<Row> load = spark.read().csv(file.getPath());
        load.show();
        MongoSpark.save(load, writeConfig);


        /*Dataset<Row> df = spark.read().
        df.show();
        MongoSpark.save(df, writeConfig);*/

        //spark.close();
    }

    /*private static Logger logger = LoggerFactory.getLogger(DeltaLakeStartedTests.class);

    //@Test
    public void testQueries() throws Exception
    {
        String location = "/tmp/delta-table-"+ UUID.randomUUID().toString();

        SparkSession spark = SparkSession.builder()
                .master("local")
                .appName("CRUDApp")
                .config("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension")
                .config("spark.mongodb.input.uri", "mongodb://127.0.0.1/test.myCollection")
                .config("spark.mongodb.output.uri", "mongodb://127.0.0.1/test.myCollection")
                .getOrCreate();
        JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());
        //DeltaTable deltaTable = DeltaTable.forPath(location);

        // Create a custom WriteConfig
        Map<String, String> writeOverrides = new HashMap<String, String>();
        writeOverrides.put("collection", "spark");
        writeOverrides.put("writeConcern.w", "majority");
        WriteConfig writeConfig = WriteConfig.create(jsc).withOptions(writeOverrides);

        //Dataset<String> data = spark.read().parquet(location).as(Encoders.bean(String.class));

        //JsonObject jsonObject = new JsonObject();
        //jsonObject.addProperty("email","c.s@data.world");
        //String json = jsonObject.toString();

        //Initialize the Data
        System.out.println("INIT_DATA");

        //Dataset<Long> data = spark.range(0, 5);
        //data.write().format("delta").save(location);
        //Dataset<Row> df = spark.read().format("delta").load(location);

        Dataset<Row> df = spark.read().text(Thread.currentThread().
                getContextClassLoader().
                getResource("dataLake/email.json").getFile());
        df.show();
        MongoSpark.save(df, writeConfig);

        //System.out.println("OVERWRITE_DATA");
        //data = spark.range(5, 10);
        //data.write().format("delta").mode("overwrite").save(location);
        //df = spark.read().format("delta").load(location);
        //df.show();

        // Update every even value by adding 100 to it
        //System.out.println("UPDATE_DATA");
        //deltaTable.update(
        //        functions.expr("id % 2 == 0"),
        //        new HashMap<String, Column>() {{
        //            put("id", functions.expr("id + 100"));
        //        }}
        //);
        //deltaTable.toDF().show();


        //Read in the old overwritten data set
        /*System.out.println("TIMETRAVEL_DATA_ORIGINAL");
        df = spark.read().format("delta").option("versionAsOf", 0).load(location);
        df.show();
        MongoSpark.save(df, writeConfig);

        //Read in the updated data set
        System.out.println("TIMETRAVEL_DATA_UPDATED");
        df = spark.read().format("delta").option("versionAsOf", 1).load(location);
        df.show();
        MongoSpark.save(df, writeConfig);

        //Read in the latest/live data set
        System.out.println("TIMETRAVEL_DATA_LATEST_LIVE");
        df = spark.read().format("delta").option("versionAsOf", 2).load(location);
        df.show();
        MongoSpark.save(df, writeConfig);*/

        /*System.out.println("TIMETRAVEL_DATA_UPDATED");
        df = spark.read().format("delta").option("versionAsOf", 1).load(location);
        df.show();

        df = spark.read().format("delta").option("versionAsOf", 2).load(location);
        df.show();

        df = spark.read().format("delta").option("versionAsOf", 0).load(location);
        df.show();

        spark.close();

        //TODO: Verify versions are not in-memory only
    }*/
}