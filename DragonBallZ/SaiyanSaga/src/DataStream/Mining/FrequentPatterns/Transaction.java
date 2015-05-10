package DataStream.Mining.FrequentPatterns;

/* 
 * POJO to send transaction events.
 * A transaction will typically have 
 * 	- a transaction id
 * 	- list if items in the transaction, separated by a delimiter
 * 	- no timestamp; because we will consider physical windows only
 */

public class Transaction {
	private int transaction_id;
	private String item_list;
	
	public Transaction() 
	{
		transaction_id = 0;
		item_list = null;
	}
	
	public Transaction(int id, String input_record) 
	{
		transaction_id = id;
		item_list = input_record;
	}
	
	public int getTransaction_id()
	{
		return transaction_id;
	}
	
	public String getItem_list()
	{
		return this.item_list;
	}
}
