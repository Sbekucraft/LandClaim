package net.minespire.landclaim.Claim;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClaimManager {
  public static Map<String, List<Claim>> playerClaims = new HashMap<>();
  private Player player;
  private List<Claim> allClaims;
  private List<Claim> ownerRegions;
  private List<Claim> memberRegions;
  private List<Claim> ownerPlots;
  private List<Claim> memberPlots;

  public ClaimManager(Player player) {
    this.player = player;
  }
}
