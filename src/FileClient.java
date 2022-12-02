import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileClient {
    public static void indexCommand(Socket socket, BufferedInputStream socketIn) throws IOException {
        StringBuilder received = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(socketIn, "ASCII"));
        for (int c = reader.read(); c != -1; c=reader.read()){
            received.append((char)c);
        }
        System.out.println("Available files are listed below:");
        System.out.print(received.toString());
    }

    public static void fileCommand(Socket socket, String fileName, BufferedInputStream socketIn) throws IOException {

        BufferedReader socketReader = new BufferedReader(new InputStreamReader(socketIn));
        String status = socketReader.readLine();
        if (status.equals("ok")){
            Path path = Paths.get("received",fileName);

            BufferedOutputStream bufferedOut = new BufferedOutputStream(new FileOutputStream(path.toString()));
            OutputStreamWriter fileOut = new OutputStreamWriter(bufferedOut, "ASCII"); // file out: fileOut

            for (int c = socketReader.read(); c != -1; c=socketReader.read()){
                fileOut.write(c);
                System.out.print((char)c);
            }
            fileOut.flush();
            System.out.println("\nFile written to: "+path.toString());
        }else{
            System.out.println(status);
        }

    }
    public static void main(String[] args) throws Exception{
        while (true){
            try{
                Socket socket = new Socket("127.0.0.1", 80);
                BufferedReader cliReader = new BufferedReader(new InputStreamReader(System.in)); // Sys in: cliReader
                BufferedInputStream socketIn = new BufferedInputStream(socket.getInputStream());// Socket in: socketIn
                OutputStream socketOut = socket.getOutputStream(); // Socket out: socketOut


                String command = cliReader.readLine();
                if (command.equals("index")){
                    socketOut.write((command+"\r\n").getBytes(Charset.forName("ASCII")));
                    System.out.println("send command: " +command);
                    socketOut.flush();
                    indexCommand(socket, socketIn);
                }else if (command.equals("q")) {
                    break;
                }else{
                    String[] commands = command.split(" ");
                    if ((commands.length<=1) || !commands[0].equals("get")) {
                        System.out.println("Wrong syntax");
                    }else{
                        socketOut.write((command+"\r\n").getBytes(Charset.forName("ASCII")));
                        System.out.println("send command: " +command);
                        socketOut.flush();
                        fileCommand(socket, commands[1], socketIn);
                    }
                }

                socket.close();
            }catch (RuntimeException e){
                e.printStackTrace();
            }

        }




    }
}
