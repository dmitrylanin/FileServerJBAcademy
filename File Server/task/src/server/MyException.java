package server;

import java.io.IOException;

public class MyException extends IOException {
    public static boolean isGlobalStop;

    MyException(boolean isGlobalStop){
       this.isGlobalStop = isGlobalStop;
    }

}