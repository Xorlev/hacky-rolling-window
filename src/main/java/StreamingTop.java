import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.*;

/**
 * Hacky bullshit rolling window stuff
 * 2012-09-20
 *
 * @author Michael Rose <michael@fullcontact.com>
 */
public class StreamingTop {
    static Map<String, String> mappings = new HashMap<String, String>(){{

    }};
    static PriorityBlockingQueue<DataPoint> rollingWindow = new PriorityBlockingQueue<DataPoint>(8, new Comparator<DataPoint>() {
        public int compare(DataPoint o1, DataPoint o2) {
            return o1.getCreatedAt().compareTo(o2.getCreatedAt());
        }
    });

    static Long cadence = 5000l;

    static ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    public static void main(String[] args) throws Exception {
        File file = new File(args[0]);
        if (!file.exists()) {
            System.err.println("Cannot open file '" + args[0] + "'");
            System.exit(1);
        }

        try {
            cadence = Long.parseLong(args[1]);
        } catch(NumberFormatException e) { }

        BufferedReader reader = new BufferedReader(new FileReader(file));

        service.scheduleAtFixedRate(new Runnable() {
            public void run() {
                expireEntries(); //TODO: remove?
                snapshotData(rollingWindow);
            }
        }, cadence, cadence, TimeUnit.MILLISECONDS);

        while(true) {
            if (reader.ready()) {
                rollingWindow.add(new DataPoint(reader.readLine(), 1));
                expireEntries();
            } else {
                Thread.sleep(50);
            }
        }

    }

    private static void expireEntries() {
        Date expirationThreshold = new Date();
        expirationThreshold.setTime(System.currentTimeMillis() - cadence);

        while (rollingWindow.peek() != null && rollingWindow.peek().getCreatedAt().before(expirationThreshold)) {
            System.out.println("Expiring entries...");
            rollingWindow.poll();
        }
    }

    private static void snapshotData(AbstractQueue<DataPoint> fiveSeconds) {Map<String, Integer> keys = new TreeMap<String, Integer>();
        Iterator<DataPoint> it = fiveSeconds.iterator();
        while(it.hasNext()) {
            DataPoint point = it.next();
            if (!keys.containsKey(point.getKey())) {
                keys.put(point.getKey(), 1);
            } else {
                keys.put(point.getKey(), keys.get(point.getKey()) + 1);
            }
        }

        SortedSet<Map.Entry<String, Integer>> set = new TreeSet<Map.Entry<String, Integer>>(new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        set.addAll(keys.entrySet());
        for (Map.Entry<String, Integer> entry : set) {
            String name = entry.getKey();

            if (mappings.containsKey(name)) name = mappings.get(name);

            System.out.println(entry.getValue() + " - " + name);
        }
    }
}
