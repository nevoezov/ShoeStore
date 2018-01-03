package app;

public class RestockInformation {

	private int orderedAmount;
	private int reservedAmount;

	public RestockInformation() {
		this.orderedAmount = 0;
		this.reservedAmount = 0;
	}
	
	/**
	 * Object used to save information regarding the {@param amount} of shoes ordered from a 
	 * {@link ShoeFactoryService} and {@param reserved} for customer who are waiting for manufacturing
	 */
	public RestockInformation(int amount, int reserved) {
		this.orderedAmount = amount;
		this.reservedAmount = reserved;
	}

	/**
	 * @return the amount of ordered shoes
	 */
	public int getOrderedAmount() {
		return orderedAmount;
	}

	/**
	 * add {@param additional} to the already ordered shoes
	 */
	public void setOrderedAmount(int additional) {
		this.orderedAmount = orderedAmount + additional;
	}
	
	/**
	 * add an {@param additional} amount of shoes to the {@param reserved} shoes
	 */
	public void setReservedAmount(int additional) {
		this.orderedAmount = orderedAmount + additional;
	}
	
	/**
	 * add a single reservation
	 */
	public void addReservation(){
		reservedAmount++;
	}
	
	/**
	 * @return amount of shoes already reserved
	 */
	public int getReservedAmount(){
		return reservedAmount;
	}
	
	/**
	 * @return should a new request be sent or not
	 */
	public boolean sendNewRequest(){
		return orderedAmount == reservedAmount;
	}
	
	/**
	 * clear
	 */
	public void init(){
		orderedAmount = 0;
		reservedAmount = 0;
	}
	public void removeOrderedAmount(int additional) {
		this.orderedAmount = orderedAmount - additional;
	}
	public void removeReservedAmount(int additional) {
		this.orderedAmount = orderedAmount - additional;
	}
}
