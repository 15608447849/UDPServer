package utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by user on 2017/5/23.
 */
public class ThreadUtil {
    private static ExecutorService mThreadpool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    public static void runing(Runnable runnable){
        mThreadpool.execute(runnable);
    }
}
