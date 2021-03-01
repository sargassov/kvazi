package client;

public class FirstTask {

    private final Object mon = new Object();
    private static char c = 'A';
    private static volatile int count = 5;


    public static void main(String[] args) {
        //1. Создать три потока, каждый из которых выводит определенную букву
        //(А, В, С) 5 раз (порядок АВСАВСАВС)ю Используйте wait/notify/notifyall
        FirstTask task = new FirstTask();

        new Thread(()->{
           task.printA();
        }).start();

        new Thread(()->{
            task.printB();
        }).start();

        new Thread(()->{
            task.printC();
        }).start();

    }
    private void printA(){
        synchronized (mon){
            while(count > 0){
                while(c != 'A'){
                    try {
                        mon.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(count > 0)
                    System.out.print(c);
                c = 'B';
                mon.notifyAll();
            }
        }
    }

    private void printB(){
        synchronized (mon){
            while (count > 0){
                while(c != 'B'){
                    try {
                        mon.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(count > 0)
                    System.out.print(c);
                c = 'C';
                mon.notifyAll();
            }

        }
    }

    private void printC(){
        synchronized (mon){
            while (count > 0){
                while(c != 'C'){
                    try {
                        mon.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(count > 0)
                    System.out.println(c);
                c = 'A';
                count--;
                mon.notifyAll();
            }

        }
    }


}
