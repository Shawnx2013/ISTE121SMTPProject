/**
  *The client class
  *@ author: Shawn Xu, Zirong cao, Shoujing Wu, Vincent Li
  *@version: 2018.5.4
 **/

import java.net.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class SMTPClient1 extends JFrame implements ActionListener,ListSelectionListener
{
   private JButton send = new JButton("Send");
   
   private JLabel jlFrom = new JLabel("From: ");
   private JLabel jlTo = new JLabel("     To: ");
   private JLabel jlSubject = new JLabel("Subject: ");
   
   private JTextField jtfFrom = new JTextField(20);
   private JTextField jtfTo = new JTextField(20);
   private JTextField jtfSubject = new JTextField(20);
   
   private JMenu jm = new JMenu("Menu");
   private JMenuItem jmiExit = new JMenuItem("Exit");
   private JMenuItem jmiConnect = new JMenuItem("Connect");
   
   private JTextArea jtaMessage = new JTextArea();
   private JTextArea jtaLog = new JTextArea();
   private JScrollPane jspInbox = new JScrollPane();
   private JList list = new JList<>();
   
   private JButton jbSend = new JButton("Send");
   private JButton jbEncrypt = new JButton("Encrypt");
   
   private Socket soc = null;
   private PrintWriter pw = null;
   private BufferedReader br = null;
   private File fl = null;
   private String[] mailbox = new String[1000];
   
   private String user = null;  
   private String encrypted = "";
   private boolean encrypt = false;
   private String path = null;
   private String cont = null;
   
   private SimpleDateFormat sdf = new SimpleDateFormat("E, dd MM yyyy hh:mm:ss");
   
   /* Main method */
   public static void main(String args[])
   {
      new SMTPClient1();
   }
   
   /*Constructor*/
   public SMTPClient1() 
   {
      this.setLocation(100,100);
      this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
      JPanel jpLeft = new JPanel();
      
      JPanel jpAddress = new JPanel();
   
      JScrollPane message = new JScrollPane();
      JScrollPane log = new JScrollPane();
      JMenuBar jMenuBar1 = new JMenuBar();
   
      //jspInbox.setViewportView(list);
   
      GroupLayout jpLeftLayout = new GroupLayout(jpLeft);
      jpLeft.setLayout(jpLeftLayout);
      jpLeftLayout.setHorizontalGroup(
            jpLeftLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jspInbox, GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
         );
      jpLeftLayout.setVerticalGroup(
            jpLeftLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jspInbox)
         );
   
   
      GroupLayout jpAddressLayout = new GroupLayout(jpAddress);
      jpAddress.setLayout(jpAddressLayout);
      jpAddressLayout.setHorizontalGroup(
            jpAddressLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jpAddressLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpAddressLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(jlFrom)
                    .addComponent(jlTo))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jpAddressLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jtfFrom)
                    .addComponent(jtfTo))
                .addContainerGap())
         );
      jpAddressLayout.setVerticalGroup(
            jpAddressLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jpAddressLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jpAddressLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jlFrom)
                    .addComponent(jtfFrom, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jpAddressLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jlTo)
                    .addComponent(jtfTo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(22, Short.MAX_VALUE))
         );
   
      jtaMessage.setColumns(20);
      jtaMessage.setRows(5);
      message.setViewportView(jtaMessage);
   
      jtaLog.setColumns(20);
      jtaLog.setRows(5);
      log.setViewportView(jtaLog);
   
      jm.add(jmiExit);
      jmiExit.addActionListener(this);
      jtaMessage.setLineWrap(true);//formatting policy
      jtaMessage.setWrapStyleWord(true);
   
      jm.add(jmiConnect);
      jmiConnect.addActionListener(this);
      jbEncrypt.addActionListener(this);
      jbSend.addActionListener(this);
      
      //list.addListSelectionListener(this);
      
      jMenuBar1.add(jm);
   
      setJMenuBar(jMenuBar1);
   
      GroupLayout layout = new GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jpLeft, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jpAddress, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(message, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
                            .addComponent(log, GroupLayout.Alignment.TRAILING)
                            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jbEncrypt)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jbSend))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jlSubject)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jtfSubject)))
                        .addContainerGap())))
         );
      layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jpLeft, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jpAddress, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jlSubject)
                    .addComponent(jtfSubject, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(message, GroupLayout.PREFERRED_SIZE, 223, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jbSend)
                    .addComponent(jbEncrypt))
                .addGap(11, 11, 11)
                .addComponent(log, GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                .addGap(7, 7, 7))
         ); 
      jtfFrom.setEditable(false);
      jtfTo.setToolTipText("Please enter the format like this if you are sending mails to other servers, \"yourID@ip address\"");
       
      this.pack();
      this.setVisible(true);
   }//constructor
   
   /* Mailbox */
   public void valueChanged(ListSelectionEvent lse)
   {
      if(lse.getValueIsAdjusting()) 
      {
         JFrame jf = new JFrame();
         JTextArea jta1 = new JTextArea();
         JButton jb = new JButton("Decrypt");
         jf.setTitle("Mail");
         jf.setSize(500,500);
         jf.setLocationRelativeTo(this);
         jta1.setFont(new Font("MONOSPACED",Font.PLAIN, 13));
         JPanel jp = new JPanel();
         jp.add(jb);
         jf.add(jp,BorderLayout.NORTH);
         jf.add(new JScrollPane(jta1), BorderLayout.CENTER);
         
         try
         {  
            fl = new File(path + "\\" + user + "\\" + mailbox[list.getSelectedIndex()]+".txt");
            BufferedReader br1 = new BufferedReader(new FileReader(fl));
            String line = null;
            int lineCount = 0;
            
            
            while((line = br1.readLine())!=null)
            {
               lineCount++;
               jta1.append(line+"\n");
               if(line.startsWith("Content:"))
               {
                  String [] abc = line.split(":");
                  cont = abc[1];
               }
            }
            br1.close();
         }catch(FileNotFoundException fnfe){
                   //nothing
         }catch(IOException ioe){
            System.out.println(ioe);
         }
         
         jb.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent aet)
               {
               
                  jta1.append(" \n Original Message: "+doDecrypt(cont));
               
               }
            });// annoymous inner class
        
         jf.setVisible(true);
      }
   }// valueChanged
   
   /* Override ActionListener */
   public void actionPerformed(ActionEvent ae)
   {
      switch(ae.getActionCommand())
      {
         case "Exit":
            System.exit(0);
            break;
         
         case "Connect":
            File mail = null;
            File folder = null;
            
            LogIn log = new LogIn();
            int result = JOptionPane.showConfirmDialog(this, log, "Enter UserName and Server address", 
                                                       JOptionPane.OK_CANCEL_OPTION);
            if(result == JOptionPane.OK_OPTION)
            {
               try
               {
                  soc = new Socket(log.jtfServerIp.getText(), 42069);
                  pw = new PrintWriter(new OutputStreamWriter(soc.getOutputStream()));
                  br = new BufferedReader(new InputStreamReader(soc.getInputStream()));
                  
                  user = log.jtfUser.getText(); 
                  boolean guest = false;
                    
                  String prompt = br.readLine();
                  
                  if(prompt != null)
                  {
                     jtaLog.append("S: " + prompt + "\n");//server attempting log in
                     pw.println(user);
                     pw.flush();
                  }
                  else
                  {
                     throw new Exception("Server didn't prompt log in / No reponse from server");
                  }
                  
                  prompt = br.readLine();//server sends back confirmation
                  if(prompt.equalsIgnoreCase("Ok"))
                  {
                     jtaLog.append("S: " + prompt + "\n");
                     jtfFrom.setText(user.trim());
                     
                     /* creating folders for user */
                     folder = new File(user);
                     if(!folder.exists())
                     {
                        folder.mkdir();
                     }
                     
                     //to get the emails
                     PrintWriter pw1 = null;
                     int fileNumber = Integer.parseInt(br.readLine());
                     for(int k = 0;k < fileNumber;k++)
                     {
                        String fileName = br.readLine();
                        int lineCount = Integer.parseInt(br.readLine());
                        System.out.println(fileName);
                        mail = new File(path + File.separator + user + File.separator + fileName);
                        if(!mail.exists())
                        {
                           mail.createNewFile();
                        }
                        pw1 = new PrintWriter(new FileWriter(mail,true));
                        for(int i = 0;i < lineCount;i++)
                        {
                           pw1.println(br.readLine());   
                        }
                        pw1.flush();
                        pw1.close();
                     }
                     
                     //to create mailbox
                     File[] all = folder.listFiles();
                     for(int k = 0;k < all.length;k++)
                     {
                        String [] w =  all[k].getName().split(".txt");
                        mailbox[k] = w[0];
                     }
                     list = new JList<>(mailbox);
                     jspInbox.setViewportView(list);
                     list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);   
                     
                     list.addListSelectionListener(this);
                     
                     //guest = true;   // if the client can't connect to other servers,  
                     //  please remove this comment, make guest true.
                  }
                  else
                  {
                     jtaLog.append(prompt + "\n");
                     jtfFrom.setText("guest");
                     //guest = true;
                  }
                  
                  File fl2 = new File(".");
                  path = fl2.getCanonicalPath();//get current path
                  System.out.println(path);
                  jmiConnect.setText("Disconnect");                  
               }
               catch(Exception e)
               {
                  JOptionPane.showMessageDialog(null, "Cannot Open Socket: " + e);
                  break;
               }
            }
            break;
        
         case "Disconnect":
            try
            {
               soc.close();
               br.close();
               pw.close();
               jtfFrom.setText("");
               jtfTo.setText("");
               jtfSubject.setText("");
               jtaMessage.setText("");
               jmiConnect.setText("Connect");
            }
            catch(IOException ioe)
            {
               JOptionPane.showMessageDialog(this, "Cannot close socket " + ioe);
            }
            catch(Exception e)
            {
               JOptionPane.showMessageDialog(this, "Exception closing socket " + e);
               
            } 
            break;
         
         case "Send":
            try
            {
               if(jtfTo.getText().length() == 0 || jtaMessage.getText().length() == 0)
               {
                  throw new Exception("Please make sure you enter the recipent and the subject");
               }
               else
               {
                  this.send("Send");
                  //System.out.println(jtaMessage.getText());
                  String response = br.readLine();
                  if(response != null)
                  {
                     jtaLog.append("S: " + response + "\n");
                  } 
                  if(!response.startsWith("220"))
                  {
                     throw new Exception("220 reply not received");
                  }
               
                  this.send("HELO " + user);
                  response = br.readLine(); 
                  if(response != null)
                  {
                     jtaLog.append("S: " + response + "\n");
                  }
                  if(!response.startsWith("250"))
                  {
                     throw new Exception("250 reply not received");
                  }
               
                  this.send("MAIL FROM:<" + jtfFrom.getText()+">");
                  response = br.readLine();
                  if(response != null)
                  {
                     jtaLog.append("S: " + response + "\n");
                  }
                  if(!response.startsWith("250"))
                  {
                     throw new Exception("250 reply not received");
                  }
               
                  this.send("RCPT TO:<" + jtfTo.getText()+">");
                  response = br.readLine();  
                  if(response != null)
                  {
                     jtaLog.append("S: " + response + "\n");
                  }
                  if(!response.startsWith("250"))
                  {           
                     throw new Exception("250 reply not received");
                  }
               
                  this.send("DATA");
                  response = br.readLine(); 
                  if(response != null)
                  {
                     jtaLog.append("S: " + response + "\n");
                  }
                  if(!response.startsWith("354"))
                  {
                     throw new Exception("354 reply not received");
                  }
               
                  this.send("From: <" + jtfFrom.getText()+">");
                  this.send("TO: <" + jtfTo.getText()+">");
               
                  Date currentDate = new Date();
                  String time = sdf.format(currentDate);
                  this.send(time);
               
                  //this.send("Subject: " + jtfSubject.getText());
               
                  if(encrypt)
                  {
                     this.send("Encrypted");
                     this.send("--Begin encrypted message--");
                     this.send(doEncrypt("Subject: " + jtfSubject.getText() + "\nMessage:" + jtaMessage.getText().replaceAll("\n"," ")));               
                     this.send("--End encrypted message--");
                  }
                  else
                  {
                     this.send("Subject: " + jtfSubject.getText() + "\nMessage:" +  jtaMessage.getText().replaceAll("\n"," "));  
                  }
               
                  this.send(".");
                  response = br.readLine();
                  if(response != null)
                  {
                     jtaLog.append("S: " + response + "\n");
                  }
                  if(!response.startsWith("250"))
                  {
                     throw new Exception("250 reply not received");
                  }
                  
                  this.send("QUIT");
                  response = br.readLine(); 
                  if(response != null)
                  {
                     jtaLog.append("S: " + response + "\n");
                  }               
                  if(!response.startsWith("221"))
                  {
                     throw new Exception("221 reply not received");   
                  }
               }          
            }
            catch(IOException ioe)
            {
               jtaLog.append("IOE Error: " + ioe + "\n");
               return;
            }
            catch(Exception e)
            {
               jtaLog.append("Error: " + e + "\n");
               return;
            }
            break;
            
         case "Encrypt": 
            encrypt = true;
            jbEncrypt.setText("Cancel");
            break;
          
         case "Cancel":
            encrypt = false;
            jbEncrypt.setText("Encrypt");
            break;
            
      }//switch
      
   }//actionPerformed
   
   private void send(String str) throws IOException
   {
      if(str != null)
      {
         pw.println(str);
         pw.flush();
         jtaLog.append("C: " + str+"\n");
      }
   }
   
   /* encrypt message */ 
   public String doEncrypt(String inStr)
   {
      
      char [] ptArray = inStr.toCharArray();
      char [] ctArray = new char [ptArray.length];
      
      for(int i = 0;i < ptArray.length;i++)
      { 
         int asc = ptArray[i];
         asc = (asc - 32 + 3)%95 + 32;
         ctArray[i] = (char)asc;
      }
      String ct = new String(ctArray);   
      return ct;
   }
   
   /* decrypt message */
   public String doDecrypt(String inStr)
   {
      char [] ptArray = inStr.toCharArray();
      char [] ctArray = new char [ptArray.length];
      
      for(int i = 0;i < ptArray.length;i++)
      { 
         int asc = ptArray[i];
         asc = (asc - 32 - 3)%95 + 32;
         ctArray[i] = (char)asc;
      }
      String ct = new String(ctArray); 
      return ct;
   }
   
   /* Login window */
   class LogIn extends JPanel 
   {
      JLabel jlUser = new JLabel("UserName: ");
      JLabel jlServerIp = new JLabel("Server: ");
      
      JTextField jtfUser = new JTextField(10);
      JTextField jtfServerIp = new JTextField(15);
   
      public LogIn()
      { 
         JPanel jpNorth = new JPanel();
         jpNorth.add(jlUser);
         jpNorth.add(jtfUser);
         jpNorth.add(jlServerIp);
         jpNorth.add(jtfServerIp);
         this.add(jpNorth, BorderLayout.NORTH);
         jtfServerIp.setText("localhost");
         this.setVisible(true);
      }
   }//LogIn window
         
}//client