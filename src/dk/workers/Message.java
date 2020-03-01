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
    public Message(String from, String to, String subject, String text, List<String> base64Files) {
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

        //new
        Headers += "MIME-Version: 1.0" + CRLF;
        Headers += "Content-Type: multipart/mixed; boundary=outerboundary" + CRLF;

        Headers += CRLF + "--outerboundary" + CRLF;
        Headers += "Content-Type: text/plain; charset=us-ascii" + CRLF;
        //Headers += CRLF + "Some text for body" + CRLF;
        Headers += CRLF + text + CRLF;
        for(String base64File : base64Files){
            Headers += CRLF + "--outerboundary" + CRLF;
            Headers += "Content-Type: image/jpeg" + CRLF;
            Headers += "Content-Disposition: inline" + CRLF;
            Headers += "Content-Transfer-Encoding: base64" + CRLF;
            Headers += "Content-ID: frown@here.ko" + CRLF;
            //Headers += CRLF + "R0lGODlhEAAQAKEBAAAAAP//AP//AP//ACH5BAEKAAIALAAAAAAQABAAAAIzlA2px6IBw2IpWglOvTahDgGdI0ZlGW5meKlci6JrasrqkypxJr8S0oNpgqkGLtcY6hoFADs=" + CRLF;
            Headers += CRLF + base64File + CRLF;
        }
        Headers += CRLF +"--outerboundary--" + CRLF;



        Body = "";
        //Body = text;
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
        String res;

        //res = Headers + CRLF;
        //res += Body;
        /*
        //new
        res += "MIME-Version: 1.0" + CRLF;
        res += "Content-Type: multipart/mixed; boundary=\"outer-boundary\"" + CRLF;

        res += CRLF + "--outer-boundary" + CRLF;
        res += "Content-Type: text/plain; charset=us-ascii" + CRLF;
        res += Body;
        res += CRLF + "--outer-boundary" + CRLF;
        res += "Content-Type: image/gif" + CRLF;
        res += "Content-Disposition: inline" + CRLF;
        res += "Content-Transfer-Encoding: base64" + CRLF;
        res += "Content-ID: <frown@here>" + CRLF;
        res += "R0lGODlhEAAQAKEBAAAAAAD//wD//wD//yH5BAEKAAIALAAAAAAQABAAAAIzlA2px6IBw2" +
                "IpWglOvTahDgGdI0ZlGW5meKlci75drDzm5uLZyZ1I3Mv8ZB5Krtgg1RoFADs=" + CRLF;
        res += CRLF +"--outer-boundary--" + CRLF;
        */
        res = Headers;
        return res;
    }
}
