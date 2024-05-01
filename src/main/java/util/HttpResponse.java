package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class HttpResponse {
    private String testDirectory="./src/test/resources/";


    public void responseForward() throws Exception{
       // HttpResponse response = new HttpResponse(createOutputStream("Http_Forward.txt"));
    }

    private OutputStream createOutputStream(String filename) throws FileNotFoundException {
        return new FileOutputStream(new File(testDirectory+filename));
    }
}
