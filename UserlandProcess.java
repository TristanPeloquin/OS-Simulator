import java.util.concurrent.Semaphore;

//Represents a generic process created by the user, this runs on its own thread and will be managed
//by the OS and kernel, of which this has no access to apart from calling OS methods to interact
//with the kernel.
public abstract class UserlandProcess implements Runnable{

    protected Thread thread;
    protected Semaphore semaphore;
    protected boolean expired;
    private static int count = 0;
    private int pid;
    private static int[][] tlb = new int[2][2];
    private static byte[] memory = new byte[1048576];

    public UserlandProcess(){
        semaphore = new Semaphore(0);
        expired = false;
        pid = count;
        count++;
        thread = new Thread(this);
        thread.start();
    }
    void requestStop(){
        expired = true;
    }

    abstract void main() throws InterruptedException;

    boolean isStopped(){
        return semaphore.getQueueLength() >= 1;
    }

    boolean isDone(){
        return !thread.isAlive();
    }

    void start(){
        semaphore.release();
    }

    void stop() throws InterruptedException{
        semaphore.acquire();
    }

    @Override
    public void run() {
        try {
            semaphore.acquire();
            main();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //Used in the main method of inherited classes, this ensures the process stops and hands control
    //back to the OS/kernel.
    public void cooperate() throws InterruptedException {
        if(expired){
            expired = false;
            OS.switchProcess();
        }
    }

    public int getPID(){
        return pid;
    }

    //Reads the given address from memory by translating the virtual address (given) to the physical
    //address as found in the PCB page table - may fail if the address is outside the given space
    //allocated to it
    public byte read(int address) throws InterruptedException {
        int virtualPage = address/1024;
        int pageOffset = address%1024;
        int physicalAddress = 0;
        int physicalPage = -1;

        //Loops until the physical page is found in the page table of the PCB using getMapping() to
        //reset the TLB if the page is not found
        while(physicalPage==-1){
            if(tlb[0][0] == virtualPage && tlb[0][1] != -1){
                physicalPage = tlb[0][1];
            }
            else if(tlb[1][0] == virtualPage && tlb[1][1] != -1){
                physicalPage = tlb[1][1];
            }
            else{
                OS.getMapping(virtualPage);
            }
        }

        //Calculates the physical address to access the memory directly
        physicalAddress = physicalPage*1024+pageOffset;
        return memory[physicalAddress];
    }

    //Writes the given value to memory by translating the virtual address (given) to the physical
    //address as found in the PCB page table - may fail if the address is outside the given space
    //allocated to it
    public void write(int address, byte value) throws InterruptedException {
        int virtualPage = address/1024;
        int pageOffset = address%1024;
        int physicalAddress = 0;
        int physicalPage = -1;

        //Loops until the physical page is found in the page table of the PCB using getMapping() to
        //reset the TLB if the page is not found
        while(physicalPage==-1){
            if(tlb[0][0] == virtualPage){
                physicalPage = tlb[0][1];
            }
            else if(tlb[1][0] == virtualPage){
                physicalPage = tlb[1][1];
            }
            else{
                OS.getMapping(virtualPage);
            }
        }

        //Calculates the physical address to access the memory directly
        physicalAddress = physicalPage*1024+pageOffset;
        memory[physicalAddress] = value;
    }

    //Used by PCB to access the TLB and update its values appropriately
    public void updateTlb(int index, int virtualPage, int physicalPage){
        tlb[index][0] = virtualPage;
        tlb[index][1] = physicalPage;
    }
}
