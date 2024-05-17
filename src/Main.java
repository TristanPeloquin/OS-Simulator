public class Main {
    public static void main(String[] args) throws InterruptedException {
        OS.startup(new HelloWorld());
        //OS.createProcess(new GoodbyeWorld(), Priority.REAL_TIME);
        //OS.createProcess(new DemotionTest(), Priority.REAL_TIME); //Uncomment to test demotion
        //OS.createProcess(new FileTest(), Priority.REAL_TIME); //Uncomment to test FFS device
        //OS.createProcess(new RandomTest(), Priority.REAL_TIME); //Uncomment to test random device
        //OS.createProcess(new CloseTest()); //Uncomment to test closing devices
        OS.createProcess(new Ping()); //Uncomment to test message sending/receiving
        OS.createProcess(new Pong()); //Uncomment to test message sending/receiving
        //OS.createProcess(new MemoryTest()); //Uncomment to test general memory write/read
        //OS.createProcess(new MemoryKillTest()); //Uncomment to test memory clearing after a process dies
        //OS.createProcess(new MemoryExtendingTest()); //Uncomment to test memory reallocation
    }
}
