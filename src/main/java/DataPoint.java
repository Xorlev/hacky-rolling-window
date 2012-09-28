import java.util.Date;

/**
 * 2012-09-20
 *
 * @author Michael Rose <michael@fullcontact.com>
 */
public class DataPoint {
    private final Date createdAt = new Date();
    String key;
    int value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public DataPoint(String key, int value) {

        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return "DataPoint{" +
                "createdAt=" + createdAt +
                ", value=" + value +
                '}';
    }
}
