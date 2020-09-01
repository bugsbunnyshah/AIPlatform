package io.bugsbunny.datalake;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
//import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.*;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.*;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.apache.tinkerpop.gremlin.util.Gremlin;
import java.io.IOException;
import java.util.Map;

import org.apache.tinkerpop.gremlin.sparql.process.traversal.dsl.sparql.SparqlTraversalSource;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TinkerPopStartedTests {
    private static Logger logger = LoggerFactory.getLogger(TinkerPopStartedTests.class);

    @Test
    public void testStart() throws Exception
    {
        TinkerGraph tg = TinkerGraph.open();

        //tinkerGraph.addVertex("0","1","3");
        // Create a Traversal source object
        GraphTraversalSource g = tg.traversal();

        // Add some nodes and vertices - Note the use of "iterate".
        GraphTraversal traversal = g.addV("airport").property("code","AUS").as("aus").
                addV("airport").property("code","DFW").as("dfw").
                addV("airport").property("code","LAX").as("lax").
                addV("airport").property("code","JFK").as("jfk").
                addV("airport").property("code","ATL").as("atl").
                addE("route").from("aus").to("dfw").
                addE("route").from("aus").to("atl").
                addE("route").from("atl").to("dfw").
                addE("route").from("atl").to("jfk").
                addE("route").from("dfw").to("jfk").
                addE("route").from("dfw").to("lax").
                addE("route").from("lax").to("jfk").
                addE("route").from("lax").to("aus").
                addE("route").from("lax").to("dfw").
                iterate();

        logger.info("******************************");
        logger.info(traversal.toString());
        logger.info("******************************");

        TinkerGraph graph = TinkerFactory.createModern();
        SparqlTraversalSource blah =
                (SparqlTraversalSource)
                        tg.traversal(SparqlTraversalSource.class);
        //blah.sparql("""SELECT ?name ?age
        //     WHERE { ?person v:name ?name . ?person v:age ?age }
        //    ORDER BY ASC(?age)""");
        GraphTraversal gt = blah.sparql("SELECT ?airport WHERE { ?airportValue v:aus }");
        GraphTraversal map = gt.group();
        logger.info(map.toString());
        //logger.info(blah.sparql("SELECT ?name ?age WHERE { ?person v:name ?name . ?person v:age ?age } ORDER BY ASC(?age)").getClass().getName());
        //logger.info(blah.sparql("SELECT ?name ?age WHERE { ?person v:name ?name . ?person v:age ?age } ORDER BY ASC(?age)").toString());
    }
}
