package common;
/**
 * (文档编号、词项、词频）三元组对象
 * @author ouym
 *
 */
public class Inverted {

	//文档编号
	private int docId;
	
	//词项
	private String  lexicalItem;
	
	//词频
	private double frequency;

	public int getDocId() {
		return docId;
	}

	public void setDocId(int docId) {
		this.docId = docId;
	}

	public String getLexicalItem() {
		return lexicalItem;
	}

	public void setLexicalItem(String lexicalItem) {
		this.lexicalItem = lexicalItem;
	}

	public double getFrequency() {
		return frequency;
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}

	
	
	
}
