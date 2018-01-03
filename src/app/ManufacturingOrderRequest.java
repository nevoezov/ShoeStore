package app;
import mics.*;

public class ManufacturingOrderRequest implements Request<Receipt> {

	private String shoeType;
	private String senderName;
	private int tick;
	private int amount;

	/**
	 * 
	 * @param shoeType 		String	Shoe Type
	 * @param senderName	String 	Name of {@link ManagementService}'s {@param name} 
	 * @param tick			Int		The tick at which the {@link Request} was received
	 * @param amount		Int		The manufacturing amount sent to the {@link ShoeFactoryService}
	 */
	public ManufacturingOrderRequest(String shoeType, String senderName, int tick, int amount) {
		this.shoeType = shoeType;
		this.senderName = senderName;
		this.tick = tick;
		this.amount = amount;
	}

	public String getShoeType() {
		return shoeType;

	}	
	public String getSenderName() {
		return senderName;
	}

	public int getTick() {
		return tick;
	}

	public int getAmount() {
		return amount;
	}


}
