import com.rabbitmq.client.*;

import static java.nio.file.StandardCopyOption.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

// javac -cp amqp-client-5.7.1.jar src/ChatRoom.java
// java -cp .;../amqp-client-5.7.1.jar.;../slf4j-api-1.7.26.jar;../slf4j-simple-1.7.26.jar ChatRoom
// C:\\Users\\Legion\\Desktop\\proces1
//C:\\Users\\Legion\\Desktop\\proces2


public class ChatRoom {
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
        Scanner in = new Scanner(System.in);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare("chatroom","fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName,"chatroom","");

        System.out.println("slusam poruke");
        System.out.print("Unesite ime: ");
        name = in.nextLine();
        System.out.println();
        System.out.print("Unesite putanju do foldera: ");
        path = in.nextLine();
        System.out.println();


        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");

            String [] parts = message.split("\r?\n|\r");

            if(parts[1].equals("IMAGE")){
                String imagePath = parts[2];
                decodeImage(parts[4], path+"\\"+parts[3]);
                String message1 = message.substring(0, message.lastIndexOf("\n"));
                System.out.println(message1);
            }else {
                System.out.println(message);
            }

        };

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});


        while(true){
            input = in.nextLine();
            if(input.endsWith(".jpg") || input.endsWith(".png"))
            {
                String temp = input.substring(input.lastIndexOf('\\')+1);
                messageType = "IMAGE";
                fileName = temp;
                String encodedImage = encodeImage(input);
                message = name+":\n"+messageType+"\n"+input+"\n"+fileName+"\n"+encodedImage;

            }else{
                messageType="TEXT";
                message = name+":\n"+messageType+"\n"+input+"\n";
            }

            channel.basicPublish("chatroom", "", MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
        }


    }
}
