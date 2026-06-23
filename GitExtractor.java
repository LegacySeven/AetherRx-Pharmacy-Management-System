import java.io.*;

public class GitExtractor {
    public static void main(String[] args) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("git", "show", "HEAD:src/main/java/com/pharmacy/controller/MainController.java");
        pb.directory(new File("c:\\Users\\crispin\\Desktop\\HWGA\\PharmacyManagementSystem"));
        Process p = pb.start();
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new FileWriter("c:\\Users\\crispin\\Desktop\\old_MainController.java"));
        
        String line;
        while ((line = reader.readLine()) != null) {
            writer.write(line);
            writer.newLine();
        }
        writer.close();
        p.waitFor();
    }
}
