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
import root.FileStreamingOutput;
import root.ProcessMonitor;
import root.WantedValues;

import javax.imageio.IIOImage;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
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

        if (rootNode == null
                || (rootNode.get("filePath") == null && rootNode.get("fileData") == null)
                || rootNode.get("КоэффНачалаОбластиX") == null
                || rootNode.get("КоэффНачалаОбластиY") == null
                || rootNode.get("КоэффРазмераОбластиX") == null
                || rootNode.get("КоэффРазмераОбластиY") == null) {

            responseObj.put("Error", true);
            responseObj.put("ErrorText",
                    "Должны быть переданы данные: \n filePath ИЛИ fileData\n КоэффНачалаОбластиX \n КоэффНачалаОбластиY \n КоэффРазмераОбластиX \n КоэффРазмераОбластиY");
//            responseObj.put("ErrorText", "Не указан путь к файлу для распознавания");
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
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response convertPdf2Png(@FormDataParam("datafile") InputStream fileInputStream,
                                       @FormDataParam("datafile") FormDataContentDisposition fileMetaData) {

        File filePNG;
        File[] filesPNG = null;
        File filePDF = null;
        OutputStream outPDF = null;
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

            // Конечно не хорошо создавать файлы, лучше конвертировать из одного input stream в другой.
            // Но файлов будет немного, поэтому воспользуемся встроенной в tesseract библиотекой
           filesPNG = PdfUtilities.convertPdf2Png(filePDF);
           filePNG = filesPNG[0];
        } catch (IOException e) {

            // Удалим временные файлы
            if (filesPNG != null && filesPNG.length > 0) {
                File var10 = new File(filesPNG[0].getParent());
                File[] var11 = filesPNG;
                int var12 = filesPNG.length;

                for (int var13 = 0; var13 < var12; ++var13) {
                    File var14 = var11[var13];
                    var14.delete();
                }

                var10.delete();
            }

            // В случае ошибки вернём её в 1с
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
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

        // Сформируем ответ с преобразованным файлом.
        return Response.status(Response.Status.OK).entity(new FileStreamingOutput(filePNG, filesPNG)).build();
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
