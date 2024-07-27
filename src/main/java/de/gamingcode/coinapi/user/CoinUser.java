package de.gamingcode.coinapi.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Builder
public class CoinUser {

  private final UUID uuid;
  @Getter
  @Setter
  private long coins;

  public void increaseCoins(long coins) {
    setCoins(getCoins() + coins);
  }

  public void decreaseCoins(long coins) {
    setCoins(getCoins() - coins);
  }

  public boolean hasEnough(long coins) {
    return getCoins() >= coins;
  }

  public UUID getUUID() {
    return uuid;
  }
}
