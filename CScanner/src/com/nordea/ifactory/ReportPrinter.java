package com.nordea.ifactory;

import java.io.BufferedWriter;
import java.io.File;

public class ReportPrinter
{
  public ReportPrinter() {}
  
  public static StringBuilder startHtmlOutput()
  {
    StringBuilder htmlBuilder = new StringBuilder();
    htmlBuilder.append("<html>");
    htmlBuilder.append("<head>");
    htmlBuilder.append("<title>COMPLIANCE REPORT");
    htmlBuilder.append("</title>");
    htmlBuilder.append("</head>");
    htmlBuilder.append("<body BGCOLOR=#E5EAEF>");
    htmlBuilder.append("<TABLE BORDER=1 CELLPADDING=0 CELLSPACING=0 ALIGN=CENTER>");
    htmlBuilder.append("<THEAD>");
    
    htmlBuilder.append("<TR><TH  colspan=2> COMPLIANCE REPORT </TH></TR>");
    
    htmlBuilder.append("<TR><TH>FILE NAME</TH>");
    htmlBuilder.append("<TH>OBSERVATIONS</TH></TR>");
    htmlBuilder.append("</THEAD>");
    return htmlBuilder;
  }
  
  public static void endHtmlOutput(File file, StringBuilder htmlBuilder) throws java.io.IOException {
    htmlBuilder.append("</TABLE>");
    htmlBuilder.append("</body>");
    htmlBuilder.append("</html>");
    BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(file.getAbsolutePath(), true));
    writer.write(htmlBuilder.toString());
    writer.close();
  }
  

  public static void writeErrors(File file, StringBuilder htmlBuilder, java.util.Set<String> errors, boolean printErrors, boolean printWarings, boolean printInfo)
    throws java.io.IOException
  {
    htmlBuilder.append("<TR>");
    htmlBuilder.append("<td> <a href='" + file.getAbsolutePath() + "'>" + file.getName() + "</a></td>");
    htmlBuilder.append("<TD>");
    int i = 1;
    boolean noMsgFlag = true;
    for (String error : errors) {
      if (error.startsWith("INFO")) {
        if (printInfo) {
          htmlBuilder.append(i + ". " + error + "<br>");
          i++;
          noMsgFlag = false;
        }
      } else if (error.startsWith("Warning")) {
        if (printWarings) {
          htmlBuilder.append(i + "." + error + "<br>");
          i++;
          noMsgFlag = false;
        }
      } else if (error.startsWith("Error")) {
        if (printErrors) {
          htmlBuilder.append(i + ".<font color='red'> " + error + "</font><br>");
          i++;
          noMsgFlag = false;
        }
      } else {
        htmlBuilder.append(i + ". " + error + "<br>");
        i++;
        noMsgFlag = false;
      }
    }
    if(noMsgFlag)
    {
    	htmlBuilder.append("<font color='gray'> Good Code </font><br>");
    }
    
    htmlBuilder.append("</TD>");
    htmlBuilder.append("</TR>");
  }
}
