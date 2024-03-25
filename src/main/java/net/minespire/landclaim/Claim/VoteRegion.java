package net.minespire.landclaim.Claim;

import org.bukkit.configuration.ConfigurationSection;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class VoteRegion {
  private final String regionCommaWorld;
  private final String regionName;
  private final String regionWorld;
  private int votesThisYear;
  private int votesThisMonth;
  private int votesToday;
  private static final List<VoteRegion> voteRegionList = new ArrayList<>();

  public VoteRegion(String regionCommaWorld) {
    this.regionCommaWorld = regionCommaWorld;
    this.regionName = regionCommaWorld.split(",")[0];
    this.regionWorld = regionCommaWorld.split(",")[1];
    this.votesThisYear = 0;
    this.votesThisMonth = 0;
    this.votesToday = 0;
  }

  public static void tallyAllVotes() {
    VoteFile voteFile = VoteFile.get();
    voteRegionList.clear();
    for (String region : voteFile.getYml().getKeys(false)) {
      ConfigurationSection regionSection = voteFile.getYml().getConfigurationSection(region);
      VoteRegion voteRegion = new VoteRegion(region);
      for (String vote : regionSection.getConfigurationSection("votes").getKeys(false)) {
        countVotes(voteRegion, vote);
      }
      voteRegionList.add(voteRegion);
    }
  }

  public static void tallyVotesFor(String regionCommaWorld) {
    VoteFile voteFile = VoteFile.get();
    ConfigurationSection regionSection = voteFile.getYml().getConfigurationSection(regionCommaWorld);
    VoteRegion voteRegion = voteRegionList.stream().filter(a -> a.getRegionCommaWorld().equals(regionCommaWorld)).findFirst().orElseGet(() -> {
      VoteRegion vRegion = new VoteRegion(regionCommaWorld);
      voteRegionList.add(vRegion);
      return vRegion;
    });
    voteRegion.clearVotes();
    for (String vote : regionSection.getConfigurationSection("votes").getKeys(false)) {
      countVotes(voteRegion, vote);
    }
  }

  private void clearVotes() {
    votesToday = 0;
    votesThisMonth = 0;
    votesThisYear = 0;
  }

  public static void addVote(String regionCommaWorld) {
    VoteRegion voteRegion = voteRegionList.stream().filter(a -> a.getRegionCommaWorld().equals(regionCommaWorld)).findFirst().orElseGet(() -> {
      VoteRegion vRegion = new VoteRegion(regionCommaWorld);
      voteRegionList.add(vRegion);
      return vRegion;
    });
    voteRegion.votesThisYear++;
    voteRegion.votesThisMonth++;
    voteRegion.votesToday++;
  }

  public static void removeRegion(String regionCommaWorld) {
    VoteRegion voteRegion = null;
    try{
      voteRegion = voteRegionList.stream().filter(a -> a.getRegionCommaWorld().equals(regionCommaWorld)).toList().get(0);
    } catch(Exception e){ return; }
    voteRegion.votesThisYear = 0;
    voteRegion.votesThisMonth = 0;
    voteRegion.votesToday = 0;
    voteRegionList.remove(voteRegion);
  }

  private static void countVotes(VoteRegion voteRegion, String vote) {
    LocalDateTime voteDateTime = LocalDateTime.parse(vote);
    LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    if (Duration.between(voteDateTime, now).toDays() < 1) {
      voteRegion.votesToday++;
      voteRegion.votesThisMonth++;
      voteRegion.votesThisYear++;
    } else if (Duration.between(voteDateTime, now).toDays() < voteDateTime.getMonth().length(false)) {
      voteRegion.votesThisMonth++;
      voteRegion.votesThisYear++;
    } else if (Duration.between(voteDateTime, now).toDays() < 365) {
      voteRegion.votesThisYear++;
    }
  }

  public static List<VoteRegion> getVoteRegionList() {
    return voteRegionList;
  }

  public int getVotesThisYear() {
    return votesThisYear;
  }

  public int getVotesThisMonth() {
    return votesThisMonth;
  }

  public int getVotesToday() {
    return votesToday;
  }

  public void addVote() {
    votesThisYear++;
    votesThisMonth++;
    votesToday++;
  }

  public String getRegionCommaWorld() {
    return regionCommaWorld;
  }

  public String getRegionName() {
    return regionName;
  }

  public String getRegionWorld() {
    return regionWorld;
  }
}
