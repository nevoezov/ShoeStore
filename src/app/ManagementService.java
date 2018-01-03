package app;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import app.ShoeStoreRunner;
import mics.MicroService;

public class ManagementService extends MicroService {

	private HashMap<String, RestockInformation> reStock;
	private HashMap<Integer, LinkedList<NewDiscountBroadcast>> schedule;
	private HashMap<String, LinkedBlockingQueue<RestockRequest>> completion;
	private int currTick;

	/**
	 * Costructor
	 * @param discountSchedule  an array of {@link DiscountSchedule}s to be performed by the manager
	 * @param doneSignal		count down latch object
	 */
	public ManagementService(List<DiscountSchedule> discountSchedule, CountDownLatch doneSignal) {
		super("manager", doneSignal);
		this.schedule = new HashMap<Integer, LinkedList<NewDiscountBroadcast>>();
		this.reStock = new HashMap<String, RestockInformation>();
		for (DiscountSchedule discount : discountSchedule)
			if (this.schedule.get(discount.getTick())!=null || this.schedule.containsKey(discount.getTick()))
				this.schedule.get(discount.getTick()).add(this.createDiscountBroadcastFromSchedule(discount));
			else {
				this.schedule.put(discount.getTick(), new LinkedList<NewDiscountBroadcast>());
				this.schedule.get(discount.getTick()).add(this.createDiscountBroadcastFromSchedule(discount));
			}
		this.completion = new HashMap<String, LinkedBlockingQueue<RestockRequest>>();

	}

	/**
	 * this method takes {@link DiscountSchedule}{@param discount} and 
	 * @return {@link NewDiscountBroadcast} 
	 */
	private NewDiscountBroadcast createDiscountBroadcastFromSchedule(DiscountSchedule discount) {
		return (new NewDiscountBroadcast(discount.getShoeType(), discount.getAmount()));
	}

	/**
	 * 	@Override
	 * 	this method includes all the {@link Message}s this {@link MicroService} is subscribed to and their corresponding
	 * {@link Callback}s 
	 */
	protected void initialize() {
		ShoeStoreRunner.LOGGER.info("manager has started!\n");
		subscribeBroadcast(TerminationBroadcast.class, terminate -> {terminate();});  //terminate when receive TerminationBroadcast
		subscribeBroadcast(TickBroadcast.class, tick_brod -> {  //get the broadcast of current Tick
			currTick = tick_brod.getTick();
			if (schedule.containsKey(currTick))  //check if should issue NewDiscountBroadcast at current tick, if so - update store and send the broadcast
				for (NewDiscountBroadcast discount : schedule.get(currTick)){
					Store.getInstance().addDiscount(discount.getShoeType(), discount.getDiscountedAmount());
					sendBroadcast(discount);
					ShoeStoreRunner.LOGGER.info("New Discount announced for " + discount.getDiscountedAmount() + " " + discount.getShoeType() +"\n");
				}
			
		});
		subscribeRequest(RestockRequest.class, restock -> {  //receive RestockRequest from the SellingService
			ShoeStoreRunner.LOGGER.info("Restock Request recieved for " + restock.getShoeType() + "\n");
			Integer tickOfRestock =  currTick;
			if (!reStock.containsKey(restock.getShoeType()))
				reStock.put(restock.getShoeType(), new RestockInformation(0,0));
			if (reStock.get(restock.getShoeType()).sendNewRequest()){  //check if new ManufacturingOrderRequest needed
				ManufacturingOrderRequest request = new ManufacturingOrderRequest(restock.getShoeType(), "manager",tickOfRestock, tickOfRestock % 5 + 1);
				if(!completion.containsKey(restock.getShoeType()))
						completion.put(restock.getShoeType(), new LinkedBlockingQueue<RestockRequest>() );
				completion.get(restock.getShoeType()).add(restock);
				if(sendRequest(request, req -> {  //send ManufacturingOrderRequest to the factory and wait for callback
					ShoeStoreRunner.LOGGER.info(req.getAmountSold() +" "+ req.getShoeType() + " delivered from " + req.getSeller() + "\n");
					if (req.getAmountSold() <= reStock.get(restock.getShoeType()).getReservedAmount()) {
						reStock.get(restock.getShoeType()).setOrderedAmount(req.getAmountSold());
						reStock.get(restock.getShoeType()).removeReservedAmount(req.getAmountSold());
					} else {
						ShoeStoreRunner.LOGGER.info(req.getAmountSold() - reStock.get(restock.getShoeType()).getReservedAmount()+ " added to store\n");
						Store.getInstance().add(restock.getShoeType(),req.getAmountSold() - reStock.get(restock.getShoeType()).getReservedAmount()) ;	
					}
					if(req!=null)
						Store.getInstance().file(req);
					for(int i = 0;i<req.getAmountSold()&&!completion.get(restock.getShoeType()).isEmpty();i++){
						complete(completion.get(restock.getShoeType()).poll(),true);
					}
				})){
					ShoeStoreRunner.LOGGER.info("One " + restock.getShoeType() + " reserved\n");
					reStock.get(restock.getShoeType()).setOrderedAmount(tickOfRestock % 5 + 1);
					reStock.get(restock.getShoeType()).addReservation();
				}
				else 
					complete(restock,false);
			}
			else { //no need to manufacture more of this item, was already ordered
				reStock.get(restock.getShoeType()).addReservation();
				completion.get(restock.getShoeType()).add(restock);
			}
		});

		doneSignal.countDown();
		ShoeStoreRunner.LOGGER.info(getName()+" initialized\n");
	}
}