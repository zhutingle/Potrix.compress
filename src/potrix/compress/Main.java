package potrix.compress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Properties;

public class Main {

	public static void main(String[] args) {

		System.out.println(ENCODEING);

		int i = 1;
		String step = getConfig("step1", null);

		while (step != null) {

			String[] stepParams = step.split(":");
			for (String str : stepParams) {
				System.out.print(str + " ");
			}
			System.out.println();

			try {
				if (stepParams.length == 1) {
					Method method = Main.class.getMethod(stepParams[0]);
					method.invoke(null);
				} else if (stepParams.length == 2) {
					Method method = Main.class.getMethod(stepParams[0], String.class);
					method.invoke(null, stepParams[1]);
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

			step = getConfig("step" + (++i), null);
		}

		// HtmlCompressor.compressHtmlJsCssFolder(new File(path), encoding, true);
		//
		// HtmlCompressor.showCanNotCompressJsFile();
		//
		// String toAllCssFileName = "all.css";
		// boolean deleteResource = true;
		// String cssPath = path + "kui-base\\themes\\";
		// String[] cssFiles = { "accordion.css", "autocomplete.css", "calendar.css", "combo.css", "combobox.css", "datagrid.css", "datebox.css", "dialog.css", "fieldset.css", "flow.css", "form.css", "layout.css", "linkbutton.css", "menu.css", "menubutton.css", "message.css", "messager.css", "obviousbox.css", "pagination.css", "panel.css", "portal.css", "progressbar.css", "propertygrid.css", "pushmsg.css", "report.css", "searchbox.css", "sform.css", "sibar.css", "slider.css", "spinner.css", "splitbutton.css", "tabs.css", "tree.css", "uploadify.css", "validatebox.css", "window.css", "keyboard.css", "colorpicker.css" };
		// String[] themeStrings = { "default", "black", "blue", "gray", "metro", "trans" };
		// for (int i = 0; i < themeStrings.length; i++) {
		// HtmlCompressor.mergeFiles(cssPath + themeStrings[i], cssFiles, toAllCssFileName, deleteResource);
		// }
		//
		// String loaderJsFolder = path + "kui-base\\js\\core\\";
		// String[] coreJsFiles = { "jquery.frame.checking.js", "jquery.plugins.min.js", "jquery.parser.js", "kui.view.src.js", "des.min.js", "kui.core.src.js", "kui.datacache.js", "kui.loader.src.js", "../comp_src/jquery.localStorage.js", "../comp_src/jquery.draggable.js", "../comp_src/jquery.resizable.js", "../comp_src/jquery.panel.js", "../comp_src/jquery.linkbutton.js", "../comp_src/jquery.window.js", "../comp_src/jquery.progressbar.js", "../comp_src/jquery.message.js" };
		// String toAllJsFileName = "kui.loader.js";
		// HtmlCompressor.mergeFiles(loaderJsFolder, coreJsFiles, toAllJsFileName, false);

	}

	public final static void compressHtmlJsCssFolderRecursion(String filePath) {
		HtmlCompressor.compressHtmlJsCssFolder(new File(ROOT_PATH + filePath), ENCODEING, true);
		HtmlCompressor.showCanNotCompressJsFile();
	}

	public final static void compressHtmlJsCssFolder(String filePath) {
		HtmlCompressor.compressHtmlJsCssFolder(new File(ROOT_PATH + filePath), ENCODEING, false);
		HtmlCompressor.showCanNotCompressJsFile();
	}

	public final static void mergeFile(String paramStr) {
		String[] params = paramStr.split("\\|");
		HtmlCompressor.mergeFiles(params[0], params[1].split(","), params[2], false);
	}

	private final static String getPath() {

		String className = Main.class.getName();
		String classNamePath = className.replace(".", "/") + ".class";
		URL is = Main.class.getClassLoader().getResource(classNamePath);
		String path = "";
		try {
			path = URLDecoder.decode(is.getFile().replace("file:", ":"), "UTF-8");
			path = path.replaceAll("%20", " ").replace("!/" + classNamePath, "");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		String rootPath = "";
		if ("\\".equals(File.separator)) {
			rootPath = path.substring(1, path.length());
			rootPath = rootPath.replace("/", "\\") + "\\";
		} else if ("/".equals(File.separator)) {
			rootPath = path.substring(1, path.length());
			rootPath = rootPath.replace("\\", "/") + "/";
		}

		return rootPath;
	}

	private final static String PATH = getPath();

	private final static String ROOT_PATH = PATH + ".." + File.separator + ".." + File.separator;

	private static Properties properties = null;

	private final static String getConfig(String key, String defaultValue) {
		if (properties == null) {
			try {
				FileInputStream in = new FileInputStream(PATH + ".." + File.separator + "compress.properties");
				properties = new Properties();
				properties.load(in);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String value = properties.getProperty(key);
		if (value == null) {
			return defaultValue;
		} else {
			return value;
		}
	}

	private final static String ENCODEING = getConfig("encoding", "UTF-8");

}
