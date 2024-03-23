package net.minespire.landclaim.Economy;

public class FlagCost {
  private String flag;
  private double cost;

  public FlagCost(String flag, double cost) {
    this.flag = flag.toLowerCase();
    this.cost = cost;
  }

  public String getFlag() {
    return this.flag;
  }

  public double getCost() {
    return this.cost;
  }
}
