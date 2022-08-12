import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeoutException;

// javac -cp amqp-client-5.7.1.jar src/gui.java
// java -cp .;../amqp-client-5.7.1.jar.;../slf4j-api-1.7.26.jar;../slf4j-simple-1.7.26.jar gui
// C:\\Users\\Legion\\Desktop\\proces1
//C:\\Users\\Legion\\Desktop\\proces2

import com.rabbitmq.client.*;
public class gui  {

    static String input;
    static String name;
    static String path;
    static String messageType;
    static String message;
    static String fileName;

    public static String encodeImage(String imagePath) throws IOException {
        FileInputStream imageStream = new FileInputStream(imagePath);
        byte[] data = imageStream.readAllBytes();
        String imageString = Base64.getEncoder().encodeToString(data);
        imageStream.close();
        return imageString;
    }

    public static void decodeImage(String encodedImage, String savePath) throws IOException {
        byte[] data = Base64.getDecoder().decode(encodedImage.getBytes());

        FileOutputStream fileOutputStream = new FileOutputStream(savePath);
        fileOutputStream.write(data);
        fileOutputStream.close();

    }


    public static void main(String[] args) throws IOException, TimeoutException {
        JFrame frame = new JFrame();


        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("GUI");
        frame.setVisible(true);
        frame.setLayout(null);
        frame.setSize(800, 800);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare("chatroom","fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName,"chatroom","");

        JTextArea textArea = new JTextArea();
        textArea.setVisible(true);
//        textArea.setSize(300, 300);
        textArea.setBounds(1,1,600, 500);



        JTextField textField = new JTextField();
        textField.setVisible(true);
//        textField.setSize( 300, 100);
        textField.setBounds(1,505 ,600, 50);



        JButton button = new JButton();
        button.setText("Send");
//        button.setSize(200,100);
        button.setBounds(1 , 560, 600, 50);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                input = textField.getText();
                if(input.endsWith(".jpg") || input.endsWith(".png"))
                {
                    String temp = input.substring(input.lastIndexOf('\\')+1);
                    messageType = "IMAGE";
                    fileName = temp;
                    String encodedImage = null;
                    try {
                        encodedImage = encodeImage(input);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    message = name+":\n"+messageType+"\n"+input+"\n"+fileName+"\n"+encodedImage;

                }else{
                    messageType="TEXT";
                    message = name+":\n"+messageType+"\n"+input+"\n";
                }

                try {
                    channel.basicPublish("chatroom", "", MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                textField.setText("");
            }
        });


        JPanel introPanel = new JPanel();
        introPanel.setLayout(null);


        JTextField nameField = new JTextField();
        nameField.setBounds( 1, 1 , 300, 50);

        JTextField pathField = new JTextField();
        pathField.setBounds( 1, 55 , 300, 50);

        JButton register = new JButton("Register");
        register.setBounds(1, 110, 300, 50);
        register.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(nameField.getText() != "" && pathField.getText() != ""){
                    name = nameField.getText();
                    path = pathField.getText();
                    introPanel.setVisible(false);
                }
            }
        });



        introPanel.setBounds(300, 300, 300, 300);
        introPanel.add(nameField);
        introPanel.add(pathField);
        introPanel.add(register);

        frame.add(introPanel);
        frame.add(textArea);
        frame.add(textField);
        frame.add(button);


        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");

            String [] parts = message.split("\r?\n|\r");

            if(parts[1].equals("IMAGE")){
                String imagePath = parts[2];
                decodeImage(parts[4], path+"\\"+parts[3]);
                String message1 = message.substring(0, message.lastIndexOf("\n"));
                textArea.append(message1+'\n');
            }else {
                textArea.append(message+'\n');
            }

        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});


    }


}
