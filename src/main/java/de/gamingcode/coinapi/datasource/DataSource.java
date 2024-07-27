package de.gamingcode.coinapi.datasource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public interface DataSource {

  void update(String query);

  void update(String query, RuntimeSqlAction<PreparedStatement> action);

  void search(String query, RuntimeSqlAction<ResultSet> action);

  void search(String query, RuntimeSqlAction<PreparedStatement> action,
              RuntimeSqlAction<ResultSet> response);

  Connection getConnection() throws SQLException;

  interface RuntimeSqlAction<Type> {

    void accept(Type type) throws SQLException;
  }
}

