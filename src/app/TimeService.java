package app;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.*;

import mics.*;


public class TimeService extends MicroService {

	private int speed;
	private int duration;
	private AtomicInteger current;

	public TimeService(int speed, int duration, CountDownLatch doneSignal) {
		super("timer", doneSignal);
		this.speed = speed;
		this.duration = duration;
		this.current = new AtomicInteger(1);

	}

	/**
	 * 	@Override
	 * 	this method includes all the {@link Message}s this {@link MicroService} is subscribed to and their corresponding
	 * {@link Callback}s 
	 */
	protected void initialize() {
		try {
			doneSignal.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//create new timer
		ShoeStoreRunner.LOGGER.info("Timer has started!\n");
		Timer time =new Timer();
		time.schedule(new TimerTask() {
			
			public void run() {
				if(current.get()<duration+1){ //if the program did not reach to its duration tick
					ShoeStoreRunner.LOGGER.info("Current Time: "+current +"\n");
				sendBroadcast(new TickBroadcast(current.get(),duration)); //sending broadcast of current time to all MicroServices
				current.incrementAndGet();
				}
				else{
					time.cancel();
					ShoeStoreRunner.LOGGER.info("Timer has stopped!\n");
					sendBroadcast(new TerminationBroadcast()); //sending termination broadcast to all MicroServices 
				} 
			}
		}
			, 0, speed);
		ShoeStoreRunner.LOGGER.info(getName()+" initialized\n");
		terminate();
		ShoeStoreRunner.LOGGER.info(getName() +" has terminated\n");
	}

	public long getSpeed() {
		return speed;
	}

	public int getDuration() {
		return duration;
	}

	public int getCurrent() {
		return current.get();
	}
}
