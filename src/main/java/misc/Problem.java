package misc;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Patrick on 06.02.2015.
 */
public class Problem {

  private final Problem parent;
  private boolean isTerminal = false;
  private Problem left;
  private Problem right;
  private String value;


  public Problem(String problem) {
    this(Arrays.asList(problem.trim().split(" ")).stream().collect(Collectors.toList()), null);
  }

  private Problem(List<String> subProblem, Problem problem) {
    this.parent = problem;
    value = subProblem.remove(0);

    if (!isOperator(value)) {
      isTerminal = true;
      return;
    }

    left = new Problem(subProblem, this);
    right = new Problem(subProblem, this);
  }

  private boolean isOperator(String s) {
    return s.matches("[+-/\\*]");
  }

  public Problem getLeft() {
    return left;
  }

  public Problem getRight() {
    return right;
  }

  public String getValue() {
    return value;
  }

  public boolean isTerminal() {
    return isTerminal;
  }

  public Problem getSubproblem() {
    return left.isTerminal && !right.isTerminal ? right.getSubproblem() //
                                                : left.isTerminal && right.isTerminal ? this : left.getSubproblem();
  }

  public void solve(String answer) {
    value = answer;
    setTerminal();
  }

  private void setTerminal() {
    right = null;
    left = null;
    isTerminal = true;
  }

  public String getType() {
    return isOperator(value) ? value : null;
  }

  @Override
  public String toString() {
    return value.toString() + " " + (isTerminal ? "" : left.toString() + right.toString());
  }

  public void solve() {
    switch (value.charAt(0)) {
      case '+':
        value = String.valueOf(Integer.parseInt(left.value) + Integer.parseInt(right.value));
        break;
      case '*':
        value = String.valueOf(Integer.parseInt(left.value) * Integer.parseInt(right.value));
        break;
      case '/':
        value = String.valueOf(Integer.parseInt(left.value) / Integer.parseInt(right.value));
        break;
      case '-':
        value = String.valueOf(Integer.parseInt(left.value) - Integer.parseInt(right.value));
        break;
    }

    solve(value);
  }

  public boolean isSolved() {
    return Arrays.asList(value.toCharArray()).stream().noneMatch(c -> isOperator(String.valueOf(c)));
  }
}
