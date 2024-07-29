package artifacts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.Constant;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * The Artifacts class handles the execution of external commands, such as scripts,
 * and logs their output and errors. It includes methods to check video and command logs
 * related to test sessions.
 */
public class Artifacts extends Constant {

    private final Logger ltLogger = LogManager.getLogger(Artifacts.class);

    /**
     * Default constructor for the Artifacts class.
     * Initializes an instance of the Artifacts with default settings.
     */
    public Artifacts() {
        super();
    }

    /**
     * Executes a command using the provided list of command strings and logs the output.
     *
     * @param command A list of strings representing the command and its arguments.
     */
    public void processCommand(List<String> command) {
        try {
            // Initialize a ProcessBuilder with the command list.
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            // Capture and log the standard output of the process.
            BufferedReader stdOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = stdOutput.readLine()) != null) {
                ltLogger.info("Standard Output :- {}", line);
            }

            // Capture and log the standard error output of the process.
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = stdError.readLine()) != null) {
                ltLogger.error("Standard Error :- {}", line);
            }

            // Wait for the process to complete and log the exit code.
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                ltLogger.info("Exited with code: {}", exitCode);
            }

        } catch (Exception e) {
            // Log any exceptions that occur during the command execution.
            ltLogger.error(e);
        }
    }


    /**
     * Checks the video for the given session ID, username, and access key.
     *
     * @param sessionID The session ID of the video.
     * @param userName The username for authentication.
     * @param accessKey The access key for authentication.
     */
    public void checkVideo(String sessionID, String userName, String accessKey) {
        // Prepare the command to execute the video verification script.
        List<String> command = new ArrayList<>();
        command.add(System.getProperty(Constant.USER_DIR) + "/src/main/resources/bash/video.sh");
        command.add(sessionID);
        command.add(userName);
        command.add(accessKey);

        // Log the constructed command and execute it.
        ltLogger.info("Video Verification Command :- {}", command);
        processCommand(command);
    }


    /**
     * Checks the command logs for the given session ID, username, and access key.
     *
     * @param sessionID The session ID for the logs.
     * @param userName The username for authentication.
     * @param accessKey The access key for authentication.
     */
    public void checkCommandLogs(String sessionID, String userName, String accessKey) {
        // Prepare the command to execute the command logs verification script.
        List<String> command = new ArrayList<>();
        command.add(System.getProperty(Constant.USER_DIR) + "/src/main/resources/bash/command_logs.sh");
        command.add(sessionID);
        command.add(userName);
        command.add(accessKey);

        // Log the constructed command and execute it.
        ltLogger.info("Command Logs Verification Command :- {}", command);
        processCommand(command);
    }
}
