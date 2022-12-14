package top.plutomc.qqbind;

import cc.carm.lib.easysql.EasySQL;
import cc.carm.lib.easysql.api.SQLManager;
import cc.carm.lib.easysql.api.enums.IndexType;
import cc.carm.lib.easysql.hikari.HikariConfig;
import okhttp3.OkHttpClient;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import top.plutomc.qqbind.utils.BindUtil;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public final class QQBindPlugin extends JavaPlugin {
    public static JavaPlugin INSTANCE;
    public static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(4);
    public static final HikariConfig HIKARI_CONFIG = new HikariConfig();
    public static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient();
    private static File configFile;
    private static FileConfiguration config;
    private static SQLManager sqlManager;

    public static File getConfigFile() {
        return configFile;
    }

    private static void initConfig() {
        QQBindPlugin.INSTANCE.saveDefaultConfig();

        configFile = new File(QQBindPlugin.INSTANCE.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            QQBindPlugin.INSTANCE.getLogger().info("Creating default config file...");
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                QQBindPlugin.INSTANCE.getLogger().severe("Failed to create config file!");
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        config.addDefault("database.host", "127.0.0.1");
        config.addDefault("database.port", 3306);
        config.addDefault("database.user", "root");
        config.addDefault("database.password", "123456");
        config.addDefault("database.database", "minecraft");
        config.addDefault("database.table", "qq_bind");
        config.addDefault("database.waiting-to-verify-table", "qq_bind_waiting_to_verify");

        config.options().copyDefaults(true);

        QQBindPlugin.INSTANCE.saveConfig();
    }

    public static SQLManager getSqlManager() {
        return sqlManager;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        getLogger().info(" ");
        getLogger().info("PlutoMC QQ Bind | By GerryYuu");

        initConfig();

        HIKARI_CONFIG.setDriverClassName("com.mysql.cj.jdbc.Driver");
        HIKARI_CONFIG.setJdbcUrl("jdbc:mysql://" + config.getString("database.host") + ":" + config.getInt("database.port") + "/");
        HIKARI_CONFIG.setUsername(config.getString("database.user"));
        HIKARI_CONFIG.setPassword(config.getString("database.password"));

        sqlManager = EasySQL.createManager(HIKARI_CONFIG);
        try {
            sqlManager.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        sqlManager.executeSQLBatch("use " + config.getString("database.database") + ";");
        try {
            // Create main data table
            sqlManager.createTable(config.getString("database.table"))
                    .addColumn("uuid", "LONGTEXT")
                    .addColumn("name", "LONGTEXT")
                    .addColumn("qq", "BIGINT")
                    .setIndex(IndexType.INDEX, "qq_index", "qq")
                    .build().execute();

            // Create verify data table
            sqlManager.createTable(config.getString("database.waiting-to-verify-table"))
                    .addColumn("uuid", "LONGTEXT")
                    .addColumn("name", "LONGTEXT")
                    .addColumn("qq", "BIGINT")
                    .setIndex(IndexType.INDEX, "qq_index", "qq")
                    .build().execute();
        } catch (SQLException e) {
            getLogger().severe("Failed to create table!");
        }

        BindUtil.setSqlManager(sqlManager);

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        getLogger().info("Plugin enabled.");
        getLogger().info(" ");
    }

    @Override
    public void onDisable() {
        getLogger().info(" ");
        getLogger().info("Saving data...");

        EXECUTOR_SERVICE.shutdown();

        getLogger().info("Plugin disabled.");
        getLogger().info(" ");
    }
}