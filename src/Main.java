import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;

public class Main {
    private static Matrix matrix;
    private static MainServer mainServer;
    public static void main(String[] args) {

        matrix = new Matrix(600,600);
        try {


                
                long start = System.currentTimeMillis();
                Thread.sleep(200);
                sort(2);
                long end = System.currentTimeMillis();

                System.out.printf("Sorted by this time %f", (end - start) / 100.0f);

        }
        catch (InterruptedException e){
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
