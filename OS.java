import java.util.ArrayList;

//The main component of the Operating System, this translates calls from the user in userland to
//calls in the kernel, where we create and run the actual processes. Note - all members and methods
//are static since there should only be one instance of the operating system and kernel.
public class OS{
    private static Kernel kernel;
    private static CallType currentCall = null;
    //initOrCreate is used in createProcess() to indicate if we are at startup or regular create
    private static CallType initOrCreate = null;
    private static Priority priority = Priority.INTERACTIVE;
    private static ArrayList<Object> params = new ArrayList<>();
    public static Object retVal;

    //Leaves a note that the kernel should sleep the current process in the params list
    public static void sleep(int millis) throws InterruptedException {
        params.clear();
        params.add(millis);
        currentCall = CallType.SLEEP;

        switchToKernel();
    }

    //Adds the process to the params - essentially leaves a note for the kernel on what process to
    //create, as well as what priority to create it with, and sets the call type accordingly, then
    //switches to the kernel and returns the pid
    public static int createProcess(UserlandProcess up, Priority pr) throws InterruptedException {
        PCB pcb = new PCB(up, pr);
        params.clear();
        params.add(pcb);
        currentCall = CallType.CREATE_PROCESS;
        priority = pr;

        switchToKernel();

        retVal = pcb.getPid();
        return (int)retVal;
    }

    //Adds the process to the params - essentially leaves a note for the kernel on what process to
    //create, and sets the call type accordingly, then switches to the kernel and returns the pid
    public static int createProcess(UserlandProcess up) throws InterruptedException {
        PCB pcb = new PCB(up, Priority.INTERACTIVE);
        params.clear();
        params.add(pcb);
        currentCall = initOrCreate;
        priority = Priority.INTERACTIVE;


        //Switches to the kernel and stops any currently running processes
        switchToKernel();

        retVal = pcb.getPid();
        return (int)retVal;
    }

    //Initializes the kernel and the first processes of the operating system, including an idle
    //process
    public static void startup(UserlandProcess init) throws InterruptedException {
        kernel = new Kernel();

        //Indicates that this is startup to the kernel
        initOrCreate = CallType.INIT_CREATE;

        //Semaphore ensures the kernel has started before we try creating a process
        kernel.waitForStartup();
        createProcess(init);

        //Again semaphore ensures the previous process was created before we go again
        kernel.waitForStartup();
        createProcess(new IdleProcess());

        initOrCreate = CallType.CREATE_PROCESS;
    }

    //Helper method for readability used whenever we want kernel to run and read the "note" left in
    //the params list
    private static void switchToKernel() throws InterruptedException {
        PCB currentProcess = kernel.getScheduler().getCurrentProcess();
        kernel.start();
        if(currentProcess != null) {
            currentProcess.stop();
        }
    }

    //Used when userland process cooperates and has to switch with another process
    public static void switchProcess() throws InterruptedException {
        params.clear();
        currentCall = CallType.SWITCH_PROCESS;
        switchToKernel();
    }

    public static CallType getCurrentCall() {
        return currentCall;
    }

    public static Priority getPriority(){
        return priority;
    }

    public static ArrayList<Object> getParams() {
        return new ArrayList<>(params);
    }

    //Passes on the open call to kernel by leaving the parameter in the params list and setting the
    //call type appropriately
    public static int open(String s) throws InterruptedException {
        params.clear();
        params.add(s);
        currentCall = CallType.OPEN;
        switchToKernel();
        return (int)retVal;
    }

    //Passes on the open call to kernel by leaving the parameter in the params list and setting the
    //call type appropriately
    public static void close(int id) throws InterruptedException {
        params.clear();
        params.add(id);
        currentCall = CallType.CLOSE;
        switchToKernel();
    }

    //Passes on the open call to kernel by leaving the parameter in the params list and setting the
    //call type appropriately
    public static byte[] read(int id, int size) throws InterruptedException {
        params.clear();
        params.add(id);
        params.add(size);
        currentCall = CallType.READ;
        switchToKernel();
        return (byte[])retVal;
    }

    //Passes on the open call to kernel by leaving the parameter in the params list and setting the
    //call type appropriately
    public static void seek(int id, int to) throws InterruptedException {
        params.clear();
        params.add(id);
        params.add(to);
        currentCall = CallType.SEEK;
        switchToKernel();
    }

    //Passes on the open call to kernel by leaving the parameter in the params list and setting the
    //call type appropriately
    public static int write(int id, byte[] data) throws InterruptedException {
        params.clear();
        params.add(id);
        params.add(data);
        currentCall = CallType.WRITE;
        switchToKernel();
        return (int)retVal;
    }

    //Returns the PID of the current process running in scheduler
    public static int getPid() throws InterruptedException {
        params.clear();
        currentCall = CallType.GET_PID;
        switchToKernel();
        return (int)retVal;
    }

    //Returns the PID specified by the given string by searching through all processes in scheduler
    public static int getPidByName(String name) throws InterruptedException {
        params.clear();
        params.add(name);
        currentCall = CallType.GET_PID_BY_NAME;
        switchToKernel();
        return (int)retVal;
    }

    //Sends a KernelMessage to another process, as specified by the target in message
    public static void sendMessage(KernelMessage km) throws InterruptedException {
        params.clear();
        params.add(km);
        currentCall = CallType.SEND_MESSAGE;
        switchToKernel();
    }

    //Waits for a message to be received from any other process, as indicated by the message queue
    //in the PCB
    public static KernelMessage waitForMessage() throws InterruptedException {
        PCB currentProcess = kernel.getScheduler().getCurrentProcess();
        params.clear();
        currentCall = CallType.WAIT_FOR_MESSAGE;
        switchToKernel();

        //If the kernel indicates that the PCB needs to wait for a message, loops until a message
        //arrives and sets the retVal accordingly so that we don't return a null value
        if(currentProcess.isWaiting()){
            while(true){
                if(currentProcess.hasMessage()){
                    retVal = currentProcess.popMessage();
                    break;
                }
                Thread.sleep(1);
            }
        }
        return (KernelMessage)retVal;
    }

    public static void getMapping(int virtualPage) throws InterruptedException{
        params.clear();
        params.add(virtualPage);
        currentCall = CallType.GET_MAPPING;
        switchToKernel();
    }

    //Allocates the given amount of bytes (size) in memory through kernel, may return -1 if the size
    //is not a multiple of 1024 or there is no space left in the memory, otherwise returns the
    //virtual address where the memory was allocated
    public static int allocateMemory(int size) throws InterruptedException{
        if(size%1024!=0){
            return -1;
        }
        params.clear();
        params.add(size);
        currentCall = CallType.ALLOCATE_MEM;
        switchToKernel();
        return (int) retVal;
    }

    //Frees an amount of memory at the specified address, returns false if the pointer or size is
    //not a multiple of 1024
    public static boolean freeMemory(int pointer, int size) throws InterruptedException{
        if(pointer%1024!=0 || size%1024!=0){
            return false;
        }
        params.clear();
        params.add(pointer);
        params.add(size);
        currentCall = CallType.FREE_MEM;
        switchToKernel();
        return (boolean) retVal;
    }
    
}

