package mltiply.events.datastructures;

public class Pair<F, S> {
  public F f;
  public S s;

  public Pair(F k, S v) {
    this.f = k;
    this.s = v;
  }

  public F getFirst() {
    return f;
  }

  public S getSecond() {
    return s;
  }
}
