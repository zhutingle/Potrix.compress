package potrix.compress;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSSourceFile;
import com.google.javascript.jscomp.SourceFile;

@SuppressWarnings("deprecation")
public class JsCompressor {

	private static CompilerOptions options = null;

	private static CompilationLevel level = null;

	/**
	 * 构造函数
	 */
	public JsCompressor() {
		if (options == null && level == null) {
			options = new CompilerOptions();
			level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
			level.setOptionsForCompilationLevel(options);
		}
	}

	/**
	 * 压缩JS文件
	 * 
	 * @param file
	 * @return
	 */
	public String compressJsFile(File file) {
		Compiler compiler = new Compiler();
		compiler.compile(SourceFile.fromCode("extern", ""), SourceFile.fromFile(file), options);
		return compiler.toSource();
	}

	/**
	 * 压缩JS文件
	 * 
	 * @param fileName
	 * @return
	 */
	public String compressJsFile(String fileName) {
		return compressJsFile(new File(fileName));
	}

	/**
	 * 压缩JS代码
	 * 
	 * @param code
	 * @return
	 */
	public String compressJsCode(String code) {
		Compiler compiler = new Compiler();
		compiler.compile(SourceFile.fromCode("extern", ""), SourceFile.fromCode("input", code), options);
		return compiler.toSource();
	}

	/**
	 * 将文件夹下的文件全部压缩并写入该文件夹下的toFileName，并是否删掉源文件
	 * 
	 * @param path
	 * @param toFileName
	 * @param deleteSource
	 * @return
	 */
	public void compressJsFolder(String path, String[] fileNames, final String toFileName, boolean deleteSource) {

		if (path == null || fileNames == null || fileNames.length < 1 || toFileName == null) {
			System.out.println("参数均不能为null");
			return;
		}

		int len = fileNames.length;
		File[] files = new File[len];
		for (int i = 0; i < len; i++) {
			files[i] = new File(path + "\\" + fileNames[i]);
		}

		String result = compressJsFolder(files, deleteSource);

		FileOutputStream fops = null;
		try {
			fops = new FileOutputStream(path + "\\" + toFileName);
			fops.write(result.getBytes());
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

	/**
	 * 压缩一堆JS文件
	 * 
	 * @param files
	 * @param deleteSource
	 * @return
	 */
	private String compressJsFolder(File[] files, boolean deleteSource) {
		int len = files.length;
		JSSourceFile[] sourceFiles = new JSSourceFile[len];
		for (int i = 0; i < len; i++) {
			sourceFiles[i] = JSSourceFile.fromFile(files[i]);
		}
		Compiler compiler = new Compiler();
		compiler.compile(SourceFile.fromCode("extern", ""), sourceFiles, options);

		if (deleteSource) {
			for (int i = 0; i < len; i++) {
				files[i].delete();
			}
		}

		return compiler.toSource();
	}

}
