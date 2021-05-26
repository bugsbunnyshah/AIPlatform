package io.bugsbunny.query;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataIngestion.service.MapperService;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

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

    private GraphData graphData;

    @PostConstruct
    public void start()
    {
        //TODO: instantiate with a RemoteGraphData
        //SparqlTraversalSource server = new SparqlTraversalSource(graph);
        //this.graphData = new LocalGraphData(server);
    }

    public void setGraphData(GraphData graphData)
    {
        this.graphData = graphData;
    }

    public JsonArray queryByCriteria(String entity, JsonObject criteria)
    {
        JsonArray response = new JsonArray();
        String query = this.graphQueryGenerator.generateQueryByCriteria(entity,criteria);
        GraphTraversal result = this.graphQueryProcessor.query(this.graphData, query);
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

        GraphTraversal result = this.graphQueryProcessor.navigate(this.graphData,navQuery);
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
