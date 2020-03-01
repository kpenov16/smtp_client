package dk.workers;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Base64;

/* $Id: MailClient.java,v 1.7 1999/07/22 12:07:30 kangasha Exp $ */

/**
 * A simple mail client with a GUI for sending mail.
 *
 * @author Jussi Kangasharju, modified by Kaloyan Penov
 */
public class MailClient extends Frame {
    /* The stuff for the GUI. */
    private Button btSend = new Button("Send");
    private Button btAdd = new Button("Add");
    private Button btClear = new Button("Clear");
    private Button btQuit = new Button("Quit");
    private Label serverLabel = new Label("Local mailserver:");
    private TextField serverField = new TextField("", 40);
    private Label fromLabel = new Label("From:");
    private TextField fromField = new TextField("", 40);
    private Label toLabel = new Label("To:");
    private TextField toField = new TextField("", 40);
    private Label subjectLabel = new Label("Subject:");
    private TextField subjectField = new TextField("", 40);
    private Label messageLabel = new Label("Message:");
    private TextArea messageText = new TextArea(10, 40);

    private ArrayList<String> base64Files = new ArrayList<>();


    /**
     * Create a new MailClient window with fields for entering all
     * the relevant information (From, To, Subject, and message).
     */
    public MailClient() {
        super("Java Mailclient");

	/* Create panels for holding the fields. To make it look nice,
	   create an extra panel for holding all the child panels. */
        Panel serverPanel = new Panel(new BorderLayout());
        Panel fromPanel = new Panel(new BorderLayout());
        Panel toPanel = new Panel(new BorderLayout());
        Panel subjectPanel = new Panel(new BorderLayout());
        Panel messagePanel = new Panel(new BorderLayout());
        serverPanel.add(serverLabel, BorderLayout.WEST);
        serverPanel.add(serverField, BorderLayout.CENTER);
        fromPanel.add(fromLabel, BorderLayout.WEST);
        fromPanel.add(fromField, BorderLayout.CENTER);
        toPanel.add(toLabel, BorderLayout.WEST);
        toPanel.add(toField, BorderLayout.CENTER);
        subjectPanel.add(subjectLabel, BorderLayout.WEST);
        subjectPanel.add(subjectField, BorderLayout.CENTER);
        messagePanel.add(messageLabel, BorderLayout.NORTH);
        messagePanel.add(messageText, BorderLayout.CENTER);
        Panel fieldPanel = new Panel(new GridLayout(0, 1));
        fieldPanel.add(serverPanel);
        fieldPanel.add(fromPanel);
        fieldPanel.add(toPanel);
        fieldPanel.add(subjectPanel);

	/* Create a panel for the buttons and add listeners to the
	   buttons. */
        Panel buttonPanel = new Panel(new GridLayout(1, 0));
        btSend.addActionListener(new SendListener());
        btAdd.addActionListener(new AddListener());
        btClear.addActionListener(new ClearListener());
        btQuit.addActionListener(new QuitListener());
        buttonPanel.add(btSend);
        buttonPanel.add(btAdd);
        buttonPanel.add(btClear);
        buttonPanel.add(btQuit);

        /* Add, pack, and show. */
        add(fieldPanel, BorderLayout.NORTH);
        add(messagePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        show();
    }

    static public void main(String argv[]) {
        new MailClient();
    }
    // Kaloyan Penov, adding on click listener to open the image file
    // and convert it to base64 encoded string that the smtp protocol works with
    // when images are send via body parts in a mixed multipart content type
    // as described in rfc 1521
    //Create a file chooser
    final JFileChooser fc = new JFileChooser();
    /* Handler for the Add-button. */
    class AddListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // modified source: https://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html
            //Handle open button action.
            if (e.getSource() == btAdd) {
                int returnVal = fc.showOpenDialog(MailClient.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    //show what file is being opened for debugging
                    System.out.println("Opening: " + file.getName());
                    String base64File = "";
                    try (FileInputStream imageInFile = new FileInputStream(file)) {
                        // Reading a file from file system
                        byte fileData[] = new byte[(int) file.length()];
                        imageInFile.read(fileData); //read the bytes from the stream with the image file
                                                    // and write them to the byte array
                        base64File = Base64.getEncoder().encodeToString(fileData);  //encode the bytes to base64 string as specified in table 1 of rfc 4648 and 2045
                                                                                    //source: https://docs.oracle.com/javase/8/docs/api/java/util/Base64.html
                                                                                    //one long line string
                        //add file string to the shared array list with base64 encoded files
                        attachFile(base64File);
                    } catch (FileNotFoundException ex) {
                        System.out.println("File not found" + ex);
                    } catch (IOException ioe) {
                        System.out.println("Exception while reading the file " + ioe);
                    }
                } else {
                    System.out.println("Open command cancelled by user.");
                }
            }
        }
    }

    //one thread at a time can access it
    public synchronized void attachFile(String base64File){
        this.base64Files.add(base64File);
    }
    public synchronized void setupBase64Files(){
        base64Files = new ArrayList<>();
    }

    /* Handler for the Send-button. */
    class SendListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            System.out.println("Sending mail");

            /* Check that we have the local mailserver */
            if ((serverField.getText()).equals("")) {
                System.out.println("Need name of local mailserver!");
                return;
            }

            /* Check that we have the sender and recipient. */
            if((fromField.getText()).equals("")) {
                System.out.println("Need sender!");
                return;
            }
            if((toField.getText()).equals("")) {
                System.out.println("Need recipient!");
                return;
            }

            /* Create the message */
            Message mailMessage = new Message(fromField.getText(),
                    toField.getText(),
                    subjectField.getText(),
                    messageText.getText(),
                    base64Files);

	    /* Check that the message is valid, i.e., sender and
	       recipient addresses look ok. */
            if(!mailMessage.isValid()) {
                return;
            }

	    /* Create the envelope, open the connection and try to send
	       the message. */
            try {
                Envelope envelope = new Envelope(mailMessage,
                        serverField.getText());
                try {
                    SMTPConnection connection = new SMTPConnection(envelope);
                    connection.send(envelope);
                    connection.close();
                } catch (IOException error) {
                    System.out.println("Sending failed: " + error);
                    return;
                }
                System.out.println("Mail sent succesfully!");
            } catch (UnknownHostException e) {
                /* If there is an error, do not go further */
                return;
            }

        }
    }

    /* Clear the fields on the GUI. */
    class ClearListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.out.println("Clearing fields");
            fromField.setText("");
            toField.setText("");
            subjectField.setText("");
            messageText.setText("");
            setupBase64Files();
        }
    }


    /* Quit. */
    class QuitListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }
}
