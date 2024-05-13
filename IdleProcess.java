public class IdleProcess extends UserlandProcess{
    void main() throws InterruptedException {
        while(true){
            OS.sleep(50);
            cooperate();
            Thread.sleep(50);
        }
    }
}
