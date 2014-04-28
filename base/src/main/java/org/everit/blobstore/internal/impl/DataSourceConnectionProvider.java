/**
 * This file is part of Everit - Blobstore Base.
 *
 * Everit - Blobstore Base is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Blobstore Base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Blobstore Base.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.blobstore.internal.impl;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.everit.blobstore.internal.api.ConnectionProvider;
/**
 * {@link ConnectionProvider} that gets the connections from the {@link DataSource} provided via the constructor.
 */
public class DataSourceConnectionProvider implements ConnectionProvider {

    /**
     * DataSource providing XAConnections. If available xaDatasource is used and if not simple {@link #dataSource}.
     */
    private XADataSource xaDataSource;

    /**
     * Simple datasource. When no {@link #xaDataSource} is available this datasource is used. If none of the dataSources
     * are available an exception will be thrown.
     */
    private DataSource dataSource;

    /**
     * Constructor.
     * 
     * @param dataSource
     *            Standard datasource. If null xaDataSource must be provided.
     * @param xaDataSource
     *            {@link XADataSource} that if null simple {@link DataSource} must be provided.
     */
    public DataSourceConnectionProvider(final DataSource dataSource, final XADataSource xaDataSource) {
        super();
        this.dataSource = dataSource;
        this.xaDataSource = xaDataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (xaDataSource != null) {
            return xaDataSource.getXAConnection().getConnection();
        } else {
            if (dataSource != null) {
                return dataSource.getConnection();
            } else {
                throw new SQLException("Either dataSource or xaDataSource has to be provided.");
            }
        }
    }
}
