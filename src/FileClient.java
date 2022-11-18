import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Socket;

public class FileClient {
    public static final int POST_SIZE = 1000;
    public static String f = "received/received1.txt";
    public static void main(String[] args) throws Exception{
        Socket socket = new Socket("127.0.0.1", 5000);
        byte[] contents = new byte[POST_SIZE];

        InputStream in = socket.getInputStream();
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
        int received;
        while ((received = in.read()) != -1){
            out.write(contents, 0, received);
            System.out.println(contents);
        }
        out.flush();
        socket.close();
        System.out.println("File received");

    }
}
