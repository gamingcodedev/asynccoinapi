package de.gamingcode.coinapi.user;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import de.gamingcode.coinapi.CoinAPI;
import de.gamingcode.coinapi.datasource.DataSource;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class LocalCoinUserRepository implements CoinUserRepository {

  private final Queue<CoinUser> saveQueue = Queues.newConcurrentLinkedQueue();

  private final Map<UUID, CoinUser> coinUserMap = Maps.newHashMap();

  private final DataSource dataSource;

  private LocalCoinUserRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public static LocalCoinUserRepository create(DataSource dataSource) {
    return new LocalCoinUserRepository(dataSource);
  }

  public LocalCoinUserRepository saveQueue() {
    Bukkit.getScheduler().scheduleSyncRepeatingTask(CoinAPI.getInstance(), () -> {
      int size = saveQueue.size();
      if (size == 0) {
        return;
      }
      long start = System.currentTimeMillis();
      while (!saveQueue.isEmpty()) {
        save(saveQueue.poll());
      }
      long duration = System.currentTimeMillis() - start;
      System.out.println("Saved " + size + " user(s). (" + duration + "ms)");
    }, 20 * 60, 20 * 60);
    return this;
  }

  private Optional<CoinUser> findCoinUser(UUID uuid) {
    return Optional.ofNullable(this.coinUserMap.get(uuid));
  }

  @Override
  public CompletableFuture<CoinUser> getOrCreate(UUID uuid) {
    return findCoinUser(uuid)
      .map(CompletableFuture::completedFuture).orElseGet(() -> {
        CompletableFuture<CoinUser> coinUserCompletableFuture = new CompletableFuture<>();
        CoinUser coinUser = CoinUser.builder().uuid(uuid).build();
        this.dataSource.search("SELECT coins FROM coin_users WHERE uuid=?",
          preparedStatement -> preparedStatement.setString(1, uuid.toString()),
          resultSet -> {
            if (resultSet.next()) {
              coinUser.setCoins(resultSet.getLong(1));
            } else {
              coinUser.setCoins(0);
              this.insertUser(coinUser);
            }
            coinUserCompletableFuture.complete(coinUser);
          });
        this.coinUserMap.put(uuid, coinUser);
        return coinUserCompletableFuture;
      });
  }

  private void insertUser(CoinUser coinUser) {
    this.dataSource.update("INSERT INTO coin_users (uuid,coins) VALUES (?,?)", preparedStatement -> {
      preparedStatement.setString(1, coinUser.getUUID().toString());
      preparedStatement.setLong(2, coinUser.getCoins());
    });
  }

  @Override
  public CompletableFuture<CoinUser> increaseCoins(UUID uuid, long coins) {
    return getOrCreate(uuid).thenAccept(coinUser -> {
        coinUser.increaseCoins(coins);
        saveToQueue(coinUser);
      })
      .thenApply(unused -> this.coinUserMap.get(uuid));
  }

  @Override
  public CompletableFuture<CoinUser> decreaseCoins(UUID uuid, long coins) {
    return getOrCreate(uuid).thenAccept(coinUser -> {
        coinUser.decreaseCoins(coins);
        saveToQueue(coinUser);
      })
      .thenApply(unused -> this.coinUserMap.get(uuid));
  }

  @Override
  public void saveAll() {
    this.saveQueue.clear();
    for (CoinUser coinUser : this.coinUserMap.values()) {
      save(coinUser);
    }
  }

  @Override
  public void invalidate(UUID uuid) {
    CoinUser coinUser = this.coinUserMap.remove(uuid);
    this.saveQueue.removeIf(coinUser1 -> coinUser1.getUUID().equals(coinUser.getUUID()));

    this.save(coinUser);
  }

  private void save(CoinUser coinUser) {
    this.dataSource.update("UPDATE coin_users SET coins=? WHERE uuid=?", preparedStatement -> {
      preparedStatement.setLong(1, coinUser.getCoins());
      preparedStatement.setString(2, coinUser.getUUID().toString());
    });
  }

  private void saveToQueue(CoinUser coinUser) {
    this.saveQueue.removeIf(coinUser1 -> coinUser1.getUUID().equals(coinUser.getUUID()));
    this.saveQueue.add(coinUser);
  }
}
