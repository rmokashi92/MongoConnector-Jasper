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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.jaspersoft.mongodb.connection.MongoDbConnection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * 
 * @author Eric Diaz
 * 
 */
public class MongoDbImporter {
  private Connection connection;

  private Statement statement;

  private static final Logger logger = Logger.getLogger(MongoDbImporter.class);

  public static final String CHARACTER_SET = "UTF-8";

  private MongoDbConnection mongodbConnection;

  public MongoDbImporter(Connection connection, MongoDbConnection mongodbConnection) throws Exception {
    this.connection = connection;
    this.mongodbConnection = mongodbConnection;
    createConnection();
  }

  private void createConnection() throws ClassNotFoundException {
    try {
      statement = connection.createStatement();
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
    if (statement != null) {
      try {
        statement.close();
      } catch (SQLException e) {
        logger.error(e);
      }
      statement = null;
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

  public void importTable(String tableName) throws Exception {
    createConnection();
    logger.info("Initialize import");
    ResultSet resultSet = null;
    List<DBObject> objectsList = new ArrayList<DBObject>();
    try {
      resultSet = statement.executeQuery("SELECT * FROM " + tableName);
      ResultSetMetaData metaData = resultSet.getMetaData();
      int index, columnCount = metaData.getColumnCount(), count = 0;
      logger.info("Importing rows");
      DBCollection collection = null;
      if (!mongodbConnection.getMongoDatabase().collectionExists(tableName)) {
        logger.info("Collection \"" + tableName + "\" doesn't exist");
        DBObject options = new BasicDBObject("capped", false);
        collection = mongodbConnection.getMongoDatabase().createCollection(tableName, options);
      } else {
        logger.info("Collection \"" + tableName + "\" exists");
        collection = mongodbConnection.getMongoDatabase().getCollectionFromString(tableName);
        collection.drop();
        logger.info("Collection \"" + tableName + "\" was cleaned up");
      }
      Object value;
      DBObject newObject;
      while (resultSet.next()) {
        newObject = new BasicDBObject();
        for (index = 1; index <= columnCount; index++) {
          value = resultSet.getObject(index);
          if (value != null) {
            newObject.put(metaData.getColumnName(index), value);
          }
        }
        objectsList.add(newObject);
        count++;
        if (count % 100 == 0) {
          logger.info("Processed: " + count);
          logger.info("Result: " + collection.insert(objectsList).getField("ok"));
          objectsList.clear();
        }
      }
      if (objectsList.size() > 0) {
        collection.insert(objectsList);
        logger.info("Result: " + collection.insert(objectsList).getField("ok"));
        objectsList.clear();
      }
      logger.info("Rows added: " + count);
      logger.info("Import done");
    } finally {
      if (resultSet != null) {
        resultSet.close();
      }
    }
  }

  public void validate(String tableName) {
    DBCollection collection = mongodbConnection.getMongoDatabase().getCollection(tableName);
    long size = collection.getCount();
    if (size == 0) {
      logger.error("No data in Mongo database");
      return;
    }
    logger.info("Elements in collection: " + size);
    logger.info("Validating the first 5 entries");
    DBCursor cursor = collection.find().limit(5);
    DBObject object;
    Object value;
    logger.info("---------------");
    while (cursor.hasNext()) {
      object = cursor.next();
      for (String id : object.keySet()) {
        value = object.get(id);
        logger.info(value + " -> " + value.getClass().getName());
      }
      logger.info("---------------");
    }
  }
}
