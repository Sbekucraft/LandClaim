package net.minespire.landclaim.Command;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.minespire.landclaim.Claim.*;
import net.minespire.landclaim.GUI.GUIManager;
import net.minespire.landclaim.GUI.NGUI;
import net.minespire.landclaim.LandClaim;
import net.minespire.landclaim.Prompt.Prompt;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MainCommand implements CommandExecutor {
  private Player player = null;
  private boolean isPlayer = false;
  private final GUIManager guiManager = GUIManager.getInst();

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (sender instanceof Player) {
      player = (Player) sender;
      isPlayer = true;
    }
    if (args.length < 1) {
      if (isPlayer) {
        GUIManager.getInst().openMainGUI(player);
      }
      return true;
    }
    try {
      switch (args[0].toLowerCase()) {
        case "gui":
          if (isPlayer) {
            GUIManager.getInst().openMainGUI(player);
          }
          break;
        case "claim":
          if (player != null) {
            if (args[1] == null) {
              return false;
            }
            if (!Claimer.permToOwnAnotherRegion(player) && !player.isOp()) {
              player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_CannotClaim"));
              return true;
            }
            if (!Claimer.permissionToClaimInWorld(player) && !player.isOp()) {
              player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_CannotClaimInWorld"));
              return true;
            }
            if (!ProtectedRegion.isValidId(args[1])) {
              player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_InvalidRegionName"));
              return true;
            }
            Claim claim = new Claim(player, args[1]);
            if (!claim.createClaim()) {
              return true;
            }
            if (claim.overlapsUnownedRegion()) {
              player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_OverlappingRegions"));
              return true;
            }
            if (!meetsClaimLimits(claim)) {
              return true;
            }
            if (LandClaim.econ.getBalance(player) < claim.getClaimCost()) {
              player.sendMessage(Claim.parsePlaceholders(LandClaim.plugin.getLocalizedString("Messages.Error_NotEnoughMoney"), claim));
              return true;
            }
            LandClaim.claimMap.put(player.getUniqueId().toString(), claim);
            NGUI claimGUI = new NGUI(36, LandClaim.plugin.getLocalizedString("GUI.Title_ClaimRegion"));
            claimGUI.addItem(Material.GRASS_BLOCK, LandClaim.plugin.getLocalizedString("GUI.PopupTitle_ClaimRegion"), guiManager.parseLoreString(Claim.parsePlaceholders(LandClaim.plugin.getLocalizedString("GUI.Lore_RegionDescription"), claim)), 13);
            claimGUI.addItem(Material.ARROW, LandClaim.plugin.getLocalizedString("GUI.Action_Back"), null, 31);
            claimGUI.addItem(Material.BIRCH_DOOR, LandClaim.plugin.getLocalizedString("GUI.Action_Close"), null, 33);
            claimGUI.open(player);
          } else {
            sender.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_NotAPlayer"));
          }
          break;
        case "claimplot":
          if (player != null) {
            if (args[1] == null) {
              return false;
            }
            if (!Claimer.permToOwnAnotherPlot(player) && !player.isOp()) {
              player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_CannotClaim"));
              return true;
            }
            if (!Claimer.permissionToClaimInWorld(player) && !player.isOp()) {
              player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_CannotClaimInWorld"));
              return true;
            }
            if (!ProtectedRegion.isValidId(args[1])) {
              player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_InvalidPlotName"));
              return true;
            }
            Claim claim = new Claim(player, args[1], true);
            if (!claim.createClaim()) {
              return true;
            }
            if (!claim.insideOwnedRegion()) {
              player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_CannotClaimPlot"));
              return true;
            }
            if (claim.overlapsUnownedRegion()) {
              player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_OverlappingRegions"));
              return true;
            }
            if (LandClaim.econ.getBalance(player) < claim.getClaimCost()) {
              player.sendMessage(Claim.parsePlaceholders(LandClaim.plugin.getLocalizedString("Messages.Error_NotEnoughMoney"), claim));
              return true;
            }
            LandClaim.claimMap.put(player.getUniqueId().toString(), claim);
            NGUI claimGUI = new NGUI(36, LandClaim.plugin.getLocalizedString("GUI.Title_ClaimPlot"));
            claimGUI.addItem(Material.DIRT, LandClaim.plugin.getLocalizedString("GUI.PopupTitle_ClaimPlot"), guiManager.parseLoreString(Claim.parsePlaceholders(LandClaim.plugin.getLocalizedString("GUI.Lore_RegionDescription"), claim)), 13);
            claimGUI.addItem(Material.ARROW, LandClaim.plugin.getLocalizedString("GUI.Action_Back"), null, 31);
            claimGUI.addItem(Material.BIRCH_DOOR, LandClaim.plugin.getLocalizedString("GUI.Action_Close"), null, 33);
            claimGUI.open(player);
          } else {
            sender.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_NotAPlayer"));
          }
          break;
        case "reload":
          if ((sender instanceof Player) && !player.hasPermission("landclaim.reload") && !player.isOp()) {
            sender.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_MissingPermission"));
            return true;
          }
          LandClaim.plugin.reloadConfig();
          LandClaim.plugin.getCommand("lc").setTabCompleter(new CommandCompleter());
          LandClaim.plugin.getCommand("lc").setExecutor(new MainCommand());
          sender.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Info_ConfigReloaded"));
          break;
        case "cancel":
          if (Prompt.hasActivePrompt(player)) {
            Prompt.getPrompt(player.getName()).cancelPrompt();
          }
          break;
        case "nearby":
          if (!(sender instanceof Player)) {
            sender.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_NotAPlayer"));
          }
          if ((sender instanceof Player) && !player.hasPermission("landclaim.nearby")) {
            sender.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_MissingPermission"));
            return true;
          }
          String playerName = player.getName();
          Player nPlayer = Bukkit.getPlayer(player.getName());
          if (Visualizer.seeNearbyBukkitTask.containsKey(playerName) && Visualizer.seeNearbyAsyncService.containsKey(playerName)) {
            Bukkit.getScheduler().cancelTask(Visualizer.seeNearbyBukkitTask.get(playerName));
            Visualizer.seeNearbyAsyncService.get(playerName).shutdown();
            Visualizer.seeNearbyBukkitTask.remove(playerName);
            Visualizer.seeNearbyAsyncService.remove(playerName);
            Visualizer.timer.remove(playerName);
            nPlayer.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Info_NearbyDisabled"));
            return true;
          }
          AtomicInteger timer = new AtomicInteger(0);
          Visualizer.timer.put(playerName, timer);
          nPlayer.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Info_NearbyEnabled"));
          try {
            Visualizer.seeNearbyAsyncService.put(playerName, Executors.newSingleThreadScheduledExecutor());
            final Runnable task = () -> {
              Visualizer.timer.get(playerName).getAndAdd(2);
              Visualizer.seeNearbyRegions(nPlayer);
              if (Visualizer.timer.get(playerName).get() >= 14) {
                Visualizer.seeNearbyAsyncService.get(playerName).shutdown();
                Visualizer.seeNearbyAsyncService.remove(playerName);
              }
            };
            Visualizer.seeNearbyAsyncService.get(playerName).scheduleAtFixedRate(task, 0L, 2L, TimeUnit.SECONDS);
          } catch (Throwable t) {
            t.printStackTrace();
          }
          BukkitRunnable bukkitTask = new BukkitRunnable() {
            Queue<BlockVector3> particleLocations = Visualizer.playerParticleCoords.get(playerName);
            int maxSeconds = 15;
            int ticks = 0;

            @Override
            public void run() {
              ticks++;
              if (particleLocations != null) {
                int loops = Math.min(particleLocations.size(), 400);
                for (int i = 0; i < loops && particleLocations != null; i++) {
                  BlockVector3 loc = particleLocations.remove();
                  Location location = Visualizer.getBestSpawnLocation(nPlayer.getWorld(), loc.getX(), loc.getY(), loc.getZ());
                  if (location != null) {
                    nPlayer.spawnParticle(Particle.BLOCK_MARKER, location.getX() + .5, location.getY() + .5, location.getZ() + .5, 1, Bukkit.createBlockData(Material.BARRIER));
                  }
                }
              } else {
                particleLocations = Visualizer.playerParticleCoords.get(playerName);
              }
              if (ticks / 20 == maxSeconds) {
                this.cancel();
                Visualizer.seeNearbyBukkitTask.remove(playerName);
                nPlayer.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Info_NearbyDisabled"));
              }
            }
          };
          bukkitTask.runTaskTimer(LandClaim.plugin, 0, 1);
          Integer taskID = bukkitTask.getTaskId();
          Visualizer.seeNearbyBukkitTask.put(player.getName(), taskID);
          break;
        case "teleport":
          if (!(sender instanceof Player)) {
            sender.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_NotAPlayer"));
            return true;
          }
          if (!player.hasPermission("landclaim.teleport")) {
            sender.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_MissingPermission"));
            return true;
          }
          if (args.length < 2) {
            player.sendMessage(LandClaim.plugin.getLocalizedString("Help.Teleport"));
            return true;
          }
          Claim.teleportToClaim(player, args[1].split(",")[0], args[1].split(",")[1]);
          return true;
        case "delete":
          if (!(sender instanceof Player)) {
            sender.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_NotAPlayer"));
            return true;
          }
          if (!player.hasPermission("landclaim.delete.own")) {
            sender.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_MissingPermission"));
            return true;
          }
          if (args.length < 2) {
            player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_UnspecifiedClaim"));
            return true;
          }
          if (args[1].split(",").length < 2) {
            player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_RegionFormat"));
            return true;
          }
          Set<UUID> regionOwners = Claim.getRegionOwners(args[1].split(",")[0], args[1].split(",")[1]);
          if (regionOwners != null) {
            if (regionOwners.contains(player.getUniqueId()) || player.hasPermission("landclaim.delete.others")) {
              guiManager.promptForRemoval(player.getName(), args[1].split(",")[0], args[1].split(",")[1]);
              return true;
            }
            player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_NotAnOwner"));
          } else {
            player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_RegionNotFound"));
          }
          break;
        case "list":
          if (!player.hasPermission("landclaim.list")) {
            sender.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_MissingPermission"));
            return true;
          }
          guiManager.openAllClaimsGUI(player);
          break;
        case "recountvotes":
          if (!player.hasPermission("landclaim.recountvotes") && !player.isOp()) {
            sender.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_MissingPermission"));
            return true;
          }
          VoteRegion.tallyAllVotes();
          player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Success_RecountedVotes"));
          break;
        case "world":
          if (!player.hasPermission("landclaim.getworld") && !player.isOp()) {
            sender.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_MissingPermission"));
            return true;
          } else {
            sender.sendMessage(LandClaim.plugin.getLocalizedString("Messages.PlayerInWorld").replace("{WorldName}", player.getWorld().getName()));
          }
          break;
        case "inspect":
          if (!(sender instanceof Player)) {
            sender.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_NotAPlayer"));
            return true;
          }
          if (!player.hasPermission("landclaim.inspect.own") && !player.isOp()) {
            sender.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_MissingPermission"));
            return true;
          }
          if (args.length < 2) {
            player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_UnspecifiedClaim"));
            return true;
          }
          if (args[1].split(",").length < 2) {
            player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_RegionFormat"));
            return true;
          }
          String regionName = args[1].split(",")[0];
          String worldName = args[1].split(",")[1];
          if (!Claim.exists(regionName, worldName)) {
            player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_RegionNotFound"));
            return true;
          }
          String ownerOrMember = Claim.playerIsOwnerOrMember(player, regionName, worldName);
          if ((ownerOrMember != null && player.hasPermission("landclaim.inspect.own")) || player.hasPermission("landclaim.inspect.others")) {
            guiManager.openClaimInspector(player, args[1].split(",")[0], args[1].split(",")[1]);
            return true;
          }
          player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_CannotInspect"));
          break;
        case "vote":
          if (!(sender instanceof Player)) {
            sender.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_NotAPlayer"));
            return true;
          }
          if (!player.hasPermission("landclaim.vote") && !player.isOp()) {
            sender.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_MissingPermission"));
            return true;
          }
          if (args.length < 2) {
            List<ProtectedRegion> regionsList = Claims.getRegionsAtLocation(player.getLocation());
            if (regionsList.isEmpty()) {
              player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_CannotVoteOutsideRegions"));
              return true;
            }
            if (regionsList.size() == 1) {
              VoteFile voteFile = VoteFile.get();
              Vote lastVote = voteFile.getLatestVote(regionsList.get(0).getId(), player.getWorld().getName(), player.getUniqueId().toString());
              if (lastVote != null && !lastVote.dayHasPassed()) {
                player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_VoteTimeout").replace("{VoteTimeout}", Long.toString(1 - lastVote.daysSinceLastVote())));
                return true;
              }
              VoteFile.get().addVote(regionsList.get(0).getId(), player.getWorld().getName(), player.getUniqueId().toString()).save();
              VoteRegion.addVote(regionsList.get(0).getId() + "," + player.getWorld().getName());
              player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Success_VoteRegistered"));
              return true;
            }
            String message = LandClaim.plugin.getLocalizedString("Messages.Info_PLayerInRegion");
            StringBuilder extraMessage = new StringBuilder();
            for (ProtectedRegion region : regionsList) {
              extraMessage.append(region.getId()).append(", ");
            }
            extraMessage.delete(extraMessage.length() - 2, extraMessage.length());
            player.sendMessage(message + extraMessage + ".");
            player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Info_VoteRegion"));
            return true;
          }
          ProtectedRegion region = Claims.getRegionByName(args[1], BukkitAdapter.adapt(player.getWorld()));
          if (region != null) {
            VoteFile voteFile = VoteFile.get();
            Vote lastVote = voteFile.getLatestVote(region.getId(), player.getWorld().getName(), player.getUniqueId().toString());
            if (lastVote != null && !lastVote.dayHasPassed()) {
              player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_VoteTimeout").replace("{VoteTimeout}", Long.toString(1 - lastVote.daysSinceLastVote())));
              return true;
            }
            VoteFile.get().addVote(region.getId(), player.getWorld().getName(), player.getUniqueId().toString()).save();
            VoteRegion.addVote(region.getId() + "," + player.getWorld().getName());
            player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Success_VoteRegistered"));
            return true;
          }
          player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_RegionNotFound"));
          break;
        default:
          return false;
      }
    } catch (IndexOutOfBoundsException e) {
      return false;
    }
    return true;
  }

  public boolean meetsClaimLimits(Claim claim) {
    if (!claim.isPlot()) {
      int area = claim.getClaimArea();
      int length = claim.getClaimLength();
      int width = claim.getClaimWidth();
      if (area < LandClaim.plugin.getConfig().getInt("Claims.Regions.MinSize")) {
        player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_SelectionTooSmall"));
        return false;
      }
      if (area > LandClaim.plugin.getConfig().getInt("Claims.Regions.MaxSize")) {
        player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_SelectionTooLarge"));
        return false;
      }
      if (((double) length / width) > LandClaim.plugin.getConfig().getDouble("Claims.Regions.MaxLWRatio") || ((double) width / length) > LandClaim.plugin.getConfig().getDouble("Claims.Regions.MaxLWRatio")) {
        player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_UnmetProportions"));
        return false;
      }
    }
    return true;
  }
}
