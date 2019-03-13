package mlsched.events;

import java.util.Comparator;

import mlsched.simulator.Main;

public class EventComparator implements Comparator<Event>{
	
	public int compare(Event e1, Event e2) {
		
		final double THRESHOLD = .000000001;
		
		if (Math.abs(e1.timeStamp - e2.timeStamp) < THRESHOLD) {
			
			Integer pE1 = Main.eventPriority(e1);
			Integer pE2 = Main.eventPriority(e2);
			
			if(pE1 < pE2)
				return -1;
			else
				return 1;
		}
		
		if (e1.timeStamp < e2.timeStamp)
			return -1;
		else
			return 1;

	}

}
