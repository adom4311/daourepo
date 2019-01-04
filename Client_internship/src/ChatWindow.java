import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
 
public class ChatWindow {
  private String name;
  private Frame frame;
  private Panel pannel;
  private Button buttonSend;
  private Button buttonfile;
  private TextField textField;
  private TextArea textArea;
   
  private Socket socket;
 
  public ChatWindow(String name, Socket socket) {
    this.name = name;
    frame = new Frame(name);
    pannel = new Panel();
    buttonSend = new Button("Send");
    buttonfile = new Button("File send");
    textField = new TextField();
    textArea = new TextArea(30, 80);
    this.socket = socket;
 
    new ChatClientReceiveThread(socket).start();
  }
 
  public void show() {
    // Button
    buttonSend.setBackground(Color.GRAY);
    buttonSend.setForeground(Color.WHITE);
    buttonfile.setForeground(Color.WHITE);
    buttonSend.addActionListener( new ActionListener() {
      @Override
      public void actionPerformed( ActionEvent actionEvent ) {
        sendMessage();
      }
    });
    buttonfile.addActionListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			//sendfile();
			
		}
    	
    });
     
 
    // Textfield
    textField.setColumns(80);
    textField.addKeyListener( new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        char keyCode = e.getKeyChar();
        if (keyCode == KeyEvent.VK_ENTER) {
          sendMessage();
        }
      }
    });
 
    // Pannel
    pannel.setBackground(Color.LIGHT_GRAY);
    pannel.add(textField);
    pannel.add(buttonSend);
    pannel.add(buttonfile);
    frame.add(BorderLayout.SOUTH, pannel);
 
    // TextArea
    textArea.setEditable(false);
    frame.add(BorderLayout.CENTER, textArea);
 
    // Frame
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        PrintWriter pw;
        try {
          pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
          String request = "quit\r\n";
          pw.println(request);
          System.exit(0);
        }
        catch (IOException e1) {
          e1.printStackTrace();
        }
      }
    });
    frame.setVisible(true);
    frame.pack();
  }
   
  // 쓰레드를 만들어서 대화를 보내기
  private void sendMessage() {
    PrintWriter pw;
    try {
      pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
      String message = textField.getText();
      String request = "message:" + message + "\r\n";
      pw.println(request);
 
      textField.setText("");
      textField.requestFocus();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
  //file transfer
  private void sendfile() {
	  PrintWriter pw;
	  File file = new File("C:\\Users\\user\\Desktop\\asdf.jpg");
	  OutputStream out;
	  InputStream in;
	  long length = file.length();
	  byte[] bytes = new byte[16*1024];
	  try
	  {
		  pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
		  String request="file:start\r\n";
		  pw.println(request);
		  
		  in = new FileInputStream(file);
		  out=socket.getOutputStream();
		  int count;
		  while((count=in.read(bytes))>0) {
			  out.write(bytes,0,count);
		  }
	  }catch(IOException e) {
		  e.printStackTrace();
	  }
  }
  //file transfer
  private class ChatClientReceiveThread extends Thread{
    Socket socket = null;
     
    ChatClientReceiveThread(Socket socket){
      this.socket = socket;
    }
 
    public void run() {
      try {
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        while(true) {
          String msg = br.readLine();
          textArea.append(msg);
          textArea.append("\n");
        }
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
