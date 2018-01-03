package app;

public class PurchaseSchedule {
	private String shoeType;
	private int tick;
	
	/**
	 * One instance of a client's purchase schedule which includes
	 * @param shoeType	name of the shoe
	 * @param tick		when they wish to buy it
	 */
	public PurchaseSchedule(String shoeType, int tick){
		this.shoeType = shoeType;
		this.tick = tick;
	}

	public String getShoeType() {
		return shoeType;
	}

	public int getTick() {
		return tick;
	}
}
