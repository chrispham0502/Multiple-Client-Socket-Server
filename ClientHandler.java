// CS4065 - Programming Assignment 2
// Triet Pham
// Trien Dau

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.ArrayList;

public class ClientHandler implements Runnable{
    
    // Array of usernames to keep track of unique usernames in the server
    public static ArrayList<String> usernames = new ArrayList<>();

    // PUBLIC GROUP
    // Array of clients in the public group
    public static ArrayList<ClientHandler> publicGroup = new ArrayList<>();
    // Message ID for the public group
    public static int publicMsgCount = 1;
    // Array of messages for the public group
    public static ArrayList<Message> publicMessages = new ArrayList<>();

    // PRIVATE GROUP
    // Arrays of clients in the private groups
    public static ArrayList<ClientHandler> privateGroup1 = new ArrayList<>();
    public static ArrayList<ClientHandler> privateGroup2 = new ArrayList<>();
    public static ArrayList<ClientHandler> privateGroup3 = new ArrayList<>();
    public static ArrayList<ClientHandler> privateGroup4 = new ArrayList<>();
    public static ArrayList<ClientHandler> privateGroup5 = new ArrayList<>();

    // 1 ID used across private messages
    public static int privateIMsgCount = 1;
    // Arrays of messages in the private groups
    public static ArrayList<Message> privateMessages1 = new ArrayList<>();
    public static ArrayList<Message> privateMessages2 = new ArrayList<>();
    public static ArrayList<Message> privateMessages3 = new ArrayList<>();
    public static ArrayList<Message> privateMessages4 = new ArrayList<>();
    public static ArrayList<Message> privateMessages5 = new ArrayList<>();
    
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;


    // Constructor
    public ClientHandler(Socket socket) throws IOException{
        try {

            // Establish connection with client
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Get username from the client
            this.clientUsername = bufferedReader.readLine();

            // If a username already existed, ask them to enter another one
            while(usernames.contains(this.clientUsername)){
                this.bufferedWriter.write("\n<SERVER> Username taken. Please try a different one:\n");
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();

                this.clientUsername = bufferedReader.readLine();
            }

            // Add username and client to the public group
            usernames.add(this.clientUsername);
            publicGroup.add(this);
            
            this.bufferedWriter.write("\n<SERVER> Hello " + this.clientUsername + "! You are now in the Public Group!\n");
            this.bufferedWriter.write("         Type /help to learn more about our commands!\n");
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
            
            // Print most recent messages in the public group
            printRecentMessages("PG");

            // Send notification for current online clients
            broadcastMessage(publicGroup, "<SERVER> " + this.clientUsername + " has entered the Server!\n");

        } catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run(){
        String commandFromClient;

        while (!socket.isClosed()){
            try {

                // Getting command from client
                commandFromClient = bufferedReader.readLine();
                //Execute command
                executeCommand(commandFromClient);

            } catch (IOException e){

                // Close connection if error
                try {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                break;
            }
        }
    }

    // Check to see if the client is in a group
    public boolean isInGroup(ArrayList<ClientHandler> group){

        boolean isInGroup = false;

        // If name of clients in a group same as current client, return True
        for (ClientHandler clients : group){

            if (clients.clientUsername.equals(clientUsername)){
                isInGroup = true;
            }

        }

        return isInGroup;
    }

    // Send a message to clients in a group
    public void broadcastMessage(ArrayList<ClientHandler> group, String message) throws IOException{

        for (ClientHandler clients : group){
            
            try {
                // Send for each client that is not current client
                if (!clients.clientUsername.equals(clientUsername)){
                    clients.bufferedWriter.write(message);
                    clients.bufferedWriter.newLine();
                    clients.bufferedWriter.flush();
                }
            } catch (IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
            }

        }

    }

    // Get a group by group id input by the user
    public ArrayList<ClientHandler> getGroupById(String groupId) throws IOException{

        ArrayList<ClientHandler> group;

        switch(groupId) {
            case "PG":
                group = publicGroup;
                break;
            case "G1":
                group = privateGroup1;
                break;
            case "G2":
                group = privateGroup2;
                break;
            case "G3":
                group = privateGroup3;
                break;
            case "G4":
                group = privateGroup4;
                break;
            case "G5":
                group = privateGroup5;
                break;
            default:
                try {

                    // Error if not a valid ID
                    this.bufferedWriter.write("\n<SERVER> Invalid Group ID.\n");
                    this.bufferedWriter.newLine();
                    this.bufferedWriter.flush();
                    
                } catch (IOException e){
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
                return null;
                
        }

        return group;
    }

    // Get a group message array by group id input by the user
    public ArrayList<Message> getGroupMessagesById(String groupId) throws IOException{

        ArrayList<Message> groupMessages;

        switch(groupId) {
            case "PG":
                groupMessages = publicMessages;
                break;
            case "G1":
                groupMessages = privateMessages1;
                break;
            case "G2":
                groupMessages = privateMessages2;
                break;
            case "G3":
                groupMessages = privateMessages3;
                break;
            case "G4":
                groupMessages = privateMessages4;
                break;
            case "G5":
                groupMessages = privateMessages5;
                break;
            default:
                try {
                    // Error if ID is invalid
                    this.bufferedWriter.write("\n<SERVER> Invalid Group ID.\n");
                    this.bufferedWriter.newLine();
                    this.bufferedWriter.flush();
                    
                } catch (IOException e){
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
                return null;
                
        }

        return groupMessages;
    }

    // Print all users in a group
    public void printGroupUsers(String groupId) throws IOException{
        
        ArrayList<ClientHandler> group = getGroupById(groupId);

        // If group not exist, return
        if (group == null){
            return;
        }

        // If there are user, put them into a list
        String usersString = "Users in Group " + groupId + ":\n";

        for (ClientHandler clientHandler : group){               
                usersString += "\n         - " + clientHandler.clientUsername;
        }

        // If there are no user, notify
        if (group.size() == 0){
            usersString = "There is no one in Group " + groupId + ".";
        }

        try {
            // Write the message
            this.bufferedWriter.write("\n<SERVER> " + usersString + "\n");
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();

        } catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    // Join an exisiting group
    public void joinGroup(String groupId) throws IOException{

        //Get the group
        ArrayList<ClientHandler> group = getGroupById(groupId);
       
        // If group does not exist then return
        if (group == null){
            return;
        }

        try {
            
            // If client is not in the group yet, join
            if (!isInGroup(group)){

                // Add client to group
                group.add(this);

                // Announce to current group members
                broadcastMessage(group, "\n<SERVER> " + this.clientUsername + " has entered Group " + groupId + ".\n");

                this.bufferedWriter.write("\n<SERVER> Successfully joined Group "+ groupId + ".\n");
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();
                
                // Print most recents messages
                printRecentMessages(groupId);
                
            }
            else{
                // If client is already in a group, print error
                this.bufferedWriter.write("\n<SERVER> ERROR. You are already in Group " + groupId + ".\n");
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();
            }

        } catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }
    
    // Leave a group the client is currently in
    public void leaveGroup(String groupId) throws IOException{

        // Since the client is automatically put in public group when they join, this is one group they cannot leave, they can instead exit the server.
        if (groupId.equals("PG")){

            try {
                
                this.bufferedWriter.write("\n<SERVER> ERROR. Cannot leave Public Group. Type /exit to leave the Server.\n");
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();
                
            } catch (IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
            }

            return;
        }

        // get group by Id
        ArrayList<ClientHandler> group = getGroupById(groupId);

        // If group does not exist, return
        if (group == null){
            return;
        }

        try {

            // If the client tries to leave a gorup they're not in, print error.
            if (!isInGroup(group)){

                this.bufferedWriter.write("\n<SERVER> ERROR. You are not in Group "+ groupId + ".\n");
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();
            }
            // If not then leave the group
            else {
                this.bufferedWriter.write("\n<SERVER> Successfully left Group "+ groupId + ".\n");
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();
                group.remove(this);

                // Announce to all group members
                broadcastMessage(group, "\n<SERVER> User " + this.clientUsername + " has left Group "+ groupId + ".\n");
            }

        } catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
        
    }

    // Print most recent messages by group ID
    public void printRecentMessages(String groupId) throws IOException{
        
        // Get the group messages
        ArrayList<Message> groupMessages = getGroupMessagesById(groupId);

        try{
            // Only print if there are messages to print
            if (!groupMessages.isEmpty()){

                this.bufferedWriter.write("<SERVER> The most recent messages: \n");
                this.bufferedWriter.newLine();
    
                // If there are 2 or more messages, print the most recent 2
                if (groupMessages.size() > 1){
                    this.bufferedWriter.write(groupMessages.get(groupMessages.size() - 2).printFullMessage());
                    this.bufferedWriter.newLine();
                    this.bufferedWriter.write(groupMessages.get(groupMessages.size() - 1).printFullMessage());
                    this.bufferedWriter.newLine();
                    this.bufferedWriter.flush();
                }
                // If there is only 1 message, print it
                else {
                    this.bufferedWriter.write(groupMessages.get(groupMessages.size() - 1).printFullMessage());
                    this.bufferedWriter.newLine();
                    this.bufferedWriter.flush();
                }
            }
        } catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }

    // Post message into a group
    public void groupPostMessage(String groupId, String subject, String content) throws IOException{

        // Get group and group messages by ID
        ArrayList<ClientHandler> group = getGroupById(groupId);
        ArrayList<Message> groupMessages = getGroupMessagesById(groupId);

        // If group does not exist, return
        if (group == null){
            return;
        }

        // If group messages not exist, return
        if (groupMessages == null){
            return;
        }

        // Message ID, which will be in the form <GroupID + Message Number>
        // EXAMPLE: Group 1, message 1 => ID = G11
        String msgId = groupId;

        // Getting Message ID
        if (groupId.equals("PG")){
            msgId += String.valueOf(publicMsgCount);
            publicMsgCount++;
        }
        else{
            msgId += String.valueOf(privateIMsgCount);
            privateIMsgCount++;
        }

        // Get current Date
        Date date = new Date();

        // Compose new message
        Message newMessage = new Message(msgId, clientUsername, date, subject, content);

        try {
            // Post the message in the group if they are in the group
            if(isInGroup(group)){
                groupMessages.add(newMessage);
                broadcastMessage(group, newMessage.printFullMessage());
            }
            // If the client is not in a group, they can not post to that group, throw error
            else {
                this.bufferedWriter.write("\n<Server> ERROR. You are not in Group "+ groupId + ".\n");
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();
            }
    
        } catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
        

    }
    
    // Get a message by message
    public Message getMessageById(String msgId) throws IOException{

        // Get group ID, which is the first 2 letter of message ID
        String groupId = msgId.substring(0,2);

        //Get group and group messages by group ID
        ArrayList<Message> groupMessages = getGroupMessagesById(groupId);
        ArrayList<ClientHandler> group = getGroupById(groupId);

        // If a client is not in a group, they cannot get a message from that group
        if(!isInGroup(group)){
            this.bufferedWriter.write("\n<SERVER> ERROR. You are not in Group "+ groupId + ".\n");
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
            return null;
        }

        // Go through the group messages until the requested message is found
        for (Message msg : groupMessages){   
                if (msg.id.equals(msgId)){
                    return msg;
                }
        }

        // If the message is not found, return
        this.bufferedWriter.write("\n<SERVER> ERROR. Message not found. Try again.\n");
        this.bufferedWriter.newLine();
        this.bufferedWriter.flush();

        return null;
    }

    // Execute command from the user, command words will be separated by --, so that spaces can be allowed for message
    public void executeCommand(String command) throws IOException{

        // cmd to print list of users given group ID
        if (command.startsWith("/users")){
            //  /users--<GroupID>

            String[] cmdList = command.split("--");

            if (cmdList.length == 2){
                String group = cmdList[1];
                printGroupUsers(group);
            }

            else {
                this.bufferedWriter.write("\n<SERVER> ERROR. Please specify exactly 1 Group ID.\n");
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();
            }

        }

        // cmd to print list of groups
        else if (command.equals("/groups")){
            //  /groups

            this.bufferedWriter.write("\n<SERVER> Available groups:\n");
            this.bufferedWriter.write("\n         Public Group - Group ID: PG - Joined\n");
            if(isInGroup(privateGroup1)){
                this.bufferedWriter.write("         Group 1      - Group ID: G1 - Joined\n");
            }
            else{
                this.bufferedWriter.write("         Group 1      - Group ID: G1\n");
            }
            if(isInGroup(privateGroup2)){
                this.bufferedWriter.write("         Group 2      - Group ID: G2 - Joined\n");
            }
            else{
                this.bufferedWriter.write("         Group 2      - Group ID: G2\n");
            }
            if(isInGroup(privateGroup3)){
                this.bufferedWriter.write("         Group 3      - Group ID: G3 - Joined\n");
            }
            else{
                this.bufferedWriter.write("         Group 3      - Group ID: G3\n");
            }
            if(isInGroup(privateGroup4)){
                this.bufferedWriter.write("         Group 4      - Group ID: G4 - Joined\n");
            }
            else{
                this.bufferedWriter.write("         Group 4      - Group ID: G4\n");
            }
            if(isInGroup(privateGroup5)){
                this.bufferedWriter.write("         Group 5      - Group ID: G5 - Joined\n");
            }
            else{
                this.bufferedWriter.write("         Group 5      - Group ID: G5\n");
            }
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();

        }

        // cmd to join a group given group ID
        else if (command.startsWith("/join")){
            String[] cmdList = command.split("--");
            //  /join--<GroupID>

            if (cmdList.length == 2){
                String group = cmdList[1];
                joinGroup(group);
            }
            else {
                this.bufferedWriter.write("\n<SERVER> ERROR. Please specify exactly 1 Group ID.");
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();
            }
        }

        // cmd to leave a group given group ID
        else if (command.startsWith("/leave")){
            String[] cmdList = command.split("--");
            // /leave--<GroupID>

            if (cmdList.length == 2){
                String group = cmdList[1];
                leaveGroup(group);
            }
            else {
                this.bufferedWriter.write("\n<SERVER> ERROR. Please specify exactly 1 Group ID.");
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();
            }
        }

        // cmd to post a message to a group given group ID, message subject and content
        else if (command.startsWith("/post")){
            String[] cmdList = command.split("--");
            
            // /post--<GroupID>--<Subject>--<Content>

            if (cmdList.length == 4){
                String groupId = cmdList[1];
                String subject = cmdList[2];
                String msg = cmdList[3];

                groupPostMessage(groupId, subject, msg);
            }
            else {
                this.bufferedWriter.write("\n<SERVER> ERROR. Invalid arguments.\n");
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();
            }
        }

        // cmd to get a message given the message ID
        else if (command.startsWith("/msg")){
            // msg--<msgID>
            
            String[] cmdList = command.split("--");

            if (cmdList.length == 2){
                String msgId = cmdList[1];
                Message msg = getMessageById(msgId);

                if (msg != null){
                    this.bufferedWriter.write("\n" + msg.printFullMessage());
                    this.bufferedWriter.newLine();
                    this.bufferedWriter.flush();
                }

            }
            else {
                this.bufferedWriter.write("\n<SERVER> ERROR. Invalid arguments.\n");
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();
            }
        }

        // cmd to exit the server and close the connection
        else if (command.equals("/exit")){
            // /exit

            this.bufferedWriter.write("\n<SERVER> SEE YOU AGAIN!");
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
            
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

        // cmd to print list of all available commands from the server
        else if (command.equals("/help")){
            
            this.bufferedWriter.write("\n<SERVER> Server Commands:\n");
            this.bufferedWriter.write("\n         /users--<GroupID>: Print all users in a group provided the Group ID\n");
            this.bufferedWriter.write("\n         /groups: Print all groups available\n");
            this.bufferedWriter.write("\n         /join--<GroupID>: Join a group provided the Group ID\n");
            this.bufferedWriter.write("\n         /leave--<GroupID>: Leave a group provided the Group ID\n");
            this.bufferedWriter.write("\n         /post--<GroupID>--<Subject>--<Content>: Post a message in a group provided the Group ID, Message Subject and Message Content\n");
            this.bufferedWriter.write("\n         /msg--<MsgID>: Get a message provided the Message ID\n");
            this.bufferedWriter.write("\n         /exit: Leave all groups and exit the server\n");
            this.bufferedWriter.newLine(); 
            this.bufferedWriter.flush();
        }

        // If user input anything else, cmd is not supported, throw error
        else {
            this.bufferedWriter.write("\n<SERVER> Unknown command. Please try again.\n");
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
        }
    }

    // Leave all the group the client is currently in and close the socket.
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) throws IOException{
        
        if (isInGroup(privateGroup1)){
            broadcastMessage(privateGroup1, "<SERVER> User " + this.clientUsername + " has left the Group G1.\n");
            privateGroup1.remove(this);
        }
        if (isInGroup(privateGroup2)){
            broadcastMessage(privateGroup2, "<SERVER> User " + this.clientUsername + " has left the Group G2.\n");
            privateGroup2.remove(this);
        }
        if (isInGroup(privateGroup3)){
            broadcastMessage(privateGroup3, "<SERVER> User " + this.clientUsername + " has left the Group G3.\n");
            privateGroup3.remove(this);
        }
        if (isInGroup(privateGroup4)){
            broadcastMessage(privateGroup4, "<SERVER> User " + this.clientUsername + " has left the Group G4.\n");
            privateGroup4.remove(this);
        }
        if (isInGroup(privateGroup5)){
            broadcastMessage(privateGroup5, "<SERVER> User " + this.clientUsername + " has left the Group G5.\n");
            privateGroup5.remove(this);
        }

        publicGroup.remove(this);
        broadcastMessage(publicGroup, "<SERVER> User " + this.clientUsername + " has left the Server.\n");

        try {
            if (bufferedReader != null){
                bufferedReader.close();
            }
            if (bufferedWriter != null){
                bufferedWriter.close();
            }
            if (socket != null){
                socket.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }

    }
}
