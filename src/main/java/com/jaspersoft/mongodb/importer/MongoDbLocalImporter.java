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
import java.util.Properties;

import net.sf.jasperreports.engine.JRException;

import org.apache.log4j.Logger;

import com.jaspersoft.mongodb.connection.MongoDbConnection;

/**
 * 
 * @author Eric Diaz
 * 
 */
public class MongoDbLocalImporter {
  private Properties settings;

  private Connection connection;

  private static final Logger logger = Logger.getLogger(MongoDbLocalImporter.class);

  public static final String CHARACTER_SET = "UTF-8";

  private MongoDbConnection mongodbConnection;

  private MongoDbImporter importer;

  public MongoDbLocalImporter() throws Exception {
    settings = new Properties();
    settings.load(getClass().getClassLoader().getResourceAsStream("MongoDbImporter.properties"));
    createMongoConnection();
    createConnection();
    importer = new MongoDbImporter(connection, mongodbConnection);
  }

  private void createMongoConnection() throws JRException {
    mongodbConnection = new MongoDbConnection(settings.getProperty("com.jaspersoft.mongodb.uri"),
        settings.getProperty("com.jaspersoft.mongodb.mUser"), settings.getProperty("com.jaspersoft.mongodb.mPassword"));
  }

  private void createConnection() throws ClassNotFoundException {
    Class.forName(settings.getProperty("com.jaspersoft.mongodb.driver", null));
    try {
      connection = DriverManager.getConnection(settings.getProperty("com.jaspersoft.mongodb.jdbcURL", null),
          settings.getProperty("com.jaspersoft.mongodb.user", null),
          settings.getProperty("com.jaspersoft.mongodb.password", null));
      logger.info("Database connection created");
    } catch (SQLException e) {
      logger.error(e);
    }
  }

  public void shutdown() {
    if (mongodbConnection != null) {
      mongodbConnection.close();
      mongodbConnection = null;
    }
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException e) {
        logger.error(e);
      }
      connection = null;
    }
  }

  public void processTable(String tableName) throws Exception {
    importer.importTable(tableName);
    importer.validate(tableName);
  }

  public static void main(String[] arguments) throws Exception {
    arguments = new String[] { "accounts" };
    if (arguments.length != 1) {
      System.out.println("Usage: sh importer.sh TABLE_NAME");
      System.out.println("Example: sh impporter.sh accounts");
      return;
    }
    MongoDbLocalImporter localImporter = null;
    try {
      localImporter = new MongoDbLocalImporter();
      localImporter.processTable(arguments[0]);
    } finally {
      if (localImporter != null) {
        localImporter.shutdown();
      }
    }
  }
}
