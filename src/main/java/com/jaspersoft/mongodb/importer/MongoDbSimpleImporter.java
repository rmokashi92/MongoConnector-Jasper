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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import net.sf.jasperreports.engine.JRException;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

import com.jaspersoft.mongodb.connection.MongoDbConnection;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

/**
 * 
 * @author Eric Diaz
 * 
 */
public class MongoDbSimpleImporter {
  private final Logger logger = Logger.getLogger(MongoDbSimpleImporter.class);

  public MongoDbSimpleImporter(String mongoURI, String collectionName, Resource scriptResource) throws JRException {
    this(mongoURI, null, null, collectionName, scriptResource);
  }

  public MongoDbSimpleImporter(String mongoURI, String username, String password, String collectionName,
      Resource scriptResource) throws JRException {
    MongoDbConnection connection = new MongoDbConnection(mongoURI, username, password);
    populate(connection, collectionName, scriptResource);
  }

  private void populate(MongoDbConnection connection, String collectionName, Resource scriptResource)
      throws JRException {
    DBCollection collection = null;
    DB mongoDatabase = null;
    try {
      mongoDatabase = connection.getMongoDatabase();
      if (!mongoDatabase.collectionExists(collectionName)) {
        logger.info("Collection \"" + collectionName + "\" doesn't exist");
        DBObject options = new BasicDBObject("capped", false);
        collection = mongoDatabase.createCollection(collectionName, options);
      } else {
        logger.info("Collection \"" + collectionName + "\" exists");
        collection = mongoDatabase.getCollectionFromString(collectionName);
        collection.drop();
        logger.info("Collection \"" + collectionName + "\" was cleaned up");
      }
    } catch (MongoException e) {
      logger.error(e);
    }

    if (mongoDatabase == null) {
      throw new JRException("Failed connection to mongoDB database: " + connection.getMongoURIObject().getDatabase());
    }

    FileInputStream fileInputStream = null;
    InputStreamReader inputStreamReader = null;
    BufferedReader reader = null;
    try {
      inputStreamReader = new InputStreamReader(scriptResource.getInputStream());
      reader = new BufferedReader(inputStreamReader);
      StringBuilder stringBuilder = new StringBuilder();
      String currentLine;
      while ((currentLine = reader.readLine()) != null) {
        stringBuilder.append(currentLine);
      }
      Object parseResult = JSON.parse(stringBuilder.toString());
      if (!(parseResult instanceof BasicDBList)) {
        throw new JRException("Unsupported type: " + parseResult.getClass().getName() + ". It must be a list");
      }
      BasicDBList list = (BasicDBList) parseResult;
      List<DBObject> objectsList = new ArrayList<DBObject>();
      for (int index = 0; index < list.size(); index++) {
        objectsList.add((DBObject) list.get(index));
      }
      collection.insert(objectsList);
      logger.info("Collection count: " + collection.count() + "\nSuccessfully populated collection: " + collectionName);
    } catch (UnsupportedEncodingException e) {
      logger.error(e);
    } catch (IOException e) {
      logger.error(e);
    } finally {
      if (fileInputStream != null) {
        try {
          fileInputStream.close();
        } catch (IOException e) {
          logger.error(e);
        }
      }
      if (inputStreamReader != null) {
        try {
          inputStreamReader.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

}