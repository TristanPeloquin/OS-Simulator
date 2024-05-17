import java.util.Arrays;

public class CloseTest extends UserlandProcess{
    public CloseTest(){
        super();
    }

    public void main() throws InterruptedException {
        byte[] num;
        while(true){
            int rand = OS.open("random 49835793");
            num = OS.read(rand, 2);
            System.out.println("Close test randoms: " + Arrays.toString(num));
            OS.sleep(50);
            cooperate();
            Thread.sleep(50);
            OS.close(rand);
        }
    }
}
