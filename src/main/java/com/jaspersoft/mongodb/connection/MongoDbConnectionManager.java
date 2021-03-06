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

package com.jaspersoft.mongodb.connection;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;
import org.apache.log4j.Logger;

/**
 * 
 * @author Eric Diaz
 * 
 */
public class MongoDbConnectionManager {
  private GenericObjectPool<MongoDbConnection> connectionsPool;

  private Config poolConfiguration;

  private MongoDbConnectionFactory connectionFactory;

  private final Logger logger = Logger.getLogger(MongoDbConnectionManager.class);

  public MongoDbConnectionManager() {
    connectionFactory = new MongoDbConnectionFactory();
    poolConfiguration = new Config();
    poolConfiguration.testOnBorrow = true;
    poolConfiguration.testWhileIdle = true;
    poolConfiguration.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_GROW;
    poolConfiguration.maxActive = 4;
    poolConfiguration.maxIdle = 2;
    poolConfiguration.minIdle = 1;
  }

  private GenericObjectPool<MongoDbConnection> startConnectionsPool() {
    if (connectionsPool == null) {
      connectionsPool = new GenericObjectPool<MongoDbConnection>(connectionFactory, poolConfiguration);
    }
    return connectionsPool;
  }

  public MongoDbConnection borrowConnection() throws Exception {
    if (connectionsPool == null) {
      startConnectionsPool();
    }
    if (connectionsPool == null) {
      logger.error("No connection pool created");
      return null;
    }
    return connectionsPool.borrowObject();
  }

  public void returnConnection(MongoDbConnection connection) {
    if (connectionsPool == null) {
      logger.error("No connection pool created");
      return;
    }
    try {
      connectionsPool.returnObject(connection);
    } catch (Exception e) {
      logger.error(e);
    }
  }

  public void shutdown() {
    if (connectionsPool != null) {
      try {
        connectionsPool.clear();
        connectionsPool.close();
      } catch (Exception e) {
        logger.error(e);
      }
    }
  }

  public void setMaxActive(int maxActive) {
    poolConfiguration.maxActive = maxActive;
  }

  public void setMaxIdle(int maxIdle) {
    poolConfiguration.maxIdle = maxIdle;
  }

  public void setMinIdle(int minIdle) {
    poolConfiguration.minIdle = minIdle;
  }

  public void setMongoURI(String mongoURI) {
    connectionFactory.setMongoURI(mongoURI);
  }

  public void setUsername(String username) {
    connectionFactory.setUsername(username);
  }

  public void setPassword(String password) {
    connectionFactory.setPassword(password);
  }
}