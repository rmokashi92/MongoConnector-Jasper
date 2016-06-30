package mypack;

import net.sf.jasperreports.engine.design.JRDesignQuery;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

public class Sampler {
	 private final static String PATH = "src/test/resources/";

	  public static void main(String[] args) {
		  try
		  {
		  JasperDesign jasper = JRXmlLoader.load(PATH + "report2.jrxml");
		  JRDesignQuery query = new JRDesignQuery();
		  query.setText("{ collectionName : 'json1' }");
		  query.setLanguage("MongoDbQuery");
		  jasper.setQuery(query);
		  
		  }
		  catch(Exception e)
		  {
			  e.printStackTrace();
		  }
	  }

}
