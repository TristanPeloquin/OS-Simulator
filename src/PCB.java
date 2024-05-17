import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedList;

//Represents a process control block, which wraps a userland process and manages the resources and
//information assigned to said process
public class PCB {
    private static int nextPID;
    private int pid;
    private UserlandProcess up;
    private Priority priority;

    //This represents the time that kernel should wake this process up at
    private Instant wakeTime;

    //Once this counter surpasses 5, demotes the process down a priority
    private int demotionCounter;

    private int[] deviceIDs = new int[10];
    private String name;
    private boolean waiting;

    //The queue of messages being sent to this PCB - if the process calls waitForMessage(), it will
    //pop the first message off the list
    private LinkedList<KernelMessage> messages;

    private int[] pageTable = new int[100];

    public PCB(UserlandProcess up, Priority priority){
        this.up = up;
        this.priority = priority;
        pid = up.getPID();
        Arrays.fill(deviceIDs, -1);
        name = up.getClass().getSimpleName();
        messages = new LinkedList<>();
        waiting = false;
        Arrays.fill(pageTable, -1);
    }

    public PCB(UserlandProcess up){
        this.up = up;
        priority = Priority.INTERACTIVE;
        pid = up.getPID();
        Arrays.fill(deviceIDs, -1);
        name = up.getClass().getSimpleName();
        messages = new LinkedList<>();
        waiting = false;
        Arrays.fill(pageTable, -1);
    }

    //Wrapped version of userland process stop(), also waits until the process starts again
    public void stop() throws InterruptedException {
        up.stop();
        while(up.isStopped()){
            Thread.sleep(10);
        }
    }

    public boolean isDone(){
        return up.isDone();
    }

    public void run(){
        up.start();
    }

    public UserlandProcess getProcess(){
        return up;
    }

    public int getPid(){
        return pid;
    }

    public Priority getPriority(){
        return priority;
    }

    //Used by scheduler to update the wakeTime variable
    public void addTime(int millis){
        wakeTime = Instant.now().plusMillis(millis);
    }

    public Instant getWakeTime(){
        return wakeTime;
    }

    //Demotes this process a priority if the counter (indicating the process has not slept since
    //the last # of switches) surpasses 5
    public void demote(){
        demotionCounter++;
        if(demotionCounter > 5){
            switch (priority){
                case REAL_TIME -> priority = Priority.INTERACTIVE;
                case INTERACTIVE -> priority = Priority.BACKGROUND;
            }
        }
    }

    //Indicates that the process has slept
    public void promote(){
        demotionCounter = 0;
    }

    //Finds an empty spot in the id array and returns its index - returns -1 if no empty spot
    public int getEmptyID(){
        for(int i = 0; i<deviceIDs.length; i++){
            if(deviceIDs[i]==-1){
                return i;
            }
        }
        return -1;
    }

    public void addID(int index, int id){
        deviceIDs[index] = id;
    }

    public int getID(int index){
        return deviceIDs[index];
    }

    public void clearID(int index) {
        deviceIDs[index] = -1;
    }

    public String getName(){
        return name;
    }

    public void addMessage(KernelMessage message){
        messages.add(message);
    }

    public boolean hasMessage(){
        return !messages.isEmpty();
    }

    public KernelMessage popMessage(){
        return messages.pop();
    }

    public boolean isWaiting(){
        return waiting;
    }

    public void setWaiting(boolean waiting){
        this.waiting = waiting;
    }

    //Gets the physical page number from the page table and updates the TLB accordingly, may kill
    //itself if the address is outside the allocated space
    public void getMapping(int virtualPage, Scheduler scheduler){
        if(pageTable[virtualPage] == -1){
            scheduler.kill(this);
        }
        int index = (int)(Math.random()*2);
        int physicalPage = pageTable[virtualPage];
        up.updateTlb(index, virtualPage, physicalPage);
    }

    //Used in scheduler to reset the TLB before the process runs again
    public void clearTlb() {
        up.updateTlb(0, 0, 0);
        up.updateTlb(1, 0, 0);
    }

    //Used in allocate memory to allow for pages to be mapped in the page table, mapping the index
    //(virtual page number) to the physical page number
    public void mapPage(int physicalPage){
        for(int i = 0; i<pageTable.length; i++){
            if(pageTable[i] == -1){
                pageTable[i] = physicalPage;
                break;
            }
        }
    }
}
