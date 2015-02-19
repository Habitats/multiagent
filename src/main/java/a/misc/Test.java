package a.misc;

/**
 * Simple test to check that the prefix calculator tree thingy actually works
 */
public class Test {

  public static void main(String[] args) {
//    Problem p = new Problem("+ - 5 6 3");
    Problem p = new Problem("- * / 15 - 7 + 1 1 3 + 2 + 1 1");
    System.out.println(p);

//  sub.solve("2");

    while (!p.isSolved()) {
      Problem sub = p.getSubproblem();
      sub.solve();
      System.out.println(p);
    }
  }
}
