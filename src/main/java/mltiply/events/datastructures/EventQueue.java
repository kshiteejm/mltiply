package mltiply.events.datastructures;

import java.util.Comparator;
import java.util.PriorityQueue;

public class EventQueue {
  // ascending order on time
  public PriorityQueue<Event> eventQueue = new PriorityQueue<Event>(256, new Comparator<Event>() {
    public int compare(Event o1, Event o2) {
      return Double.compare(o1.getTime(), o2.getTime());
    }
  });

  public boolean add(Event e) {
    return eventQueue.add(e);
  }

  public Event peek() {
    return eventQueue.peek();
  }

  public Event poll() {
    // returns null when eventQueue is empty
    return eventQueue.poll();
  }

  public boolean isEmpty() {
    return eventQueue.isEmpty();
  }
}
