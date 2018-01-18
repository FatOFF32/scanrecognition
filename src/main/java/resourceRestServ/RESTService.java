package resourceRestServ;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import root.ProcessMonitor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("recognizer")
public class RESTService {

    @POST
    @Path("updateAuthData")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAuthData(String requestJson) {
        String response = "Данные REST сервера 1С обновлены!";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = null;
        try {
            rootNode = mapper.readValue(requestJson, JsonNode.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (rootNode == null || rootNode.get("URLRESTService1C") == null
                || rootNode.get("Пользователь") == null
                || rootNode.get("Пароль") == null
                || rootNode.get("КоличествоПроцессовАвтораспознавания") == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Должны быть переданы данные: \n URLRESTService1C \n Пользователь \n Пароль \n КоличествоПроцессовАвтораспознавания")
                    .build();
        }

        ProcessMonitor.setSettings1C(rootNode);

        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }

    @GET
    @Path("testStartService")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAuthData() {

        return Response.status(Response.Status.OK)
                .entity("REST сервис автораспознавателя стартовал!")
                .build();
    }

}
