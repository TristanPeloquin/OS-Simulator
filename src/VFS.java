//"Virtual File System", stores an array of DeviceIDPair, indicating the device type and the id to
//find the respective device inside. Translates calls from the kernel using this array for open,
//read, write, seek, close.
public class VFS implements Device{

    private static DeviceIDPair[] devices = new DeviceIDPair[10];

    //Opens a new device depending on the string s, for example "random 100" will create a random
    //device and pass the remaining string "100" to the random device as the seed
    public int open(String s) {
        //Splits the string into two parts - essentially the first word and the remainder
        String[] params = s.split(" ", 2);
        switch(params[0]){
            case "random":
                //Loops through the devices array to find an open spot
                for(int i = 0; i<devices.length; i++){
                    if(devices[i]==null){
                        devices[i] = new DeviceIDPair(new RandomDevice());
                        //Sets the id (index in the devices array) to refer to in future operations
                        devices[i].setID(devices[i].getDevice().open(params[1]));
                        return i;
                    }
                }
            case "file":
                //Loops through the devices array to find an open spot
                for(int i = 0; i<devices.length; i++){
                    if(devices[i]==null){
                        devices[i] = new DeviceIDPair(new FakeFileSystem());
                        //Sets the id (index in the devices array) to refer to in future operations
                        devices[i].setID(devices[i].getDevice().open(params[1]));
                        return i;
                    }
                }
            default:
                return -1;
        }
    }

    //Passes on the close call to the device and nulls the entry
    public void close(int id) {
        devices[id].getDevice().close(devices[id].getID());
        devices[id] = null;
    }

    //Passes on the read call to the device
    public byte[] read(int id, int size) {
        return devices[id].getDevice().read(devices[id].getID(), size);
    }

    //Passes on the seek call to the device
    public void seek(int id, int to) {
        devices[id].getDevice().seek(devices[id].getID(), to);
    }

    //Passes on the write call to the device
    public int write(int id, byte[] data) {
        return devices[id].getDevice().write(devices[id].getID(), data);
    }
}
