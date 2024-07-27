package de.gamingcode.coinapi;

import de.gamingcode.coinapi.datasource.DataSource;
import de.gamingcode.coinapi.datasource.HikariCPDataSource;
import de.gamingcode.coinapi.datasource.configuration.DatasourceConfiguration;
import de.gamingcode.coinapi.user.CoinUserRepository;
import de.gamingcode.coinapi.user.LocalCoinUserRepository;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class CoinAPI extends JavaPlugin {

  @Getter
  private static CoinAPI instance;

  @Getter
  private CoinUserRepository coinUsers;

  @Override
  public void onEnable() {
    instance = this;

    DataSource dataSource = HikariCPDataSource.create(DatasourceConfiguration.create(this));
    this.coinUsers = LocalCoinUserRepository.create(dataSource).saveQueue();
  }

  @Override
  public void onDisable() {
    this.coinUsers.saveAll();
  }
}
