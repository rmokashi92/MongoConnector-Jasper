/*
 * Copyright (C) 2005 - 2012 Jaspersoft Corporation. All rights reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License  as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero  General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaspersoft.mongodb.importer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import net.sf.jasperreports.engine.JRException;

import org.apache.log4j.Logger;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.MongoServiceInfo;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;

import com.jaspersoft.mongodb.connection.MongoDbConnection;

/**
 * 
 * @author Eric Diaz
 * 
 */
public class MongoDbCloudFoundryImporter {
  private String tableName;

  private final Logger logger = Logger.getLogger(MongoDbCloudFoundryImporter.class);

  private MongoDbConnection mongoDbConnection;

  private String databaseServiceName;

  private String mongodbServiceName;

  private Connection databaseConnection;

  public MongoDbCloudFoundryImporter(String databaseServiceName, String tableName, String mongodbServiceName)
      throws JRException, SQLException {
    this.databaseServiceName = databaseServiceName;
    this.mongodbServiceName = mongodbServiceName;
    this.tableName = tableName;
    configureConnection();
    importData();
  }

  private void configureConnection() throws JRException, SQLException {
    CloudEnvironment cloudEnvironment = new CloudEnvironment();
    MongoServiceInfo mongoServiceInfo = cloudEnvironment.getServiceInfo(mongodbServiceName, MongoServiceInfo.class);
    if (mongoServiceInfo == null) {
      logger.error("No mongodb service with name: " + mongodbServiceName);
      return;
    }
    mongoDbConnection = new MongoDbConnection("mongodb://" + mongoServiceInfo.getHost() + ":"
        + mongoServiceInfo.getPort() + "/" + mongoServiceInfo.getDatabase(), mongoServiceInfo.getUserName(),
        mongoServiceInfo.getPassword());
    System.out.println("MongoDB connection configured");

    RdbmsServiceInfo databaseServiceInfo = cloudEnvironment.getServiceInfo(databaseServiceName, RdbmsServiceInfo.class);
    if (databaseServiceInfo == null) {
      logger.error("No database service with name: " + databaseServiceName);
      return;
    }
    databaseConnection = DriverManager.getConnection(databaseServiceInfo.getUrl(), databaseServiceInfo.getUserName(),
        databaseServiceInfo.getPassword());
    System.out.println("Database connection configured");
  }

  private void importData() {
    try {
      MongoDbImporter importer = new MongoDbImporter(databaseConnection, mongoDbConnection);
      importer.importTable(tableName);
    } catch (Exception e) {
      logger.error(e);
    } finally {
      try {
        databaseConnection.close();
      } catch (SQLException e) {
        logger.error(e);
      }
      mongoDbConnection.close();
    }
  }
}