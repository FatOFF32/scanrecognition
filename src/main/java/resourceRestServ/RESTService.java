package resourceRestServ;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import root.ProcessMonitorAPI;

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
                || rootNode.get("Пароль") == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Должны быть переданы данные: \n URLRESTService1C \n Пользователь \n Пароль")
                    .build();
        }

        ProcessMonitorAPI.setUrl1C(rootNode.get("URLRESTService1C").asText());
        ProcessMonitorAPI.setUrl1C(rootNode.get("Пользователь").asText());
        ProcessMonitorAPI.setUrl1C(rootNode.get("Пароль").asText());

        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }

    @GET
    @Path("test")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAuthData() {

        return Response.status(Response.Status.OK)
                .entity("Привет мир!")
                .build();
    }

/*
        @POST
        @Path("registration")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response registration(String requestJson){
            String responseJson = "";
            try {
                JSONParser parser = new JSONParser();
                JSONObject parse = (JSONObject) parser.parse(requestJson);
                if (parse.get("userId") != null && parse.get("password") != null)
                    responseJson = DataBaseUtils.registration(parse);
                else
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Invalid request json")
                            .build();
            } catch (SQLException | ParseException e) {
                e.printStackTrace();
            }
            return Response.status(Response.Status.OK)
                    .entity(responseJson)
                    .build();
        }

        @POST
        @Path("user/chats")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response getChatsByUserID(String requestJson){
            String responseJson = "";
            try {
                JSONParser parser = new JSONParser();
                JSONObject parse = (JSONObject) parser.parse(requestJson);
                if (parse.get("userId") != null)
                    responseJson = DataBaseUtils.getChatsByUserId((String)parse.get("userId"));
                else
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Invalid request json")
                            .build();
            } catch (SQLException | ParseException e) {
                e.printStackTrace();
            }
            return Response.status(Response.Status.OK)
                    .entity(responseJson)
                    .build();
        }

        @POST
        @Path("friend")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response getUserById(String requestJson){
            String responseJson = "";
            try {
                JSONParser parser = new JSONParser();
                JSONObject parse = (JSONObject) parser.parse(requestJson);
                if (parse.get("userId") != null)
                    responseJson = DataBaseUtils.getUserById(parse);
                else
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Invalid request")
                            .build();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return Response.status(Response.Status.OK)
                    .entity(responseJson)
                    .build();
        }

        @POST
        @Path("user/chat/messages")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response getMessagesByChatId(String requestJson){
            String responseJson = "";
            try {
                System.out.println(requestJson);
                JSONParser parser = new JSONParser();
                JSONObject parse = (JSONObject) parser.parse(requestJson);
                System.out.println(parse.toJSONString());
                if (parse.get("chatId") != null)
                    responseJson = DataBaseUtils.getMessagesByChatId(Integer.parseInt((String) parse.get("chatId")));
                else
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Invalid request json")
                            .build();
            } catch (SQLException | ParseException e) {
                e.printStackTrace();
            }
            return Response.status(Response.Status.OK)
                    .entity(responseJson)
                    .build();
        }

        @POST
        @Path("user/chats/newchat")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response createChat(String requestJson){
            String responseJson = "";
            try {
                System.out.println(requestJson);
                JSONParser parser = new JSONParser();
                JSONObject parse = (JSONObject) parser.parse(requestJson);
                System.out.println(parse.toJSONString());
                if (parse.get("userId") != null && parse.get("chatName") != null &&  parse.get("users") != null)
                    responseJson = DataBaseUtils.createChat(parse);
                else
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Invalid request json")
                            .build();
            } catch (SQLException | ParseException e) {
                e.printStackTrace();
            }
            return Response.status(Response.Status.OK)
                    .entity(responseJson)
                    .build();
        }
*/
    }
