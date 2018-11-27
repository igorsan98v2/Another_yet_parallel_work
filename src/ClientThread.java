import jdk.nashorn.internal.runtime.regexp.joni.Regex;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientThread extends Thread{
    private  InetSocketAddress ioAddr;
    private InetSocketAddress socketAddress;
    private   SocketChannel ioClient;
    private  ServerSocketChannel ioSocket;
    private  byte[]getted;
    private  int columns;
    private  int length;
    private int[]arrCI;
    private int[][]arr;

    @Override
    public void run(){
        try {

            Pattern pattern = Pattern.compile("\\d");
            String s = this.getName();
            Matcher m = pattern.matcher(s);

            int port=1111;
            String num = new String();
            while (m.find()){
                num +=m.group();
            }
            port+=Integer.parseInt( num);

          //  int port =1111+( LocalDateTime.now().getNano()/10000 -LocalDateTime.now().getNano()/10000)*-1;
            Thread.sleep(50);
            InetSocketAddress socketAddress = new InetSocketAddress("localhost", 1111);
            ioClient = SocketChannel.open(socketAddress);
            ioClient.configureBlocking(false);

            byte[] message = new String("start : "+Integer.toString(port)).getBytes();
            ByteBuffer buffer = ByteBuffer.wrap(message);
            ioClient.write(buffer);
            buffer.clear();

            getData(port);




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
            System.out.println(this.getName());
            e.printStackTrace();
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }

    }
    private void sendSorted(byte []sorted){
        try {
            if(sorted!=null){
                InetSocketAddress socketAddress = new InetSocketAddress("localhost", 1111);
                SocketChannel ioClient = SocketChannel.open(socketAddress);

                ByteBuffer buffer = ByteBuffer.wrap(sorted);
                ioClient.write(buffer);
                buffer.clear();
                ioClient.close();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    private void getData(int port){
        try {
            boolean isAll = false;
            boolean isGetSize = false;
            int size = 0;

            ioAddr = new InetSocketAddress("localhost", port);
            ioSocket = ServerSocketChannel.open();
            ioSocket.bind(ioAddr);
            ioSocket.configureBlocking(false);
            int ops = ioSocket.validOps();
            Selector selector = Selector.open();
            SelectionKey selectionKey = ioSocket.register(selector, ops, null);
            while (!isAll) {


                // Selects a set of keys whose corresponding channels are ready for I/O operations
                selector.select();

                // token representing the registration of a SelectableChannel with a Selector
                Set<SelectionKey> ioKeys = selector.selectedKeys();
                Iterator<SelectionKey> ioIterator = ioKeys.iterator();

                while (ioIterator.hasNext()) {
                    SelectionKey myKey = ioIterator.next();

                    // Tests whether this key's channel is ready to accept a new socket connection
                    if (myKey.isAcceptable()) {
                        SocketChannel ioClient = ioSocket.accept();

                        // Adjusts this channel's blocking mode to false
                        ioClient.configureBlocking(false);

                        // Operation-set bit for read operations
                        ioClient.register(selector, SelectionKey.OP_READ);

                        // Tests whether this key's channel is ready for reading
                    } else if (myKey.isReadable()) {
                        if(isGetSize){
                            SocketChannel ioClient = (SocketChannel) myKey.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(size);
                            int read=0;
                            try {
                                
                                Thread.sleep(size/10000);

                                 read  =  ioClient.read(buffer);



                            }
                            catch (InterruptedException e){
                                e.printStackTrace();
                            }

                            getted = new byte[size];

                            //переписываем число столбцов и их строк
                            for(int i=0;i<8;i++){
                                getted[i]=buffer.get(i);
                            }

                            columns = buffer.getInt(0);//индекс 0 число колонок
                            length =buffer.getInt(4);//индекс 4 начало int для числа чисел в колнке

                             arrCI =new int [columns]; //array of column index
                            arr =new int[columns][length];

                            for(int i=8,c=0;i<read;i+=(length+1)*4,c++){
                                arrCI[c] = buffer.getInt(i);
                                for(int j=4,z=0;j-4<length*4;j+=4){
                                    arr[c][z]=buffer.getInt(i+j);

                                    z++;
                                }
                            }


                            ioClient.close();
                            buffer.clear();

                            isAll=true;


                        }
                        else{
                            SocketChannel ioClient = (SocketChannel) myKey.channel();
                            ByteBuffer ioBuffer = ByteBuffer.allocate(4);

                            ioClient.read(ioBuffer);

                            size =ioBuffer.getInt(0);
                            isGetSize = true;

                            ioBuffer.clear();


                        }
                    }
                    ioIterator.remove();
                }
            }
        }
        catch (IOException e){
            log(this.getName()+"port"+port);

            e.printStackTrace();
        }

    }
    private static void log(String str) {
        System.out.println(str);
    }
}
