package com.jaspersoft.mongodb;

import junit.framework.Assert;

import org.junit.Test;

import com.jaspersoft.mongodb.connection.MongoDbConnectionManager;
import com.jaspersoft.mongodb.jasperserver.MongoDbDataSourceService;

/**
 * 
 * @author Eric Diaz
 * 
 */
public class ConnectionTest {

  @Test
  public void test() {
    MongoDbDataSourceService service = null;
    MongoDbConnectionManager connectionManager = null;
    try {
      connectionManager = new MongoDbConnectionManager();
      service = new MongoDbDataSourceService();
      service.setConnectionManager(connectionManager);
      // Correct URL
      service.setMongoURI("mongodb://bdsandbox6:27017/test");
      Assert.assertEquals(true, service.testConnection());
      // Incorrect database name but still valid due to MongoDB behavior
      service.setMongoURI("mongodb://bdsandbox6:27017/test2");
      Assert.assertEquals(true, service.testConnection());
      // Wrong port
      service.setMongoURI("mongodb://bdsandbox6:27011/test");
      Assert.assertEquals(false, service.testConnection());
      // Wrong servername
      service.setMongoURI("mongodb://bdsandbox5:27017/test");
      Assert.assertEquals(false, service.testConnection());
      // Malformed URL
      service.setMongoURI("jdbc://bdsandbox6:27017/test");
      Assert.assertEquals(false, service.testConnection());
    } catch (Exception e) {
      System.out.println("ERROR:" + e.getMessage());
    } finally {
      if (connectionManager != null) {
        connectionManager.shutdown();
      }
    }
  }
}