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
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.MongoServiceInfo;

import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceService;
import com.jaspersoft.mongodb.MongoDbDataSource;
import com.jaspersoft.mongodb.connection.MongoDbConnection;

/**
 * 
 * @author Eric Diaz
 * 
 */
public class MongoDbCloudFoundryDataSourceService implements ReportDataSourceService {
  Logger logger = Logger.getLogger(MongoDbCloudFoundryDataSourceService.class);

  private CloudEnvironment cloudEnvironment;

  private MongoDbConnection connection;

  private String serviceName;

  public MongoDbCloudFoundryDataSourceService() {
    cloudEnvironment = new CloudEnvironment();
  }

  @Override
  public void closeConnection() {
    if (connection != null) {
      connection.close();
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public void setReportParameterValues(Map parameters) {
    if (serviceName == null) {
      logger.error("No service name specified");
      return;
    }
    try {
      createConnection();
      parameters.put(MongoDbDataSource.CONNECTION, connection);
    } catch (JRException e) {
      logger.error(e);
    }
  }

  private void createConnection() throws JRException {
    MongoServiceInfo mongoServiceInfo = cloudEnvironment.getServiceInfo(serviceName, MongoServiceInfo.class);
    if (mongoServiceInfo == null) {
      logger.error("No mongodb service with name: " + serviceName);
      return;
    }
    if (connection != null) {
      closeConnection();
    }
    connection = new MongoDbConnection("mongodb://" + mongoServiceInfo.getHost() + ":" + mongoServiceInfo.getPort()
        + "/" + mongoServiceInfo.getDatabase(), mongoServiceInfo.getUserName(), mongoServiceInfo.getPassword());
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

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getServiceName() {
    return serviceName;
  }
}