package app;

import java.util.*;
import java.util.concurrent.CountDownLatch;

import mics.MicroService;

public class WebsiteClientService extends MicroService {

	
	private HashMap<Integer, LinkedList<PurchaseOrderRequest>> schedule;
	private Set<String> wishList;
	private int currTick;


	/**
	 * Costructor
	 * @param schedule  a list of {@link PurchaseSchedule}s the client is interested in buying
	 * @param wishList a set of {@link String}s that the client wishes to buy at discounted price
	 * @param name	a {@link String} contain the client name
	 */
	public WebsiteClientService(List<PurchaseSchedule> schedule, Set<String> wishList, String name) {
		super(name);
		this.schedule = new HashMap<Integer, LinkedList<PurchaseOrderRequest>>();
		for (PurchaseSchedule i:schedule){
			if (this.schedule.containsKey(i.getTick()))
				this.schedule.get(i.getTick()).add(this.createRequestFromSchedule(i));
				else {
					this.schedule.put(i.getTick(), new LinkedList<PurchaseOrderRequest>());
					this.schedule.get(i.getTick()).add(this.createRequestFromSchedule(i));
				}
		}
		this.wishList = wishList;
		
	}
	
	public WebsiteClientService(WebsiteClientService w, CountDownLatch doneSignal) {
		super(w.getName(), doneSignal);
		this.schedule = w.schedule;
		this.wishList = w.wishList;
		this.doneSignal = doneSignal;
	}
	
	private PurchaseOrderRequest createRequestFromSchedule(PurchaseSchedule s) {
		return (new PurchaseOrderRequest(s.getShoeType(), getName(), s.getTick(), false, 1));
	}

	/**
	 * 	@Override
	 * 	this method includes all the {@link Message}s this {@link MicroService} is subscribed to and their corresponding
	 * {@link Callback}s 
	 */
	protected void initialize() {
		ShoeStoreRunner.LOGGER.info("Web Client Service " + getName() + " has started\n");
		if(shouldTerminate())
			this.terminate();
		subscribeBroadcast(TerminationBroadcast.class, terminate -> {terminate();}); //terminate when receive TerminationBroadcast
		subscribeBroadcast(TickBroadcast.class, tick_brod -> { //get the broadcast of current Tick
			currTick = tick_brod.getTick();
			if(schedule.containsKey(currTick)){  //if the client have an item to buy at the current tick - send request to buy the item
				int orderingTick = currTick;
				for (PurchaseOrderRequest request : schedule.get(currTick)){
					sendRequest(request, req -> { //send the request to buy the item and wait for callback
						if (req == null) { //request not available
							ShoeStoreRunner.LOGGER.info(request.getShoeType() + " requested by " + getName() + " is not available!\n");
						}
						else{  //request was handled
							schedule.get(orderingTick).remove(request);
							if(schedule.get(orderingTick).isEmpty())
								schedule.remove(orderingTick);
							if (wishList.contains(request.getShoeType()))
								wishList.remove(request.getShoeType());	
						}
						ShoeStoreRunner.LOGGER.info(request.getShoeType() + " sold to " + getName() + "\n");
					});
				}
			}
			if(shouldTerminate())
				this.terminate();
		});

		subscribeBroadcast(NewDiscountBroadcast.class, NewDiscountBroadcast -> {  //in case the manager issue a discount
			
			if(wishList.contains(NewDiscountBroadcast.getShoeType())){ //if the client have the discounted item on its wishList - send request to buy the item
				PurchaseOrderRequest request = new PurchaseOrderRequest(NewDiscountBroadcast.getShoeType(), getName(), currTick, true, 1);
				sendRequest(request, req -> {//send the request to buy the item and wait for callback
					if (req != null) { //request was handled (item bought at discounted price)
						wishList.remove(NewDiscountBroadcast.getShoeType());
						ShoeStoreRunner.LOGGER.info(NewDiscountBroadcast.getShoeType() + " sold to "+ this.getName() + " at discounted price. shoe was removed from his wishList\n");
					}
					if(shouldTerminate())
						this.terminate();
				});
			}
			
		});
		
		doneSignal.countDown();
		ShoeStoreRunner.LOGGER.info(getName()+" initialized\n");
	}
	
	//if both wishList and schedule are empty will return true
	private boolean shouldTerminate(){
		return (wishList.isEmpty() && schedule.isEmpty());
	}
}
