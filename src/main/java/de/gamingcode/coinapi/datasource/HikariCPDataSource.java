package de.gamingcode.coinapi.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.gamingcode.coinapi.datasource.configuration.DatasourceConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.*;

public class HikariCPDataSource implements DataSource {
  private final HikariDataSource dataSource;

  private final Executor threadPool =
    new ThreadPoolExecutor(3, 12, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

  private HikariCPDataSource(DatasourceConfiguration configuration) {
    HikariConfig config = new HikariConfig(configuration.getFile().getAbsolutePath());
    this.dataSource = new HikariDataSource(config);
  }

  public static HikariCPDataSource create(DatasourceConfiguration configuration) {
    return new HikariCPDataSource(configuration);
  }

  public void destroy() {
    dataSource.close();
  }

  @Override
  public void update(String query) {
    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(query)
    ) {
      statement.executeUpdate();
    } catch (SQLException sqlFailure) {
      throw new RuntimeException(sqlFailure);
    }
  }

  @Override
  public void search(String query, RuntimeSqlAction<ResultSet> action) {
    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(query);
         ResultSet result = statement.executeQuery()
    ) {
      action.accept(result);
    } catch (SQLException sqlFailure) {
      throw new RuntimeException(sqlFailure);
    }
  }

  @Override
  public void update(String query, RuntimeSqlAction<PreparedStatement> action) {
    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(query)
    ) {
      action.accept(statement);
      statement.executeUpdate();
    } catch (SQLException sqlFailure) {
      throw new RuntimeException(sqlFailure);
    }
  }

  @Override
  public void search(String query, RuntimeSqlAction<PreparedStatement> action, RuntimeSqlAction<ResultSet> response) {
    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(query)
    ) {
      action.accept(statement);
      try (ResultSet result = statement.executeQuery()) {
        response.accept(result);
      }
    } catch (SQLException sqlFailure) {
      throw new RuntimeException(sqlFailure);
    }
  }

  @Override
  public Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }
}
