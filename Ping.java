import java.util.Arrays;

public class Ping extends UserlandProcess{
    public Ping(){
        super();
    }

    public void main() throws InterruptedException {
        KernelMessage outgoing = new KernelMessage(OS.getPid(), OS.getPidByName("Pong"),
                0, new byte[]{0});
        KernelMessage incoming = null;
        System.out.println("Ping initialized, Pong PID = " + outgoing.getTpid());
        while(true){
            System.out.println("Ping sending message from " + outgoing.getSpid() + " to " +
                    outgoing.getTpid() + " with contents " + Arrays.toString(outgoing.getData()));
            OS.sendMessage(outgoing);
            incoming = OS.waitForMessage();
            System.out.println("Ping received message from " +  incoming.getSpid() +
                    " with contents " + Arrays.toString(incoming.getData()));
            outgoing.setData(new byte[]{(byte) (incoming.getData()[0]+1)});
            cooperate();
            Thread.sleep(50);
        }
    }
}
