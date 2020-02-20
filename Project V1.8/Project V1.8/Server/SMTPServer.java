/**
  *The Server class
  *@ author: Shawn Xu, Zirong cao, Shoujing Wu, Vincent Li
  *@version: 2018.5.4
 **/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.net.*;

public class SMTPServer extends JFrame implements ActionListener
{ 
   private JButton jbStart = new JButton("Start");
   private JLabel jl = new JLabel("IP Address: "); 
   
   private JTextField jtf = new JTextField(15);
   private JTextArea jta = new JTextArea(25, 25);
   
   private ServerSocket serversoc = null;
   
   private String[] users = {"sxx6945", "vxl9440", "zxc9821", "sxw6348"};
   
   private boolean connection = false;
   
   private int queNum = 0;
   
   private File logFi = null;;
   
   /* Main method */
   public static void main(String[] args)
   {
      new SMTPServer();
   }// end main method
   
   /*Constructor*/
   public SMTPServer() 
   {
      this.setTitle("Server");
      this.setSize(400,700);
      this.setLocation(1200,100);
      
      JPanel jp1 = new JPanel();
      jp1.add(jl);
      jp1.add(jtf);
      jp1.add(jbStart);
      this.add(jp1, BorderLayout.NORTH);
      
      JScrollPane jsp = new JScrollPane(jta);
      jta.setLineWrap(true);
      jta.setWrapStyleWord(true);
      this.add(jsp, BorderLayout.CENTER);
   
      this.addWindowListener(
         new WindowAdapter()
         {
            public void windowClosing(WindowEvent we)
            {
               JOptionPane.showMessageDialog(null, "Thank you for using the server");
               System.exit(0);            
            }
         }); // end window listener
      
      jtf.setEditable(false);
      //jta.setEditable(false);
      jbStart.addActionListener(this);
    
      this.setVisible(true);
   }// end constructor
   
   public void actionPerformed(ActionEvent ae)
   {
      switch(ae.getActionCommand())
      {
         case "Start":
            try
            {  
               jtf.setText(InetAddress.getLocalHost().getHostAddress());
               (new Threads()).start();
            }
            catch(UnknownHostException une)
            {
               JOptionPane.showMessageDialog(this, "Cannot get server IP (constructor): " + une, "Unknown host exception", 0);
            }
            jbStart.setText("Stop");
            break;
         
         case "Stop":
            try
            {
               serversoc.close(); 
            }
            catch(Exception e)
            {
               JOptionPane.showMessageDialog(this, "Exception closing serversocket: " + e);
               break;
            }
            jbStart.setText("Start");
            break;
            
      }// end switch
      
   }// end action perform
   
   /* Accept multiple clients */
   class Threads extends Thread
   {
      Object ob = new Object();
      public void run()
      {
         try
         {
            serversoc = new ServerSocket(42069);
            logFi = new File("Log.txt");
            while(true)
            {
               (new ServerConnection(serversoc.accept())).start();
            }
         }
         catch(Exception e)
         {
            jta.append("Exception: " + e + "\n");
         }
      }//run
      
      public void close()
      {
         try
         {
            serversoc.close();
         }
         catch(Exception ee)
         {
         
         }
      }   
   }//class Threads
   
   // Connect Client //
   class ServerConnection extends Thread
   {
      Socket soc = null;
      String clientIp = null;
      String user = null;
      String message = null;
      String to = null;
      String from = null;
      String fr = null;
      String recip = null;
      String content = null;
      String subject = null;
      String time = null;
      File fl = null;
      
      boolean encrypted = false;
      Threads th = new Threads();
      
      Socket soc1 = null;
      BufferedReader br1 = null;
      PrintWriter pw1 = null;
      PrintWriter pw = null;
      BufferedReader br = null;
      PrintWriter logWriter = null;
       
      public ServerConnection(Socket in_soc)
      {
         soc = in_soc;
         clientIp = soc.getInetAddress().getHostName();
      }
      
      public void run()
      {
         synchronized(th.ob)
         {
            try
            {
               pw = new PrintWriter(new OutputStreamWriter(soc.getOutputStream()));
               br = new BufferedReader(new InputStreamReader(soc.getInputStream()));
               logWriter = new PrintWriter(new FileWriter(logFi));
               boolean confirm = false;
               pw.println("Login Required");
               pw.flush();
               jta.append("S: Log in required \n");
               logWriter.println("S: Log in required \n");
               logWriter.flush();
               user = br.readLine();//reads the username sent from client
               
               for(int i = 0; i < users.length; i++)
               {
                  if(user.equals(users[i]))
                  {
                     confirm = true;
                     break; 
                  }
                  else
                  {
                     continue;
                  }
               }
               if(confirm)
               {
                  pw.println("Ok");
                  pw.flush();
                  jta.append("<"+soc.getInetAddress()+">S: User: " + user + " is logged in\n");
                  logWriter.println("<"+soc.getInetAddress()+">S: User: " + user + " is logged in\n");
                  logWriter.flush();
                  connection = true;
                  System.out.println("Checking...");
                  this.check(user);
               }
               else
               {
                  pw.println("User Not Found, logged in as Guest");
                  pw.flush();
                  jta.append("<"+soc.getInetAddress()+">S: User Not Found, logged in as Guest\n");
                  logWriter.println("<"+soc.getInetAddress()+">S: User Not Found, logged in as Guest\n");
                  logWriter.flush();
                  connection = true;
               }
               // if(confirm)
            //                {
            //                   System.out.println("Checking...");
            //                   this.check(user);// check method to see if there's new emails
            //                }
            /* SMTP protocol */
               while(connection)
               {
                  this.smtp();
               }         
            }
            catch(IOException ioe)
            {
               JOptionPane.showMessageDialog(null, "IOException: " + ioe);
               return;
            }
            catch(Exception e)
            {
               JOptionPane.showMessageDialog(null, "Exception: " + e);
               return;
            }
         }
      }
      
      public void smtp()
      {
         try
         {  
            String msgs = br.readLine();
            pw.println("220 smtp." + InetAddress.getLocalHost().getHostAddress() + ".com ESMTP Postfix");
            pw.flush();
            jta.append("S: 220 smtp."  + InetAddress.getLocalHost().getHostAddress() +  " ESMTP Postfix\n");
            logWriter.println("S: 220 smtp."  + InetAddress.getLocalHost().getHostAddress() +  " ESMTP Postfix\n");
         
            String response = br.readLine();
            System.out.println(response);
            if(!response.startsWith("HELO"))
            {
               throw new Exception("HELO reply not received from client");
            }
            jta.append("C: " + response + "\n");
            logWriter.println("C: " + response + "\n");
               
               
            pw.println("250 Hello " + user + ", i am glad to meet you");
            pw.flush();
            jta.append("S: 250 Hello " + user + ", I am glad to meet you\n");
            logWriter.println("S: 250 Hello " + user + ", I am glad to meet you\n");
            
            
            from = br.readLine(); 
                    
            if(!from.startsWith("MAIL FROM"))
            {
               throw new Exception("MAIL FROM not received from client");
            }
            jta.append("C: " + from + "\n");
            logWriter.println("C: " + from + "\n");
             
            String [] sender = from.split(":");
            fr = sender[1].trim().substring(1,sender[1].length()-1);   
            pw.println("250 Ok");
            pw.flush();
            jta.append("S: 250 Ok\n");
            logWriter.println("S: 250 Ok\n");
              
               
            to = br.readLine();
            if(!to.startsWith("RCPT TO"))
            {
               throw new Exception("RCPT TO not received from client");
            }
            jta.append("C: " + to + "\n");
            String [] recipent = to.split(":");
            recip = recipent[1].trim().substring(1,recipent[1].length()-1);
            pw.println("250 Ok");
            pw.flush();
            jta.append("S: 250 Ok\n");
            logWriter.println("S: 250 Ok\n");
            
                              
            response = br.readLine();
            if(!response.equals("DATA"))
            {
               throw new Exception("DATA not received from client");
            }
            jta.append("C: " + response + "\n");
            logWriter.println("C: " + response + "\n");
            pw.println("354 End data with <CR><LF>.<CR><LF>");
            pw.flush();
            jta.append("S: 354 End data with <CR><LF>.<CR><LF>\n");
            logWriter.println("S: 354 End data with <CR><LF>.<CR><LF>\n");
              
            
            String msg = br.readLine();//from
            jta.append("C: " + msg + "\n");
            logWriter.println("C: " + msg + "\n");
               
                              
            msg = br.readLine();//to
            jta.append("C: " + msg + "\n");
            logWriter.println("C: " + msg + "\n");
              
            
            time = br.readLine();//date
            jta.append("C: " + time + "\n");
            logWriter.println("C: " + time + "\n");
               
               
            //subject = br.readLine();//subject
            //jta.append("C: " + subject + "\n");
            //logWriter.println("C: " + subject + "\n");
            String[] subj = null;  
            
            msg = br.readLine();
            if(msg.equals("Encrypted"))
            {
               msg = br.readLine();//encryption header
               jta.append("C: " + msg + "\n");
               logWriter.println("C: " + msg + "\n");
                  
               
               content = br.readLine();// content 
               
               jta.append("C: " + content + "\n");
               logWriter.println("C: " + content + "\n");
               logWriter.flush();
               msg = br.readLine();//encryption ending
               jta.append("C: " + msg + "\n");
               logWriter.println("C: " + msg + "\n");
               
               subj = msg.split("Message:");
               
            }
            else
            { 
               content = msg;
               System.out.println("Content: "+content);
               jta.append("C: " + msg + "\n");
               logWriter.println("C: " + msg + "\n");
               
               subj = msg.split("Message:");
                  
            }
            queNum++; 
            response = br.readLine();
            //System.out.println("The response: "+response);
            if(!response.contains("."))
            {
               throw new Exception("\".\" not received");
            }
            jta.append("C: " + response + "\n");
            logWriter.println("C: " + response + "\n");
         
            pw.println("250 Ok: queued as " + queNum);
            pw.flush();
            jta.append("S: 250 Ok: queued as " + queNum + "\n");
            logWriter.println("S: 250 Ok: queued as " + queNum + "\n");
         
               
            response = br.readLine();
            System.out.println("This should be :"+response);
            if(!response.equals("QUIT"))
            {
               throw new Exception("Client must QUIT");
            }
            jta.append("C: " + response + "\n");
            logWriter.println("C: " + response + "\n");
         
            pw.println("221 Bye");
            pw.flush();
            jta.append("S: 221 Bye\n");
            logWriter.println("S: 221 Bye\n");
            logWriter.flush();
            logWriter.close();
               
              /* SMTP conversation ends */
            String [] s = recip.split("@"); 
            fl = new File(s[0].trim());
            if(!fl.exists())
            {
               fl.mkdir();//creating folder for the user
            }
            
            File fi = new File(".");
            String path = fi.getCanonicalPath();
            /* subject as the name of the file stores under the recipent's folder */
            
            if(subj.length == 0)
            {
               subj[1] = "No subject";
            }
            
            if(s[0].length() ==0)
            {
               s[0] = null;
            }
            File received = new File(path + File.separator + s[0].trim() + File.separator + subj[1].trim() + ".txt");
            
            PrintWriter pw1 = new PrintWriter(new FileWriter(received));
            pw1.println("From:" + fr);
            pw1.println("Time: " + time);
            pw1.println("Subject: " + subj[1].trim());
            pw1.println("Content: " + content);
            pw1.flush();
            pw1.close();
             
            String [] serverIp = to.split("@") ;
            if(serverIp.length > 2)
            {
               String [] ip = serverIp[1].split(">");
               this.connectServer(ip[0]);
            }              
         }catch(Exception e){
            jta.append("ERROR: " + e + "\n");
            connection = false;
         }
      }
      
      //Connect to other server
      public void connectServer(String in_Ip)
      {
         try
         {              
            soc1 = new Socket(in_Ip, 42069);
            pw1 = new PrintWriter(new OutputStreamWriter(soc1.getOutputStream()));
            br1 = new BufferedReader(new InputStreamReader(soc1.getInputStream()));
            this.receive();// receives messages from other server
         
            this.send("Send");
            String response = br1.readLine();
            
            if(response != null)
            {
               jta.append("S: " + response + "\n");
            }
            if(!response.startsWith("220"))
               throw new Exception("220 reply not received");
               
            this.send("HELO" + user);
            response = br1.readLine(); 
            if(response != null)
            {
               jta.append("S: " + response + "\n");
            }
            if(!response.startsWith("250"))
               throw new Exception("250 reply not received");
               
            this.send(from);
            response = br1.readLine();
            if(response != null)
            {
               jta.append("S: " + response + "\n");
            }
            if(!response.startsWith("250"))
               throw new Exception("250 reply not received"); 
            
            this.send(to);
            response = br1.readLine();  
            if(response != null)
            {
               jta.append("S: " + response + "\n");
            }
            if(!response.startsWith("250"))             
               throw new Exception("250 reply not received");
                  
            this.send("DATA");
            response = br1.readLine(); 
            if(response != null)
            {
               jta.append("S: " + response + "\n");
            }
            if(!response.startsWith("354"))
               throw new Exception("354 reply not received");
               
            this.send(from);
            this.send(to);      
            this.send(time);       
            this.send(subject);
                 
            if(encrypted)
            {      
               this.send("Encrypted");
               this.send("--Begin encrypted message--");
               this.send(content);                  
               this.send("--End encrypted message--");
            }
            else
            {
               this.send(content);  
            }
              
            this.send(".");
            response = br1.readLine();
            if(response != null)
            {
               jta.append("S: " + response + "\n");
            }
            if(!response.startsWith("250"))
               throw new Exception("250 reply not received");
               
            this.send("QUIT");
            response = br1.readLine(); 
            if(response != null)
            {
               jta.append("S: " + response + "\n");
            }               
            if(!response.startsWith("221"))
            {
               throw new Exception("221 reply not received");
            }
            
            pw1.close();
            br1.close();
            soc1.close();
         }catch(ArrayIndexOutOfBoundsException aeob){
            return;
            // /nothing
         }catch(IOException ioe){
            jta.append("ERROR: "+ioe);
            return;
          
         }catch(Exception ep){
            jta.append("ERROR: "+ep);
            return;
         }
      }
      private void send(String str) throws IOException
      {
         if(str != null)
         {
            pw1.println(str);
            pw1.flush();
            jta.append("C: " + str+"\n");
         }
      }
      
      // To login to other server
      public void receive()
      {
         try
         {
            String prompt = br1.readLine();
            if(prompt != null)
            {
               jta.append("S: " + prompt + "\n");//server attempting log in
               this.send("guest");
            }
            else
            {
               throw new Exception("Server didn't prompt log in / No reponse from server");
            }
            prompt = br1.readLine();//server sends back confirmation
            if(prompt.equals("Ok"))
            {
               jta.append("S: " + prompt + "\n");
            }
            else
            {
               jta.append(prompt + "\n");
            }  
         }
         catch(Exception e)
         {
            JOptionPane.showMessageDialog(null, "Cannot Open Socket: " + e);
            return;
         }
      }
      
      /* To check the users whether have email to receive when they log into server */
      public void check(String str)
      {
         BufferedReader brRead = null;
         ArrayList<String> lines = new ArrayList<String>(); 
         try
         { 
            File fi = new File(str);
            if(fi.isDirectory())
            {
               File[] mails = fi.listFiles();
               String num = Integer.toString(mails.length);
               pw.println(num);
               pw.flush();
               if(mails.length != 0)
               {     
                  for(int i = 0; i < mails.length; i++)
                  {
                     lines.clear();
                     String fileName = mails[i].getName();
                     pw.println(fileName);
                     pw.flush();
                     brRead = new BufferedReader(new FileReader(mails[i]));
                     String line = " ";
                     while((line = brRead.readLine())!=null)
                     {
                        lines.add(line);
                     }
                     pw.println(Integer.toString(lines.size()));
                     pw.flush();
                     for(int k = 0;k < lines.size();k++)
                     {
                        pw.println(lines.get(k));
                     }
                     pw.flush();
                     brRead.close();
                     mails[i].delete();
                  }
               }    
            }//if the user has mails stored inside the server 
         }
         catch(FileNotFoundException fnfe){
            jta.append("ERROR: "+fnfe+"\n");
         }
         catch(IOException ioe){
            jta.append("ERROR: "  + ioe + "\n");
            return;
         }
         catch(Exception e){
            jta.append("ERROR "+e+"\n");
            return;
         }
      }
               
   }//class ServerConnection
   
}// end SMTPServer class