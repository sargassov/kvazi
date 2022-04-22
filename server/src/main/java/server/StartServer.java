package server;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.*;
import java.util.logging.SimpleFormatter;


public class StartServer {

    private static final Logger log = Logger.getLogger(StartServer.class.getName());

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        try{
            Handler h = new FileHandler("logging.log");
            h.setFormatter(new SimpleFormatter());
            log.addHandler(h);

            for(Handler o : log.getHandlers()){
                System.out.println(o);
            }

        }catch (IOException e){
            e.printStackTrace();
        }
        new Server(log);
    }
}
