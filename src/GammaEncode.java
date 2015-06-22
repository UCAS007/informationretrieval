import java.util.ArrayList;
import java.util.List;


public class GammaEncode
{
	//����
	public static String encode(int iSource)
	{
		if (iSource == 1)
		{
			return "0";
		}
		String strOffset = "";
		String strLength = "";
		String strBinary = Integer.toBinaryString(iSource);
		strOffset = strBinary.substring(1);
		for(int i = 0;i < strOffset.length();i++)
		{
			strLength += "1";
		}
		strLength += "0";
		return strLength + strOffset;
	}
	//����һ��������һ����
	public static int decode(String strDest)
	{
		String strTemp = "";
		int iIndex = strDest.indexOf('0');
		String strOffset = strDest.substring(iIndex + 1);
		strTemp += "1" + strOffset;
		String strSource = Integer.valueOf(strTemp,2).toString();
		return Integer.parseInt(strSource);
	}
	//����һ��������ԭ�ĵĸ�������
	public static List<Integer> decodeLine(String strDest)
	{
		List<Integer> listNum = new ArrayList<Integer>();
		int i = 0;
		int iNumberOne = 0;
		while(i < strDest.length())
		{
			iNumberOne = 0;
			while (strDest.charAt(i) == '1')
			{
				iNumberOne++;
				i++;
			}
			i++;
			String strSingle = iNumberOne == 0 ? "1" : "1" + strDest.substring(i, i + iNumberOne);
			Integer iDecode = Integer.valueOf(strSingle,2);
			listNum.add(iDecode);
			i += iNumberOne;
		}
		return listNum;
	}
}
