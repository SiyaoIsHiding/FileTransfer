import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileHTTPServer {
    private final int port;
    private final String encoding;


    public FileHTTPServer(int port, String encoding) {
        this.port = port;
        this.encoding = encoding;
    }

    public void start(){
        ExecutorService pool = Executors.newFixedThreadPool(100);
        try (ServerSocket server = new ServerSocket(this.port)){
            System.out.println("Server listening on port " + server.getLocalPort());
            System.out.println("Data to be sent");
            while(true){
                try{
                    Socket connection = server.accept();
                    pool.submit(new SendSingle(connection));
                }catch(IOException e){
                    e.printStackTrace();
                }catch(RuntimeException e){
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class SendSingle implements Callable<Void> {
        private final Socket connection;

        private SendSingle(Socket connection) {
            this.connection = connection;
        }

        @Override
        public Void call() throws Exception {
            try{
                Path path = Paths.get("public/data1.txt");
                byte[] data = Files.readAllBytes(path);
                OutputStream out = new BufferedOutputStream(connection.getOutputStream());
                InputStream in = new BufferedInputStream(connection.getInputStream());
                out.write(data);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                connection.close();
            }
            return null;
        }
    }

    public static void main(String[] args){
        int port = 80;
        String encoding = "US-ASCII";
        try {
            FileHTTPServer server  = new FileHTTPServer(port, encoding);
            server.start();

        } catch (ArrayIndexOutOfBoundsException e){
            System.out.println("Usage: java SingleFileHTTPServer.java filename port encoding");
        }
    }
}
