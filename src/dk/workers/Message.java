package dk.workers;

import java.util.*;
import java.text.*;

/* $Id: Message.java,v 1.5 1999/07/22 12:10:57 kangasha Exp $ */

/**
 * Mail message.
 *
 * @author Jussi Kangasharju
 */
public class Message {
    /* The headers and the body of the message. */
    public String Headers;
    public String Body;

    /* Sender and recipient. With these, we don't need to extract them
       from the headers. */
    private String From;
    private String To;

    /* To make it look nicer */
    private static final String CRLF = "\r\n";

    /* Create the message object by inserting the required headers from
       RFC 822 (From, To, Date). */
    public Message(String from, String to, String subject, String text, List<ImageFile> imageFiles) {
        /* Remove whitespace */
        From = from.trim();
        To = to.trim();
        Headers = "From: " + From + CRLF;
        Headers += "To: " + To + CRLF;
        Headers += "Subject: " + subject.trim() + CRLF;

	/* A close approximation of the required format. Unfortunately
	   only GMT. */
        SimpleDateFormat format =
                new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'");
        String dateString = format.format(new Date());
        Headers += "Date: " + dateString + CRLF;

        // Kaloyan Penov: we want to be able to send both text and images
        // rfc 1521 redefines the format of the textual message body (rfc 822)
        // to be used for multipart textual and non-textual bodies,
        // that has been used here:
        Headers += "MIME-Version: 1.0" + CRLF; //basically defines that we are using rfc 1521, and version

        //describes the data contained in the body and how to be understand on the receiving side
        Headers += "Content-Type: multipart/mixed; boundary=outerboundary" + CRLF;
                                                    //the boundary defines the different body parts

        Headers += CRLF + "--outerboundary" + CRLF; //signals start of a part, syntax is --boundaryName
                                                    //be aware that each part is starting with CRLF and then the boundary
        Headers += "Content-Type: text/plain; charset=us-ascii" + CRLF; //the type and the character set in the body of that part
        //Headers += CRLF + "Some text for body" + CRLF; //hardcoded example
        Headers += CRLF + text + CRLF; //the body of each part also starts and ends with CRLF
        int i = 1;
        //if we do not have attachments there is going to be only text body part from above
        for(ImageFile imageFile : imageFiles){
            //each attached image gets its own body part with headers describing the content
            //as we only support images as attachments the content type is image/jpeg
            Headers += CRLF + "--outerboundary" + CRLF; //signals start of a part, syntax is --boundaryName
                                                        //be aware that each part is starting with CRLF and then the boundary
            Headers += "Content-Type: image/jpeg" + CRLF; //the body type
            //Headers += "Content-Disposition: inline" + CRLF;
            Headers += "Content-Disposition: attachment; filename=\""+imageFile.fileName+"\"" + CRLF; //the disposition of the following body part
            Headers += "Content-Transfer-Encoding: base64" + CRLF; //decoding to be used on the receiver side
            Headers += "Content-ID: dont@care-for-now.com" + CRLF;
            //Headers += CRLF + "R0lGODlhEAAQAKEBAAAAAP//AP//AP//ACH5BAEKAAIALAAAAAAQABAAAAIzlA2px6IBw2IpWglOvTahDgGdI0ZlGW5meKlci6JrasrqkypxJr8S0oNpgqkGLtcY6hoFADs=" + CRLF;
            Headers += CRLF + imageFile.base64File + CRLF; //the body of each part also starts and ends with CRLF
        }

        Headers += CRLF +"--outerboundary--" + CRLF; //signals end of a multipart body, syntax: --boundaryName--

        //Kaloyan Penov: goodies used from here: https://stackoverflow.com/questions/30351465/html-email-with-inline-attachments-and-non-inline-attachments
        //                                       https://stackoverflow.com/questions/10631856/mime-type-to-satisfy-html-email-images-and-plain-text

        Body = ""; //not used for now
    }

    /* Two functions to access the sender and recipient. */
    public String getFrom() {
        return From;
    }

    public String getTo() {
        return To;
    }

    /* Check whether the message is valid. In other words, check that
       both sender and recipient contain only one @-sign. */
    public boolean isValid() {
        int fromat = From.indexOf('@');
        int toat = To.indexOf('@');

        if(fromat < 1 || (From.length() - fromat) <= 1) {
            System.out.println("Sender address is invalid");
            return false;
        }
        if(toat < 1 || (To.length() - toat) <= 1) {
            System.out.println("Recipient address is invalid");
            return false;
        }
        if(fromat != From.lastIndexOf('@')) {
            System.out.println("Sender address is invalid");
            return false;
        }
        if(toat != To.lastIndexOf('@')) {
            System.out.println("Recipient address is invalid");
            return false;
        }
        return true;
    }

    /* For printing the message. */
    public String toString() {
        return Headers;
    }
}
