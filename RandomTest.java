import java.util.Arrays;

public class RandomTest extends UserlandProcess{

    public RandomTest(){
        super();
    }

    public void main() throws InterruptedException {
        int id = OS.open("random 100");
        byte[] num;
        while(true){
            num = OS.read(id, 5);
            System.out.println("Random test: " + Arrays.toString(num));
            OS.sleep(50);
            cooperate();
            Thread.sleep(50);
        }
    }
}
