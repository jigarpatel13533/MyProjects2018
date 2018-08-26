package com.nordea.ifactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;



public class RulesEngine
{
  public RulesEngine() {}
  
  public static void applyRules(Collection<File> sourceFiles, XSSFWorkbook rulesBook, File report, String output, boolean printErrors, boolean printWarings, boolean printInfo)
    throws IOException
  {
    Map<String, List<Map<String, String>>> rules = parseRuleBook(rulesBook);
    StringBuilder html = ReportPrinter.startHtmlOutput();
    
    for (File file : sourceFiles) {
    	output = output + " Validating " + file.getName() + "\n";
      Set<String> errorMsgs = new HashSet();
      
      String code = FileUtils.readFileToString(file).trim();
      Matcher m = Pattern.compile("(?s)/\\*.*?\\*/").matcher(code);
      
      while (m.find()) {
        code = code.replace(m.group(), "");
      }
      
      List<Map<String, String>> ruleList = (List)rules.get(
        FilenameUtils.getExtension(file.getName()).toUpperCase());
      
      for (Map<String, String> rule : ruleList) {
        applyRule(code, rule, errorMsgs);
      }
      
      ReportPrinter.writeErrors(file, html, errorMsgs, printErrors, printWarings, printInfo);
    }
    ReportPrinter.endHtmlOutput(report, html);
  }
  
  private static Map<String, List<Map<String, String>>> parseRuleBook(XSSFWorkbook rulesBook)
  {
    Map<String, List<Map<String, String>>> rules = new HashMap();
    XSSFSheet ruleSheet = rulesBook.getSheetAt(0);
    for (Row rule : ruleSheet) {
      if (rule.getRowNum() != 0) {
        if (rules.containsKey(rule.getCell(1).getStringCellValue().toUpperCase())) {
          ((List)rules.get(rule.getCell(1).getStringCellValue().toUpperCase())).add(parseRule(rule));
        } else {
          List<Map<String, String>> ruleInfoList = new ArrayList();
          ruleInfoList.add(parseRule(rule));
          rules.put(rule.getCell(1).getStringCellValue().toUpperCase(), ruleInfoList);
        }
      }
    }
    return rules;
  }
  
  private static void applyRule(String sourceCode, Map<String, String> rule, Set<String> errorMsgs)
    throws IOException
  {
    boolean isLookUpOkay = true;
    

    for (String artifcat : rule.keySet()) {
      if (artifcat.contains("LookFor"))
      {
        if (!isContains(sourceCode, (String)rule.get(artifcat)))
        {
          errorMsgs.add("INFO:" + 
            (String)rule.get("Rule") + 
            ": rule can't be applied becaue of source doesn't conatains " + 
            artifcat + "[" + 
            (String)rule.get(artifcat) + "]");
          isLookUpOkay = false;
        }
      }
    }
    
    if (isLookUpOkay)
    {
      String stringToBeVerified = (String)rule.get("Existence");
      String check = (String)rule.get("Should Exists");
      if ((check == null) || (stringToBeVerified == null))
      {
        errorMsgs.add("INFO:" + 
          (String)rule.get("Rule") + 
          ":rule can't be applied, check for 'Should Exists,Existence'");
      } else if ((!isContains(sourceCode, stringToBeVerified)) || (!check.equals("Y")))
      {
        if ((!isContains(sourceCode, stringToBeVerified)) && (check.equals("Y"))) {
          errorMsgs.add(((String)rule.get("Recommendation"))
            .replace("<RULE>", (CharSequence)rule.get("Rule"))
            .replace("<F>", (CharSequence)rule.get("Existence")));
        } else if ((isContains(sourceCode, stringToBeVerified)) && (check.equals("N"))) {
          errorMsgs.add(((String)rule.get("Recommendation"))
            .replace("<RULE>", (CharSequence)rule.get("Rule"))
            .replace("<F>", (CharSequence)rule.get("Existence")));
        } else if ((isContains(sourceCode, stringToBeVerified)) || (!check.equals("N")))
        {


          errorMsgs.add("INFO:I don't understand the rule condition 'Existence[" + 
            stringToBeVerified + 
            "]' with Should Exists[" + 
            check + "]");
        }
      }
    }
    
    if (!sourceCode.endsWith(";")) {
      errorMsgs.add("End String ';' is missing");
    }
  }
  
  
  private static Map<String, String> parseRule(Row rule)
  {
    Map<String, String> artifacts = new HashMap();
    for (Iterator<Cell> iterator = rule.cellIterator(); iterator.hasNext();) {
      Cell c = (Cell)iterator.next();
      String name = c.getSheet().getRow(0).getCell(c.getColumnIndex()).getStringCellValue();
      if ((name != null) && (name.contains("LookFor"))) {
        if ((c.getStringCellValue() != null) && 
          (StringUtils.isNotEmpty(c.getStringCellValue()))) {
          artifacts.put(name, c.getStringCellValue().trim());
        }
      } else {
        artifacts.put(name, c.getStringCellValue().trim());
      }
    }
    return artifacts;
  }
  
  public static boolean isContains(String source, String wordToBeSearched) {
    if ((wordToBeSearched.contains(".")) || (wordToBeSearched.contains("_")))
      return source.contains(wordToBeSearched);
    return Pattern.compile("\\b" + wordToBeSearched.toUpperCase() + "\\b").matcher(source).find();
  }
}
