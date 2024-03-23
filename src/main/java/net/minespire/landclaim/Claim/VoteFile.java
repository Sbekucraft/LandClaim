package net.minespire.landclaim.Claim;

import org.bukkit.configuration.ConfigurationOptions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class VoteFile {
  private YamlConfiguration yml;
  private File saveFile;
  private Path votesFilePath;
  private String pathString;
  public boolean newRegionAdded = false;
  private int indexToWrite;
  private static VoteFile voteFile;

  private VoteFile() {}

  public static void load() {
    voteFile = new VoteFile();

    voteFile.pathString = "plugins/LandClaim/votes" + ".yml";
    voteFile.votesFilePath = Paths.get(voteFile.pathString);

    if (!Files.exists(voteFile.votesFilePath)) {
      try {
        Files.createFile(voteFile.votesFilePath);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    voteFile.saveFile = new File(voteFile.pathString);
    voteFile.yml = YamlConfiguration.loadConfiguration(voteFile.saveFile);
    ConfigurationOptions configOptions = voteFile.yml.options();
    configOptions.pathSeparator('|');
    voteFile.save();
  }

  public static VoteFile get() {
    load();
    return voteFile;
  }

  public void save() {
    try {
      yml.options().copyDefaults(true);
      yml.save(saveFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public YamlConfiguration getYml() {
    return yml;
  }

  public VoteFile addVote(String regionName, String worldName, String playerUUID) {
    String regionSectionName = regionName + "," + worldName + "|votes";
    ConfigurationSection regionSection;
    if ((regionSection = yml.getConfigurationSection(regionSectionName)) == null) {
      regionSection = yml.createSection(regionSectionName);
      newRegionAdded = true;
    }
    regionSection.set(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString(), playerUUID);
    return this;
  }

  public Vote getLatestVote(String regionName, String worldName, String playerUUID) {
    String regionSectionName = regionName + "," + worldName + "|votes";
    ConfigurationSection regionSection;
    if ((regionSection = yml.getConfigurationSection(regionSectionName)) == null) {
      return null;
    }

    List<String> previousVotes = new ArrayList<>();
    regionSection.getValues(false).forEach((timeStamp, voteUUID) -> {
      if (voteUUID.equals(playerUUID)) {
        previousVotes.add(timeStamp);
      }
    });

    if (previousVotes.isEmpty()) {
      return null;
    }

    String newestTimeStamp = previousVotes.get(0);
    for (String timeStamp : previousVotes) {
      if (LocalDateTime.parse(timeStamp).isAfter(LocalDateTime.parse(newestTimeStamp))) {
        newestTimeStamp = timeStamp;
      }
    }

    //if(regionSection.getString(newestTimeStamp) == null) return null;
    return new Vote(regionName, worldName, playerUUID, LocalDateTime.parse(newestTimeStamp));
  }
}
