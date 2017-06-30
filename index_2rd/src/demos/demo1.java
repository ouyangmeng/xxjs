package demos;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import common.Constant;
import service.DocHandle;

public class demo1 {
	
	public static void main(String[] args){
		
		DocHandle docHandle = new DocHandle();
		try {
			// 每一篇文档成成一个词项文本（中间结果）
			docHandle.docToTerms();
			
			//得到总的词频统计文件：DocIndex.txt
			//docHandle.getTF();
			
			//生成归一化tf:DocIndex(tf).txt
			//docHandle.getNormTF();
			
			//统计所有文档出现的词
			/*List<String> result = docHandle.getAllTerms();
			System.out.println(result.size());
			System.out.println(result);*/
			
			//统计所有需要建索引的词项
			/*List<String> result1 = docHandle.getIndexTerms();
			System.out.println(result1.size());*/
			
			//将30篇文档放到hashMap数组里面
			/*Map<String,Double>[] map = docHandle.getMapArray();
			System.out.println(map.length);
			Iterator iter = map[0].entrySet().iterator();
			while(iter.hasNext()){
				Map.Entry enter = (Map.Entry)iter.next();
				String word = (String)enter.getKey();
				double num = (double) enter.getValue();
				System.out.println(word+num);
			}*/
			
			
			//统计词-文档数
			//docHandle.countTermDocNum();
			//docHandle.countTermDocNumCopy();
			
			//生成idf文件
			//docHandle.getDocIdf();
			
			//生成tf*idf
			//docHandle.getTfIdf();
			
			//建立词项 文档倒排
			//docHandle.invertedIndex();
			
			//构建文档向量（向量空间模型）
			//docHandle.getVector();
			
			//检索查询
			/*List<Integer> list = docHandle.seaching();
			System.out.println(list.size());
			System.out.println(list);*/
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
