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

package com.jaspersoft.mongodb.jasperserver;

import java.util.Map;

import net.sf.jasperreports.engine.JRException;

import org.apache.log4j.Logger;

import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceService;
import com.jaspersoft.mongodb.MongoDbDataSource;
import com.jaspersoft.mongodb.connection.MongoDbConnection;
import com.jaspersoft.mongodb.connection.MongoDbConnectionManager;

/**
 * 
 * @author Eric Diaz
 * 
 */
public class MongoDbDataSourceService implements ReportDataSourceService {
    private final static Logger logger = Logger.getLogger(MongoDbDataSourceService.class);

    private MongoDbConnection connection;

    private String mongoURI;

    private String username;

    private String password;

    private MongoDbConnectionManager connectionManager;

    /**
     * Returns the active connection to the pool
     */
    public void closeConnection() {
        if (connectionManager != null && connection != null) {
            connectionManager.returnConnection(connection);
            connection = null;
        }
    }

    /**
     * Creates a new {@link MongoDbDataSource} base on the parameters set
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void setReportParameterValues(Map parameters) {
        try {
            createConnection();
            parameters.put(MongoDbDataSource.CONNECTION, connection);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void createConnection() throws JRException {
        if (connection != null) {
            closeConnection();
        }
        connectionManager.setMongoURI(mongoURI);
        connectionManager.setUsername(username);
        connectionManager.setPassword(password);
        try {
            connection = connectionManager.borrowConnection();
        } catch (Exception e) {
            logger.error(e);
            throw new JRException(e.getMessage());
        }
    }

    public boolean testConnection() throws JRException {
        try {
            createConnection();
            if (connection == null) {
                return false;
            }
            connection.test();
            return true;
        } finally {
            closeConnection();
        }
    }

    public void setMongoURI(String mongoURI) {
        this.mongoURI = mongoURI;
    }

    public String getMongoURI() {
        return mongoURI;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setConnectionManager(MongoDbConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public MongoDbConnectionManager getConnectionManager() {
        return connectionManager;
    }
}
