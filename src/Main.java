import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.*;
public class Main {

    public static void main(String[] args) throws IOException {

        File root = new File("C:\\Users\\Legion\\Desktop\\proces1");

        Files.copy(Path.of("C:\\Users\\Legion\\Desktop\\proces1\\MM_tiger_wallpaper_2560x1080.jpg"), Path.of("C:\\Users\\Legion\\Desktop\\proces2\\MM_tiger_wallpaper_2560x1080.jpg"),REPLACE_EXISTING);
        /*File [] fileList = root.listFiles();

        for(File obj: fileList)
        {
            FileInputStream inputStream = new FileInputStream(obj);
            FileOutputStream outputStream = new FileOutputStream("C:\\Users\\Legion\\Desktop\\proces2\\"+obj.getName());
        }*/
    }
}
