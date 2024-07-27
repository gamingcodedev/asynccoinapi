package de.gamingcode.coinapi.datasource.configuration;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

@Getter
public class DatasourceConfiguration {

  private final File file;

  private DatasourceConfiguration(JavaPlugin javaPlugin) {
    if (!javaPlugin.getDataFolder().exists())
      javaPlugin.getDataFolder().mkdir();
    File file = new File(javaPlugin.getDataFolder(), "datasource.properties");
    if (!file.exists()) {
      try {
        file.createNewFile();
        this.file = file;
        this.fillFile();
        return;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    this.file = file;
  }

  private void fillFile() {
    if (this.file == null) {
      System.out.println("Could not create datasource.properties.");
      return;
    }
    Properties properties = new Properties();

    properties.setProperty("driverClassName", "com.mysql.cj.jdbc.Driver");
    properties.setProperty("jdbcUrl", "jdbc:mysql://localhost:3306/database?createDatabaseIfNotExist=true&useSSL=false&characterEncoding=utf-8&useUnicode=true&serverTimezone=Europe/Berlin");
    properties.setProperty("maximumPoolSize", "16");
    properties.setProperty("minimumIdle", "2");
    properties.setProperty("username", "user");
    properties.setProperty("password", "password");
    properties.setProperty("poolName", "CoinAPI");
    properties.setProperty("dataSource.cachePrepStmts", "true");
    properties.setProperty("dataSource.prepStmtCacheSize", "350");
    properties.setProperty("dataSource.prepStmtCacheSqlLimit", "2048");
    properties.setProperty("dataSource.useServerPrepStmts", "true");
    properties.setProperty("dataSource.useLocalSessionState", "true");
    properties.setProperty("dataSource.rewriteBatchedStatements", "true");
    properties.setProperty("dataSource.cacheResultSetMetadata", "true");
    properties.setProperty("dataSource.cacheServerConfiguration", "true");
    properties.setProperty("dataSource.elideSetAutoCommits", "true");
    properties.setProperty("dataSource.maintainTimeStats", "false");

    try (FileOutputStream outputStream = new FileOutputStream(this.file.getPath())) {
      properties.store(outputStream, "Database Configuration");
      System.out.println("Properties file created successfully!");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static DatasourceConfiguration create(JavaPlugin javaPlugin) {
    return new DatasourceConfiguration(javaPlugin);
  }
}

