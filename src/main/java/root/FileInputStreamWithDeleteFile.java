package root;

import java.io.*;

@Deprecated
public class FileInputStreamWithDeleteFile extends FileInputStream {

    private final File[] fileFromDelete;

    public FileInputStreamWithDeleteFile(File file, File[] fileFromDelete) throws FileNotFoundException {
        super(file);
        this.fileFromDelete = fileFromDelete;
    }

    @Override
    public void close() throws IOException {
        super.close();

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
