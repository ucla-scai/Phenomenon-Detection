package DataStream.Mining.FrequentPatterns;

public class FPTreeHeaderElement implements Comparable<FPTreeHeaderElement> {
	private String item;
	private FPTreeNode node_link;
	
	public FPTreeHeaderElement()
	{
		item = null;
		node_link = null;
	}
	
	public FPTreeHeaderElement(String item)
	{
		this.item = item;
		node_link = null;
	}
	
	public FPTreeHeaderElement(String item, FPTreeNode fptn)
	{
		this.item = item;
		node_link = fptn;
	}
	
	public String getItem()
	{
		return item;
	}
	
	public FPTreeNode getNodeLink()
	{
		return node_link;
	}
	
	public void setNodeLink(FPTreeNode node_link)
	{
		this.node_link = node_link; 
	}
	
	@Override
	public int compareTo(FPTreeHeaderElement elem) 
	{		
		return (this.getItem()).compareTo(elem.getItem()); //simple lexicographic order
	}
	
	@Override
	public String toString() //<item>:<Pointed FPTree Node> 
	{
		return item + "-> " + node_link;
	}
}