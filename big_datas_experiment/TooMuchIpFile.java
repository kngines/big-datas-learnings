package com.kngines.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.kngines.utils.DateTimeUtils;


	 
/**
 * ������־������ȡĳ�շ��ʰٶȴ��������Ǹ�IP��Javaʵ��
 * 
 * 0ʱ 0��50��
 * 1678174096148 -- 1678174138619
 * 0ʱ 0��42��
 * ======================���ִ�������IP==================
 * 10.34.7.236:18
 * 0ʱ 0�� 0��
 *  
 *  
 *  
 * @author kngin
 *
 */
public class TooMuchIpFile {

	public final Map<Integer, BufferedWriter> bwMap = new HashMap<Integer, BufferedWriter>();// ����ÿ���ļ���������
	public final Map<Integer, List<String>> dataMap = new HashMap<Integer, List<String>>();// �ָ��ļ���
	private Map<String, Integer> ipNumMap = new HashMap<String, Integer>();// ����ÿ���ļ��е�ÿ��IP���ֵĴ���
	private List<String> keyList = new LinkedList<String>();// ���������������IP
	private int ipMaxNum = 0;// ������������ֵ
	private long totalTime = 0;// ����ͳ�����ĵ�ʱ��
	private static String mockIPsFlPath = "E:/ipAddr.txt";// IPģ�������ļ�·��
	private static String ipFlSplitPath = "E:/tmp/";// IP�ļ����·��

	static {
		File file = new File(mockIPsFlPath);
		if (file.exists()) {
			file.delete();
		}
		
		File dir = new File(ipFlSplitPath);
		boolean bln = false;
		if (!dir.exists()) {
			bln = dir.mkdir();
		}
	}

	/**
	 * ������Ϊ1�ڸ�IP���ݣ����ɹ�����10.��ͷ��������0-255���������
	 * 
	 * @param ipFile
	 * @param numberOfLine
	 */
	public void gernBigFile(File ipFile, long numberOfLine) {
		BufferedWriter bw = null;
		FileWriter fw = null;
		long startTime = System.currentTimeMillis();
		try {
			fw = new FileWriter(ipFile, true);
			bw = new BufferedWriter(fw);

			SecureRandom random = new SecureRandom();
			for (int i = 0; i < numberOfLine; i++) {
				bw.write("10." + random.nextInt(255) + "." + random.nextInt(255) + "." + random.nextInt(255) + "\n");
				if ((i + 1) % 1000 == 0) {
					bw.flush();
				}
			}
			bw.flush();

			long endTime = System.currentTimeMillis();
			System.err.println(DateTimeUtils.convertMillsToTime(endTime - startTime));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fw != null) {
					fw.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * ���ļ��ָ�ΪС�ļ���ȡÿ��IP��hashCode����1000ȡģ����IPɢ�е���ͬ���ļ���ȥ��
	 * 
	 * @param ipFile
	 * @param numberOfFile
	 */
	public void splitFile4(File ipFile, int numberOfFile) {
		BufferedReader br = null;
		FileReader fr = null;
		BufferedWriter bw = null;
		FileWriter fw = null;
		long startTime = System.currentTimeMillis();
		try {
			fr = new FileReader(ipFile);
			br = new BufferedReader(fr);
			String ipLine = br.readLine();
			// �ȴ����ļ��������󷽱�ʹ��
			for (int i = 0; i < numberOfFile; i++) {
				File file = new File(ipFlSplitPath + i + ".txt");
				bwMap.put(i, new BufferedWriter(new FileWriter(file, true)));
				dataMap.put(i, new LinkedList<String>());
			}
			while (ipLine != null) {
				int hashCode = ipLine.hashCode();
				hashCode = hashCode < 0 ? -hashCode : hashCode;
				int fileNum = hashCode % numberOfFile;
				List<String> list = dataMap.get(fileNum);
				list.add(ipLine + "\n");
				if (list.size() % 1000 == 0) {
					BufferedWriter writer = bwMap.get(fileNum);
					for (String line : list) {
						writer.write(line);
					}
					writer.flush();
					list.clear();
				}
				ipLine = br.readLine();
			}
			for (int fn : bwMap.keySet()) {
				List<String> list = dataMap.get(fn);
				BufferedWriter writer = bwMap.get(fn);
				for (String line : list) {
					writer.write(line);
				}
				list.clear();
				writer.flush();
				writer.close();
			}
			bwMap.clear();
			long endTime = System.currentTimeMillis();
			System.out.println(startTime + " -- " + endTime);
			System.err.println(DateTimeUtils.convertMillsToTime(endTime - startTime));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fr != null) {
					fr.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (br != null) {
					br.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (fw != null) {
					fw.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * ͳ�ƣ��ҳ���������IP ������� Map<String, Integer> �������� key��Ӧ�� value�Ѿ����ڣ���ִ�� value
	 * �滻���������ؾɵ�valueֵ�� �����������ִ�в��룬���� null��
	 * ���õķ�����һ��ͳ�Ƹ���IP���ֵĴ�����һ���������������Ǹ�IP��
	 * 
	 * @param ipFile
	 */
	public void read(File ipFile) {
		BufferedReader br = null;
		FileReader fr = null;
		long startTime = System.currentTimeMillis();
		try {
			fr = new FileReader(ipFile);
			br = new BufferedReader(fr);
			String ipLine = br.readLine();
			while (ipLine != null) {
				ipLine = ipLine.trim();
				Integer count = ipNumMap.get(ipLine);
				if (count == null) {
					count = 0;
				}
				count++;
				ipNumMap.put(ipLine, count);

				if (count >= ipMaxNum) {
					if (count > ipMaxNum) {
						keyList.clear();
					}
					keyList.add(ipLine);
					ipMaxNum = count;
				}
				ipLine = br.readLine();
			}
			long endTime = System.currentTimeMillis();
			System.err.println(ipFile.getName() + ":" + DateTimeUtils.convertMillsToTime(endTime - startTime));
			totalTime += (endTime - startTime);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fr != null) {
					fr.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (br != null) {
					br.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {

		// 1����һ������1��(ʵ�������Ϊ16581375)��ip��ַ����Ҫʱ��Ϊ3�ֶ��Ӳ���4���ӡ�
		TooMuchIpFile tooMuchIpFile = new TooMuchIpFile();
		File ipFile = new File(mockIPsFlPath);
		try {
			ipFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		tooMuchIpFile.gernBigFile(ipFile, 100000000);
		tooMuchIpFile.splitFile4(ipFile, 1000);

		/*
		 * 1����1000���ļ��в�ѯIp��������Ip��10.164.143.57:24,3.0��18.748999999999995��
		 * 2����1000���ļ��в�ѯIp��������Ip��10.164.143.57:24,3.0��27.366000000000014��
		 * 3����1000���ļ��в�ѯIp��������Ip��10.164.143.57:24,2.0��42.781000000000006��
		 */

		File ipFiles = new File(ipFlSplitPath);
		for (File ipf : ipFiles.listFiles()) {
			tooMuchIpFile.read(ipf);
			tooMuchIpFile.ipNumMap.clear();

			System.out.println(ipf.getPath());
			break;
		}

		System.err.println("======================���ִ�������IP==================");
		for (String key : tooMuchIpFile.keyList) {
			System.err.println(key + ":" + tooMuchIpFile.ipMaxNum);
		}
		System.err.println(DateTimeUtils.convertMillsToTime(tooMuchIpFile.totalTime));

	}

}
