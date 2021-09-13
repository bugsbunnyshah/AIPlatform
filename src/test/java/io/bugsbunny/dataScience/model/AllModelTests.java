package io.bugsbunny.dataScience.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.util.JsonUtil;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class AllModelTests {
    private static Logger logger = LoggerFactory.getLogger(AllModelTests.class);

    @Test
    public void testScientistSer() throws Exception{
        Scientist scientist = this.mockScientist();
        String email = scientist.getEmail();

        JsonObject json = scientist.toJson();
        JsonUtil.print(json);
        String emailInJson = json.get("email").getAsString();
        assertEquals(email,emailInJson);

        Scientist deser = Scientist.parse(json.toString());
        logger.info(deser.toString());
        assertEquals(email,deser.getEmail());
    }

    @Test
    public void testTeamSer() throws Exception{
        Team team = this.mockTeam();

        JsonObject json = team.toJson();
        JsonUtil.print(json);
        JsonArray array = JsonParser.parseString(json.toString()).getAsJsonObject().
                get("scientists").getAsJsonArray();
        assertEquals(array.size(),team.getScientists().size());

        Team deser = Team.parse(json.toString());
        assertEquals(team.getScientists().size(),deser.getScientists().size());
        Scientist original = team.getScientists().get(0);
        assertTrue(deser.getScientists().contains(original));
    }

    private Scientist mockScientist(){
        String email = "test@test.io";
        Scientist scientist = new Scientist();
        scientist.setEmail(email);
        return scientist;
    }

    private Team mockTeam(){
        Team team = new Team();
        for(int i=0; i<3; i++) {
            Scientist scientist = new Scientist();
            scientist.setEmail("test"+i+"@test.io");
            team.addScientist(scientist);
        }
        return team;
    }
}
