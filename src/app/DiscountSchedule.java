package app;

public class DiscountSchedule {
	private String shoeType;
	private int tick;
	private int amount;
	
	/**
	 * Constructor
	 * @param shoeType	Name of Shoe
	 * @param tick		Desired tick for discount {@link Broadcast}
	 * @param amount
	 */
	public DiscountSchedule(String shoeType, int tick,int amount){
		this.shoeType = shoeType;
		this.tick = tick;
		this.amount = amount;
	}

	/** 
	 * @return {@param shoeType}
	 */
	public String getShoeType() {
		return shoeType;
	}

	/** 
	 * @return {@param tick}
	 */
	public int getTick() {
		return tick;
	}

	/** 
	 * @return {@param amount}
	 */
	public int getAmount() {
		return amount;
	}
}