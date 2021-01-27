package OneAIO;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

public class ConvertCode {

	public ByteBuffer string2ByteBuffer(String data) {
		try {
			return ByteBuffer.wrap(data.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * @Title:bytes2HexString @Description:字节数组转16进制字符串 @param b 字节数组 @return
	 *                        16进制字符串 @throws
	 */
	public String bytes2HexString(byte[] b) {
		StringBuffer result = new StringBuffer();
		String hex;
		for (int i = 0; i < b.length; i++) {
			hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			result.append(hex.toUpperCase());
		}
		return result.toString();
	}

	public String bytes2HexString(byte[] b, int len) {
		StringBuffer result = new StringBuffer();
		String hex;
		for (int i = 0; i < len; i++) {
			hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			result.append(hex.toUpperCase());
		}
		return result.toString();
	}

	/**
	 * @Title:hexString2Bytes
	 * @Description:16进制字符串转字节数组
	 * @param src 16进制字符串
	 * @return 字节数组
	 */
	public byte[] hexString2Bytes(String src) {
		int l = src.length() / 2;
		byte[] ret = new byte[l];
		for (int i = 0; i < l; i++) {
			ret[i] = Integer.valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
		}
		return ret;
	}

	/**
	 * @Title:string2HexString
	 * @Description:字符串转16进制字符串
	 * @param strPart 字符串
	 * @return 16进制字符串
	 */
	public String string2HexString(String strPart) {
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < strPart.length(); i++) {
			int ch = strPart.charAt(i);
			String strHex = Integer.toHexString(ch);
			hexString.append(strHex);
		}
		return hexString.toString();
	}

	/**
	 * @Title:hexString2String @Description:16进制字符串转字符串 @param src 16进制字符串 @return
	 *                         字节数组 @throws
	 */
	public String hexString2String(String src) {
		String temp = "";
		for (int i = 0; i < src.length() / 2; i++) {
			// System.out.println(Integer.valueOf(src.substring(i * 2, i * 2 +
			// 2),16).byteValue());
			temp = temp + (char) Integer.valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
		}
		return temp;
	}

	/**
	 * @Title:char2Byte @Description:字符转成字节数据char-->integer-->byte @param
	 *                  src @return @throws
	 */
	public Byte char2Byte(Character src) {
		return Integer.valueOf(src).byteValue();
	}

	/**
	 * @Title:intToHexString @Description:10进制数字转成16进制 @param a 转化数据 @param len
	 *                       占用字节数 @return @throws
	 */
	public String intToHexString(int a, int len) {
		len <<= 1;
		String hexString = Integer.toHexString(a);
		int b = len - hexString.length();
		if (b > 0) {
			for (int i = 0; i < b; i++) {
				hexString = "0" + hexString;
			}
		}
		return hexString;
	}

	/**
	 * 将16进制的2个字符串进行异或运算 http://blog.csdn.net/acrambler/article/details/45743157
	 * 
	 * @param strHex_X
	 * @param strHex_Y 注意：此方法是针对一个十六进制字符串一字节之间的异或运算，如对十五字节的十六进制字符串异或运算：1312f70f900168d900007df57b4884
	 *                 先进行拆分：13 12 f7 0f 90 01 68 d9 00 00 7d f5 7b 48 84 13 xor
	 *                 12-->1 1 xor f7-->f6 f6 xor 0f-->f9 .... 62 xor 84-->e6
	 *                 即，得到的一字节校验码为：e6
	 * @return
	 */
	public String xor(String strHex_X, String strHex_Y) {
		// 将x、y转成二进制形式
		String anotherBinary = Integer.toBinaryString(Integer.valueOf(strHex_X, 16));
		String thisBinary = Integer.toBinaryString(Integer.valueOf(strHex_Y, 16));
		String result = "";
		// 判断是否为8位二进制，否则左补零
		if (anotherBinary.length() != 8) {
			for (int i = anotherBinary.length(); i < 8; i++) {
				anotherBinary = "0" + anotherBinary;
			}
		}
		if (thisBinary.length() != 8) {
			for (int i = thisBinary.length(); i < 8; i++) {
				thisBinary = "0" + thisBinary;
			}
		}
		// 异或运算
		for (int i = 0; i < anotherBinary.length(); i++) {
			// 如果相同位置数相同，则补0，否则补1
			if (thisBinary.charAt(i) == anotherBinary.charAt(i))
				result += "0";
			else {
				result += "1";
			}
		}
		return Integer.toHexString(Integer.parseInt(result, 2));
	}

	/**
	 * Convert byte[] to hex string.这里我们可以将byte转换成int
	 * 
	 * @param src byte[] data
	 * @return hex string
	 */
	public String bytes2Str(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	/**
	 * @param
	 * @return 接收字节数据并转为16进制字符串
	 */
	public String receiveHexToString(byte[] by) {
		try {
			/*
			 * io.netty.buffer.WrappedByteBuf buf = (WrappedByteBuf)msg; ByteBufInputStream
			 * is = new ByteBufInputStream(buf); byte[] by = input2byte(is);
			 */
			String str = bytes2Str(by);
			str = str.toLowerCase();
			return str;
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("接收字节数据并转为16进制字符串异常");
		}
		return null;
	}

	/**
	 * "7dd",4,'0'==>"07dd"
	 * 
	 * @param input  需要补位的字符串
	 * @param size   补位后的最终长度
	 * @param symbol 按symol补充 如'0'
	 * @return N_TimeCheck中用到了
	 */
	public String fill(String input, int size, char symbol) {
		while (input.length() < size) {
			input = symbol + input;
		}
		return input;
	}

	public void main(String args[]) {
		String productNo = "3030303032383838";
		System.out.println(hexString2String(productNo));
		productNo = "04050103000001070302050304";
		System.out.println(hexString2String(productNo));
	}

	// 用Java语言实现对十六进制字符串异或运算http://blog.csdn.net/acrambler/article/details/45743157
	// CRC校验
	public int crcget(int stp, byte data[], int length) {
		int j = stp;
		int i = 0;
		int crc = 0xffff;
		while (length > 0) {
			length--;
			crc ^= (data[j] & 0xff);
			j++;
			for (i = 0; i < 8; ++i) {
				if ((crc & 1) != 0)
					crc = (crc >> 1) ^ 0xa001;
				else
					crc = (crc >> 1);
			}
		}
		return crc;
	}

	public byte[] listTobyte(List<Byte> list) {
		if (list == null || list.size() < 0)
			return null;
		byte[] bytes = new byte[list.size()];
		int i = 0;
		Iterator<Byte> iterator = list.iterator();
		while (iterator.hasNext()) {
			bytes[i] = iterator.next();
			i++;
		}
		return bytes;
	}

	/**
	 * 去除字符串str1中的str2
	 *
	 * @param str1 原字符串
	 * @param str2 去掉的字符串
	 * @return
	 */
	public String getSubString(String str1, String str2) {
		StringBuffer sb = new StringBuffer(str1);
		while (true) {
			int index = sb.indexOf(str2);
			if (index == -1) {
				break;
			}
			sb.delete(index, index + str2.length());
		}
		return sb.toString();
	}

}
