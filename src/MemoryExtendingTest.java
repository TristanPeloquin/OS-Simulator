import java.util.Arrays;

public class MemoryExtendingTest extends UserlandProcess{

    public MemoryExtendingTest(){
        super();
    }

    public void main() throws InterruptedException {
        int i = 0;
        int address = 1023;
        int num;
        while(true){
            OS.allocateMemory(1024);
            write(address, (byte)i);
            num = read(address);
            System.out.println("Read number " + num + " from extended memory address " + address);
            address+=1024;
            i++;
            OS.sleep(50);
            cooperate();
            Thread.sleep(50);
        }
    }
}
