package app;
import mics.*;

public class NewDiscountBroadcast implements Broadcast {

	private String shoeType;
	private int discountedAmount;
	
	/**
	 * @param shoeType 			String	Shoe Type
	 * @param discountedAmount	Int		Amount of shoes that the {@link ManagementService} has allowed to be discounted
	 */
	public NewDiscountBroadcast(String shoeType, int discountedAmount){
		this.shoeType = shoeType;
		this.discountedAmount = discountedAmount;
	}

	public String getShoeType() {
		return shoeType;
	}

	public int getDiscountedAmount() {
		return discountedAmount;
	}
}
