package app;
import mics.*;

public class TickBroadcast implements Broadcast {

	private int tick;
	private int duration;
	
	/**
	 * {@link Broadcast} used to send the current {@param tick} to all services
	 */
	public TickBroadcast(int tick, int duration){
		this.tick = tick;
		this.duration = duration;
	}
	
	public int getDuration() {
		return duration;
	}

	public int getTick(){
		return this.tick;
	}
}
