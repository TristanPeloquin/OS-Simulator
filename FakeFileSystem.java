import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

//Represents a file system using the RandomAccessFile class from Java, straightforwardly translating
//system calls to the respective operations in a file
public class FakeFileSystem implements Device{

    private static RandomAccessFile[] files = new RandomAccessFile[10];

    //Instantiates a new RAF inside the RAF array (default "rw"), representing opening a new file
    //inside the file system
    public int open(String s) {
        try {
            for(int i = 0; i<files.length; i++){
                if(files[i]==null){
                    files[i] = new RandomAccessFile(s, "rw");
                    return i;
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }

    //Closes and nulls the entry in the file array
    public void close(int id) {
        try {
            files[id].close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        files[id] = null;
    }

    //Reads from the given file indicated by id for the given amount of data (size)
    public byte[] read(int id, int size) {
        byte[] bytes = new byte[size];
        try {
            files[id].read(bytes, 0, size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bytes;
    }

    //Seeks in the given file indicated by id to the given spot indicated by to
    public void seek(int id, int to) {
        try {
            files[id].seek(to);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //Writes data to the given file indicated by id
    public int write(int id, byte[] data) {
        try {
            files[id].write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return id;
    }
}
