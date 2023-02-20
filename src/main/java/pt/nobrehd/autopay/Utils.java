package pt.nobrehd.autopay;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Utils {
    public static final Gson GSON = new Gson();
    public static String PREFIX;
    public static final File CONFIG = new File(FabricLoader.getInstance().getConfigDir().toFile(), "autopay-dic.json");
    public static Map<String, ServerConfig> DICT;

    public static void init() {
        // Prefix
        Utils.PREFIX = "§6AutoPay§r » ";

        // Config
        if (CONFIG.exists()) {
            Utils.DICT = ServerConfig.convertToMap(GSON.fromJson(readFile(), ServerConfig[].class));
        } else {
            saveFile("[]");
            Utils.DICT = new HashMap<>();
        }
    }

    public static void addToDict(String serverIP, String pattern, int timeout) {
        ServerConfig config = new ServerConfig(serverIP, pattern, timeout);
        Utils.DICT.put(serverIP, config);
        saveDict();
    }

    public static void reloadDict() {
        Utils.DICT = ServerConfig.convertToMap(GSON.fromJson(readFile(), ServerConfig[].class));
    }

    public static void downloadDict(String url, boolean overwrite) {
        try {
            Scanner scanner = new Scanner(new java.net.URL(url).openStream());
            StringBuilder builder = new StringBuilder();
            while (scanner.hasNextLine()) {
                builder.append(scanner.nextLine());
            }
            if (overwrite) {
                saveFile(builder.toString());
            } else {
                Map<String, ServerConfig> map = ServerConfig.convertToMap(GSON.fromJson(builder.toString(), ServerConfig[].class));
                Utils.DICT.putAll(map);
            }
            reloadDict();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void openConfig() {
        try {
            Runtime.getRuntime().exec("explorer.exe /select," + CONFIG.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveDict() {
        saveFile(GSON.toJson(ServerConfig.convertToArray(Utils.DICT)));
    }

    private static String readFile() {
        try {
            Scanner scanner = new Scanner(CONFIG);
            StringBuilder builder = new StringBuilder();
            while (scanner.hasNextLine()) {
                builder.append(scanner.nextLine());
            }
            return builder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveFile(String json) {
        try {
            FileWriter writer = new FileWriter(CONFIG);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Text format(String s) {
        return Text.literal(Utils.PREFIX + s);
    }
    public static String getPattern(String server) {
        if (Utils.DICT.containsKey(server)) {
            return Utils.DICT.get(server).pattern;
        }
        return null;
    }

    public static int getDelay(String server) {
        if (Utils.DICT.containsKey(server)) {
            return Utils.DICT.get(server).timeout;
        }
        return 0;
    }

    public static class ServerConfig {
        public String serverIP;
        public String pattern;
        public int timeout;

        public ServerConfig(String serverIP, String pattern, int timeout) {
            this.serverIP = serverIP;
            this.pattern = pattern;
            this.timeout = timeout;
        }

        public static Map<String, ServerConfig> convertToMap(ServerConfig[] configs) {
            Map<String, ServerConfig> map = new HashMap<>();
            for (ServerConfig config : configs) {
                map.put(config.serverIP, config);
            }
            return map;
        }

        public static ServerConfig[] convertToArray(Map<String, ServerConfig> map) {
            ServerConfig[] configs = new ServerConfig[map.size()];
            int i = 0;
            for (Map.Entry<String, ServerConfig> entry : map.entrySet()) {
                configs[i] = entry.getValue();
                i++;
            }
            return configs;
        }
    }
}
