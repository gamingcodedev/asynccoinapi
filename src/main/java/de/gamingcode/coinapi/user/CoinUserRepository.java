package de.gamingcode.coinapi.user;

import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface CoinUserRepository {

  default CompletableFuture<CoinUser> getOrCreate(Player player) {
    return getOrCreate(player.getUniqueId());
  }

  CompletableFuture<CoinUser> getOrCreate(UUID uuid);

  default CompletableFuture<CoinUser> increaseCoins(Player player, long coins) {
    return increaseCoins(player.getUniqueId(), coins);
  }

  CompletableFuture<CoinUser> increaseCoins(UUID uuid, long coins);

  default CompletableFuture<CoinUser> decreaseCoins(Player player, long coins) {
    return decreaseCoins(player.getUniqueId(), coins);
  }

  CompletableFuture<CoinUser> decreaseCoins(UUID uuid, long coins);

  void saveAll();

  void invalidate(UUID uuid);

}
