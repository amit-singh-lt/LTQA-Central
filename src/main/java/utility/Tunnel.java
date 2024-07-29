package utility;

import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static utility.Constant.*;

/**
 * The Tunnel class provides methods and properties for managing tunnel configurations and operations,
 * typically used for creating secure connections or network tunnels in testing environments.
 * This class may include functionalities such as starting, stopping, and configuring tunnels.
 * It extends the Utilities class to utilize additional utility methods and properties.
 */
public class Tunnel extends Utilities {
    private final Logger ltLogger = LogManager.getLogger(Tunnel.class);


    String userName;
    String accessKey;
    String tunnelName;

    /**
     * Default constructor for the Tunnel class.
     * Initializes an instance of Tunnel with default settings.
     */
    public Tunnel() {
        super();
    }


    /**
     * Constructs a Tunnel instance with the given credentials and tunnel name.
     *
     * @param userName The username for authentication.
     * @param accessKey The access key for authentication.
     * @param tunnelName The name of the tunnel.
     */
    public Tunnel(String userName, String accessKey, String tunnelName) {
        this.userName= userName;
        this.accessKey= accessKey;
        this.tunnelName= tunnelName;
    }


    /**
     * Selects a random mode from the TUNNEL_MODES array.
     * @return a random mode as a String.
     */
    public String getRandomMode() {
        return TUNNEL_MODES[newRandom().nextInt(2)];
    }


    /**
     * Determines the operating system and returns the corresponding tunnel directory path.
     * @return the tunnel directory path based on the OS.
     */
    public String tunnelOS() {
        String osName = System.getProperty(OS_NAME);
        ltLogger.info("OS Name :- {}", osName);
        if (osName.contains("Mac")) {
            return TUNNEL_DIRECTORY_MAC;
        } else if (osName.contains("Linux")) {
            return TUNNEL_DIRECTORY_LINUX;
        } else if (osName.contains("Win")) {
            return TUNNEL_DIRECTORY_WINDOWS;
        }
        return null;
    }


    /**
     * Constructs and returns a ProcessBuilder for starting the tunnel with the specified mode.
     * @param tunnelMode the mode in which to start the tunnel.
     * @return a configured ProcessBuilder for starting the tunnel.
     */
    public ProcessBuilder startTunnelCLI (String tunnelMode) {
        String tunnelPath = tunnelOS();

        ltLogger.info("Tunnel Name :- {}", tunnelName);

        String tunnelLogPath = System.getProperty(USER_DIR) + TUNNEL_LOG_DIRECTORY + tunnelName + ".log";
        ltLogger.info("Tunnel Log Path :- {}", tunnelLogPath);

        String availableOpenPort = getOpenPort();
        ltLogger.info("Port which is available :- {}", availableOpenPort);

        ProcessBuilder startTunnelCLICommand = new ProcessBuilder(tunnelPath, "--user",
                userName, "--key", accessKey,
                "--tunnelName", tunnelName, "--port", availableOpenPort, "--logFile", tunnelLogPath,
                "--mitm", tunnelMode, "--verbose");

        String startTunnelCLICommandString = String.join(" ", startTunnelCLICommand.command());
        ltLogger.info("Tunnel Start Command :- {}", startTunnelCLICommandString);

        return startTunnelCLICommand;
    }


    /**
     * Starts the tunnel process and checks if the tunnel was successfully started by reading its output.
     * @param startTunnelCLICommand the ProcessBuilder for the tunnel command.
     * @return true if the tunnel started successfully, false otherwise.
     */
    @SneakyThrows
    public boolean isTunnelStatusSuccess(ProcessBuilder startTunnelCLICommand) {
        Process process = startTunnelCLICommand.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        String line;
        boolean breakAfterNextLine = false;
        int currentLineNumber = 0;

        ltLogger.info("Reading process output ...");
        while ((line = reader.readLine()) != null) {
            currentLineNumber ++;
            ltLogger.info(line);
            if (line.contains("Tunnel Name was not specified")) {
                tunnelName = line.split("Tunnel Name was not specified, using: ")[1].trim();
                ltLogger.info("Tunnel Name was not specified, using: {}", tunnelName);
            }
            if (line.contains("You can start testing now")) {
                breakAfterNextLine = true;
            }
            if (line.contains("Tunnel ID:") && breakAfterNextLine) {
                return true;
            }
            if (currentLineNumber >= 50) {
                ltLogger.error("Looks like Tunnel is not started");
            }
        }

        ltLogger.info("Reading process error output (if any) ...");
        while ((line = errorReader.readLine()) != null) {
            ltLogger.error(line);
        }

        ltLogger.info("Process exited with code :- {}", process.waitFor());

        return false;
    }


    /**
     * Attempts to start the tunnel, retrying up to a specified number of times if it fails.
     * @param tunnelMode the mode in which to start the tunnel.
     * @param maxTunnelRetries the maximum number of retry attempts.
     */
    public void startTunnel(String tunnelMode, int maxTunnelRetries) {
        for (int retries = 0; retries <= maxTunnelRetries; retries++) {
            ProcessBuilder startTunnelCLICommand = startTunnelCLI(tunnelMode);
            if (isTunnelStatusSuccess(startTunnelCLICommand)) {
                return;
            }
            if (retries < maxTunnelRetries) {
                ltLogger.warn("Tunnel Server is not initiated. Retrying Tunnel Command Again With Different Port (Attempt {}/{})", retries + 1, maxTunnelRetries);
                String availableOpenPort = getOpenPort();
                List<String> command = startTunnelCLICommand.command();
                for (int i = 0; i < command.size(); i++) {
                    if (command.get(i).equals("--port")) {
                        command.set(i + 1, availableOpenPort);
                        break;
                    }
                }
                startTunnelCLICommand.command(command);
            }
        }
        ltLogger.error("Tunnel Server is not initiated after {} attempts", maxTunnelRetries);
        throw new RuntimeException("Tunnel Server is not initiated after " + maxTunnelRetries + " attempts");
    }


    /**
     * Constructs and returns a ProcessBuilder for stopping the tunnel.
     * @return a configured ProcessBuilder for stopping the tunnel.
     */
    public ProcessBuilder stopTunnelCLI () {
        List<String> stopTunnelCLICommand = Arrays.asList("/bin/sh", "-c", "ps -ef | grep '" + tunnelName + "' | grep -v grep | awk '{print $2}' | xargs kill -9");
        ltLogger.info("Tunnel Stop Command :- {}", stopTunnelCLICommand);

        return new ProcessBuilder(stopTunnelCLICommand);
    }


    /**
     * Uploads the logs for the tunnel.
     */
    public void uploadTunnelLogs() {
        String tunnelFilePath = System.getProperty(USER_DIR) + TUNNEL_LOG_DIRECTORY + tunnelName + ".log";
        try {
            ltLogger.info("RP_MESSAGE#FILE#{}#{}", tunnelFilePath, tunnelFilePath);
        } catch (Exception e) {
            ltLogger.error("Tunnel Log file attachment failed");
        }
    }



    /**
     * Stops the tunnel by executing the stop tunnel command.
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if the process is interrupted while waiting.
     */
    public void stopTunnel () throws IOException, InterruptedException {
        ProcessBuilder stopTunnelCLICommand = stopTunnelCLI();
        Process process = stopTunnelCLICommand.start();
        stopTunnelCLICommand.start();

        String line;
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        ltLogger.info("Reading process error output (if any) ...");
        while ((line = errorReader.readLine()) != null) {
            ltLogger.error(line);
        }

        ltLogger.info("Process exited with code :- {}", process.waitFor());
    }
}
