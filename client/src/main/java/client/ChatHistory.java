package client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ChatHistory {

    private static ArrayList<String> history;
    private static int initialCapacity = 100;
    private static File file;
    private static FileWriter fileWriter;

    public ChatHistory(){
        history = new ArrayList<>(initialCapacity);
        file = new File("1.txt");
        try {
            if(!file.exists())
                file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void addToHistory(String note){
            history.add(note);
            try {
                fileWriter = new FileWriter(file.getAbsolutePath(), true);
                fileWriter.write(note + "\n");
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    public int getInitialCapacity() {
        return initialCapacity;
    }

    public ArrayList<String> exportHistory(){
        return history;
    }


}
