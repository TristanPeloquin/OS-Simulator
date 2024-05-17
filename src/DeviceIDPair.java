public class DeviceIDPair {

    private Device device;
    private int id;

    public DeviceIDPair(Device device){
        this.device = device;
    }

    public DeviceIDPair(Device device, int id){
        this.device = device;
        this.id = id;
    }

    public int getID(){return id;}

    public void setID(int id){this.id = id;}

    public Device getDevice(){return device;}

}
