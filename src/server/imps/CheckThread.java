package server.imps;

/**
 * Created by lzp on 2017/5/23.
 *
 */
public class CheckThread extends Thread {
    private Action action;

    private int time = 10;

    public CheckThread(Action action, int time) {
        this.action = action;
        this.time = time;
        start();
    }

    @Override
    public void run() {
        while (true){
            try{
                synchronized (action){
                    action.wait(10 * 1000);
                }
                action.check();
            }catch (Exception e){

            }
        }

    }

    public interface Action{
        void check();
    }
}
