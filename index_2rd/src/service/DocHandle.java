package service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;

import common.Constant;
import common.Inverted;

import common.MyTools;
import net.paoding.analysis.analyzer.PaodingAnalyzer;

/**
 * 文档处理service
 * @author ouym
 *
 */
public class DocHandle {
	
	private Properties prop;
	
	
	
	/**
	 * 每一篇文档成成一个词项文本（中间结果）
	 * 文本每行格式 ：文档ID/词项/词频
	 * @throws Exception
	 */
	public void docToTerms() throws Exception{
		long start = System.currentTimeMillis(); 
		prop = MyTools.getProp("application.properties");
		if(prop==null){
			return;
		}
		String DOC_PATH = prop.getProperty("doc_path");
	
		Analyzer analyzer = new PaodingAnalyzer();
		File root = new File(DOC_PATH);
		File[] docs = root.listFiles();		
		
		try {
			String maxStr = "";
			for(int i=0;i<docs.length;i++){
				String docStr = MyTools.wordToStr(docs[i]);
				List<String> tokenList = new ArrayList<>(); 
				tokenList = MyTools.dissectToList(analyzer, docStr);
				Map<String,Integer> tfmap = new HashMap<>();
				tfmap = MyTools.count(tokenList);
	
				StringBuilder sb = new StringBuilder();

				int max=1;
				Iterator iter = tfmap.entrySet().iterator();
				while(iter.hasNext()){
					Map.Entry enter = (Map.Entry)iter.next();
					String word = (String)enter.getKey();
					int wordNum = (int)enter.getValue();
					
					if(wordNum>max){
						max = wordNum;
					}
					
					String docId = docs[i].getName().substring(3);
					docId= docId.substring(0,docId.length()-4);
					sb.append(docId+"/"+word+"/"+wordNum+"\r\n");
				}
				maxStr += (max+"/");
				

				//每一篇文档成成一个词项文本
				String docId = docs[i].getName().substring(3);
				docId= docId.substring(0,docId.length()-4);
				String fileStr = "E:/index2/termDoc/"+docId+".txt";
				File file = new File(fileStr);
				if(!file.exists()){
					file.createNewFile();
				}
				FileOutputStream fo = new FileOutputStream(file);
				OutputStreamWriter os = new OutputStreamWriter(fo);
				BufferedWriter bw = new BufferedWriter(os);
				bw.write(sb.toString().trim());
				
				bw.close();
				os.close();
				fo.close();
				
				//统计每片文档中的最大词频数
				File maxtffile = new File("E:/index2/maxtf.txt");
				if(!maxtffile.exists()){
					maxtffile.createNewFile();
				}
				FileOutputStream fo1 = new FileOutputStream(maxtffile);
				OutputStreamWriter os1 = new OutputStreamWriter(fo1);
				BufferedWriter bw1 = new BufferedWriter(os1);
				bw1.write(maxStr);
				
				bw1.close();
				os1.close();
				fo1.close();
				
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		long end = System.currentTimeMillis(); 
		
		double runTimes = (end-start)/1000.0;
		System.out.print("文档词频统计中间文件生成时间是："+runTimes+"秒");
				
	}
	/**
	 * 得到总的词频统计文件：DocIndex.txt
	 * @throws Exception
	 */
	public void getTF() throws Exception{
		long start = System.currentTimeMillis();
		File file = new File("E:/index2/termDoc");
		File[] fileArray = file.listFiles();
		File docIndex = new File("E:/index2/DocIndex.txt");
		if(!docIndex.exists()){
			docIndex.createNewFile();
		}
		FileOutputStream fo = new FileOutputStream(docIndex);
		OutputStreamWriter ow = new OutputStreamWriter(fo);
		BufferedWriter bw = new BufferedWriter(ow);
		for(int i=0;i<fileArray.length;i++){
			FileInputStream is = new FileInputStream(fileArray[i]);
			InputStreamReader isr= new InputStreamReader(is,"UTF-8");
			BufferedReader in= new BufferedReader(isr);
			String line=null;

		    while( (line=in.readLine())!=null ){
		    	bw.write(line);
		    	bw.write("\r\n");
		    }
		    in.close();
		    isr.close();
		    is.close();
		}
		bw.close();
		ow.close();
		fo.close();
		
		long end = System.currentTimeMillis(); 
		double runTimes = (end-start)/1000.0;
		System.out.println("DocIndex.txt生成时间是："+runTimes+"秒");
		return;
	}

	/**
	 * 生成归一化tf
	 * tf/maxtf
	 */
	public void getNormTF(){
		long start = System.currentTimeMillis(); 
		File file = new File("E:/index2/DocIndex.txt");
		//反序列化
		List<Inverted> list = new ArrayList<>();
		list = MyTools.readDocIndex(file);
		
		File file1 = new File("E:/index2/maxtf.txt");
		String maxtfstr = MyTools.fileToString(file1);
		maxtfstr = maxtfstr.substring(0, maxtfstr.length()-1);
		
		String[] maxtfs = maxtfstr.split("/");
		int[] maxtf = new int[30];
		for(int i = 0 ; i < maxtf.length ; i++){
			maxtf[i] = Integer.parseInt(maxtfs[i]);
			//System.out.println(maxtf[i]);
		}
		
		for(int i = 0;i<Constant.DOC_NUMS;i++){
			for(int j = 0;j<list.size();j++){
				int t = i+1;
				if(list.get(j).getDocId()==Constant.docnum[i]){
					double frequency = list.get(j).getFrequency();
					list.get(j).setFrequency(frequency/maxtf[i]);
				}
			}
						
		}
		
		String str = new  String();
		for(Inverted inverted : list){
			str += inverted.getDocId()+"/"+inverted.getLexicalItem()+"/"+inverted.getFrequency()+"\r\n";
		}
		File filetf = new File("E:/index2/DocIndex(tf).txt");
		if(!filetf.exists()){
			try {
				filetf.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		FileOutputStream fo=null;
		try {
			fo = new FileOutputStream(filetf);
			OutputStreamWriter os = new OutputStreamWriter(fo);
			BufferedWriter bw = new BufferedWriter(os);
			bw.write(str);
			
			bw.close();
			os.close();
			fo.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		long end = System.currentTimeMillis(); 
		
		double runTimes = (end-start)/1000.0;
		System.out.println("生成归一化tf运行时间是："+runTimes+"秒");
		
	}
	
	/**
	 * 统计文档出现的所有词项
	 * @return
	 */
	public List<String> getAllTerms(){
		
		long start = System.currentTimeMillis(); 
		
		Set<String> set = new HashSet<>();
		File file = new File("E:/index2/DocIndex.txt");
		List<Inverted> list = MyTools.readDocIndex(file);
		for(Inverted invert : list){
			set.add(invert.getLexicalItem());
		}
		List<String> result = new ArrayList<>(set);
		
		long end = System.currentTimeMillis(); 
		double runTimes = (end-start)/1000.0;
		System.out.println("统计文档所有词项运行时间是："+runTimes+"秒");
		return result;
	}
	
	/**
	 * 统计文档出现的所有需要建索引的词项
	 * @return
	 */
	public List<String> getIndexTerms(){
		
		long start = System.currentTimeMillis(); 
		double threshold = Constant.threshold;
		Set<String> set = new HashSet<>();
		File file = new File("E:/index2/DocIndex(tfidf).txt");
		List<Inverted> list = MyTools.readDocIndex(file);
		for(Inverted invert : list){
			if(invert.getFrequency()>threshold){
				set.add(invert.getLexicalItem());
				//System.out.println(invert.getFrequency());
			}
			
		}
		List<String> result = new ArrayList<>(set);
		
		long end = System.currentTimeMillis(); 
		double runTimes = (end-start)/1000.0;
		System.out.println("统计文档所有需要建索引de词项运行时间是："+runTimes+"秒");
		return result;
	}
	
	/*
	 * 将所有的文档放入hashMap数组中 <词项, 词频>
	 * 文档编号-1，对应数组编号
	 */
	public Map<String,Double>[] getMapArray(){
	
		long start = System.currentTimeMillis(); 
		
		File file = new File(Constant.TERMDOC_PATH);
		File[] fileList = file.listFiles();
		Map<String,Double>[] map = new Map[30];
		for(int i=0;i<Constant.DOC_NUMS;i++){
			List<Inverted> list = MyTools.readDocIndex(fileList[i]);
			map[(Constant.docnum[i]-1)] = new HashMap<String,Double>();
			for(Inverted inverted:list){
				map[(Constant.docnum[i]-1)].put(inverted.getLexicalItem(), inverted.getFrequency());
			}
		}
		
		long end = System.currentTimeMillis(); 
		double runTimes = (end-start)/1000.0;
		System.out.println("文档转成hash表运行时间是："+runTimes+"秒");
		
		return map;
		
	}
	
	/**
	 * 统计词-文档数
	 */
	public void countTermDocNum(){
		long start = System.currentTimeMillis(); 
		List<String> result = getAllTerms();
		Map<String,Double>[] map = getMapArray();
		System.out.println(result.size());
		for(String s:result){
			int num=0;
			for(int i=0;i<Constant.DOC_NUMS;i++){
				if(map[i].containsKey(s)){
					num++;
				}
			}
			System.out.println(s+":"+num);
		}
		long end = System.currentTimeMillis(); 
		double runTimes = (end-start)/1000.0;
		System.out.println("运行时间是："+runTimes+"秒");
	}
	
	/**
	 * 建立词项文档数
	 * @throws Exception
	 */
	public void countTermDocNumCopy() throws Exception{
		long start = System.currentTimeMillis(); 
		List<String> result = getAllTerms();
		Map<String,Double>[] map = getMapArray();
		File file = new File("E:/index2/DocIndex(df).txt");
		if(!file.exists()){
			file.createNewFile();
		}
		FileOutputStream fo = new FileOutputStream(file);
		OutputStreamWriter os = new OutputStreamWriter(fo);
		BufferedWriter bw = new BufferedWriter(os);
		List<Inverted> list = MyTools.readDocIndex(new File("E:/index2/DocIndex(tf).txt"));
		for(Inverted inverted:list){
			String term = inverted.getLexicalItem();
			int num=0;
			for(int i=0;i<Constant.DOC_NUMS;i++){
				if(map[i].containsKey(term)){
					num++;
				}
			}
			//inverted.setFrequency(num);
			bw.write(inverted.getDocId()+"/"+inverted.getLexicalItem()+"/"+num);
			bw.write("\r\n");
		}
		bw.close();
		os.close();
		fo.close();
		//System.out.println(result.size());
		
		long end = System.currentTimeMillis(); 
		double runTimes = (end-start)/1000.0;
		System.out.println("建立词项文档数运行时间是："+runTimes+"秒");
	}
	
	/**
	 * 生成idf
	 * @throws Exception
	 */
	public void getDocIdf() throws Exception{
		long start = System.currentTimeMillis(); 
		List<String> result = getAllTerms();
		Map<String,Double>[] map = getMapArray();
		File file = new File("E:/index2/DocIndex(idf).txt");
		if(!file.exists()){
			file.createNewFile();
		}
		FileOutputStream fo = new FileOutputStream(file);
		OutputStreamWriter os = new OutputStreamWriter(fo);
		BufferedWriter bw = new BufferedWriter(os);
		List<Inverted> list = MyTools.readDocIndex(new File("E:/index2/DocIndex(df).txt"));
		for(Inverted inverted:list){
			String term = inverted.getLexicalItem();
			int num=0;
			for(int i=0;i<Constant.DOC_NUMS;i++){
				if(map[i].containsKey(term)){
					num++;
				}
			}
			//inverted.setFrequency(num);
			bw.write(inverted.getDocId()+"/"+inverted.getLexicalItem()+"/"+(Math.log(Constant.DOC_NUMS/num))/Math.log(10));
			bw.write("\r\n");
		}
		bw.close();
		os.close();
		fo.close();
		//System.out.println(result.size());
		
		long end = System.currentTimeMillis(); 
		double runTimes = (end-start)/1000.0;
		System.out.println("生成idf文件运行时间是："+runTimes+"秒");
	}
	
	/**
	 * 生成tf*idf文件
	 * @throws Exception 
	 */
	public void getTfIdf() throws Exception{
		long start = System.currentTimeMillis(); 
		File fileTfIdf = new File("E:/index2/DocIndex(tfidf).txt");
		if(!fileTfIdf.exists()){
			fileTfIdf.createNewFile();
		}
		
		FileOutputStream fo = new FileOutputStream(fileTfIdf);
		OutputStreamWriter os = new OutputStreamWriter(fo);
		BufferedWriter bw = new BufferedWriter(os);
		
		List<Inverted> listTf = MyTools.readDocIndex(new File("E:/index2/DocIndex(tf).txt"));
		List<Inverted> listIdf = MyTools.readDocIndex(new File("E:/index2/DocIndex(idf).txt"));
		
		for(int i=0;i<listTf.size();i++){
			double tf = listTf.get(i).getFrequency();
			double idf = listIdf.get(i).getFrequency();
			bw.write(listTf.get(i).getDocId()+"/"+listTf.get(i).getLexicalItem()+"/"+tf*idf);
			bw.write("\r\n");
		}
		bw.close();
		os.close();
		fo.close();
		long end = System.currentTimeMillis(); 
		double runTimes = (end-start)/1000.0;
		System.out.println("生成tf*idf文件运行时间是："+runTimes+"秒");
	}
	
	/**
	 * 倒排
	 * @throws Exception 
	 */
	public void invertedIndex() throws Exception{
		long start = System.currentTimeMillis(); 
		List<String> result = getAllTerms();
		Map<String,Double>[] map = getMapArray();
		System.out.println(result.size());
		
		File file = new File("E:/index2/inverted.txt");
		FileOutputStream fo = new FileOutputStream(file);
		OutputStreamWriter os = new OutputStreamWriter(fo);
		BufferedWriter bw = new BufferedWriter(os);
		
		for(String s:result){
			
			List<Integer> sortDoc = new ArrayList<>();
			for(int i=0;i<Constant.DOC_NUMS;i++){
				if(map[i].containsKey(s)){
					sortDoc.add(i+1);
				}
			}
			bw.write(s+":");
			String str = "";
			for(int i:sortDoc){
				str+=i+"/";
				
			}
			bw.write(str.substring(0, str.length()-1));
			bw.write("\r\n");
			//System.out.println(s+":"+sortDoc);
		}
		bw.close();
		os.close();
		fo.close();
		long end = System.currentTimeMillis(); 
		double runTimes = (end-start)/1000.0;
		System.out.println("建立倒排运行时间是："+runTimes+"秒");
	}
	
	
	/**
	 * 构建文档向量（向量空间模型）
	 * 问题：得到的文档向量是一个稀疏矩阵，存储空间有待优化
	 * @throws Exception 
	 */
	public void getVector() throws Exception{
		
		long start = System.currentTimeMillis(); 
		//List<String> result = getAllTerms();
		List<String> result = getIndexTerms();
		Map<String,Double>[] map = getMapArray();
		
		File file = new File("E:/index2/DocVector1.txt");
		FileOutputStream fo = new FileOutputStream(file);
		OutputStreamWriter os = new OutputStreamWriter(fo);
		BufferedWriter bw = new BufferedWriter(os);
		
		for(int i=0;i<Constant.DOC_NUMS;i++){
			List<Double> list = new ArrayList<>();
			for(String s : result){
				if(map[i].containsKey(s)){
					list.add(map[i].get(s));
				}else{
					list.add(0.0);
				}
			}
			bw.write((i+1)+":");
			String str = "";
			for(double j:list){
				str+=j+"/";
				
			}
			bw.write(str.substring(0, str.length()-1));
			bw.write("\r\n");
		}
		bw.close();
		os.close();
		fo.close();
		long end = System.currentTimeMillis(); 
		double runTimes = (end-start)/1000.0;
		System.out.println("构建向量空间运行时间是："+runTimes+"秒");
	}
	
	/**
	 * 检索
	 * @return
	 */
	public List<Integer> seaching(){
		long start = System.currentTimeMillis(); 
		List<Integer> docNums=new ArrayList<>();
		try {
			/*prop = MyTools.getProp("application.properties");
			if(prop==null){
				return null;
			}
			String query1 = prop.getProperty("query1");*/
			String query1 = "用经济常识知识简要说明重视安全生产的经济意义。";
			Analyzer analyzer = new PaodingAnalyzer();
			List<String> query1Terms = MyTools.dissectToList(analyzer, query1);
			System.out.println(query1Terms);
			Map<String,Double>[] map = getMapArray();
			List<List<Integer>> list = new ArrayList<>();
			Set<Integer> set = new HashSet<>();
			for(String term:query1Terms){
				List<Integer> docList = new ArrayList<>();
				for(int i=0;i<Constant.DOC_NUMS;i++){
					if(map[i].containsKey(term)){
						docList.add((i+1));
						set.add((i+1));
					}
				}
				list.add(docList);
			}
			System.out.println(list);
			//迭代set
			Iterator iter = set.iterator();
			while(iter.hasNext()){
				int term = (int) iter.next();
				boolean t = true;
				for(List<Integer> docs :list){
					if(!docs.contains(term)){
						t = false;
					}
				}
				if(t){
					docNums.add(term);
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long end = System.currentTimeMillis(); 
		double runTimes = (end-start)/1000.0;
		System.out.println("查询结果运行时间是："+runTimes+"秒");
		return docNums;
	}

}
