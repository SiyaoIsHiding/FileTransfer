import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class FileHTTPServer {
    private final int port;
    private final String folder;
    public FileHTTPServer(int port, String folder) {
        this.port = port;
        this.folder = folder;
    }

    public void start(){
        ExecutorService pool = Executors.newFixedThreadPool(100);
        try (ServerSocket server = new ServerSocket(this.port)){
            System.out.println("Server listening on port " + server.getLocalPort());
            while(true){
                try{
                    Socket connection = server.accept();
                    pool.submit(new SendSingle(connection, this.folder)); // Multi-threads!
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
        private final String folder;

        private SendSingle(Socket connection, String folder) {
            this.connection = connection;
            this.folder = folder;
        }

        @Override
        public Void call() throws Exception {
            try {
                OutputStream out = new BufferedOutputStream(connection.getOutputStream());
                InputStream in = new BufferedInputStream(connection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "ASCII"));

                String command = reader.readLine();
                if (command == null){
                    return null; //TODO: ask TA!
                }
                System.out.println("received command: " + command);
                String[] commands = command.split(" ");
                if (command.equals("index")) {
                    Path path = Paths.get(this.folder);
                    Path[] availableFiles = Files.list(path).toArray(Path[]::new);
                    StringBuilder toSend = new StringBuilder();
                    for (int i = 0; i < availableFiles.length; i++) {
                        toSend.append(availableFiles[i].getFileName() + "\r\n");
                    }
                    System.out.println(toSend);

                    out.write(toSend.toString().getBytes(Charset.forName("US-ASCII")));
                    out.flush();
                } else {
                    Path path = Paths.get(this.folder, commands[1]);

                    String err = "error\r\n";
                    String ok = "ok\r\n";
                    try {
                        byte[] data = Files.readAllBytes(path); //IOException
                        out.write(ok.getBytes(Charset.forName("ASCII")));
                        out.write(data);
                        out.flush();
                    } catch (Exception e){
                        out.write(err.getBytes(Charset.forName("ASCII")));
                        out.flush();
                    }
                }

            } catch (InvalidPathException e){

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
        String folder = args.length > 0? args[0] : "public";
        String encoding = "US-ASCII";
        try {
            FileHTTPServer server  = new FileHTTPServer(port, folder);
            server.start();

        } catch (ArrayIndexOutOfBoundsException e){
            System.out.println("Missing folder name");
        }
    }
}
