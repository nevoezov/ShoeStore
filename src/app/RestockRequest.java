package app;
import mics.*;

public class RestockRequest implements Request<Boolean>{

	private String shoeType;
	private int amount;
	
	/**
	 * Contructor for a request to add more shoes to storage
	 * @param ShoeType	name of shoe
	 * @param amount	amount of shoes requested
	 */
	public RestockRequest(String ShoeType, int amount){
		this.shoeType = ShoeType;
		this.amount = amount;
	}
	
	public String getShoeType(){
		return shoeType;
	}
	
	public int getAmount(){
		return amount;
	}
	
}
