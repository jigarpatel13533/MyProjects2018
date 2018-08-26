package com.nordea.ifactory;
import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CScanner {
	static String[] fileExtensions = { "tbl", "TBL", "sql", "SQL", "vw", "VW" };
	static SimpleDateFormat format = new SimpleDateFormat("DD-MM-YYYY_HH_mm_ss");

	static File srcFile = new File("D:/CodeScannerFiles/BuildPackage");
	static File ruleFile = new File("D:/CodeScannerFiles/Rulebook/Rules_verification.xlsx");

	File report = null;
	JFrame frame = null;
	boolean printErrors = true;
	boolean printWarings = true;
	boolean printInfo = false;

	XSSFWorkbook rulesBook = null;
	Collection<File> sourceFiles = null;

	String output = "";

	public CScanner() {
		
		//Sneha code changes
		
		frame = new JFrame();

		try {

			if (ruleFile == null) {
				JOptionPane.showMessageDialog(frame, "Rule book not found at location ", "Rule book is required", 2,
						null);
				return;
			}

			if (srcFile == null) {
				JOptionPane.showMessageDialog(frame, "Please select source directory", "Source is required.", 2, null);
				return;
			}

			sourceFiles = FileUtils.listFiles(CScanner.srcFile, CScanner.fileExtensions, true);
			rulesBook = new XSSFWorkbook(new FileInputStream(CScanner.ruleFile));

			if ((sourceFiles == null) || (sourceFiles.isEmpty())) {
				JOptionPane.showMessageDialog(frame, "No source files found.", "Source is required.", 2, null);
				return;
			}

			output = output + "Total rules found " + rulesBook.getSheetAt(0).getLastRowNum() + "\n";
			output = output + "Total source files found " + sourceFiles.size() + "\n";
			output = output + "Validation started \n";
		
			report = new File("COMPLIANCE_REPORT" + CScanner.format.format(new Date()) + ".htm");
		
			RulesEngine.applyRules(sourceFiles, rulesBook, this.report, output, printErrors, printWarings, printInfo);
			output = output + "report created in following location " + report.getAbsolutePath() + "\n";
			output = output + "Validation completed";
		
			
			Desktop.getDesktop().browse(report.toURI());
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(frame, "Error occured while applying rules \n" + ex.getMessage(), "Error.", 0,null);
			report.delete();
			output = output+ "Error occured while applying rules, please check your rule book or contact techinal team";
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new CScanner();
	}
}
