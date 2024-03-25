package net.minespire.landclaim.Claim;

import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class ClaimManager {
  public static Map<String, List<Claim>> playerClaims = new HashMap<>();
  private Player player;
  private static List<Claim> allClaims = new ArrayList<Claim>();
  private List<Claim> ownerRegions;
  private List<Claim> memberRegions;
  private List<Claim> ownerPlots;
  private List<Claim> memberPlots;

  public ClaimManager(Player player) {
    this.player = player;
  }

  public static Claim getClaimByRegionName(String rgName){
    Claim target = null;
    try{
      target = ClaimManager.allClaims.stream()
              .filter(c -> c.getRegionName().equals(rgName))
              .toList().get(0);
    } catch(Exception e){
        throw new NoSuchElementException();
    }
    return target;
  }

  public static void addClaim(Claim c){
    allClaims.add(c);
  }

  public static void removeClaim(Claim c){
    allClaims.remove(c);
  }
}
