import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;

public class Main {
    private static Matrix matrix;
    private static MainServer mainServer;
    public static void main(String[] args) {

        matrix = new Matrix(600,600);
        int []threads ={1,2,4,8};
        try {

            for(int j=0;j<10;j++){
                for(int i:threads) {
                    Thread.sleep(1000);
                    long start = System.currentTimeMillis();
                    sort(i);
                    long end = System.currentTimeMillis();
                    System.out.printf("Sorted by this time %.2f`s using threads:%d\n", (end - start) / 1000.0f,i);
                }
            }


        }
        catch (Exception e){
            e.printStackTrace();
        }
        Matrix matrixSorted = new Matrix(mainServer.getArr());
     //   matrixSorted.show();

    }
    public static void sort(int threadNum){
        mainServer= new  MainServer(threadNum,matrix.getInitedMatrix());
        mainServer.start();
        while (!mainServer.getInitState());
        for(int i=0;i<threadNum;i++) {
            new ClientThread().start();
        }
        while (mainServer.isAlive());
    }
}
