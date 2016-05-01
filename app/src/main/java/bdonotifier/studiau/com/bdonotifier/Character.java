package bdonotifier.studiau.com.bdonotifier;

/**
 * Created by Daniel Au on 4/27/2016.
 */
public class Character {

    int id;
    String name;
    float energy;
    long lastTimeStamp;

    public Character(){}

    public Character(String name, float energy, long timeStamp) {
        super();
        this.name = name;
        this.energy = energy;
        lastTimeStamp = timeStamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLastTimeStamp() {
        return lastTimeStamp;
    }

    public void setLastTimeStamp(long lastTimeStamp) {
        this.lastTimeStamp = lastTimeStamp;
    }

    public float getEnergy() {
        return energy;
    }

    public void setEnergy(float energy) {
        this.energy = energy;
    }

    @Override
    public String toString() {
        return "Character [id=" + id + ", name=" + name + ", energy=" + energy +
                ", lastTimeStamp=" + lastTimeStamp + "]";
    }
}
