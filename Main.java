public class Main {
    public static void main(String[] args) throws InterruptedException {
        OS.startup(new HelloWorld());
        OS.createProcess(new GoodbyeWorld(), Priority.REAL_TIME);
        //OS.createProcess(new DemotionTest(), Priority.REAL_TIME); //Uncomment to test demotion
        //OS.createProcess(new FileTest(), Priority.REAL_TIME); //Uncomment to test FFS device
        //OS.createProcess(new RandomTest(), Priority.REAL_TIME); //Uncomment to test random device
        //OS.createProcess(new CloseTest()); //Uncomment to test closing devices
        //OS.createProcess(new Ping());
        //OS.createProcess(new Pong());
        OS.createProcess(new MemoryTest());
        OS.createProcess(new MemoryKillTest());
        OS.createProcess(new MemoryExtendingTest());
    }
}
