package common;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.textmining.text.extraction.WordExtractor;

import common.Inverted;

/**
 * 工具类
 * @author ouym
 *
 */
public class MyTools {
	
	/**
	 * 将(.txt)文件转换成字符串
	 * @param file
	 * @return
	 */
	public static String fileToString(File file){
		
		int len=0;
        StringBuffer str=new StringBuffer("");
        
		try {
			FileInputStream is = new FileInputStream(file);
			InputStreamReader isr= new InputStreamReader(is,"UTF-8");
			BufferedReader in= new BufferedReader(isr);
			String line=null;

		    while( (line=in.readLine())!=null ){
		    	 if(len != 0)  // 处理换行符的问题
	             {
	                str.append("\r\n"+line);
	                
	             }else{	            	 
	                    str.append(line);
	             }
	             len++;
		    }
		    in.close();
		    isr.close();
		    is.close();
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
            
		return str.toString();
	}
	
	
	/**
	 * 将word文档转换为存文本
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static String wordToStr(File file) throws Exception{
		
		FileInputStream in = new FileInputStream(file);
		WordExtractor extractor = null;
		  String text = null;
		  // 创建WordExtractor
		  extractor = new WordExtractor();
		  // 对doc文件进行提取
		  text = extractor.extractText(in);
		return text.trim();
	}
	
	
	/**
	 * 切词器:返回一个字符串序列，词项以'/'分开
	 * @param analyzer
	 * @param input
	 * @return
	 */
	public static String dissect(Analyzer analyzer,String input) {
		StringBuilder sb = new StringBuilder();
	       try {
	           TokenStream ts = analyzer.tokenStream("", new StringReader(input));
	           Token token;
	           sb.setLength(0);
	           while ((token = ts.next()) != null) {
	              sb.append(token.termText()).append('/');
	           }
	           if (sb.length() > 0) {
	              sb.setLength(sb.length() - 1);
	           }
	           return sb.toString();
	       } catch (Exception e) {
	           e.printStackTrace();
	           return "error";
	       }
	    }
	
	
	/**
	 * 切词器:返回一个词项列表
	 * @param analyzer
	 * @param input
	 * @return
	 */
	public static List<String> dissectToList(Analyzer analyzer,String input) {
	
		List<String> tokenList = new ArrayList<>();
	       try {
	           TokenStream ts = analyzer.tokenStream("", new StringReader(input));
	           Token token;
	           while ((token = ts.next()) != null) {
	              tokenList.add(token.termText());
	           }
	           	          
	       } catch (Exception e) {
	           e.printStackTrace();	           
	       }
	       return tokenList;
	    }
	
	
	/**
	 * 将以词项字符串集转化为词项list(重复词项)
	 * @param tokenStr
	 * @return
	 */
	public static List<String> tokenStrToList(String tokenStr){
		List<String> tokenList = new ArrayList<>();
		if(tokenStr!=null&&!tokenStr.equals("")){
			String[] tokens = tokenStr.split("/");
			for(int i=0;i<tokens.length;i++){
				tokenList.add(tokens[i]);
			}
		}		
		return tokenList;
	}
	
	
	/**
	 * 建索引
	 * @param IDNEX_PATH
	 * @param analyzer
	 * @param content
	 * @return
	 * @throws Exception
	 */
	public static boolean indexing(String IDNEX_PATH,Analyzer analyzer,String content) throws Exception{
		
		IndexWriter writer = new IndexWriter(IDNEX_PATH, analyzer, true);
		Document doc = new Document();
		Field field = new Field("content", content, Field.Store.YES,
		    Field.Index.TOKENIZED, Field.TermVector.WITH_POSITIONS_OFFSETS);
		doc.add(field);
		writer.addDocument(doc);
		writer.close();
		System.out.println("Indexed success!");
		return true;
	}
	
	/**
	 * 创建文件
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public static boolean createFile(File fileName)throws Exception{  
		  boolean flag=false;  
		  try{  
		   if(!fileName.exists()){  
		    fileName.createNewFile();  
		    flag=true;  
		   }  
		  }catch(Exception e){  
		   e.printStackTrace();  
		  }  
		  return true;  
	} 
	
	public static List<Inverted> readDocIndex(File file){
		
		List<Inverted> list = new ArrayList<>();
		FileInputStream fi = null;
		InputStreamReader ir = null;
		BufferedReader br = null;
		try {
			fi = new FileInputStream(file);
			ir = new InputStreamReader(fi);
			br = new BufferedReader(ir);
			String line;
			String[] invertedStr = new String[3];
			while((line = br.readLine())!=null){
				
				invertedStr = line.split("/");
				if(invertedStr.length==3){
					Inverted inverted = new Inverted();
					inverted.setDocId(Integer.parseInt(invertedStr[0]));
					inverted.setLexicalItem(invertedStr[1]);
					inverted.setFrequency(Double.parseDouble(invertedStr[2]));
					list.add(inverted);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(br!=null){
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(ir!=null){
				try {
					ir.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(fi!=null){
				try {
					fi.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return list;
	}
	
	/**
	 * 统计列表中的每个词出现的次数(词频统计)
	 * @param list
	 * @return
	 */
	public static Map count(List list){
		Map<String,Integer> map = new HashMap<>();
		String temp = "";
		for(int i=0 ; i<list.size();i++){
			temp = (String) list.get(i);
			if(map == null){
				map.put(temp, 1);
			}else{
				if(map.get(temp)==null){
					map.put(temp, 1);
				}
				else{
					int num = map.get(temp);
					num++;
					map.put(temp, num);
				}
			}
		}
		return map;
	}
	
	/**
	 * 加载属性文件
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static Properties getProp(String path) throws Exception{
		
		MyTools my = new MyTools();
		Properties properties = new Properties();
		//InputStream in = new BufferedInputStream (new FileInputStream(path));
		InputStream in = my.getClass().getResourceAsStream("/"+path);
		properties.load(in);
		in.close();
		return properties;
	}
}
