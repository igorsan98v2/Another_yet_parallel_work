import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainServer extends Thread {
    private int[][] arr ;
    private int colForThread;
    private int colForFThread;
    private int getedCol;
    private int colSended;
    private int rows;
    private int columns;
    private int threadsInited;
    private boolean isReady  = false;
    public MainServer(int threads,int [][]arr){
        columns = arr[0].length;
        rows= arr.length;
        this.arr = arr;
        getedCol =0;
        threadsInited =0;
        colSended=0;

        colForFThread = columns/threads + columns%threads;
        colForThread = columns/threads;
        isReady=true;
    }

    @Override
    public  void run() {
        try {

            // Selector: multiplexor of SelectableChannel objects
            Selector selector = Selector.open(); // selector is open here

            // ServerSocketChannel: selectable channel for stream-oriented listening sockets
            ServerSocketChannel ioSocket = ServerSocketChannel.open();
            InetSocketAddress ioAddr = new InetSocketAddress("localhost", 1111);

            // Binds the channel's socket to a local address and configures the socket to listen for connections
            ioSocket.bind(ioAddr);

            // Adjusts this channel's blocking mode.
            ioSocket.configureBlocking(false);

            int ops = ioSocket.validOps();
            SelectionKey selectKy = ioSocket.register(selector, ops, null);

            // Infinite loop..
            // Keep server running
            while (true) {


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

                        SocketChannel ioClient = (SocketChannel) myKey.channel();
                        ByteBuffer ioBuffer = ByteBuffer.allocate((colForFThread*4)+(rows*(colForThread+1)*4)+(4*2));
                        ioClient.read(ioBuffer);
                        String result = new String(ioBuffer.array()).trim();
                        Pattern p = Pattern.compile("start");
                        Matcher m = p.matcher(result);

                        if (m.find()) {


                            p = Pattern.compile("[0-9]");
                            m = p.matcher(result);
                            int port=0;
                            String myPort = new String();
                            while (m.find()){
                                myPort+=m.group();
                            }
                            port+=Integer.parseInt(myPort);

                            sendData(port);
                        }
                        else {

                            getData(ioClient, ioBuffer);
                            if(getedCol==this.columns){
                                ioSocket.close();
                                return;
                            }


                        }
                    }
                    ioIterator.remove();
                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    public int[][]getArr(){
        if(columns==getedCol)return arr;
        return null;
    }
    private  void sendData(int port){
        try {
            InetSocketAddress socketAddress = new InetSocketAddress("localhost", port);
            SocketChannel ioClient = SocketChannel.open(socketAddress);
     //       ioClient.configureBlocking(false);

            sendData(ioClient,null);
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }
    public boolean getInitState(){
        return isReady;
    }
    private  void getData(SocketChannel ioClient,ByteBuffer ioBuffer){
        try {

            int columns = ioBuffer.getInt(0);
            int length = ioBuffer.getInt(4);

            int size = 8 + (columns * length * 4) + columns * 4;

            int colTarget = 0;
            for (int i = 8; i < size; i += 4 * (length + 1)) {
                for (int j = 0; j < length + 1; j++) {
                    if (j == 0) colTarget = ioBuffer.getInt(i);
                    else {
                        arr[j-1][colTarget]=   ioBuffer.getInt(i+(j*4));
                    }
                }
                getedCol++;
            }

            ioBuffer.clear();
            ioClient.close();
        }
        catch (IOException e){
            e.printStackTrace();

        }    }
    private void sendData(SocketChannel ioClient, ByteBuffer ioBuffer){
        try {
            int threadsLim=0;
            byte []arrB = null;
            byte []colNumBuf = null;
            byte []colSizeBuf = null;
            byte []bufArr=null;
            int size =0;
            int  limit = 0;

            if(threadsInited<1){
                //3 4 [1]|1 2 3 4 | [2]|1 2 3 4|    [3]|1 2 3 4|

                colNumBuf =   ByteBuffer.allocate(4).putInt(colForFThread).array();
                size = (colForFThread*4)+(rows*colForFThread*4)+(4*2);//число колонок + байты на массив колнки + байты на остальные метаданные
                limit = colForFThread;
            }
            else {

                colNumBuf = ByteBuffer.allocate(4).putInt(colForFThread).array();
                size =(colForThread*4)+(rows*colForThread*4)+(4*2);
                limit = colForThread;
            }

            byte []toSendSize=  ByteBuffer.allocate(4).putInt(size).array();


            ioClient.write(ByteBuffer.wrap(toSendSize));

           // Thread.sleep(size/1000);
            int z = 0;
            //инициализация массива
            arrB = new byte[size];

            //Записываем число колонок для потока
            for(byte bCol:colNumBuf){
                arrB[z]=bCol;
                z++;
            }
            colSizeBuf =  ByteBuffer.allocate(4).putInt(rows).array();
            //Записываем число строк в колонке
            for(byte bCol:colSizeBuf){
                arrB[z]=bCol;
                z++;
            }
            for(int c = 0;c<limit;c++){
                //Запись индекса колонки
                bufArr =ByteBuffer.allocate(4).putInt(colSended).array();
                for(byte bCol:bufArr){
                    arrB[z]=bCol;
                    z++;
                }
                //Запись массива для сортировки
                for (int i = 0; i < rows; i++) {
                    bufArr = ByteBuffer.allocate(4).putInt(arr[i][colSended]).array();
                    for(byte bCol:bufArr){
                        arrB[z]=bCol;
                        z++;
                    }

                }
                colSended++;
            }

            ioClient.write(ByteBuffer.allocate(4).putInt(size));
            int numBytesWritten = ioClient.write(ByteBuffer.wrap(arrB));
            if(ioBuffer!=null) ioBuffer.clear();
            ioClient.close();
            threadsInited++;
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }
    private static void log(String str) {
        System.out.println(str);
    }

}
