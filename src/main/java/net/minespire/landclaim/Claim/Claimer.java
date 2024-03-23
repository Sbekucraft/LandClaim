package net.minespire.landclaim.Claim;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Claimer {
  public static boolean permToOwnAnotherRegion(Player player) {
		if (player.hasPermission("landclaim.claimregion")) {
			if (player.hasPermission("landclaim.regions.*")) {
				return true;
			}
			int numRegionsAllowed = getNumAllowedRegions(player);
			if (Claim.getClaimListOwner(player, false).size() >= numRegionsAllowed) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
  }

  public static int getNumAllowedRegions(Player player) {
    Set<PermissionAttachmentInfo> playerPermissions = player.getEffectivePermissions();
    int permRegions = 0;
    int numRegionsAllowed = 0;
    for (PermissionAttachmentInfo perm : playerPermissions) {
      if (perm.getPermission().startsWith("landclaim.regions.")) {
        permRegions = Integer.valueOf(perm.getPermission().substring(18));
        numRegionsAllowed = permRegions > numRegionsAllowed ? permRegions : numRegionsAllowed;
      }
    }
    return numRegionsAllowed;
  }

  public static boolean permToOwnAnotherPlot(Player player) {
		if (player.hasPermission("landclaim.claimplot")) {
			if (player.hasPermission("landclaim.plots.*")) {
				return true;
			}
			int numPlotsAllowed = getNumAllowedPlots(player);
			if (Claim.getClaimListOwner(player, true).size() >= numPlotsAllowed) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
  }

  public static int getNumAllowedPlots(Player player) {
    Set<PermissionAttachmentInfo> playerPermissions = player.getEffectivePermissions();
    int permRegions = 0;
    int numRegionsAllowed = 0;
    for (PermissionAttachmentInfo perm : playerPermissions) {
      if (perm.getPermission().startsWith("landclaim.plots.")) {
        permRegions = Integer.valueOf(perm.getPermission().substring(16));
        numRegionsAllowed = permRegions > numRegionsAllowed ? permRegions : numRegionsAllowed;
      }
    }
    return numRegionsAllowed;
  }

  public static boolean permissionToClaimInWorld(Player player) {
		if (player.hasPermission("landclaim.world.*")) {
			return true;
		}
    Set<PermissionAttachmentInfo> playerPermissions = player.getEffectivePermissions();
    for (PermissionAttachmentInfo perm : playerPermissions) {
      if (perm.getPermission().toLowerCase().startsWith("landclaim.world.")) {
        String worldPerm = perm.getPermission().substring(16);
        if (worldPerm.equalsIgnoreCase((player.getWorld().getName()))) {
          return true;
        }
      }
    }
    return false;
  }

  public static List<World> claimableWorlds(Player player) {
    List<World> worlds = Bukkit.getWorlds();
		if (player.hasPermission("landclaim.world.*")) {
			return worlds;
		}
    List<World> claimableWorlds = new ArrayList<>();
    Set<PermissionAttachmentInfo> playerPermissions = player.getEffectivePermissions();
    for (PermissionAttachmentInfo perm : playerPermissions) {
      if (perm.getPermission().toLowerCase().startsWith("landclaim.world.")) {
        String worldPerm = perm.getPermission().substring(16);
        worlds.forEach(world -> {
					if (world.getName().equalsIgnoreCase(worldPerm)) {
						claimableWorlds.add(world);
					}
        });
      }
    }
    return claimableWorlds;
  }
}
