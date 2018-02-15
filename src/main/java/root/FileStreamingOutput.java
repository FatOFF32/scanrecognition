package root;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileStreamingOutput implements StreamingOutput {

    private final File file;
    private final File[] fileFromDelete;

    public FileStreamingOutput(File file, File[] fileFromDelete) {
        this.file = file;
        this.fileFromDelete = fileFromDelete;
    }

    @Override
    public void write(OutputStream output)
            throws IOException, WebApplicationException {
        FileInputStream input = new FileInputStream(file);
        try {
            int bytes;
            while ((bytes = input.read()) != -1) {
                output.write(bytes);
            }
        } catch (Exception e) {
            throw new WebApplicationException(e);
        } finally {
            // Закроем потоки
            if (output != null) output.close();
            if (input != null) input.close();

            // Удалим временные файлы
            if (fileFromDelete != null && fileFromDelete.length > 0) {
                File var10 = new File(fileFromDelete[0].getParent());
                File[] var11 = fileFromDelete;
                int var12 = fileFromDelete.length;

                for (int var13 = 0; var13 < var12; ++var13) {
                    File var14 = var11[var13];
                    var14.delete();
                }

                var10.delete();
            }
        }
    }

}