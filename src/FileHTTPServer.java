import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class FileHTTPServer {
    private final int port;
    public FileHTTPServer(int port) {
        this.port = port;
    }

    public void start(){
        ExecutorService pool = Executors.newFixedThreadPool(100);
        try (ServerSocket server = new ServerSocket(this.port)){
            System.out.println("Server listening on port " + server.getLocalPort());
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
                OutputStream out = new BufferedOutputStream(connection.getOutputStream());
                InputStream in = new BufferedInputStream(connection.getInputStream());
                InputStreamReader reader = new InputStreamReader(in, "ASCII");

                Path path = Paths.get("public/");
//                byte[] data = Files.readAllBytes(path);
                Path[] availableFiles = Files.list(path).toArray(Path[]::new);
                StringBuilder toSend = new StringBuilder();
                for (int i = 0; i < availableFiles.length; i++){
                    toSend.append(availableFiles[i].getFileName() + "\r\n");
                }
                System.out.println(toSend);

                out.write(toSend.toString().getBytes(Charset.forName("US-ASCII")));
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (RuntimeException e){
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
            FileHTTPServer server  = new FileHTTPServer(port);
            server.start();

        } catch (ArrayIndexOutOfBoundsException e){
            System.out.println("Usage: java SingleFileHTTPServer.java filename port encoding");
        }
    }
}
