import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        Matrix matrix = new Matrix(6,4);
        MainServer mainServer= new  MainServer(2,matrix.getInitedMatrix());
        mainServer.start();
        new ClientThread().start();
        new ClientThread().start();
        while (mainServer.isAlive());
        System.out.println("Sorted");
        Matrix matrixSorted = new Matrix(mainServer.getArr());

    }
}
