package potrix.compress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlCompressor {
	private static String tempPreBlock = "%%%HTMLCOMPRESS~PRE&&&";
	private static String tempTextAreaBlock = "%%%HTMLCOMPRESS~TEXTAREA&&&";
	private static String tempScriptBlock = "%%%HTMLCOMPRESS~SCRIPT&&&";
	private static String tempStyleBlock = "%%%HTMLCOMPRESS~STYLE&&&";
	private static String tempJspBlock = "%%%HTMLCOMPRESS~JSP&&&";

	private static Pattern commentPattern = Pattern.compile("<!--\\s*[^\\[].*?-->", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private static Pattern itsPattern = Pattern.compile(">\\s+?<", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private static Pattern prePattern = Pattern.compile("<pre[^>]*?>.*?</pre>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private static Pattern taPattern = Pattern.compile("<textarea[^>]*?>.*?</textarea>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private static Pattern jspPattern = Pattern.compile("<%([^-@][\\w\\W]*?)%>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	// <script></script>
	private static Pattern scriptPattern = Pattern.compile("(?:<script\\s*>|<script\\s*[^>]*>)(.*?)</script>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private static Pattern stylePattern = Pattern.compile("<style[^>()]*?>(.+)</style>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	// 单行注释，
	// private static Pattern signleCommentPattern = Pattern.compile("//.*");
	// 字符串匹配
	// private static Pattern stringPattern =
	// Pattern.compile("(\"[^\"\\n]*?\"|'[^'\\n]*?')");
	// trim去空格和换行符
	// private static Pattern trimPattern = Pattern.compile("\\n\\s*",
	// Pattern.MULTILINE);
	// private static Pattern trimPattern2 = Pattern.compile("\\s*\\r",
	// Pattern.MULTILINE);
	private static Pattern cssTrimPattern = Pattern.compile("\\s*([\\{\\}:=;,\\(\\)])\\s*", Pattern.MULTILINE);
	private static Pattern cssUrlPattern = Pattern.compile("url\\(.*?\\)", Pattern.MULTILINE);
	// 多行注释
	private static Pattern multiCommentPattern = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	// private static String tempSingleCommentBlock =
	// "%%%HTMLCOMPRESS~SINGLECOMMENT&&&"; // //占位符
	// private static String tempMulitCommentBlock1 =
	// "%%%HTMLCOMPRESS~MULITCOMMENT1&&&"; // 占位符
	// private static String tempMulitCommentBlock2 =
	// "%%%HTMLCOMPRESS~MULITCOMMENT2&&&";// 占位符

	private static Pattern htmlFilePattern = Pattern.compile(".*\\.html$"); // 判断文件是否是html文件的正则表达式
	private static Pattern jsFilePattern = Pattern.compile(".*\\.js$"); // 判断文件是否是js文件的正则表达式
	private static Pattern cssFilePattern = Pattern.compile(".*\\.css$"); // s判断文件是否是css文件的正则表达式

	/**
	 * 压缩JS单个文件
	 * 
	 * @param file
	 *            ： 文件对象，压缩之后会将源文件删除，并自动替换为新文件。
	 * @param encoding
	 *            ：文件字符编码
	 * 
	 **/
	public static void compressJsFile(File file, String encoding) {
		compressFile(file, encoding, new CompressMethod() {
			@Override
			public String compress(String source) {
				return HtmlCompressor.compressJspJs(source);
			}
		});
	}

	/**
	 * 压缩JS单个文件
	 * 
	 * @param fileName
	 *            ： 文件绝对路径，压缩之后会将源文件删除，并自动替换为新文件。
	 * @param encoding
	 *            ：文件字符编码
	 **/
	public static void compressJsFile(String fileName, String encoding) {
		compressJsFile(new File(fileName), encoding);
	}

	/**
	 * 压缩某个文件夹内的所有JS文件
	 * 
	 * @param file
	 *            ： 文件夹对象，压缩之后会将源文件删除，并自动替换为新文件。
	 * @param encoding
	 *            ：文件字符编码
	 * @param deep
	 *            ：是否对该文件夹内的子文件夹也进行递归操作
	 **/
	public static void compressJsFolder(File file, String encoding, boolean deep) {
		File[] files = file.listFiles();
		for (int i = 0, len = files.length; i < len; i++) {
			if (jsFilePattern.matcher(files[i].getName()).matches()) {
				compressJsFile(files[i], encoding);
			} else if (deep && files[i].isDirectory()) {
				HtmlCompressor.compressJsFolder(files[i], encoding, deep);
			}
		}
	}

	/**
	 * 压缩html单个文件
	 * 
	 * @param file
	 *            ： 文件对象，压缩之后会将源文件删除，并自动替换为新文件。
	 * @param encoding
	 *            ：文件字符编码
	 **/
	public static void compressHtmlFile(File file, String encoding) {
		compressFile(file, encoding, new CompressMethod() {
			@Override
			public String compress(String source) {
				try {
					return HtmlCompressor.compress(source);
				} catch (Exception e) {
					e.printStackTrace();
					return source;
				}
			}
		});
	}

	/**
	 * 压缩html单个文件
	 * 
	 * @param fileName
	 *            ： 文件绝对路径，压缩之后会将源文件删除，并自动替换为新文件。
	 * @param encoding
	 *            ：文件字符编码
	 **/
	public static void compressHtmlFile(String fileName, String encoding) {
		HtmlCompressor.compressHtmlFile(new File(fileName), encoding);
	}

	/**
	 * 压缩某个文件夹内的所有HTML文件
	 * 
	 * @param file
	 *            ： 文件夹对象，压缩之后会将源文件删除，并自动替换为新文件。
	 * @param encoding
	 *            ：文件字符编码
	 * @param deep
	 *            ：是否对该文件夹内的子文件夹也进行递归操作
	 **/
	public static void compressHtmlFolder(File file, String encoding, boolean deep) {
		File[] files = file.listFiles();
		for (int i = 0, len = files.length; i < len; i++) {
			if (htmlFilePattern.matcher(files[i].getName()).matches()) {
				compressHtmlFile(files[i], encoding);
			} else if (deep && files[i].isDirectory()) {
				HtmlCompressor.compressHtmlFolder(files[i], encoding, deep);
			}
		}
	}

	/**
	 * 压缩某个文件夹内的所有HTML和JS文件
	 * 
	 * @param file
	 *            ： 文件夹对象，压缩之后会将源文件删除，并自动替换为新文件。
	 * @param encoding
	 *            ：文件字符编码
	 * @param deep
	 *            ：是否对该文件夹内的子文件夹也进行递归操作
	 **/
	public static void compressHtmlAndJsFolder(File file, String encoding, boolean deep) {
		File[] files = file.listFiles();
		for (int i = 0, len = files.length; i < len; i++) {
			String fileName = files[i].getName();
			if (htmlFilePattern.matcher(fileName).matches()) {
				compressHtmlFile(files[i], encoding);
			} else if (jsFilePattern.matcher(fileName).matches()) {
				compressJsFile(files[i], encoding);
			} else if (deep && files[i].isDirectory()) {
				HtmlCompressor.compressHtmlAndJsFolder(files[i], encoding, deep);
			}
		}
	}

	/**
	 * 压缩单个css文件
	 * 
	 * @param file
	 *            ： 文件夹对象，压缩之后会将源文件删除，并自动替换为新文件。
	 * @param encoding
	 *            ：文件字符编码
	 **/
	public static void compressCssFile(File file, String encoding) {
		compressFile(file, encoding, new CompressMethod() {
			@Override
			public String compress(String source) {
				return HtmlCompressor.compressCssStyles(source);
			}
		});
	}

	/**
	 * 压缩单个css文件
	 * 
	 * @param fileName
	 *            ： 文件路径。
	 * @param encoding
	 *            ：文件字符编码
	 **/
	public static void compressCssFile(String fileName, String encoding) {
		compressCssFile(new File(fileName), encoding);
	}

	/**
	 * 压缩某个文件夹内的所有CSS文件
	 * 
	 * @param file
	 *            ： 文件夹对象，压缩之后会将源文件删除，并自动替换为新文件。
	 * @param encoding
	 *            ：文件字符编码
	 * @param deep
	 *            ：是否对该文件夹内的子文件夹也进行递归操作
	 **/
	public static void compressCssFolder(File file, String encoding, boolean deep) {
		File[] files = file.listFiles();
		for (int i = 0, len = files.length; i < len; i++) {
			String fileName = files[i].getName();
			if (cssFilePattern.matcher(fileName).matches()) {
				compressCssFile(files[i], encoding);
			} else if (deep && files[i].isDirectory()) {
				HtmlCompressor.compressCssFolder(files[i], encoding, deep);
			}
		}
	}

	/**
	 * 压缩某个文件夹内的所有HTML，JS和CSS文件
	 * 
	 * @param file
	 *            ： 文件夹对象，压缩之后会将源文件删除，并自动替换为新文件。
	 * @param encoding
	 *            ：文件字符编码
	 * @param deep
	 *            ：是否对该文件夹内的子文件夹也进行递归操作
	 **/
	public static void compressHtmlJsCssFolder(File file, String encoding, boolean deep) {
		File[] files = file.listFiles();
		if (files == null) {
			return;
		}
		for (int i = 0, len = files.length; i < len; i++) {
			String fileName = files[i].getName();
			if (htmlFilePattern.matcher(fileName).matches()) {
				compressHtmlFile(files[i], encoding);
			} else if (jsFilePattern.matcher(fileName).matches()) {
				compressJsFile(files[i], encoding);
			} else if (cssFilePattern.matcher(fileName).matches()) {
				compressCssFile(files[i], encoding);
			} else if (deep && files[i].isDirectory()) {
				HtmlCompressor.compressHtmlJsCssFolder(files[i], encoding, deep);
			}
		}
	}

	/**
	 * 该接口实现压缩方法
	 */
	public interface CompressMethod {

		public String compress(String source);

	}

	// 用来记录当前不能被压缩的JS文件或者HTML文件中的JS代码，编译出现错误导致不能压缩。
	private static ArrayList<String> canNotCompressJsFileList = new ArrayList<String>();
	private static String curCompressFileName = null;

	public static void showCanNotCompressJsFile() {
		System.out.println("----编译错误导致不能正常压缩的JS文件有如下：----------------------------------");
		for (int i = 0, len = canNotCompressJsFileList.size(); i < len; i++) {
			System.out.println(canNotCompressJsFileList.get(i));
		}
		System.out.println("------------------------------------------------------------------");

	}

	/**
	 * 通过CompressMethod接口指定的压缩方法来压缩某个文件，压缩之后新的文件会替换掉源文件
	 * 
	 * @param file
	 *            ： 文件夹对象，压缩之后会将源文件删除，并自动替换为新文件。
	 * @param encoding
	 *            ：文件字符编码
	 * @param compressMethod
	 *            ：指定压缩的方法
	 **/
	public static void compressFile(File file, String encoding, CompressMethod compressMethod) {
		FileInputStream fis = null;
		FileOutputStream fops = null;
		try {
			fis = new FileInputStream(file);
			byte[] bs = new byte[fis.available()];
			fis.read(bs);

			curCompressFileName = file.getPath();

			String result = compressMethod.compress(new String(bs, encoding));

			fops = new FileOutputStream(file);
			fops.write(result.getBytes(encoding));
			fops.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fops != null) {
				try {
					fops.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * 将文件夹下的文件全部压缩并写入该文件夹下的toFileName，并是否删掉源文件
	 * 
	 * @param path
	 * @param toFileName
	 * @param deleteSource
	 * @return
	 */
	public static void mergeFiles(String path, String[] fileNames, final String toFileName, boolean deleteSource) {

		if (path == null || fileNames == null || fileNames.length < 1 || toFileName == null) {
			System.out.println("参数均不能为null");
			return;
		}

		int len = fileNames.length;
		File[] files = new File[len];
		for (int i = 0; i < len; i++) {
			files[i] = new File(path + File.separator + fileNames[i]);
		}

		byte[] bs = new byte[1024 * 10];
		int n = 0;
		FileInputStream fis = null;
		FileOutputStream fops = null;
		try {
			fops = new FileOutputStream(path + File.separator + toFileName);
			for (int i = 0; i < len; i++) {
				fis = new FileInputStream(files[i]);
				while ((n = fis.read(bs)) != -1) {
					fops.write(bs, 0, n);
				}
				fops.write("\n".getBytes());
				try {
					if (fis != null) {
						fis.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (deleteSource) {
					files[i].delete();
				}
			}
			fops.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fops != null) {
					fops.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static String compress(String html) throws Exception {
		if (html == null || html.length() == 0) {
			return html;
		}

		List<String> preBlocks = new ArrayList<String>();
		List<String> taBlocks = new ArrayList<String>();
		List<String> scriptBlocks = new ArrayList<String>();
		List<String> styleBlocks = new ArrayList<String>();
		List<String> jspBlocks = new ArrayList<String>();

		String result = html;

		// preserve inline java code
		Matcher jspMatcher = jspPattern.matcher(result);
		while (jspMatcher.find()) {
			jspBlocks.add(jspMatcher.group(0));
		}
		result = jspMatcher.replaceAll(tempJspBlock);

		// preserve PRE tags
		Matcher preMatcher = prePattern.matcher(result);
		while (preMatcher.find()) {
			preBlocks.add(preMatcher.group(0));
		}
		result = preMatcher.replaceAll(tempPreBlock);

		// preserve TEXTAREA tags
		Matcher taMatcher = taPattern.matcher(result);
		while (taMatcher.find()) {
			taBlocks.add(taMatcher.group(0));
		}
		result = taMatcher.replaceAll(tempTextAreaBlock);

		// preserve SCRIPT tags
		Matcher scriptMatcher = scriptPattern.matcher(result);
		while (scriptMatcher.find()) {
			scriptBlocks.add(scriptMatcher.group(0));
		}
		result = scriptMatcher.replaceAll(tempScriptBlock);

		// don't process inline css
		Matcher styleMatcher = stylePattern.matcher(result);
		while (styleMatcher.find()) {
			styleBlocks.add(styleMatcher.group(0));
		}
		result = styleMatcher.replaceAll(tempStyleBlock);

		// process pure html
		result = processHtml(result);

		// process preserved blocks
		result = processPreBlocks(result, preBlocks);
		result = processTextareaBlocks(result, taBlocks);
		result = processScriptBlocks(result, scriptBlocks);
		result = processStyleBlocks(result, styleBlocks);
		result = processJspBlocks(result, jspBlocks);

		preBlocks = taBlocks = scriptBlocks = styleBlocks = jspBlocks = null;

		return result.trim();
	}

	private static String processHtml(String html) {
		String result = html;

		// remove comments
		// if(removeComments) {
		result = commentPattern.matcher(result).replaceAll("");
		// }

		// remove inter-tag spaces
		// if(removeIntertagSpaces) {
		result = itsPattern.matcher(result).replaceAll("><");
		// }

		// remove multi whitespace characters
		// if(removeMultiSpaces) {
		result = result.replaceAll("\\s{2,}", " ");
		// }

		return result;
	}

	private static String processJspBlocks(String html, List<String> blocks) {
		String result = html;
		for (int i = 0; i < blocks.size(); i++) {
			blocks.set(i, compressJsp(blocks.get(i)));
		}
		// put preserved blocks back
		while (result.contains(tempJspBlock)) {
			result = result.replaceFirst(tempJspBlock, Matcher.quoteReplacement(blocks.remove(0)));
		}

		return result;
	}

	private static String processPreBlocks(String html, List<String> blocks) throws Exception {
		String result = html;

		// put preserved blocks back
		while (result.contains(tempPreBlock)) {
			result = result.replaceFirst(tempPreBlock, Matcher.quoteReplacement(blocks.remove(0)));
		}

		return result;
	}

	private static String processTextareaBlocks(String html, List<String> blocks) throws Exception {
		String result = html;

		// put preserved blocks back
		while (result.contains(tempTextAreaBlock)) {
			result = result.replaceFirst(tempTextAreaBlock, Matcher.quoteReplacement(blocks.remove(0)));
		}

		return result;
	}

	private static String processScriptBlocks(String html, List<String> blocks) throws Exception {
		String result = html;

		// if(compressJavaScript) {
		for (int i = 0; i < blocks.size(); i++) {
			blocks.set(i, compressJavaScript(blocks.get(i)));
		}
		// }

		// put preserved blocks back
		while (result.contains(tempScriptBlock)) {
			result = result.replaceFirst(tempScriptBlock, Matcher.quoteReplacement(blocks.remove(0)));
		}

		return result;
	}

	private static String processStyleBlocks(String html, List<String> blocks) throws Exception {
		String result = html;

		// if(compressCss) {
		for (int i = 0; i < blocks.size(); i++) {
			blocks.set(i, compressCssStyles(blocks.get(i)));
		}
		// }

		// put preserved blocks back
		while (result.contains(tempStyleBlock)) {
			result = result.replaceFirst(tempStyleBlock, Matcher.quoteReplacement(blocks.remove(0)));
		}

		return result;
	}

	private static String compressJsp(String source) {
		// check if block is not empty
		Matcher jspMatcher = jspPattern.matcher(source);
		if (jspMatcher.find()) {
			String result = compressJspJs(jspMatcher.group(1));
			return (new StringBuilder(source.substring(0, jspMatcher.start(1))).append(result).append(source.substring(jspMatcher.end(1)))).toString();
		} else {
			return source;
		}
	}

	private static String compressJavaScript(String source) {
		// check if block is not empty
		Matcher scriptMatcher = scriptPattern.matcher(source);
		if (scriptMatcher.find()) {
			String result = compressJspJs(scriptMatcher.group(1));
			return (new StringBuilder(source.substring(0, scriptMatcher.start(1))).append(result).append(source.substring(scriptMatcher.end(1)))).toString();
		} else {
			return source;
		}
	}

	private static String compressCssStyles(String source) {
		String result = multiCommentPattern.matcher(source).replaceAll("");
		Matcher m = cssTrimPattern.matcher(result);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, m.group(1));
		}
		m.appendTail(sb);

		m = cssUrlPattern.matcher(sb.toString().trim());
		StringBuffer returnSb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(returnSb, m.group() + " ");
		}
		m.appendTail(returnSb);

		return returnSb.toString();
	}

	private static Pattern spacePattern = Pattern.compile("^\\s*$");

	private static String compressJspJs(String source) {
		// String result = source;
		// 因注释符合有可能出现在字符串中，所以要先把字符串中的特殊符好去掉
		// Matcher stringMatcher = stringPattern.matcher(result);
		// while (stringMatcher.find()) {
		// String tmpStr = stringMatcher.group(0);
		//
		// if (tmpStr.indexOf("//") != -1 || tmpStr.indexOf("") != -1) {
		// String blockStr = tmpStr.replaceAll("//",
		// tempSingleCommentBlock).replaceAll("/\\*",
		// tempMulitCommentBlock1).replaceAll("\\*/", tempMulitCommentBlock2);
		// result = result.replace(tmpStr, blockStr);
		// }
		// }
		// 去掉注释
		// result = signleCommentPattern.matcher(result).replaceAll("");
		// result = multiCommentPattern.matcher(result).replaceAll("");
		// result = trimPattern2.matcher(result).replaceAll("");
		// result = trimPattern.matcher(result).replaceAll("");
		// 恢复替换掉的字符串
		// result = result.replaceAll(tempSingleCommentBlock,
		// "//").replaceAll(tempMulitCommentBlock1, "");

		if (spacePattern.matcher(source).matches()) {
			return source;
		}

		JsCompressor jc = new JsCompressor();
		String returnStr = jc.compressJsCode(source);

		if (returnStr.length() < 1) {
			canNotCompressJsFileList.add(curCompressFileName);
			return source;
		} else {
			return returnStr;
		}

	}
}