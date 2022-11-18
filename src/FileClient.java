import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class FileClient {
    public static final int POST_SIZE = 1000;
    public static String f = "received/received1.txt";
    public static void main(String[] args) throws Exception{
        Socket socket = new Socket("127.0.0.1", 80);
        byte[] contents = new byte[POST_SIZE];

        InputStream in = socket.getInputStream();
        InputStreamReader reader = new InputStreamReader(in, "ASCII");
        StringBuilder received = new StringBuilder();
        for (int c = reader.read(); c != -1; c=reader.read()){
            received.append((char) c);
        }
        System.out.println(received.toString());
        socket.close();
        System.out.println("File received");

    }
}
