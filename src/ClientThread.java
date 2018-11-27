import jdk.nashorn.internal.runtime.regexp.joni.Regex;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientThread extends Thread{
    private  InetSocketAddress ioAddr;
    private  ServerSocketChannel ioSocket;
    private void sendSorted(byte []sorted){
        try {
            InetSocketAddress socketAddress = new InetSocketAddress("localhost", 1111);
            SocketChannel ioClient = SocketChannel.open(socketAddress);





            ByteBuffer buffer = ByteBuffer.wrap(sorted);
            ioClient.write(buffer);
            buffer.clear();
            ioClient.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    @Override
    public void run(){
        try {
            Thread.sleep(50);
            InetSocketAddress socketAddress = new InetSocketAddress("localhost", 1111);
            SocketChannel ioClient = SocketChannel.open(socketAddress);
            ioClient.configureBlocking(false);

            byte[] message = new String("start").getBytes();
            ByteBuffer buffer = ByteBuffer.wrap(message);
            ioClient.write(buffer);
            buffer.clear();
          //  Thread.sleep(15);
            buffer = ByteBuffer.allocate(500);//выделяем 1мб данных 1048576 байта
            Thread.sleep(15);
            int read  =  ioClient.read(buffer);
            byte[]getted = new byte[read];
            //переписываем число столбцов и их строк
            for(int i=0;i<8;i++){
                getted[i]=buffer.get(i);
            }
            Pattern pattern = Pattern.compile("\\d");
            String s = this.getName();
            Matcher m = pattern.matcher(s);

            int port=1111;
            if(m.find()) port+=Integer.parseInt( m.group());
            System.out.println("port"+port);
            ioAddr = new InetSocketAddress("localhost", port);
            ioSocket = ServerSocketChannel.open();
            ioSocket.bind(ioAddr);
            ioSocket.configureBlocking(false);


            



            int columns = buffer.getInt(0);//индекс 0 число колонок
            int length =buffer.getInt(4);//индекс 4 начало int для числа чисел в колнке
            System.out.printf("col:%d\tlen:%d",columns,length);
            int []arrCI =new int [columns]; //array of column index
            int [][]arr =new int[columns][length];

            for(int i=8,c=0;i<read;i+=(length+1)*4,c++){
                arrCI[c] = buffer.getInt(i);
                for(int j=4,z=0;j-4<length*4;j+=4){
                    arr[c][z]=buffer.getInt(i+j);

                    z++;
                }
            }


            ioClient.close();
            buffer.clear();

            int j=8;
            int c=0;
            for(int i=0;i<columns ; i++) {
                //запись индекса массива
                byte[] bufCI = ByteBuffer.allocate(4).putInt(arrCI[c]).array();
                for (byte b : bufCI) {
                    getted[j] = b;
                    j++;
                }
                c++;
                Arrays.sort(arr[i]);
                for (int digit : arr[i]) {
                    byte[] buf = ByteBuffer.allocate(4).putInt(digit).array();
                    for (byte b : buf) {
                        getted[j] = b;
                        j++;
                    }
                }


            }
            sendSorted(getted);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    private static void log(String str) {
        System.out.println(str);
    }
}
