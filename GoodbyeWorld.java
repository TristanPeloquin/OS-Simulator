public class GoodbyeWorld extends UserlandProcess {
    public GoodbyeWorld(){
        super();
    }
    void main() throws InterruptedException {
        while(true) {
            System.out.println("Goodbye world");
            cooperate();
            OS.sleep(50);
            Thread.sleep(50);
        }
    }
}
