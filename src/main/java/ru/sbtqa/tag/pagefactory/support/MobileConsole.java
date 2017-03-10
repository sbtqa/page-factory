package ru.sbtqa.tag.pagefactory.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.pagefactory.PageFactory;

public class MobileConsole {
    
    private static final Logger LOG = LoggerFactory.getLogger(MobileConsole.class);
    
    public static boolean execute(String command) {
        //TODO get static variable from mobile driver
        String deviceUDID = (String) PageFactory.getMobileDriver().getSessionDetails().get("deviceUDID");

        ProcessBuilder processBuilder = new ProcessBuilder(new String[]{"adb", "-s", deviceUDID, "shell", command});
        LOG.info("Command '{}' is processing...", command);
        Process process;
        try {
            process = processBuilder.start();

            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            LOG.debug(builder.toString());
            
            return (process.waitFor() == 0);
        } catch (IOException | InterruptedException ex) {
            LOG.error("Failed to process command '{}'", command);
        }
        
        return false;
    }
}
