import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class SearchProcess
{
	private static List<InverseNumber> mListPointers = null;   //�������ѯ����ľ�̬��
	public SearchProcess() throws IOException
	{
		mListPointers = this.getAllWordPointer();
	}
	
	//�����ѯ����ѯ��ʽ��
	//���ؼ����Կո�ָ�ո��ʾand��key1 key2 key3    
	public void search(String strInput) throws IOException
	{
		String strTemp = new Preprocess().processLine(strInput);
		strTemp = strTemp.trim();
		String[] strLines = strTemp.split(" ");
		strLines = this.processStopWord(strLines);
		List<List<Integer>> listBig = new ArrayList<List<Integer>>();
		for (String item : strLines)
		{
			if (item.trim().length() == 0)
			{
				continue;
			}
			listBig.add(this.searchKey(item));
		}
		List<Integer> listNum = this.mergeLists(listBig);
		this.remapDocID(listNum);
		return;
	}
	
	//����ѯ��������ID��ԭ�ĵ�ID���з�ӳ�䲢��ʽ��������
	public void remapDocID(List<Integer> mapBig) throws IOException
	{
		Map<Integer, String> mapDocID = new TreeMap<Integer, String>();
		List<String> listDoc = new ArrayList<String>();
		FileReader fr = new FileReader("./files/docid.txt");
		BufferedReader br = new BufferedReader(fr);
		String strLine = br.readLine();
		while(strLine != null)
		{
			String[] strLines = strLine.split(" ");
			mapDocID.put(Integer.parseInt(strLines[1]), strLines[0]);
			strLine = br.readLine();
		}
		br.close();
		fr.close();
		if (mapBig == null || mapBig.size() == 0)
		{
			System.out.println("No retrieval!");
			return;
		}
		for (Integer item : mapBig)
		{
			String strDoc = mapDocID.get(item);
			listDoc.add(strDoc);
		}
		if (listDoc.size() == 0)
		{
			System.out.println("No retrieval!");
			return;
		}
		else
		{
			System.out.println(listDoc.size() + " Docs retrieved:");
			for(int i = 0;i < listDoc.size();i++)
			{
				System.out.println(String.format("%4d", i+1) + ":" + listDoc.get(i));
			}
		}
	}
	
	//����ͣ�ô�
	private String[] processStopWord(String[] strLines) throws IOException
	{
		List<String> listStops = this.getStopWords();
		List<String> listWords = Arrays.asList(strLines);
		List<String> listStrs = this.diff(listWords, listStops);
		return listStrs.toArray(new String[0]);
	}
	
	//���ļ��ж�ȡͣ�ôʵ�һ������
	public List<String> getStopWords() throws IOException
	{
		List<String> listStops = new ArrayList<String>();
		FileReader fr = new FileReader("./Files/StopWords.txt");
		BufferedReader br = new BufferedReader(fr);
		String strLine = br.readLine();
		while (true)
		{
			strLine = br.readLine();
			if (strLine == null)
			{
				break;
			}
			listStops.add(strLine.trim());
		}
		br.close();
		fr.close();
		listStops.add(" ");
		return listStops;
	}
	
	//������������
	public List<String> diff(List<String> ls, List<String> ls2) 
	{   
		List<String> list = new ArrayList(Arrays.asList(new String[ls.size()]));   
        Collections.copy(list, ls);   
        list.removeAll(ls2);   
        if (list.size() == 0)
		{
			return ls;
		}
        return list;   
    }   

	//������
	public List<Integer> mergeLists(List<List<Integer>> listBig)
	{
		Collections.sort(listBig, new Comparator<List<Integer>>()
		{
			public int compare(List<Integer> list1,List<Integer> list2)
			{
				return list1.size() - list2.size();
			}
		});
		if (listBig.size() == 1)
		{
			return listBig.get(0);
		}
		List<Integer> listNew = listBig.get(0);
		for (int i = 1; i < listBig.size(); i++)
		{
			listNew = mergeDocIDList(listBig.get(i),listNew);
		}
		return listNew;
	}
	
	//�������ż�¼��ϲ������غϲ���ĵ��ż�¼��
	public List<Integer> mergeDocIDList(List<Integer> list1,List<Integer> list2)
	{
		List<Integer> list = new ArrayList(Arrays.asList(new String[list1.size()]));   
        Collections.copy(list, list1);   
        list.removeAll(list2);   
        return list;   
	}
	
	//����һ�����ʣ������䵹�ż�¼��
	public List<Integer> searchKey(String strKey) throws IOException
	{
		InverseNumber in = this.binarySearch(strKey);
		if (in == null)
		{
			return null;
		}
		List<Integer> listDocID = this.getDocIDByIndex(in.getiDocID(), in.getiDocIDLength());
		List<Integer> listIDs = new ArrayList<Integer>();
		Integer iFirst = listDocID.get(0) - 1;
		listIDs.add(iFirst - 1);
		for(int i = 1;i < listDocID.size();i++)
		{
			listIDs.add(listDocID.get(i) + listIDs.get(i - 1));
		}
		return listIDs;
	}
	
	//���ֲ���һ����
	public InverseNumber binarySearch(String strKey) throws IOException
	{
		int low = 0;
		int high = mListPointers.size() - 1;
		while(low <= high)
		{
			int middle = (low + high) / 2;
			Integer middleIndex = mListPointers.get(middle).getiWordID();
			Integer iLength;
			if (middle + 1 >= mListPointers.size())
			{
				iLength = -1;
			}
			else
			{
				iLength = mListPointers.get(middle + 1).getiWordID() - mListPointers.get(middle).getiWordID();
			}
			String strWord = this.getWordByIndex(middleIndex,iLength);
			if (strWord.compareTo(strKey) == 0)
			{
				int iDocIDLength = middle + 1 >= mListPointers.size() ? -1 : (mListPointers.get(middle + 1).getiDocID() - mListPointers.get(middle).getiDocID());
				InverseNumber in = new InverseNumber(middle, mListPointers.get(middle).getiDocID(), mListPointers.get(middle).getiFrequency(), middleIndex,iDocIDLength);
				return in;
			}
			else if(strWord.compareTo(strKey) > 0)
			{
				high = middle - 1;
			}
			else
			{
			    low = middle + 1;	
			}
		}
		return null;
	}
	
	//��ȡ���д����ָ�뵽һ��list��
	public  List<InverseNumber> getAllWordPointer() throws IOException
	{
		FileReader fr = new FileReader("./files/OriginalInverseTable.txt");
		BufferedReader br = new BufferedReader(fr);
		List<InverseNumber> listPointers = new ArrayList<InverseNumber>();
		String strLine = br.readLine();
		while(strLine != null)
		{
			String[] strLines = strLine.split(" ");
			InverseNumber in = new InverseNumber(Integer.parseInt(strLines[0].trim()),Integer.parseInt(strLines[3].trim()),Integer.parseInt(strLines[2].trim()),Integer.parseInt(strLines[1].trim()));
			listPointers.add(in);
			strLine = br.readLine();
		}
		br.close();
		fr.close();
		return listPointers;
	}
	
	//������ʼλ�úͳ��ȷ����ļ����ⲿ���ַ��������������-1��ʾ�������
	public String getTermByPosition(int iIndex,int iLength,String strFileName) throws IOException
	{
		File f = new File(strFileName);
		RandomAccessFile raf = new RandomAccessFile(f, "r");
		byte[] by = new byte[200];
		raf.seek(iIndex);
		if (iLength == -1)
		{
			raf.read(by);
			raf.close();
			String str = new String(by,"UTF-8");
			return str.trim();
		}
		int i = 0;
		int iSum = iIndex + iLength;
		while(iIndex < iSum)
		{
			by[i++] = raf.readByte();
			raf.seek(++iIndex);
		}
		raf.close();
		String str = new String(by,"UTF-8");
		return str.trim();
	}
	
	//������ʼλ�úͳ��ȶ�һ�����ʵĵ��ż�¼��һ��list
	public List<Integer> getDocIDByIndex(int iIndex,int iLength) throws IOException
	{
		String strLine = this.getTermByPosition(iIndex, iLength, "./files/DocIDs.txt");
		List<Integer> listIDs = GammaEncode.decodeLine(strLine);
		return listIDs;
	}
	
	//������ʼλ�úͳ��ȶ��ֵ���ĵ���
	public String getWordByIndex(int iIndex,int iLength) throws IOException
	{
		String strWord = this.getTermByPosition(iIndex, iLength, "./files/Dictionary.txt");
		return strWord;
	}
}
