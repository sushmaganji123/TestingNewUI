package com.simplifyqa.codeeditor.githandler;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.Formatter;

public class GitRelease {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_BRIGHT_ORANGE = "\u001B[38;5;202m";

    private static final Logger log = Logger.getLogger(GitRelease.class.getName());

    static {
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
        consoleHandler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                String level = record.getLevel().getLocalizedName();
                String message = record.getMessage().replaceAll("\\r?\\n", " ");
                if (level.equalsIgnoreCase("SEVERE")) {
                    return "[" + ANSI_RED + level + ANSI_RESET + "] " + message + System.lineSeparator();
                }
                return "[" + ANSI_BLUE + level + ANSI_RESET + "] " + message + System.lineSeparator();
            }
        });

        rootLogger.addHandler(consoleHandler);
        rootLogger.setLevel(Level.INFO);
    }

    public static void main(String[] args) {
        try {
            String versionReleasedString = executeRelease();
            log.info(ANSI_BRIGHT_ORANGE + "XXXXXXXXXXXXXXXXXXXXXXXXXXXXX> GIT RELEASE SUCCESSFULL: VERSION- "
                    + ANSI_YELLOW
                    + versionReleasedString + ANSI_BRIGHT_ORANGE + " <XXXXXXXXXXXXXXXXXXXXXXXXXXXXX" + ANSI_RESET);
            printSuccessMessage();
            System.exit(0);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            log.log(Level.SEVERE,
                    ANSI_RED + "FAILED TO PUBLISH THE JAR: " + ANSI_RESET + ANSI_YELLOW + e.getMessage() + ANSI_RESET);
            printErrorMessage();
            System.exit(1);
        }
    }

    private static String executeRelease() throws IOException, GitAPIException {
        Properties props = loadEnvFile();
        String gitUrl = props.getProperty("git.url");
        Path jarPath = findJarFile();

        // 1. First handle empty repository case (if needed)
        handleEmptyRepository(gitUrl, props);

        // 2. Now create release with JUST the JAR in a tag
        return createJarReleaseTag(gitUrl, jarPath, props);
    }

    private static void handleEmptyRepository(String gitUrl, Properties props)
            throws IOException, GitAPIException {
        CredentialsProvider creds = getCredentialsProvider(props);
        if (!isRepositoryEmpty(gitUrl, creds))
            return;

        Path tempDir = Files.createTempDirectory("git-init");
        try {
            Git git = Git.init().setDirectory(tempDir.toFile()).call();
            // Create minimal .gitignore for branch only
            Files.write(tempDir.resolve(".gitignore"), List.of("# Empty repo marker"));
            git.add().addFilepattern(".gitignore").call();
            git.commit().setMessage("Initial commit").call();
            git.push()
                    .setRemote(gitUrl)
                    .setCredentialsProvider(creds)
                    .add("master")
                    .call();
            log.info(ANSI_YELLOW + "Initialized empty repository" + ANSI_RESET);
        } finally {
            FileUtils.deleteDirectory(tempDir.toFile());
        }
    }

    private static String createJarReleaseTag(String gitUrl, Path jarPath, Properties props)
            throws IOException, GitAPIException {
        Path tempDir = Files.createTempDirectory("git-release");
        Git git = null;
        try {
            // Clone fresh to ensure clean state
            git = Git.cloneRepository()
                    .setURI(gitUrl)
                    .setDirectory(tempDir.toFile())
                    .setCredentialsProvider(getCredentialsProvider(props))
                    .setNoCheckout(true) // Important!
                    .call();

            // Checkout a new orphan branch for the tag
            git.checkout()
                    .setName("temp-release-branch")
                    .setOrphan(true)
                    .call();

            // Add ONLY the JAR file
            Path targetJarPath = tempDir.resolve(jarPath.getFileName());
            Files.copy(jarPath, targetJarPath);
            git.add().addFilepattern(jarPath.getFileName().toString()).call();

            // Create commit with JUST the JAR
            git.commit()
                    .setMessage("Release JAR")
                    .setAllowEmpty(false)
                    .call();

            // Create and push tag
            String newVersion = determineNextVersion(gitUrl, getCredentialsProvider(props));
            git.tag().setName(newVersion).setMessage("Release " + newVersion).call();
            git.push()
                    .setRemote(gitUrl)
                    .setCredentialsProvider(getCredentialsProvider(props))
                    .setPushTags()
                    .call();

            log.info(ANSI_GREEN + "Pushed JAR to tag: " + ANSI_YELLOW + newVersion + ANSI_RESET);
            return newVersion;
        } finally {
            if (git != null)
                git.close();
            FileUtils.deleteDirectory(tempDir.toFile());
        }
    }

    // Helper methods
    private static CredentialsProvider getCredentialsProvider(Properties props) {
        return new UsernamePasswordCredentialsProvider(
                props.getProperty("git.username"),
                props.getProperty("git.token"));
    }

    private static boolean isRepositoryEmpty(String gitUrl, CredentialsProvider creds)
            throws GitAPIException {
        return Git.lsRemoteRepository()
                .setRemote(gitUrl)
                .setCredentialsProvider(creds)
                .setHeads(true)
                .call()
                .isEmpty();
    }

    private static String determineNextVersion(String gitUrl, CredentialsProvider credentials) throws GitAPIException {
        Collection<Ref> remoteTags = Git.lsRemoteRepository()
                .setRemote(gitUrl)
                .setCredentialsProvider(credentials)
                .setTags(true)
                .call();

        int latestVersion = remoteTags.stream()
                .map(ref -> ref.getName().replace("refs/tags/", ""))
                .filter(name -> name.matches("^V\\d+$"))
                .mapToInt(name -> Integer.parseInt(name.substring(1)))
                .max()
                .orElse(0);

        return "V" + (latestVersion + 1);
    }

    private static Path findJarFile() throws IOException {
        Path target = Paths.get(System.getProperty("user.dir"), "target");
        Optional<Path> jar = Files.list(target)
                .filter(p -> p.toString().endsWith("jar-with-dependencies.jar"))
                .findFirst();
        if (jar.isEmpty()) {
            throw new RuntimeException("JAR file not found @: " + target);
        }
        Path jarPath = jar.get();
        log.info("--- " + ANSI_GREEN + "JAR file found at: " + ANSI_RESET + jarPath);
        return jarPath;
    }

    private static Properties loadEnvFile() throws IOException {
        Properties props = new Properties();
        Path envFilePath = Paths.get(".env");

        if (Files.exists(envFilePath)) {
            try (BufferedReader reader = Files.newBufferedReader(envFilePath)) {
                props.load(reader);
            }
        }

        int attempts = 0;
        final int MAX_ATTEMPTS = 3;

        while (attempts <= MAX_ATTEMPTS) {
            log.info("ACCESS ATTEMPTS: " + attempts);
            if (isAnyValueMissing(props, "git.url", "git.username", "git.token")) {
                if (!showSwingPopupAndUpdateEnvFile(props, envFilePath)) {
                    log.log(Level.SEVERE,ANSI_RED+"You cancelled the process of entering credentials."+ANSI_RESET);
                    System.exit(1);
                }
            }

            int validationResult = validateGitCredentials(props);
            switch (validationResult) {
                case 0: // Success
                    return props;
                case -1: // Repository issue
                    ConfirmPopup repoPopup = new ConfirmPopup(null,
                            "<html>Repository access problem.<br>Retry with different URL?</html>");
                    repoPopup.setVisible(true);
                    if (!repoPopup.isConfirmed())
                        System.exit(1);
                    else if (!showSwingPopupAndUpdateEnvFile(props, envFilePath)) {
                        System.exit(1);
                    }
                    break;
                default: // Credential issue
                    if (attempts + 1 <= MAX_ATTEMPTS) {
                        ConfirmPopup credPopup = new ConfirmPopup(null,
                                "<html>Invalid credentials (attempt " + (attempts + 1) + "/" + MAX_ATTEMPTS
                                        + ").<br>Try again?</html>");
                        credPopup.setVisible(true);
                        if (!credPopup.isConfirmed())
                            System.exit(1);
                        else {
                            if (!showSwingPopupAndUpdateEnvFile(props, envFilePath)) {
                                System.exit(1);
                            }
                        }
                    }

                    attempts++;
            }

            try (BufferedReader reader = Files.newBufferedReader(envFilePath)) {
                props.load(reader);
            }
        }

        new InfoPopup(null, "Maximum attempts reached. Exiting.").setVisible(true);
        System.exit(1);
        return props;
    }

    private static int validateGitCredentials(Properties props) {
        String gitUrl = props.getProperty("git.url");
        String gitUsername = props.getProperty("git.username");
        String gitToken = props.getProperty("git.token");

        CredentialsProvider credentials = new UsernamePasswordCredentialsProvider(gitUsername, gitToken);
        Path tempDir = null;
        Git git = null;

        try {
            // 1. First verify basic repository access
            try {
                Git.lsRemoteRepository()
                        .setRemote(gitUrl)
                        .setCredentialsProvider(credentials)
                        .call();
            } catch (TransportException e) {
                log.severe("Remote verification failed: " + e.getMessage());
                if (e.getMessage().toLowerCase().contains("not found")) {
                    log.severe("Remote Repo not found: " + e.getMessage());
                    return -1;
                }
                return -6;
            }

            // 2. Create a temp repo with a dummy commit
            tempDir = Files.createTempDirectory("git-auth-test");
            git = Git.init().setDirectory(tempDir.toFile()).call();

            // Create and commit a dummy file
            Files.write(tempDir.resolve("auth-test.txt"),
                    List.of("Temporary file for auth validation"),
                    StandardOpenOption.CREATE);
            git.add().addFilepattern("auth-test.txt").call();
            git.commit().setMessage("Auth test commit").call();

            // 3. Attempt dry-run push
            try {
                Iterable<PushResult> results = git.push()
                        .setRemote(gitUrl)
                        .setCredentialsProvider(credentials)
                        .setDryRun(true)
                        .setRefSpecs(new RefSpec("refs/heads/master:refs/heads/auth-test"))
                        .call();

                // Verify push would succeed
                for (PushResult result : results) {
                    for (RemoteRefUpdate update : result.getRemoteUpdates()) {
                        if (update.getStatus() != RemoteRefUpdate.Status.OK) {
                            log.severe("Push rejected: " + update.getMessage());
                            return -2; // Write permission issue
                        }
                    }
                }
                return 0; // Success
            } catch (NoHeadException e) {
                log.severe("No HEAD exists in repository: " + e.getMessage());
                return -3;
            }
        } catch (TransportException e) {
            log.severe("Error occurred during validation: " + e.getMessage());
            return -4;
        } catch (Exception e) {
            if (e.getCause().getMessage().toLowerCase().contains("not supported")) {
                log.severe("Remote Repo not supported: " + e.getCause().getMessage());
                return -1;
            }
            log.severe("Validation error: " + e.getMessage());
            return -5;
        } finally {
            if (git != null)
                git.close();
            if (tempDir != null) {
                try {
                    FileUtils.deleteDirectory(tempDir.toFile());
                } catch (IOException e) {
                    log.warning("Failed to clean temp directory: " + e.getMessage());
                }
            }
        }
    }

    private static boolean isAnyValueMissing(Properties props, String... keys) {
        for (String key : keys) {
            String value = props.getProperty(key);
            if (value == null || value.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static boolean showSwingPopupAndUpdateEnvFile(Properties props, Path envFilePath) {
        GitCredentialsDialog dialog = new GitCredentialsDialog(null, props);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            String gitUrl = dialog.getGitUrl();
            String gitUsername = dialog.getGitUsername();
            String gitToken = dialog.getGitToken();

            if (!gitUrl.isEmpty() && !gitUsername.isEmpty() && !gitToken.isEmpty()) {
                props.setProperty("git.url", gitUrl);
                props.setProperty("git.username", gitUsername);
                props.setProperty("git.token", gitToken);

                try (BufferedWriter writer = Files.newBufferedWriter(envFilePath, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING)) {
                    props.store(writer, "Git Credentials");
                } catch (IOException e) {
                    new InfoPopup(null, "Failed to save .env file: " + e.getMessage()).setVisible(true);
                }
            } else {
                new InfoPopup(null, "All fields are required!").setVisible(true);
            }
        } else {
            new InfoPopup(null, "Operation cancelled. Exiting...").setVisible(true);
            return false;
        }
        return true;
    }

    private static void printSuccessMessage() {
        String successBanner = ANSI_GREEN +
                "\t\t\t\t                                                           \n" +
                "\t\t\t\t                                                           \n" +
                "\t\t\t\t  ███████╗██╗   ██╗ ██████╗ ██████╗███████╗███████╗███████╗\n" +
                "\t\t\t\t  ██╔════╝██║   ██║██╔════╝██╔════╝██╔════╝██╔════╝██╔════╝\n" +
                "\t\t\t\t  ███████╗██║   ██║██║     ██║     █████╗  ███████╗███████╗\n" +
                "\t\t\t\t  ╚════██║██║   ██║██║     ██║     ██╔══╝  ╚════██║╚════██║\n" +
                "\t\t\t\t  ███████║╚██████╔╝╚██████╗╚██████╗███████╗███████║███████║\n" +
                "\t\t\t\t  ╚══════╝ ╚═════╝  ╚═════╝ ╚═════╝╚══════╝╚══════╝╚══════╝\n" +
                "\t\t\t\t                                                           \n" +
                "\t\t\t\t                                                           \n" + ANSI_RESET;

        System.out.println(successBanner);
    }

    private static void printErrorMessage() {
        String errorBanner = ANSI_RED +
                "\t\t\t\t                                             \n" +
                "\t\t\t\t                                             \n" +
                "\t\t\t\t  ███████╗ █████╗ ██╗██╗     ███████╗██████╗ \n" +
                "\t\t\t\t  ██╔════╝██╔══██╗██║██║     ██╔════╝██╔══██╗\n" +
                "\t\t\t\t  █████╗  ███████║██║██║     █████╗  ██║  ██║\n" +
                "\t\t\t\t  ██╔══╝  ██╔══██║██║██║     ██╔══╝  ██║  ██║\n" +
                "\t\t\t\t  ██║     ██║  ██║██║███████╗███████╗██████╔╝\n" +
                "\t\t\t\t  ╚═╝     ╚═╝  ╚═╝╚═╝╚══════╝╚══════╝╚═════╝ \n" +
                "\t\t\t\t                                             \n" +
                "\t\t\t\t                                             \n" + ANSI_RESET;

        System.out.println(errorBanner);
    }
}