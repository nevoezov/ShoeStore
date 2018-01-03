package app;

import mics.*;

public class PurchaseOrderRequest implements Request<Receipt> {

	private String shoeType;
	private String senderName;
	private String customer;
	private int tick;
	private boolean onlyDiscount;
	private int amount;
	
	/**
	 * 
	 * @param shoeType		String	Shoe Type
	 * @param customer		String 	The {@link Websiteclient}'s name who initiated the broadcast
	 * @param tick			Int		The tick at which the {@link Request} was received
	 * @param onlyDiscount	Boolean Indicates if {@link Websiteclient} is interested in purchasing a shoe only at a discounted price
	 * @param amount		Int		Amount of shoes the client is interested in purchasing
	 */
	public PurchaseOrderRequest(String shoeType, String customer, int tick, boolean onlyDiscount, int amount) {
		this.shoeType = shoeType;
		this.customer = customer;
		this.tick = tick;
		this.onlyDiscount = onlyDiscount;
		this.amount = amount;
	}

	public String getShoeType() {
		return shoeType;
	}

	public boolean getOnlyDiscount() {
		return onlyDiscount;
	}
	
	public String getSenderName(){
		return this. senderName;
	}
	
	public boolean isOnlyDiscount(){
		return onlyDiscount;
	}

	public String getCustomer() {
		return this.customer;
	}
	
	public int getTick(){
		return tick;
	}
	
	public int getAmount(){
		return amount;
	}
	
	

}
