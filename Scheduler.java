import java.time.Clock;
import java.util.*;

//Handles the creation and switching of processes within the kernel, sleeping processes queueing
// processes for running within three priority lists - real time, interactive, and background
// processes. These are picked from probabilistic (6/10, 3/10, 1/10 respectively) and can be demoted
// if they do not sleep often enough.
public class Scheduler {

    private LinkedList<PCB> realTimes;
    private LinkedList<PCB> interactives;
    private LinkedList<PCB> backgrounds;
    private Timer timer;
    private PCB currentProcess;
    private Clock clock;
    private LinkedList<PCB> sleepList;
    private Kernel kernel;
    private HashMap<Integer, PCB> processes;
    private HashMap<Integer, PCB> waitList;

    public Scheduler(Kernel kernel){
        realTimes = new LinkedList<>();
        interactives = new LinkedList<>();
        backgrounds = new LinkedList<>();
        sleepList = new LinkedList<>();
        timer = new Timer(true);
        this.kernel = kernel;
        currentProcess = null;
        clock = Clock.systemDefaultZone();
        processes = new HashMap<>();
        waitList = new HashMap<>();

        //Initializes the timer, which requests the current process to stop so that it can switch
        //to the next process in the queue
        timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
                if(currentProcess!=null) {
                    currentProcess.getProcess().requestStop();
                }
            }
        },0,250);
    }

    //Used in kernel to create a process, this adds the given process to the given priority queue and
    // switches the current one if it is finished
    //Returns the PID of the current process
    public int createProcess(PCB pcb, Priority priority){
        if(currentProcess == null){
            currentProcess = pcb;
            return currentProcess.getPid();
        }
        addToQueue(pcb, priority);
        processes.put(pcb.getPid(), pcb);
        if(currentProcess.isDone()){
            switchProcess();
        }
        return currentProcess.getPid();
    }

    //Used in kernel to create a process, this adds the given process to the interactive queue and
    //switches the current one if it is finished
    //Returns the PID of the current process
    public int createProcess(PCB pcb){
        //Runs if the kernel is at startup to initialize the current process
        if(currentProcess == null){
            currentProcess = pcb;
            return currentProcess.getPid();
        }
        interactives.add(pcb);
        processes.put(pcb.getPid(), pcb);
        if(currentProcess.isDone()){
            switchProcess();
        }
        return currentProcess.getPid();
    }

    //Handles the switching of processes, adding the current process to the appropriate queue and
    //setting it to a randomly biased pick from one of the queues
    public void switchProcess() {
        //Loops through the lists and checks if there are any available ones to wake up, then adds
        //them back to the queues
        Iterator<PCB> iterator = sleepList.iterator();
        PCB pcb;
        while(iterator.hasNext()){
            pcb = iterator.next();
            if(pcb.getWakeTime().isBefore(clock.instant())){
                iterator.remove();
                addToQueue(pcb, pcb.getPriority());
            }

        }

        if(currentProcess != null) {
            if(!currentProcess.isDone()) {
                //Note - this does not actually demote the process, only increments the counter
                //within the PCB to indicate that it has switched without sleeping
                currentProcess.demote();

                currentProcess.clearTlb();
                addToQueue(currentProcess, currentProcess.getPriority());
            }
            //if the current process is done, we want to ensure all related devices are closed
            else{
                for(int i = 0; i<10; i++){
                    kernel.close(i);
                }
                processes.remove(currentProcess.getPid());
            }
        }
        setCurrentProcess();
    }

    public void kill(PCB pcb){
        processes.remove(pcb.getPid());
        setCurrentProcess();
    }

    //Helper method primarily for readability, used in switchProcess()
    private void addToQueue(PCB pcb, Priority priority){
        switch (priority){
            case REAL_TIME -> realTimes.add(pcb);
            case INTERACTIVE -> interactives.add(pcb);
            case BACKGROUND -> backgrounds.add(pcb);
        }
    }

    public PCB getCurrentProcess() {
        return currentProcess;
    }

    //Allows a process to sleep for a specified amount of time by updating the variable inside
    //the process to indicate what time it should wake up and adding to the list to allow us to
    //search for programs which can awoken
    public void sleep(int millis) {
        currentProcess.promote();
        currentProcess.addTime(millis);
        sleepList.add(currentProcess);
        setCurrentProcess();
    }

    //Helper method for code readability/reuse which determines what list to get the current process
    //from
    private void setCurrentProcess(){
        //System.out.println(currentProcess.getProcess().getClass().getName() + ": " + currentProcess.getPriority());

        double rand = Math.random();

        //Firstly checks if the list is empty and then if rand is within the probability, then pop
        //off of said list to set the process
        if(!realTimes.isEmpty() && rand <= .6){
            currentProcess = realTimes.pop();
        }
        else if(!interactives.isEmpty() && rand <= .9){
            currentProcess = interactives.pop();
        }
        else if(!backgrounds.isEmpty()){
            currentProcess = backgrounds.pop();
        }

        //This case should run if the chosen list from rand or backgrounds were all empty, we now
        //indiscriminately look for a process to set so that the system can continue running - if
        //all else fails, we wake a process from the sleep list
        else{
            if(!realTimes.isEmpty()){
                currentProcess = realTimes.pop();
            }
            else if(!interactives.isEmpty()){
                currentProcess = interactives.pop();
            }
            else{
                currentProcess = sleepList.pop();
            }
        }
    }

    public int getPid() {
        return currentProcess.getPid();
    }

    //Checks all lists of processes to find the process pid with the given name, returning -1 if no
    //process was found
    public int getPidByName(String name) {
        for(Map.Entry<Integer, PCB> process : processes.entrySet()){
            if(process.getValue().getName().equals(name)){
                return process.getKey();
            }
        }
        return -1;
    }

    //Finds the target process and adds the message to its queue of messages
    public void sendMessage(KernelMessage km) {
        km = new KernelMessage(km);

        //Ensures that the sender is using the correct pid for security
        km.setSpid(currentProcess.getPid());

        //Uses the map of processes to get the target pid
        PCB target = processes.get(km.getTpid());

        //Adds the message to the target processes queue and removes it from the wait list to ensure
        //that it receives the message
        if(target != null){
            target.addMessage(km);
            if(waitList.containsValue(target)){
                waitList.remove(target.getPid());
                addToQueue(target, target.getPriority());
            }
        }
    }

    //Has two possible outcomes: either the message is in already in the PCB, or the message has yet
    //to arrive, meaning we should wait
    public KernelMessage waitForMessage() {
        //If the PCB already has a message, indicates to the OS that we don't need to wait and
        //returns the message
        if(currentProcess.hasMessage()){
            currentProcess.setWaiting(false);
            return currentProcess.popMessage();
        }
        //If the message has yet to arrive, dequeues the process and indicates to the OS that the
        //PCB should wait until it has a message to return
        waitList.put(currentProcess.getPid(), currentProcess);
        currentProcess.setWaiting(true);
        setCurrentProcess();
        return null;
    }
}
