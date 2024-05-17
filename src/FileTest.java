import java.util.Arrays;

public class FileTest extends UserlandProcess{

    public FileTest(){
        super();
    }

    public void main() throws InterruptedException {
        int file = OS.open("file numbers");
        int rand = OS.open("random 93840245");
        byte[] num = new byte[1];
        while(true){
            num = OS.read(rand, 5);
            System.out.println("File test actual numbers: " + Arrays.toString(num));
            OS.write(file, num);
            OS.seek(file, 0);
            System.out.println("Numbers read from file: " + Arrays.toString(OS.read(file, 5)));
            OS.seek(file, 0);
            OS.sleep(50);
            cooperate();
            Thread.sleep(50);
        }
    }

}
