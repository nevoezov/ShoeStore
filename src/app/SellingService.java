package app;

import java.util.concurrent.CountDownLatch;

import app.ShoeStoreRunner;
import mics.MicroService;

public class SellingService extends MicroService {

	private int currTick;
	private int endTick;

	/**
	 * 
	 * @param name			name of seller
	 * @param doneSignal	count down latch object
	 */
	public SellingService(String name, CountDownLatch doneSignal) {
		super(name,doneSignal);
	}

	/**
	 * 	@Override
	 * 	this method includes all the {@link Message}s this {@link MicroService} is subscribed to and their corresponding
	 * {@link Callback}s 
	 */
	protected void initialize() {
		ShoeStoreRunner.LOGGER.info("Selling Service " +getName()+ " has started\n");
		subscribeBroadcast(TerminationBroadcast.class, terminate -> {terminate();});  //terminate when receive TerminationBroadcast
		subscribeBroadcast(TickBroadcast.class, tick_brod->{  //get the broadcast of current Tick
			currTick=tick_brod.getTick();
			endTick=tick_brod.getDuration();

		});
		ShoeStoreRunner.LOGGER.info(getName() + " subscribed to Tick\n");
		subscribeRequest(PurchaseOrderRequest.class, req->{ //receive PurchaseOrderRequest from the client
			BuyResult temp =Store.getInstance().take(req.getShoeType(), req.isOnlyDiscount());  //attempt to buy the item from the store
			switch (temp){
			case NOT_IN_STOCK: { 
				if(req.isOnlyDiscount())  //if the client want to buy only in discount - the PurchaseOrderRequest cant be made
					complete(req,null);
				else{
					if(endTick>currTick+1){  //if there is still time to manufacture - sends RestockRequest to the manager and wait for callback
						sendRequest(new RestockRequest(req.getShoeType(),1), send_req->{
							if (send_req){
								Receipt regularResult = new Receipt(getName(), req.getCustomer(), req.getShoeType(), false, currTick, req.getTick(), 1);
								Store.getInstance().file(regularResult);
								complete(req,regularResult);
							}
							else
								complete(req,null);
						});
					}else
						ShoeStoreRunner.LOGGER.warning("No time to manufacture " +req.getShoeType() + "\n");
				}
				break;
			}
			case NOT_ON_DISCOUNT:{
				complete(req,null);
				break;
			}
			case REGULAR_PRICE:{
				Receipt regularResult = new Receipt(getName(), req.getCustomer(), req.getShoeType(), false, currTick, req.getTick(), 1);
				Store.getInstance().file(regularResult);
				complete(req,regularResult);
				break;
			}
			case DISCOUNTED_PRICE:{
				Receipt discountResult = new Receipt(getName(), req.getCustomer(), req.getShoeType(), true, currTick, req.getTick(), 1);
				Store.getInstance().file(discountResult);
				complete(req,discountResult);
				break;
			}
			}
		});
		ShoeStoreRunner.LOGGER.info(getName()+" initialized\n");
		doneSignal.countDown();
	}
}

