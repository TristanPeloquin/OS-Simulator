
//Represents a message, or packet of data, that a process can send from itself to another process as
//specified by the sender pid (spid) and the target pid (tpid).
public class KernelMessage {

    private int spid;
    private int tpid;

    private int mode;
    private byte[] data;

    //Copy constructor to ensure that the message that the target receives is not pointing to the
    //same area in memory, keeping processes separated
    public KernelMessage(KernelMessage message){
        spid = message.getSpid();
        tpid = message.getTpid();
        mode = message.getMode();
        data = message.getData();
    }

    //Default constructor
    public KernelMessage(int spid, int tpid, int mode, byte[] data){
        this.spid = spid;
        this.tpid = tpid;
        this.mode = mode;
        this.data = data;
    }

    //The following are simply accesors and mutators for userland and kerneland
    public int getSpid() {
        return spid;
    }

    public int getTpid() {
        return tpid;
    }

    public int getMode() {
        return mode;
    }

    public byte[] getData() {
        return data;
    }

    public void setSpid(int spid) {
        this.spid = spid;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
