package app;

public class Receipt {
	
	
	private String seller;
	private String customer;
	private String shoeType;
	private boolean discount;
	private int issuedTick;
	private int requestTick;
	private int amountSold;
	
	/**
	 * receipt information issued from a {@link SellingService} to a {@link WebsiteClientService} or from a 
	 * {@link ShoeFactoryService} to the {@link ManagementService} 
	 * @param seller		Issuer's name
	 * @param customer		Requester's name
	 * @param shoeType		Name of Shoe
	 * @param discount		Indicates if the item was sold at a discounted price
	 * @param issuedTick	When was the request generated
	 * @param requestTick	When was the request fulfilled
	 * @param amountSold	Amount of shoes sold or manufactured
	 */
	public Receipt(String seller, String customer, String shoeType, boolean discount, int issuedTick, int requestTick, int amountSold){
		this.seller = seller;
		this.customer = customer;
		this.shoeType = shoeType;
		this.discount = discount;
		this.issuedTick = issuedTick;
		this.requestTick = requestTick;
		this.amountSold = amountSold;
	}
	
	public void print(){
		System.out.println("Receipt:\n\tSeller: " + seller + "\n\tCustomer: " + customer + "\n\tShoe Type: " + shoeType + "\n\tDiscount: " + discount 
							+ "\n\tIssue Time: " + issuedTick + "\n\tRequest Time: " + requestTick + "\n\tSold Amount: " + amountSold + "\n");
	}

	public String getSeller() {
		return seller;
	}

	public String getCustomer() {
		return customer;
	}

	public String getShoeType() {
		return shoeType;
	}

	public boolean isDiscount() {
		return discount;
	}

	public int getIssuedTick() {
		return issuedTick;
	}

	public int getRequestTick() {
		return requestTick;
	}

	public int getAmountSold() {
		return amountSold;
	}
	
}