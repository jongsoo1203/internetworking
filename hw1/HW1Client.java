import java.io.*;
import java.net.*;

/*
references:
https://docs.oracle.com/javase/tutorial/networking/sockets/
 */

public class HW1Client {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java HW1Client <server host> <server port>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        try (
                Socket clientSocket = new Socket(hostName, portNumber);
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("Enter the GET command (e.g., GET example.com/index.html):");
            String userInput = stdIn.readLine();
            out.println(userInput);

            // Extract file name from the command (assuming it's after the last "/")
            String fileName = userInput.substring(userInput.lastIndexOf('/') + 1);
            if (fileName.isEmpty()) {
                fileName = "index.html";
            }

            FileOutputStream fileOutput = new FileOutputStream(fileName);
            char[] buffer = new char[8192];
            int bytesRead;
            int totalBytesRead = 0;

            System.out.println("Downloading " + fileName + "...");

            // Start receiving data from the server
            while ((bytesRead = in.read(buffer, 0, buffer.length)) != -1) {
                String data = new String(buffer, 0, bytesRead);
                fileOutput.write(data.getBytes());
                totalBytesRead += bytesRead;

                // Show download progress (example: print progress for every 1 KB)
                System.out.println("Downloaded: " + totalBytesRead / 1024 + " KB");
            }

            fileOutput.close();
            System.out.println("Download complete. File saved as: " + fileName);

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }
    }
}
