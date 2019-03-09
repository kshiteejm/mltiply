package mlsched.events;

import java.util.Comparator;

public class EventComparator implements Comparator<Event>{
	
	public int compare(Event e1, Event e2) {
		
		if (e1.timeStamp < e2.timeStamp)
			return -1;
		else
			return 1;

	}

}
