import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleFileHTTPServer {
    private final byte[] content;
    private final byte[] header;
    private final int port;
    private final String encoding;


    public SingleFileHTTPServer(byte[] content, int port, String encoding, String mimeType) {
        this.content = content;
        this.port = port;
        this.encoding = encoding;
        String header = "HTTP/1.0 200 OK\r\n" +
                "Server: OneFile 2.0\r\n" +
                "Content-length: " + this.content.length + "\r\n" +
                "Content-type: " + mimeType + "; charset=" + encoding + "\r\n\r\n";
        this.header = header.getBytes(Charset.forName("US-ASCII"));
    }

    public void start(){
        ExecutorService pool = Executors.newFixedThreadPool(100);
        try (ServerSocket server = new ServerSocket(this.port)){
            System.out.println("Server listening on port " + server.getLocalPort());
            System.out.println("Data to be sent");
            System.out.println(new String(this.content, encoding));
            while(true){
                try{
                    Socket connection = server.accept();
                    pool.submit(new HTTPHandler(connection));
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

    private class HTTPHandler implements Callable<Void> {
        private final Socket connection;

        private HTTPHandler(Socket connection) {
            this.connection = connection;
        }

        @Override
        public Void call() throws Exception {
            try{
                OutputStream out = new BufferedOutputStream(connection.getOutputStream());
                InputStream in = new BufferedInputStream(connection.getInputStream());
                out.write(header);
                out.write(content);
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
            Path path = Paths.get("public/data1.txt");
            byte[] data = Files.readAllBytes(path);

            String contentType = URLConnection.getFileNameMap().getContentTypeFor("public/data1.txt");
            SingleFileHTTPServer server  = new SingleFileHTTPServer(data, port, encoding, contentType);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e){
            System.out.println("Usage: java SingleFileHTTPServer.java filename port encoding");
        }
    }
}
