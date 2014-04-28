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
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.everit.blobstore.api.BlobstoreException;
import org.everit.blobstore.api.BlobstoreService;
import org.everit.blobstore.api.ErrorCode;
import org.everit.blobstore.internal.api.BlobstoreType;
import org.everit.blobstore.internal.cache.BlobstoreCacheService;

/**
 * Factory for Blobstore services. In case {@link BlobstoreType} is AUTO the
 * implementation class will be derived based on the data comes from the
 * database connection.
 */
public class BlobstoreServiceFactoryImpl {

	/**
	 * XA enabled datasource. If null normal datasource is used to create
	 * database connections.
	 */
	private XADataSource xaDataSource;

	/**
	 * DataSource of the Database connection. Used only if xaDataSource property
	 * is null.
	 */
	private DataSource dataSource;

	/**
	 * Caching service for the blobstore.
	 */
	private BlobstoreCacheService blobstoreCacheService;

	/**
	 * The type of the blobstore that should be instantiated by this factory.
	 */
	private BlobstoreType blobstoreType = BlobstoreType.AUTO;

	/**
	 * Calculates the type of the blobstore based on the data coming from
	 * {@link Connection#getMetaData()}.
	 * 
	 * @return The possible type of the Blobstore.
	 * @throws SQLException
	 *             if there is an error during accessing the database via the
	 *             XADataSource or DataSource property.
	 */
	protected BlobstoreType calculateBlobstoreType() throws SQLException {
		Connection connection = null;
		try {
			if (dataSource != null) {
				connection = dataSource.getConnection();
			} else if (xaDataSource != null) {
				connection = xaDataSource.getXAConnection().getConnection();
			}
			DatabaseMetaData metaData = connection.getMetaData();
			String productName = metaData.getDatabaseProductName();
			String productVersion = metaData.getDatabaseProductVersion();
			if ("PostgreSQL".equals(productName)
					&& ("7.1".compareTo(productVersion) <= 0)) {
				return BlobstoreType.POSTGRES;
			} else {
				return BlobstoreType.JDBC;
			}

		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}

	/**
	 * Creating a new blobstore service object. If the blobstoreserviceType
	 * contains the value AUTO this functions tries to instantiate the best
	 * implementation for the given datasources otherwise the specified type is
	 * instantiated.
	 * 
	 * @return A newly created Blobstore service
	 */
	public BlobstoreService createBlobstoreService() {
		BlobstoreType currentBlobstoreType = blobstoreType;
		if (BlobstoreType.AUTO.equals(currentBlobstoreType)) {
			try {
				currentBlobstoreType = calculateBlobstoreType();
			} catch (SQLException e) {
				throw new BlobstoreException(
						ErrorCode.BLOBSTORE_TYPE_DERIVATION, e);
			}
		}
		AbstractBlobstoreServiceImpl result = null;
		// if (BlobstoreType.JDBC.equals(currentBlobstoreType)) {
		// result = new JDBCBlobstoreServiceImpl(dataSource, xaDataSource,
		// blobstoreCacheService);
		// } else if (BlobstoreType.POSTGRES.equals(currentBlobstoreType)) {
		// result = new PostgresBlobstoreServiceImpl(dataSource, xaDataSource,
		// blobstoreCacheService);
		// } else {
		// result = new JDBCBlobstoreServiceImpl(dataSource, xaDataSource,
		// blobstoreCacheService);
		// }
		return result;
	}

	public void setBlobstoreCacheService(
			final BlobstoreCacheService blobstoreCacheService) {
		this.blobstoreCacheService = blobstoreCacheService;
	}

	public void setBlobstoreType(final BlobstoreType blobstoreType) {
		this.blobstoreType = blobstoreType;
	}

	public void setDataSource(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setXaDataSource(final XADataSource xaDataSource) {
		this.xaDataSource = xaDataSource;
	}
}
