import java.util.ArrayList;
import java.util.concurrent.Semaphore;

//Contains the main functionality that runs, creates, and switches processes, using other classes
//such as Scheduler, [insert other classes here] as helpers for managing this. Runs "privileged",
//meaning it can access all userland, OS, and kerneland functions and processes.
public class Kernel implements Runnable, Device{
    private Scheduler scheduler;
    private Thread thread;
    private Semaphore semaphore;
    //Used in the OS to ensure the proper initialization of processes
    private Semaphore init;
    private VFS vfs;
    private boolean[] pages;

    public Kernel(){
        scheduler = new Scheduler(this);
        thread = new Thread(this);
        semaphore = new Semaphore(0);
        init = new Semaphore(0);
        vfs = new VFS();
        thread.start();
        init.release();
        pages = new boolean[1024];
    }

    void start(){
        semaphore.release();
    }

    //Runs from the constructor, providing the main functionality for the kernel
    @Override
    public void run(){
        ArrayList<Object> params;
        CallType currentCall;
        //This loop should stop when it tries to acquire permits, where it will wait until start()
        //is called in the OS
        while(true){

            //Waits for start() to be called
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            currentCall = OS.getCurrentCall();
            params = OS.getParams();
            //Determines which operation we perform based on the "note" the OS left
            switch (currentCall){
                case CREATE_PROCESS:
                    OS.retVal = scheduler.createProcess((PCB)params.get(0), ((PCB)OS.getParams().get(0)).getPriority());
                    break;
                case SWITCH_PROCESS:
                    scheduler.switchProcess();
                    break;
                case SLEEP:
                    scheduler.sleep((int)params.get(0));
                    break;
                case INIT_CREATE:
                    OS.retVal = scheduler.createProcess((PCB)params.get(0));
                    //Used at startup to indicate that the process has been created
                    init.release();
                    break;
                case OPEN:
                    OS.retVal = open((String)params.get(0));
                    break;
                case CLOSE:
                    close((int)params.get(0));
                    break;
                case READ:
                    OS.retVal = read((int)params.get(0), (int)params.get(1));
                    break;
                case SEEK:
                    seek((int)params.get(0), (int)params.get(1));
                    break;
                case WRITE:
                    OS.retVal = write((int)params.get(0), (byte[])params.get(1));
                    break;
                case GET_PID:
                    OS.retVal = scheduler.getPid();
                    break;
                case GET_PID_BY_NAME:
                    OS.retVal= scheduler.getPidByName((String)params.get(0));
                    break;
                case SEND_MESSAGE:
                    scheduler.sendMessage((KernelMessage)params.get(0));
                    break;
                case WAIT_FOR_MESSAGE:
                    OS.retVal = scheduler.waitForMessage();
                    break;
                case GET_MAPPING:
                    scheduler.getCurrentProcess().getMapping((int)params.get(0), scheduler);
                    break;
                case ALLOCATE_MEM:
                    allocateMemory((int)params.get(0));
                    break;
                case FREE_MEM:
                    freeMemory((int)params.get(0), (int)params.get(1));
                    break;
            }
            scheduler.getCurrentProcess().run();
        }
    }

    //Allocates a (size/1024) amount of pages, indicating on the page array that the pages are in
    //use and maps them accordingly in the page table - returns the address where memory was allocated
    private int allocateMemory(int size){
        boolean fits;
        int pageNum = size/1024;
        PCB currentProcess = scheduler.getCurrentProcess();

        //Loops through all pages available to find a large enough space to fit the size of memory
        //needed to allocate
        for(int i = 0; i<pages.length; i++){
            //If the space is free, search forward to confirm that there is enough space to fit
            if(!pages[i]){
                fits = true;
                for(int j = i; j<i+pageNum; j++){
                    if(pages[j]){
                        fits = false;
                        break;
                    }
                }
                //If there is enough space, indicate in the boolean array that the spot is taken and
                //map the pages in the page table for later read/writing
                if(fits){
                    for(int j = i; j<i+pageNum; j++){
                        pages[j] = true;
                        currentProcess.mapPage(j);
                    }
                    return i;
                }
            }
        }
        return -1;
    }

    //Sets all pages to free in the indicated area
    public boolean freeMemory(int pointer, int size){
        for(int i = pointer; i<size; i++){
            pages[i] = false;
        }
        return true;
    }

    public void waitForStartup() throws InterruptedException {
        init.acquire();
    }

    public Scheduler getScheduler(){
        return scheduler;
    }

    //Passes the open call to VFS for creating a device and adds the returned ID to the current
    //process's array of IDs to reference in the future (e.g. the first open call will return 0)
    public int open(String s) {
        int id = vfs.open(s);
        PCB current = scheduler.getCurrentProcess();

        //Checks the return value to ensure we don't insert a negative id into the process array
        if(id>-1){
            int retVal = current.getEmptyID();
            current.addID(retVal, id);
            return retVal;
        }
        else{
            return -1;
        }
    }

    //Passes the close call to VFS using the id provided by the current process
    public void close(int id) {
        vfs.close(scheduler.getCurrentProcess().getID(id));
        scheduler.getCurrentProcess().clearID(id);
    }

    //Passes the read call to VFS using the id provided by the current process
    public byte[] read(int id, int size) {
        return vfs.read(scheduler.getCurrentProcess().getID(id), size);
    }

    //Passes the seek call to VFS using the id provided by the current process
    public void seek(int id, int to) {
        vfs.seek(scheduler.getCurrentProcess().getID(id), to);
    }

    //Passes the write call to VFS using the id provided by the current process
    public int write(int id, byte[] data) {
        return vfs.write(scheduler.getCurrentProcess().getID(id), data);
    }

    
}
