package co.hypersecure.Utilities;

/**
 * Created by Awanish Raj on 30/06/15.
 */
public class TimingUtil {

    public static final int DIM_MILLIS = 1;
    public static final int DIM_MICROS = 2;
    public static final int DIM_NANOS = 3;

    private int timeDimension = DIM_MILLIS;
    private String dim = "ms";

    long oldTime = 0;

    public TimingUtil() {
        this.oldTime = getNowTime();
    }

    public TimingUtil(int timeDimension) {
        this.timeDimension = timeDimension;
        switch (timeDimension) {
            case DIM_MILLIS:
                dim = "ms";
                break;
            case DIM_MICROS:
                dim = "us";
                break;
            case DIM_NANOS:
                dim = "ns";
                break;
        }
        this.oldTime = getNowTime();

    }

    public void init() {
        oldTime = getNowTime();
    }

    public void pitch(String log_tag, String message) {
        Slog.e(log_tag + "TIM", message + " ***** Time taken: " + (getTimeDifference()));
        oldTime = getNowTime();
    }

    public long getNowTime() {
        switch (timeDimension) {
            case DIM_MILLIS:
                return System.currentTimeMillis();
            case DIM_MICROS:
                return System.nanoTime() / 1000;
            case DIM_NANOS:
                return System.nanoTime();
        }
        return System.currentTimeMillis();
    }


    public String getTimeDifference() {
        return (getNowTime() - oldTime) + " " + dim;
    }

    public Long getTimeDifferenceLong() {
        return (getNowTime() - oldTime);
    }
}
