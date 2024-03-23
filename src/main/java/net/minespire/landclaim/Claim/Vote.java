package net.minespire.landclaim.Claim;

import java.time.Duration;
import java.time.LocalDateTime;

public class Vote {
  String regionName;
  String worldName;
  String playerUUID;
  LocalDateTime voteDateTime;

  public Vote(String regionName, String worldName, String playerUUID, LocalDateTime voteDateTime) {
    this.regionName = regionName;
    this.worldName = worldName;
    this.playerUUID = playerUUID;
    this.voteDateTime = voteDateTime;
  }

  public boolean dayHasPassed() {
    return duration().toDays() > 1L;
  }

  public boolean weekHasPassed() {
    return duration().toDays() > 7L;
  }

  public boolean monthHasPassed() {
    return duration().toDays() > 30L;
  }

  public boolean yearHasPassed() {
    return duration().toDays() > 365;
  }

  public long daysSinceLastVote() {
    return duration().toDays();
  }

  private Duration duration() {
    return Duration.between(voteDateTime, LocalDateTime.now());
  }
}
