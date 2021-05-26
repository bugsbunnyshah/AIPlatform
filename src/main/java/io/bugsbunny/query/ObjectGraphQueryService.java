package io.bugsbunny.query;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataIngestion.service.MapperService;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Iterator;
import java.util.Map;

@ApplicationScoped
public class ObjectGraphQueryService {
    private static Logger logger = LoggerFactory.getLogger(ObjectGraphQueryService.class);

    @Inject
    private MapperService mapperService;

    @Inject
    private GraphQueryGenerator graphQueryGenerator;

    @Inject
    private  GraphQueryProcessor graphQueryProcessor;

    private Graph graph;

    @PostConstruct
    public void start()
    {
        this.graph = TinkerGraph.open();

        JsonObject ausJson = new JsonObject();
        ausJson.addProperty("code","aus");
        ausJson.addProperty("description", "AUS");
        ausJson.addProperty("size", 100);

        JsonObject laxJson = new JsonObject();
        laxJson.addProperty("code","lax");
        laxJson.addProperty("description", "LAX");
        laxJson.addProperty("size", 1000);

        JsonObject flight = new JsonObject();
        flight.addProperty("flightId","123");
        flight.addProperty("description", "SouthWest");



        final Vertex aus = this.graph.addVertex(T.id, 1, T.label, "airport", "code", "aus",
                "description", "AUS", "size", 100 ,
                "source", ausJson.toString());
        final Vertex lax = this.graph.addVertex(T.id, 2, T.label, "airport", "code", "lax",
                "description", "LAX", "size", 1000,
                "source", laxJson.toString());
        final Vertex ausToLax = this.graph.addVertex(T.id, 3, T.label, "flight", "flightId", "123", "description", "SouthWest",
        "source",flight.toString());
        aus.addEdge("departure", ausToLax, T.id, 4, "weight", 0.5d);
        lax.addEdge("arrival",ausToLax,T.id, 5, "weight", 0.5d);
    }

    public JsonArray queryByCriteria(String entity, JsonObject criteria)
    {
        JsonArray response = new JsonArray();
        String query = this.graphQueryGenerator.generateQueryByCriteria(entity,criteria);
        GraphTraversal result = this.graphQueryProcessor.query(this.graph, query);
        Iterator<Vertex> itr = result.toSet().iterator();
        while(itr.hasNext())
        {
            Vertex vertex = itr.next();
            JsonObject vertexJson = JsonParser.parseString(vertex.property("source").value().toString()).getAsJsonObject();
            response.add(vertexJson);
        }
        return response;
    }

    public JsonArray navigateByCriteria(String startEntity, String destinationEntity, String relationship, JsonObject criteria) throws Exception
    {
        JsonArray response = new JsonArray();

        String navQuery = this.graphQueryGenerator.generateNavigationQuery(startEntity,destinationEntity,
                relationship,criteria);

        GraphTraversal result = this.graphQueryProcessor.navigate(this.graph,navQuery);
        Iterator<Map> itr = result.toSet().iterator();
        while(itr.hasNext())
        {
            Map map = itr.next();
            Vertex start = (Vertex) map.get(startEntity);
            Vertex end = (Vertex) map.get(destinationEntity);
            if(start == null || end == null)
            {
                continue;
            }

            JsonObject startJson = JsonParser.parseString(start.property("source").value().toString()).getAsJsonObject();


            JsonObject endJson = JsonParser.parseString(end.property("source").value().toString()).getAsJsonObject();
            startJson.add(destinationEntity,endJson);

            response.add(startJson);
        }

        return response;
    }
}
