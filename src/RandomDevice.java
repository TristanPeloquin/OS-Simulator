import java.util.Random;

//Represents a random number generator, such as random in unix, which contains user created
//instances of the Random class in java
public class RandomDevice implements Device{

    private static Random[] randoms = new Random[10];

    //Takes in a String and assumes that if it is not empty, it is the seed for the new Random java
    //object that will be inserted into the array at the first empty spot
    public int open(String s){
        if(s!=null && !s.isEmpty()){
            //Loops until an empty spot is found in the array
            for(int i = 0; i<randoms.length; i++) {
                if(randoms[i]==null){
                    randoms[i] = new Random(Long.parseLong(s));
                    return i;
                }
            }
        }
        else {
            //Loops until an empty spot is found in the array
            for(int i = 0; i<randoms.length; i++) {
                if(randoms[i]==null){
                    randoms[i] = new Random();
                    return i;
                }
            }
        }
        //returns -1 if we did not find an empty spot to create the new random object
        return -1;
    }

    public void close(int id){
        randoms[id] = null;
    }

    //Reads the next amount of bytes specified by the size and returns them
    public byte[] read(int id, int size){
        byte[] retVal = new byte[size];
        randoms[id].nextBytes(retVal);
        return retVal;
    }

    public int write(int id, byte[] data) {
        return 0;
    }

    //Reads in random numbers but does not return them
    public void seek(int id, int to) {
        byte[] bytes = new byte[to];
        randoms[id].nextBytes(bytes);
    }
}
