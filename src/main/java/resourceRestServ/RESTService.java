package resourceRestServ;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.ImageIOHelper;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import root.ProcessMonitor;
import root.WantedValues;

import javax.imageio.IIOImage;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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


    @POST
    @Path("recognizeFile")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response recognizeFile(String requestJson) {

        String responseJson = "";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = null;
        try {
            rootNode = mapper.readValue(requestJson, JsonNode.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectNode responseObj = mapper.createObjectNode();

        if (rootNode == null || rootNode.get("filePath") == null) {

            responseObj.put("Error", true);
            responseObj.put("ErrorText", "Не задан путь распознователя");
            responseObj.put("RecognizedText", "");

            try {
                responseJson = mapper.writeValueAsString(responseObj);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(responseJson)
                    .build();
        }

        String resultRecognize;

        ITesseract instance = new Tesseract();  // JNA Interface Mapping
        instance.setLanguage("rus");
        // удалить отсюда
        try {
            // Будет так:
            List<IIOImage> iioImages = ImageIOHelper.getIIOImageList(new File(rootNode.get("filePath").asText()));
//            if (iioImages.size() == 0)
            //iioImages.set(0).getRenderedImage(). //Поиграться, рассчитать координаты, создать зону для распознования TODO
            resultRecognize = instance.doOCR(iioImages.subList(0,1), new Rectangle(iioImages.get(0).getRenderedImage().getWidth(), iioImages.get(0).getRenderedImage().getHeight() / 2)); // Сюда передаем 0 элементы, и зону для распознования todo

//                        result = instance.doOCR(iioImages, templateRec.areaRecognition); // Пока так!) todo
//                        result = instance.doOCR(fileInfo.file, templateRec.areaRecognition);
        } catch (TesseractException | IOException e) {
            responseObj.put("Error", true);
            responseObj.put("ErrorText", e.getMessage());
            responseObj.put("RecognizedText", "");

            try {
                responseJson = mapper.writeValueAsString(responseObj);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(responseJson)
                    .build();
        }
        // До сюда
/* // Раскоментить потом, Удалить вверху по instance.setLanguage("rus");
        try {
            resultRecognize = instance.doOCR(new File(rootNode.get("filePath").asText()));
        } catch (TesseractException e) {
//            System.err.println(e.getMessage());
            responseObj.put("Error", true);
            responseObj.put("ErrorText", e.getMessage());
            responseObj.put("RecognizedText", "");

            try {
                responseJson = mapper.writeValueAsString(responseObj);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(responseJson)
                    .build();
        }
*/

        responseObj.put("Error", false);
        responseObj.put("ErrorText", "");
        responseObj.put("RecognizedText", resultRecognize);

        try {
            responseJson = mapper.writeValueAsString(responseObj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.status(Response.Status.OK)
                .entity(responseJson)
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
