public class HelloWorld extends UserlandProcess{
    public HelloWorld(){
        super();
    }
    void main() throws InterruptedException {
        while(true){
            System.out.println("Hello world");
            cooperate();
            OS.sleep(50);
            Thread.sleep(50);
        }
    }
}
