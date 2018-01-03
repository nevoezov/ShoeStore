package app;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class ShoeStorageInfo {

	private String shoeType;
	private AtomicInteger amount;
	private AtomicInteger discountedAmount;
	
	public ShoeStorageInfo(String shoeType,int amountOnStorage,int discountedAmount){
		this.shoeType = shoeType;
		this.amount = new AtomicInteger(amountOnStorage);
		this.discountedAmount = new AtomicInteger(discountedAmount); 
	}
	public ShoeStorageInfo(){
		
	}
	
	
	public String getShoeType(){
		return this.shoeType;
	}
	
	public int getAmountOnStorage(){
		return this.amount.get();
	}
	
	public int getDiscountedAmount(){
		return this.discountedAmount.get();
	}
	
	public void setDiscountedAmount(int amount){
		this.discountedAmount.set(amount);
	}
	
	public void setAmountOnStorage(int amount){
		this.amount.set(amount);
	}
	public boolean inStock(){
		return this.amount.get()>0;
	}
	public boolean discountAvailable(){
		return this.discountedAmount.get()>0;
	}
	public void sellOneShoe(){
		this.amount.decrementAndGet();
	}
	public void sellOneDiscountedShoe(){
		this.amount.decrementAndGet();
		this.discountedAmount.decrementAndGet();
	}
	public void addToStorage(int amount){
		this.amount.set(this.amount.get() + amount);
	}
	public void addToDiscount(int amount){
		this.discountedAmount.set(discountedAmount.get() + amount);
	}
	public void print(){
		ShoeStoreRunner.LOGGER.info("Shoe Type: " + shoeType + "\nAmount On Stock: " + amount + "\nDiscounted Amount: " + discountedAmount + "\n");
	}
}
