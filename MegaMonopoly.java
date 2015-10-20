import java.util.HashMap;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Map;
import java.lang.Math;
import java.io.*;
import org.apache.commons.math3.linear.*;

public class MegaMonopoly implements MegaMonopolyInterface {
  
  private String[] squareNamesList;
  private HashMap<String,Integer> rollsToSum;
  private HashMap<Integer,Integer> sumsToEventCount;
  private HashMap<Integer,Double> sumsToProb;
  private HashMap<Integer,String> sumsToProbInFraction;
  private LinkedList<Integer> sumList;
  private RealMatrix transitionMatrix;
  
  //***** Public Class Methods
  public MegaMonopoly() {
    squareNamesList = new String[] {"GO", "Mediterranean Avenue", "Community Chest", "Baltic Avenue", "Arctic Avenue", "Income Tax", "Reading Railroad", "Massachusetts Avenue", "Oriental Avenue", "Chance", "Gas Company", "Vermont Avenue", "Connecticut Avenue", "Jail", "Auction", "Maryland Avenue", "St. Charles Place", "Electric Company", "States Avenue", "Virginia Avenue", "Pennsylvania Railroad", "St. James Place", "Community Chest", "Tennessee Avenue", "New York Avenue", "New Jersey Avenue", "Free Parking", "Kentucky Avenue", "Chance", "Indiana Avenue", "Illinois Avenue", "Michigan Avenue", "Bus Ticket", "B&O Railroad", "Atlantic Avenue", "Ventnor Avenue", "Water Works", "Marvin Gardens", "California Avenue", "Go To Jail", "Pacific Avenue", "South Carolina Avenue", "North Carolina Avenue", "Community Chest", "Pennsylvania Avenue", "Short Line", "Chance", "Birthday Gift", "Florida Avenue", "Park Place", "Luxury Tax", "Boardwalk"} ;
    rollsToSum = new HashMap<String,Integer>();
    sumList = new LinkedList<Integer>();
    sumsToEventCount = new HashMap<Integer,Integer>();
    sumsToProb = new HashMap<Integer,Double>();
    sumsToProbInFraction = new HashMap<Integer,String>();
    
    generateRollsToSum();
    generateSumsToEventCountAndProbability();
    generateTransitionMatrix();
  }
  
  public int sumForRoll(String roll) {
    return rollsToSum.get(roll);
  }
  
  public int eventsWithSum(int sum) {
    return sumsToEventCount.get(sum);
  }
  
  public double probForSum(int sum) {
    return sumsToProb.get(sum);
  }
  
  public String probForSumInFraction(int sum) {
    return sumsToProbInFraction.get(sum);
  }
  
  public RealMatrix getTransitionMatrix() {
    return transitionMatrix;
  }
  
  public void validateProbsForSum( ) {
    Iterator iter1 = sumsToProb.entrySet().iterator();
    Iterator iter2 = sumsToProbInFraction.entrySet().iterator();
    double total = 0;
    System.out.println("\nSUM\tPROBABILITY\t\tIN FRACTION");
    while(iter1.hasNext() && iter2.hasNext()) {
      Map.Entry sumProbPair = (Map.Entry) iter1.next();
      Map.Entry sumProbPairInFraction = (Map.Entry) iter2.next();
      int sum = (Integer) sumProbPair.getKey();
      Double prob = (Double) sumProbPair.getValue();
      String probInFraction = (String) sumProbPairInFraction.getValue();
      System.out.println(sum + "\t" + prob + ((prob==0)?"\t\t\t":"\t\t") + probInFraction);
      total += prob;
    }
    System.out.println("\nThe sum of all probabilities is " + total);
  }
  
  public void exportTransitionMatrix(String fileName) {
    try{
    PrintWriter out = new PrintWriter(new FileOutputStream(fileName + ".csv"));
    double[][] array = transitionMatrix.getData();
    for(int i = 0; i<52; i++) {
      for(int j = 0; j<52; j++) {
        out.write(array[i][j] + ",");
      }
      out.write("\n");
    }
    out.close();
    } catch(FileNotFoundException e) {
      System.out.println("File I/O Error!");
    }
  }
  
  public void printStats(int n) {
    RealMatrix A = transitionMatrix;
    RealMatrix x = MatrixUtils.createRealMatrix(1,52);
    x.setEntry(0,0,1);
    
    A = A.power(n);
    x = x.multiply(A);
    double[][] product = x.getData();
    
    System.out.println("\nSquare No. \tProbability \t\tSquarename ");
    for(int c = 0; c < product[0].length; c++)
      //System.out.println(c+1 + "\t\t" + (product[0][c]==0?"0.00000000000000000":product[0][c]) + "\t\t" + squareNamesList[c]);
      System.out.println(product[0][c]);
  }
  
  public void adjustForJail() {
    for(int r = 0; r < 52; r++) {
      double jail = transitionMatrix.getEntry(r,13);
      double goToJail = transitionMatrix.getEntry(r,39);
      transitionMatrix.setEntry(r, 39, 0.0);
      transitionMatrix.setEntry(r, 13, jail + goToJail);
    }
  }
  
  public void adjustForChest() {
    int[] chestPositions = new int[] {2,22,43};
    
    for(int r = 1; r < 52; r++ ) {
      for(int i: chestPositions) {
        double chest = transitionMatrix.getEntry(r,i);
        
        double jail = transitionMatrix.getEntry(r,13);
        transitionMatrix.setEntry(r, 13, jail + (chest/16.0));
        
        transitionMatrix.setEntry(r, i, chest*(15.0/16.0));
      }
    }
  }
  
  public void adjustForChance() {
    int[] chancePositions = new int[] {9,28,46};
    int[] backThreeSpacePositions = new int[] {6,25,43}; // 43 is community chest
    int[] nearestRailroadPositions = new int[] {20,33,6};
    int[] nearestUtilityPositions = new int[] {10,36,10};
    
    for(int r = 0; r < 52; r++) {
      for(int i = 0; i <= 2; i++) {
        // The original chance value for the row
        double chance = transitionMatrix.getEntry(r,chancePositions[i]);
      
        // Adjust the matrix
        double jail = transitionMatrix.getEntry(r,13);
        transitionMatrix.setEntry(r, 13, jail + (chance/16.0));
        
        double illinois = transitionMatrix.getEntry(r,30);
        transitionMatrix.setEntry(r, 30, illinois + (chance/16.0));
        
        double stCharles = transitionMatrix.getEntry(r, 16);
        transitionMatrix.setEntry(r, 16, stCharles + (chance/16.0));
        
        double go = transitionMatrix.getEntry(r, 0);
        transitionMatrix.setEntry(r, 0, go + (chance/16.0));
        
        double reading = transitionMatrix.getEntry(r, 6);
        transitionMatrix.setEntry(r, 6, reading + (chance/16.0));
        
        double broadwalk = transitionMatrix.getEntry(r, 51);
        transitionMatrix.setEntry(r, 51, broadwalk + (chance/16.0));
        
        double backThreeSpace = transitionMatrix.getEntry(r, backThreeSpacePositions[i]);
        transitionMatrix.setEntry(r, backThreeSpacePositions[i], backThreeSpace + (chance/16.0));
        
        double nearestRailroad = transitionMatrix.getEntry(r, nearestRailroadPositions[i]);
        transitionMatrix.setEntry(r, nearestRailroadPositions[i], nearestRailroad + (2*chance/16.0));
        
        double nearestUtility = transitionMatrix.getEntry(r, nearestUtilityPositions[i]);
        transitionMatrix.setEntry(r, nearestUtilityPositions[i], nearestUtility + (chance/16.0));
        
        // Updating probability for chance
        double newChance = chance - (10.0/16.0*chance);
        transitionMatrix.setEntry(r, chancePositions[i], newChance);
      }
    }
  }
  
  public void adjustForChanceAndChest() {
    adjustForChance();
    adjustForChest();
    adjustForJail();
  }
  
  //**** Private Helper Methods
  private void generateRollsToSum() {
    for(int i = 1; i <= 6; i++) {
      for(int j = 1; j <= 6; j++) {
        for(int k = 1; k <= 6; k++) {
          String kStr;
          int kInt;
          if(k == 4 || k == 5) {
            kStr = "M";
            kInt = 0;
          } 
          else if (k == 6) {
            kStr = "B";
            kInt = 0;
          }
          else {
            kStr = String.valueOf(k);
            kInt = k;
          }
          String key = String.valueOf(i) + String.valueOf(j) + kStr;
          int value = i + j + kInt;
          //if(k <= 3 && i == k && j == k) value = -1;
          rollsToSum.put(key,value);
          sumList.add(value);
        }
      }
    }
  }
  
  private void generateSumsToEventCountAndProbability() {
    for(int sum = 1; sum <= 15; sum++) {
      Iterator iter = sumList.iterator();
      int eventCount = 0;
      while(iter.hasNext()) {
        Integer roll = (Integer) iter.next();
        if(roll == sum) eventCount++;
      }
      int totalEvents = 6*6*6;
      double probability = (double) eventCount/totalEvents;
      String probInFraction = String.valueOf(eventCount) + "/" + String.valueOf(totalEvents);
      sumsToEventCount.put(sum,eventCount);
      sumsToProb.put(sum,probability);
      sumsToProbInFraction.put(sum,probInFraction);
    }
  }
  
  private void generateTransitionMatrix() {
    transitionMatrix = MatrixUtils.createRealMatrix(52,52);
    for(int r = 0; r < 52; r++) {
      for(int c = 0 ; c < 52; c++) {
        int movesRequired = (r<=c)?(c-r):(52-r+c);
        if( movesRequired <= 15 && movesRequired >= 2 ) {
          transitionMatrix.setEntry(r, c, sumsToProb.get(movesRequired));
        } else {
          transitionMatrix.setEntry(r, c, (double) 0);
        }
      }
    }
  }
  
}