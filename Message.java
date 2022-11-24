// CS4065 - Programming Assignment 2
// Triet Pham
// Trien Dau

import java.util.Date;
import java.text.SimpleDateFormat;

public class Message {
    public String id;
    public String sender;
    public String postDate;
    public String subject;
    public String content;

    // Constructor
    public Message(String id, String sender, Date postDate, String subject, String content){

        // Can be used to format the date and turn it to a string
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        this.id = id;
        this.sender = sender;
        this.postDate = formatter.format(postDate);
        this.subject = subject;
        this.content = content;
    }

    // Print the formatted message
    public String printFullMessage(){

        return "   <MSG> ID: " + id + " - FROM: " + this.sender + " - DATE: " + this.postDate + " - SUBJECT: " + this.subject + "\n         " + this.content + ".\n";

    }
}
