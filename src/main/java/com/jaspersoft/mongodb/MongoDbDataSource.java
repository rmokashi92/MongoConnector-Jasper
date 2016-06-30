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

package com.jaspersoft.mongodb;

import java.util.Iterator;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.apache.log4j.Logger;

import com.jaspersoft.mongodb.query.MongoDbQueryWrapper;
import com.mongodb.DBObject;

/**
 * An implementation of a data source that uses an empty query and parameters
 * 
 * @author Eric Diaz
 * 
 */
public class MongoDbDataSource implements JRDataSource {
  private MongoDbQueryWrapper wrapper;

  private DBObject currentDbObject;
  
  public final static String CONNECTION = "com.jaspersoft.mongodb.connection";

  public static final String QUERY_LANGUAGE = "MongoDbQuery";

  private static final Logger logger = Logger.getLogger(MongoDbDataSource.class);

  private boolean hasIterator = false;

  private boolean hasCommandResult = false;

  private Iterator<?> resultsIterator;

  private Map<?, ?> currentResult;

  public MongoDbDataSource(MongoDbQueryWrapper wrapper) {
    logger.info("New MongoDB Data Source");
    this.wrapper = wrapper;
    hasIterator = wrapper.iterator != null;
    if (!hasIterator) {
      hasCommandResult = wrapper.commandResults != null;
      resultsIterator = wrapper.commandResults.iterator();
    }
  }

  /**
   * Gets the field value for the current position.
   */
  @Override
  public Object getFieldValue(JRField field) throws JRException {
    try {
      if (hasIterator) {
        return getCursorValue(field.getName());
      } else if (hasCommandResult) {
        return getCommandResult(field.getName());
      }
      return null;
    } catch (Exception e) {
      logger.error(e);
      throw new JRException(e.getMessage());
    }
  }

  private Object getCommandResult(String fieldName) {
    return currentResult.get(fieldName);
  }

  private Object getCursorValue(String fieldName) {
    String[] ids = fieldName.split("\\" + MongoDbFieldsProvider.FIELD_NAME_SEPARATOR);
    System.out.println(ids);
    DBObject fieldObject = currentDbObject;
    Object currentFieldObject;
    String id;
    boolean isLast;
    for (int index = 0; index < ids.length; index++) {
      isLast = index == (ids.length - 1);
      id = ids[index];
      currentFieldObject = fieldObject.get(id);
      if (currentFieldObject == null) {
        return null;
      }
      if (currentFieldObject instanceof DBObject) {
        fieldObject = (DBObject) currentFieldObject;
        if (isLast) {
          return fieldObject;
        }
      } else {
        if (isLast) {
          return fieldObject.get(id);
        }
        return null;
      }
    }
    return null;
  }

  /**
   * Tries to position the cursor on the next element in the data source.
   */
  @Override
  public boolean next() throws JRException {
    boolean next = false;
  
    if (hasIterator && (next = wrapper.iterator.hasNext())) {
      currentDbObject = wrapper.iterator.next();
    } else if (hasCommandResult) {
      next = resultsIterator.hasNext();
      currentResult = null;
      if (next) {
        currentResult = (Map<?, ?>) resultsIterator.next();
      }
    }
    
    return next;
  }
}