package root;

import java.io.*;

public class FileInputStreamWithDeleteFile extends FileInputStream {

    private final File currentFile;

    public FileInputStreamWithDeleteFile(String name) throws FileNotFoundException {
        super(name);
        this.currentFile = new File(name);
    }

    public FileInputStreamWithDeleteFile(File file) throws FileNotFoundException {
        super(file);
        this.currentFile = file;
    }

    public FileInputStreamWithDeleteFile(FileDescriptor fdObj, File currentFile) {
        super(fdObj);
        this.currentFile = currentFile;
    }

    @Override
    public void close() throws IOException {
        super.close();

        currentFile.delete();
    }
}
