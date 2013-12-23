package webSrvr;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: Imran
 * Date: 11/19/13
 * Time: 11:03 PM
 * To change this template use File | Settings | File Templates.
 */

public class HttpRequestHandler implements Runnable {

    final static String CarriageReturnLineFeed = "\r\n";

    private InputStream inputStream = null;
    private DataOutputStream dataOutputStream = null;
    private BufferedReader bufferedReader = null;

    FileInputStream fileInputStream = null;
    Socket socket;

    public HttpRequestHandler(Socket socket) throws Exception{
        this.socket = socket;
    }

    public void run(){

        try {
            readGetAndPostRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    * This method is for reading GET/POST requests
    * and pass the requestLine to the corresponding method to process the request further.
    */

    public void readGetAndPostRequest() throws Exception {

        try {
            inputStream = socket.getInputStream();
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        int inputStreamReader;
        int contentLengthOfPostRequest = 0;

        String requestLine = "";

        try {
            while( ( inputStreamReader = inputStream.read() ) != -1) {

                if(inputStreamReader == '\n'){

                    System.out.println(requestLine);

                    if(requestLine.startsWith("Content-Length: ")) {

                        //Calculation of the content length
                        int index = requestLine.indexOf(':') + 1;
                        String len = requestLine.substring(index).trim();
                        contentLengthOfPostRequest = Integer.parseInt(len);
                    }

                    if(requestLine.startsWith("GET")){
                        processGETRequest(requestLine);
                        break;
                    }

                    //POST Request Headers Printing
                    requestLine = "";

                }else {

                    System.out.print((char) inputStreamReader);

                    requestLine += (char) inputStreamReader;
                    if(requestLine.startsWith(("Name"))){
                        if(requestLine.length()==contentLengthOfPostRequest){
                            break;
                        }
                    }
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally{
            closeStream();
        }
    }

    private void processGETRequest(String requestLine) throws Exception{

        boolean fileExists = true;

        try{
            fileInputStream = new FileInputStream(HttpRequestHandler.getLocationOfRequestedResource(requestLine));
        }catch(FileNotFoundException e){
            fileExists = false;
            e.printStackTrace();
        }

        //Construct the response message
        String statusLineOfRequestedResource = null;
        String contentTypeOfRequestedResource = null;
        String entityBody = null;


        if(fileExists){

            statusLineOfRequestedResource = "HTTP/1.0 200 OK" + CarriageReturnLineFeed;
            contentTypeOfRequestedResource = "Content-type: "+ resourceType(HttpRequestHandler.getLocationOfRequestedResource(requestLine))+ CarriageReturnLineFeed;
            System.out.println("Status Line: " + statusLineOfRequestedResource);
            System.out.println(contentTypeOfRequestedResource);

        }
        else {

            statusLineOfRequestedResource = "HTTP/1.0 404 Not Found\n" + CarriageReturnLineFeed;
            contentTypeOfRequestedResource = "Content-type: "+"text/html"+ CarriageReturnLineFeed;
            entityBody = "<HTML>"+"<HEAD><TITLE>Not Found</TITLE></HEAD>"+
                    "<BODY>404 Not Found</BODY></HTML>";

        }

        dataOutputStream.writeBytes(CarriageReturnLineFeed);

        try{
            if(fileExists){

                writeRequestedResourceAsByte(fileInputStream, dataOutputStream);
                fileInputStream.close();

            }else {

                dataOutputStream.writeBytes(entityBody);
            }

            System.out.println("*****");
            System.out.println("File Location: "+ HttpRequestHandler.getLocationOfRequestedResource(requestLine));
            System.out.println("*****");

            String headerLineOfGetRequest = "";

            while((headerLineOfGetRequest = bufferedReader.readLine()).length()!=0){

                System.out.println(headerLineOfGetRequest);

            }

            System.out.println("\n");

        } catch (FileNotFoundException f){
            f.printStackTrace();

        } finally {
            closeStream();
        }
    }

    public void closeStream() throws IOException {
        try {

            dataOutputStream.close();
            bufferedReader.close();
            socket.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static String getLocationOfRequestedResource(String requestLine){

        StringTokenizer tokens = new StringTokenizer(requestLine);
        String fileName = tokens.nextToken();
        fileName = tokens.nextToken();

        String finalPathOfRequestedResource = fileName.substring(1, fileName.length());

        String directoryPath = System.getProperty("user.dir");

        finalPathOfRequestedResource = directoryPath + "/" + finalPathOfRequestedResource;

        return finalPathOfRequestedResource;
    }

    private static String resourceType(String fileNameOfRequestedResource){
        if(fileNameOfRequestedResource.endsWith(".htm") || fileNameOfRequestedResource.endsWith(".html")){
            return "text/html";
        }
        if(fileNameOfRequestedResource.endsWith(".jpg") || fileNameOfRequestedResource.endsWith(".jpeg")){
            return "image/jpeg";
        }
        if(fileNameOfRequestedResource.endsWith(".gif")){
            return "application/octet-stream";
        }
        if(fileNameOfRequestedResource.endsWith(".pdf")){
            return "book/article";
        }
        return fileNameOfRequestedResource;
    }

    private static void writeRequestedResourceAsByte(FileInputStream fileInputStream1, OutputStream outputStream) throws Exception{

        byte[] byteReferenceVariable = new byte[1024];

        int readerFromFileInputStream = 0;

            //copy requested file into the socket's output stream
            while ( (readerFromFileInputStream = fileInputStream1.read(byteReferenceVariable) )!=-1 ){
            outputStream.write(byteReferenceVariable, 0, readerFromFileInputStream);
            }
        }

    }
