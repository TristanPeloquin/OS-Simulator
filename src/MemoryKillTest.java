import java.util.Arrays;

public class MemoryKillTest extends UserlandProcess{

    public MemoryKillTest(){
        super();
    }

    public void main() throws InterruptedException {
        int i = 0;
        int num;
        while(true){
            System.out.println("THIS MESSAGE SHOULD ONLY PRINT ONCE TO INDICATE THIS PROCESS HAS " +
                    "BEEN KILLED");
            OS.allocateMemory(1024);
            write(50000, (byte)i);
            i++;
            OS.sleep(50);
            cooperate();
            Thread.sleep(50);
        }
    }
}
