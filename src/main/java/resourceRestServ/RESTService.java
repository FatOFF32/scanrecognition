package resourceRestServ;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.ImageIOHelper;
import net.sourceforge.tess4j.util.PdfUtilities;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import root.FileInputStreamWithDeleteFile;
import root.ProcessMonitor;
import root.WantedValues;

import javax.imageio.IIOImage;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.io.*;
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

        if (rootNode == null || rootNode.get("filePath") == null
                || rootNode.get("ratioSpecifiedX") == null
                || rootNode.get("ratioSpecifiedY") == null
                || rootNode.get("ratioWidth") == null
                || rootNode.get("ratioHeight") == null) {

            responseObj.put("Error", true);
//            responseObj.put("ErrorText", "Должны быть переданы данные: \n filePath \n ratioSpecifiedX \n ratioSpecifiedY \n ratioWidth \n ratioHeight");
            responseObj.put("ErrorText", "Не указан путь к файлу для распознавания");
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

        try {
            // Преобразуем наш PDF в list IIOImage.
            List<IIOImage> iioImages = ImageIOHelper.getIIOImageList(new File(rootNode.get("filePath").asText()));

            // Рассчитаем координаты области распознования
            int specifiedX = (int)(iioImages.get(0).getRenderedImage().getWidth() * Math.min(Math.abs(rootNode.get("ratioSpecifiedX").asDouble(0)), 1));
            int specifiedY = (int)(iioImages.get(0).getRenderedImage().getHeight() * Math.min(Math.abs(rootNode.get("ratioSpecifiedY").asDouble(0)), 1));
            int width = (int)(iioImages.get(0).getRenderedImage().getWidth() * Math.min(Math.abs(rootNode.get("ratioWidth").asDouble(1)), 1));
            int height = (int)(iioImages.get(0).getRenderedImage().getHeight() * Math.min(Math.abs(rootNode.get("ratioHeight").asDouble(1)), 1));

            // Для привязки распознаем только первую страницу (Может быть сделаем настраиваемо)
            resultRecognize = instance.doOCR(iioImages.subList(0,1), new Rectangle(specifiedX, specifiedY, width, height));

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

    @POST
    @Path("convertPdf2Png")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.MULTIPART_FORM_DATA)
    public Response convertPdf2Png(@FormDataParam("datafile") InputStream fileInputStream,
                                       @FormDataParam("datafile") FormDataContentDisposition fileMetaData) {

        Response response = null;
        OutputStream outPDF = null;
        File filePNG = null;
        File filePDF = null;
        File[] filesPNG = null;
        try {
            filePDF = File.createTempFile("forConvert", ".pdf");
            outPDF = new FileOutputStream(filePDF);

            byte[] buffer = new byte[1024];
            int bytesRead;

            while((bytesRead = fileInputStream.read(buffer)) !=-1) {
                outPDF.write(buffer, 0, bytesRead);
            }

            fileInputStream.close();
            outPDF.flush();

           filesPNG = PdfUtilities.convertPdf2Png(filePDF);
           filePNG = filesPNG[0];
        } catch (IOException e) {

            // В случае ошибки вернём её в 1с todo подумать как вернуть текст ошибки в 1с
            response = Response.status(Response.Status.BAD_GATEWAY)
                    .entity(e.getMessage())
                    .build();
        } finally {
            try {
                outPDF.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (filePDF != null && filePDF.exists()) {
                filePDF.delete();
            }

        }

        // тест todo
//        response = Response.status(Response.Status.OK)
//                .entity("Какаф то херня!")
//                .build();


        // Сформируем ответ с преобразованным файлом. Сам файл отправим в 1С как двоичные данные.
        // P.S. можно в таких случаях возвращать поток, но мы это не будем делать, т.к. нам не нужно записывать данные в файл.
        // Как реализовать возврат потока здесь https://stackoverflow.com/questions/3496209/input-and-output-binary-streams-using-jersey
        try {
            if (response == null)
                response = Response.status(Response.Status.OK).entity(new FileInputStreamWithDeleteFile(filePNG)).build();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
//            // Удалим временные файлы todo доделать удаление файлов
//            if (filesPNG != null && filesPNG.length > 0) {
//                File var10 = new File(filesPNG[0].getParent());
//                File[] var11 = filesPNG;
//                int var12 = filesPNG.length;
//
//                for(int var13 = 0; var13 < var12; ++var13) {
//                    File var14 = var11[var13];
//                    var14.delete();
//                }
//
//                var10.delete();
//            }
        }

        return response;
    }

    @GET
    @Path("testStartService")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response testStartService() {

        return Response.status(Response.Status.OK)
                .entity("REST сервис автораспознавателя стартовал!")
                .build();
    }

}
