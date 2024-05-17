public class DemotionTest extends UserlandProcess{
    public DemotionTest(){
        super();
    }
    void main() throws InterruptedException {
        while(true){
            System.out.println("Demoting");
            cooperate();
            Thread.sleep(50);
        }
    }
}