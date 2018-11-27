
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Matrix {
    private int[][] matrix;
    private int [][] initedMatrix;
    private  boolean isInit=false;
    public void show(){
        for(int i=0;i<matrix.length;i++){
            for(int j=0;j<matrix[0].length;j++){
                System.out.printf("%4d ",matrix[i][j]);
            }
            System.out.print("\n");
        }
    }

    public int[][] getInitedMatrix() {
        return matrix;
    }

    public void init(){
        if(isInit==false){
            Random random = new Random();
            for(int i=0;i<matrix.length;i++){
                for(int j=0;j<matrix[i].length;j++){
                    matrix[i][j]= random.nextInt()%100 -random.nextInt()%100;
                }
            }
            isInit = true;

            initedMatrix = new int[matrix.length][matrix[0].length];
            for(int i=0;i<matrix.length;i++){
                for(int j=0;j<matrix[0].length;j++){
                    initedMatrix[i][j]=matrix[i][j];
                }
            }
        }
        else {
     //       matrix =new int[initedMatrix.length][initedMatrix[0].length];
            for(int i=0;i<matrix.length;i++){
                for(int j=0;j<matrix[0].length;j++){
                    matrix[i][j]= initedMatrix[i][j];
                }
            }


        }
    }

    Matrix(int [][]matrix ){
        this.matrix = matrix;
        //init();
    }
    Matrix(int row, int column){
        matrix = new int[row][column];
        init();
    }
}