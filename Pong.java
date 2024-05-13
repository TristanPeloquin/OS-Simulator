import java.util.Arrays;

public class Pong extends UserlandProcess{
    public Pong(){
        super();
    }

    public void main() throws InterruptedException {
        KernelMessage outgoing = new KernelMessage(OS.getPid(), OS.getPidByName("Ping"),
                0, new byte[]{0});
        KernelMessage incoming;
        System.out.println("Pong initialized, Ping PID = " + outgoing.getTpid());
        while(true){
            incoming = OS.waitForMessage();
            System.out.println("Pong received message from " +  incoming.getSpid() +
                    " with contents " + Arrays.toString(incoming.getData()));
            outgoing.setData(new byte[]{(byte) (incoming.getData()[0]+1)});
            System.out.println("Pong sending message from " + outgoing.getSpid() +
                    " to " + outgoing.getTpid() + " with contents "
                    + Arrays.toString(outgoing.getData()));
            OS.sendMessage(outgoing);
            cooperate();
            Thread.sleep(50);
        }
    }
}
