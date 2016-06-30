package com.jaspersoft.mongodb;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignQuery;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.apache.log4j.Logger;


import com.jaspersoft.mongodb.connection.MongoDbConnection;

/**
 * 
 * @author Eric Diaz
 * 
 */
public class ReportTest {
  private final static String PATH = "src/test/resources/";
  private final static Logger logger = Logger.getLogger(ReportTest.class);

  //private final static String[] REPORT_NAMES = { "MongoDbReport", "MongoDbConditionalOperators", "MongoDbDates",
     // "MongoDbGeospatial", "MongoDbRegEx", "MongoDbReport_AdHoc" };
  private final static String[] REPORT_NAMES = {"dynamictester"};
  
  public void test() {
    long mainStart = System.currentTimeMillis();
    String mongoURI = "mongodb://localhost:27017/test";
    MongoDbConnection connection = null;
    Map<String, Object> parameters = new HashMap<String, Object>();
    try {
    	JasperDesign jasper = JRXmlLoader.load(PATH + "dynamictester.jrxml");
		  JRDesignQuery query = new JRDesignQuery();
		  query.setText("{ collectionName : 'json1' }");
		  query.setLanguage("MongoDbQuery");
		  jasper.setQuery(query);
		  
      connection = new MongoDbConnection(mongoURI, null, null);
      parameters.put(MongoDbDataSource.CONNECTION, connection);
      File jasperFile;
      long start;
      for (String reportname : REPORT_NAMES) {
        jasperFile = new File(PATH + reportname + ".jasper");
        JasperReport report = JasperCompileManager.compileReport(jasper);
          //JasperCompileManager.compileReportToFile(PATH + reportname + ".jrxml", PATH + reportname + ".jasper");
           JasperCompileManager.writeReportToXmlFile(report, PATH + "dynamictester.jrxml");
           JasperCompileManager.compileReportToFile(PATH + reportname + ".jrxml", PATH + reportname + ".jasper");

        start = System.currentTimeMillis();
        JasperFillManager.fillReportToFile(PATH + reportname + ".jasper", parameters);
        System.err.println("Filling time : " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        JasperExportManager.exportReportToPdfFile(PATH + reportname + ".jrprint");
        JasperExportManager.exportReportToXmlFile(PATH + reportname + ".jrprint", PATH+"reports.xml", false);
        //JasperExportManager.exportReportToHtmlFile(PATH + "reports.html");
        System.err.println("PDF creation time : " + (System.currentTimeMillis() - start));
      }
    } catch (Exception e) {
      logger.error(e);
    } finally {
      if (connection != null) {
        connection.close();
      }
    }
    System.err.println("Total time : " + (System.currentTimeMillis() - mainStart));
  }
}