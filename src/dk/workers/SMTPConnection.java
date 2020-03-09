package dk.workers;

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Open an SMTP connection to a mailserver and send one mail.
 * Modified by Kaloyan Penov s133967
 *
 */
public class SMTPConnection {
    /* The socket to the server */
    private Socket connection;

    /* Streams for reading and writing the socket */
    private BufferedReader fromServer;
    private DataOutputStream toServer;

    private static final int SMTP_PORT = 25;   //Kaloyan Penov: mstp port assigned for the service by the rfc 281
    private static final String CRLF = "\r\n"; //mainly used to indicate end command line

    /* Are we connected? Used in close() to determine what to do. */
    private boolean isConnected = false;

    /* Create an SMTPConnection object. Create the socket and the
       associated streams. Initialize SMTP connection. */
    public SMTPConnection(Envelope envelope) throws IOException {
        //Kaloyan Penov: pdf file "Java SocketProgramming (Server programmingClient side programmingImplementation)" by Bhupjit Singh and S.Ali Ghodrat
        connection = new Socket("localhost",25); //Kaloyan Penov: the hostname is localhost as we run the program on the machine where the mail server is located
        fromServer =  new BufferedReader(new InputStreamReader(connection.getInputStream())); //to read what the smtp server is sending
        toServer = new DataOutputStream(connection.getOutputStream()); //to send data to the smtp server

   	/* Read a line from server and check that the reply code is 220.
	   If not, throw an IOException. */
        String firstLine = fromServer.readLine();
        if(firstLine == null || !firstLine.contains("220")) { //Kaloyan Penov: check if server is ready to communicate, rfc 821
            throw new IOException("first line contains no 220");
        }

	/* SMTP handshake. We need the name of the local machine.
	   Send the appropriate SMTP handshake command. */
        String localhost = "localhost";
        sendCommand( "HELO dontcare.com", 250 ); //Kaloyan Penov: identify the sender-SMTP to the receiver-SMTP, rfc 821
        isConnected = true;
    }

    //Kaloyan Penov: general method for sending commands and confirming the reply
    /* Send an SMTP command to the server. Check that the reply code is
      what is is supposed to be according to RFC 821. */
    private void sendCommand(String command, int rc) throws IOException {
        /* Write command to server and read reply from server. */
        toServer.writeBytes(command + CRLF);
        String line = fromServer.readLine();
        if(line == null || line.isEmpty()) {
            throw new IOException("line read is null or empty for command: " + command + ", expected rc: " + rc);
        }

        /* Check that the server's reply code is the same as the parameter
         rc. If not, throw an IOException. */
        //if(!line.contains(Integer.toString(rc))) {
        if( parseReply(line) != rc ) { //Kaloyan Penov: check if the given replay code is the same as the expected
            throw new IOException("response: \""+line+"\" do not contains the expected rc: " + rc +
                                  ", for command: " + command);
        }
    }

    /* Send the message. Write the correct SMTP-commands in the
       correct order. No checking for errors, just throw them to the
       caller. */
    public void send(Envelope envelope) throws IOException {
  	/* Send all the necessary commands to send a message. Call
	   sendCommand() to do the dirty work. Do _not_ catch the
	   exception thrown from sendCommand(). */                           //Kaloyan Penov: from rfc 821
        sendCommand("MAIL FROM: " + envelope.Sender, 250);  // send from email, expected replay code 250
        sendCommand("RCPT TO: " + envelope.Recipient, 250); // send to email, expected replay code 250
        sendCommand("DATA", 354);                           // send command indicating that the data body is coming next, expected replay code 345
        sendCommand(envelope.Message.toString() + CRLF + "." + CRLF, 250); // send the body itself with end of message sequence, expected replay code 250
    }

    /* Close the connection. First, terminate on SMTP level, then
       close the socket. */
    public void close() {
        isConnected = false;
        try {
            sendCommand( "QUIT", 221); //Kaloyan Penov: from rfc 821
            connection.close();
        } catch (IOException e) {
            System.out.println("Unable to close connection: " + e);
            isConnected = true;
        }
    }



    /* Parse the reply line from the server. Returns the reply code. */
    private int parseReply(String reply) {
        try{
            return Integer.parseInt(reply.split("\\s+")[0].trim()); //Kaloyan Penov: extract the reply code
                                                                           // the protocol from rfc 821 the code is the first string followed by space
        }catch(Throwable t){
            return -1;
        }
    }

    /* Destructor. Closes the connection if something bad happens. */
    protected void finalize() throws Throwable {
        if(isConnected) {
            close();
        }
        super.finalize();
    }
}
