package eci.arep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

public class ServiceFacade {
    public static void main(String[] args) throws IOException, URISyntaxException {
        ServerSocket serverSocket = null;
        boolean running = true;

        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        while(running){
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir 35000 ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            PrintWriter out = new PrintWriter(
                    clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            String inputLine, outputLine;
            boolean isFirstLine = true;
            String firstLine = "";
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Recibí: " + inputLine);
                if (isFirstLine){
                    firstLine = inputLine;
                    isFirstLine = false;
                }
                if (!in.ready()) {break; }
            }
            URI reqURL = getRequestURL(firstLine);
            if (reqURL.getPath().startsWith("/computar")){
                String response = HttpConnection.getResponse("/compreFlex?" + reqURL.getQuery());
                outputLine = getOkResponse(response);
            }else {
                outputLine = htmlClient();
            }
            out.println(outputLine);
            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

    private static String getOkResponse(String data){
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n"
                + data;
    }

    public static String htmlClient(){
        String htmlCode = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n"
                +"<!DOCTYPE html>\n" +
                "<html>\n" +
                "    <head>\n" +
                "        <title>Form Example</title>\n" +
                "        <meta charset=\"UTF-8\">\n" +
                "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    </head>\n" +
                "    <body>\n" +
                "        <h1>Form with GET</h1>\n" +
                "        <form action=\"/hello\">\n" +
                "            <label for=\"name\">Operation:</label><br>\n" +
                "            <input type=\"text\" id=\"name\" name=\"name\" value=\"max\"><br><br>\n" +
                "            <input type=\"button\" value=\"Submit\" onclick=\"loadGetMsg()\">\n" +
                "        </form> \n" +
                "        <div id=\"getrespmsg\"></div>\n" +
                "\n" +
                "        <script>\n" +
                "            function loadGetMsg() {\n" +
                "                let nameVar = document.getElementById(\"name\").value;\n" +
                "                const xhttp = new XMLHttpRequest();\n" +
                "                xhttp.onload = function() {\n" +
                "                    document.getElementById(\"getrespmsg\").innerHTML =\n" +
                "                    this.responseText;\n" +
                "                }\n" +
                "                xhttp.open(\"GET\", \"/computar?comando=\"+nameVar);\n" +
                "                xhttp.send();\n" +
                "            }\n" +
                "        </script>\n" +
                "    </body>\n" +
                "</html>";
        return htmlCode;
    }
    private static URI getRequestURL(String firstLine) throws URISyntaxException {
        String url = firstLine.split(" ")[1];
        return new URI(url);
    }
}


