package utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by user on 2017/5/23.
 */
public class ThreadUtil {
    private static ExecutorService mThreadpool = Executors.newFixedThreadPool(10);

    public static void runing(Runnable runnable){
        mThreadpool.execute(runnable);
    }





}
