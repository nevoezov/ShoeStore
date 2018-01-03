package app;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import mics.MicroService;

public class ShoeFactoryService extends MicroService {


	private int currTick;
	private AtomicInteger endTick;
	private ConcurrentHashMap<Integer, ManufacturingOrderRequest> pendingOrders;

	/**
	 * @param name			name of seller
	 * @param doneSignal	count down latch object
	 */
	public ShoeFactoryService(String name, CountDownLatch doneSignal) {
		super(name, doneSignal);
		this.pendingOrders = new ConcurrentHashMap<Integer, ManufacturingOrderRequest>();
		this.endTick = new AtomicInteger(0);
	}

	/**
	 * 	@Override
	 * 	this method includes all the {@link Message}s this {@link MicroService} is subscribed to and their corresponding
	 * {@link Callback}s 
	 */
	protected void initialize() {
		ShoeStoreRunner.LOGGER.info("Shoe Factory Service " +getName()+ " has started\n");
		subscribeBroadcast(TerminationBroadcast.class, terminate -> {terminate();});  //terminate when receive TerminationBroadcast
		subscribeBroadcast(TickBroadcast.class, tick_brod->{  //get the broadcast of current Tick
			currTick = tick_brod.getTick();
			if(currTick>endTick.get())
				endTick.set(currTick);
			if(pendingOrders.containsKey(currTick)){  //check if items should be manufactured at current tick, if so - create receipt and complete the manager request
				Receipt result = new Receipt(getName(), "Store", pendingOrders.get(currTick).getShoeType(), false, currTick, pendingOrders.get(currTick).getTick(), pendingOrders.get(currTick).getAmount());
				complete(pendingOrders.get(currTick),result);
				ShoeStoreRunner.LOGGER.info("delivered " + result.getShoeType() + "\n");
			}
		});
		subscribeRequest(ManufacturingOrderRequest.class, req->{  //receive ManufacturingOrderRequest from manager
			endTick.getAndAdd(req.getAmount());
			pendingOrders.put(endTick.get()+1, req);
			ShoeStoreRunner.LOGGER.info(req.getShoeType()+ " is waiting for manufacturing, endTick " + endTick.get() + "\n");
		});
		
		doneSignal.countDown();
		ShoeStoreRunner.LOGGER.info(getName()+" initialized\n");
	}
}

