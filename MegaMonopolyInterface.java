public interface MegaMonopolyInterface {
  
  public int sumForRoll(String roll);
  public int eventsWithSum(int sum);
  public double probForSum(int sum);
  public String probForSumInFraction(int sum);
  public void validateProbsForSum();
  public void exportTransitionMatrix(String fileName);
  public void printStats(int n);
  public void adjustForJail();
  public void adjustForChanceAndChest();
}