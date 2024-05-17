import java.util.Arrays;

public class MemoryTest extends UserlandProcess{

    public MemoryTest(){
        super();
    }

    public void main() throws InterruptedException {
        int i = 0;
        int num;
        OS.allocateMemory(1024);
        while(true){
            write(1000, (byte)i);
            num = (int)read(1000);
            System.out.println("Number read from memory: " + num);
            i++;
            OS.sleep(50);
            cooperate();
            Thread.sleep(50);
        }
    }
}
