import java.io.*;
import java.net.*;

/*
references:
https://docs.oracle.com/javase/tutorial/networking/sockets/
 */

public class HW1Server {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java HW1Server <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Proxy Server is running on port: " + portNumber);

            while (true) {
                new ProxyClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }
}

class ProxyClientHandler extends Thread {
    private Socket clientSocket;

    public ProxyClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try (
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            // Print client's IP address
            System.out.println("Connected to client: " + clientSocket.getInetAddress());

            // Read client request (URL of the target HTML)
            String clientInput = in.readLine();
            if (clientInput != null) {
                // Extract host and path from URL (simplified for this example)
                String[] urlParts = clientInput.split("/");
                String host = urlParts[0];
                String filePath = "/" + String.join("/", java.util.Arrays.copyOfRange(urlParts, 1, urlParts.length));

                // Create a connection to the target web server
                try (Socket webSocket = new Socket(host, 80);
                     PrintWriter webOut = new PrintWriter(webSocket.getOutputStream(), true);
                     BufferedReader webIn = new BufferedReader(new InputStreamReader(webSocket.getInputStream()))) {

                    // Send HTTP GET request to the target server
                    webOut.println("GET " + filePath + " HTTP/1.1");
                    webOut.println("Host: " + host);
                    webOut.println();

                    // Read response from the web server
                    String inputLine;
                    StringBuilder htmlContent = new StringBuilder();
                    while ((inputLine = webIn.readLine()) != null) {
                        htmlContent.append(inputLine).append("\n");
                    }

                    out.println(htmlContent.toString());

                    // Save the HTML file locally (graduate requirement)
                    String fileName = "proxy-" + filePath.substring(filePath.lastIndexOf('/') + 1);
                    saveHtmlFile(fileName, htmlContent.toString());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveHtmlFile(String fileName, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
