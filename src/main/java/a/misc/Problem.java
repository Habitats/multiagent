package a.misc;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A class designed to hold a problem, in this particular scenario it holds a arithmetic math problem in the pre-fix
 * format.
 *
 * The class follows the composite pattern laid out as a tree structure. Calling "getSubProblem" on this class will
 * automatically recursively retrieve the first possible sub problem that can be solved explicitly.
 */
public class Problem {

  private boolean isTerminal = false;
  private Problem leftChild;
  private Problem rightChild;
  private String value;

  public Problem(String problem) {
    // formats a "+ + 1 1 + 2 2" expression into a mutable List<String> of characters
    this(Arrays.asList(problem.trim().split(" ")).stream().collect(Collectors.toList()));
  }

  private Problem(List<String> problemList) {
    value = problemList.remove(0);

    if (!isOperator(value)) {
      isTerminal = true;
      return;
    }

    leftChild = new Problem(problemList);
    rightChild = new Problem(problemList);
  }

  private boolean isOperator(String s) {
    return s.matches("[+-/\\*]");
  }

  public String getValue() {
    return value;
  }

  /**
   * @return - the first explicitly solvable sub problem, found by doing a depth first search in the problem tree
   */
  public Problem getSubproblem() {
    return leftChild.isTerminal && !rightChild.isTerminal ? rightChild.getSubproblem() :  //
           leftChild.isTerminal && rightChild.isTerminal ? this : leftChild.getSubproblem();
  }

  public void solve(String answer) {
    value = answer;
    setTerminal();
  }

  private void setTerminal() {
    rightChild = null;
    leftChild = null;
    isTerminal = true;
  }

  public String getType() {
    return isOperator(value) ? value : null;
  }

  /**
   * Solves the problem.
   *
   * The actual solving of the trivial arithmetic problem is encapsulated within the problem itself. This was done
   * mainly to keep the system simple. The solving method is only invoked by the solver agents.
   */
  public void solve() {
    switch (value.charAt(0)) {
      case '+':
        value = String.valueOf(Integer.parseInt(leftChild.value) + Integer.parseInt(rightChild.value));
        break;
      case '*':
        value = String.valueOf(Integer.parseInt(leftChild.value) * Integer.parseInt(rightChild.value));
        break;
      case '/':
        value = String.valueOf(Integer.parseInt(leftChild.value) / Integer.parseInt(rightChild.value));
        break;
      case '-':
        value = String.valueOf(Integer.parseInt(leftChild.value) - Integer.parseInt(rightChild.value));
        break;
    }

    solve(value);
  }

  public boolean isSolved() {
    return Arrays.asList(value.toCharArray()).stream().noneMatch(c -> isOperator(String.valueOf(c)));
  }

  @Override
  public String toString() {
    return value.toString() + " " + (isTerminal ? "" : leftChild.toString() + rightChild.toString());
  }
}
