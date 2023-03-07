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
 * 海量日志数据提取某日访问百度次数最多的那个IP的Java实现
 * 
 * 0时 0分50秒
 * 1678174096148 -- 1678174138619
 * 0时 0分42秒
 * ======================出现次数最多的IP==================
 * 10.34.7.236:18
 * 0时 0分 0秒
 *  
 *  
 *  
 * @author kngin
 *
 */
public class TooMuchIpFile {

	public final Map<Integer, BufferedWriter> bwMap = new HashMap<Integer, BufferedWriter>();// 保存每个文件的流对象
	public final Map<Integer, List<String>> dataMap = new HashMap<Integer, List<String>>();// 分隔文件用
	private Map<String, Integer> ipNumMap = new HashMap<String, Integer>();// 保存每个文件中的每个IP出现的次数
	private List<String> keyList = new LinkedList<String>();// 保存次数出现最多的IP
	private int ipMaxNum = 0;// 次数出现最多的值
	private long totalTime = 0;// 计算统计所耗的时间
	private static String mockIPsFlPath = "E:/ipAddr.txt";// IP模拟数据文件路径
	private static String ipFlSplitPath = "E:/tmp/";// IP文件拆分路径

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
	 * 总数据为1亿个IP数据，生成规则：以10.开头，其他是0-255的随机数。
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
	 * 大文件分割为小文件，取每个IP的hashCode，与1000取模，把IP散列到不同的文件中去。
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
			// 先创建文件及流对象方便使用
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
	 * 统计，找出次数最多的IP 计算分析 Map<String, Integer> 如果插入的 key对应的 value已经存在，则执行 value
	 * 替换操作，返回旧的value值， 如果不存在则执行插入，返回 null。
	 * 采用的方法是一边统计各个IP出现的次数，一边算次数出现最大那个IP。
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

		// 1、第一次生成1亿(实际上最多为16581375)的ip地址，需要时间为3分多钟不到4分钟。
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
		 * 1、从1000个文件中查询Ip次数最多的Ip，10.164.143.57:24,3.0分18.748999999999995秒
		 * 2、从1000个文件中查询Ip次数最多的Ip，10.164.143.57:24,3.0分27.366000000000014秒
		 * 3、从1000个文件中查询Ip次数最多的Ip，10.164.143.57:24,2.0分42.781000000000006秒
		 */

		File ipFiles = new File(ipFlSplitPath);
		for (File ipf : ipFiles.listFiles()) {
			tooMuchIpFile.read(ipf);
			tooMuchIpFile.ipNumMap.clear();

			System.out.println(ipf.getPath());
			break;
		}

		System.err.println("======================出现次数最多的IP==================");
		for (String key : tooMuchIpFile.keyList) {
			System.err.println(key + ":" + tooMuchIpFile.ipMaxNum);
		}
		System.err.println(DateTimeUtils.convertMillsToTime(tooMuchIpFile.totalTime));

	}

}
