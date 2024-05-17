import java.util.Arrays;

public class MemoryFreeTest extends UserlandProcess{

    public MemoryFreeTest(){
        super();
    }

    public void main() throws InterruptedException {
        int i = 0;
        int address;
        while(true){
            System.out.println("Allocating memory...");
            address = OS.allocateMemory(1024);
            write(address, (byte)i);
            i++;
            System.out.println("Read from memory before freeing: " + read(address));
            OS.freeMemory(address, 1024);
            System.out.println("CRASHING: reading from freed memory, should crash");
            read(address);
            OS.sleep(50);
            cooperate();
            Thread.sleep(50);
        }
    }
}
