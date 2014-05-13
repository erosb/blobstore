/**
 * This file is part of Everit - Blobstore JDBC.
 *
 * Everit - Blobstore JDBC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Blobstore JDBC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Blobstore JDBC.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.blobstore.jdbc;

import javax.sql.DataSource;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.everit.blobstore.api.Blobstore;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

@Component(name = "org.everit.blobstore.jdbc.JDBCBlobstore", configurationFactory = true,
        policy = ConfigurationPolicy.REQUIRE)
@Properties({ @Property(name = "dataSource.target"), @Property(name = "logService.target") })
public class JDBCBlobstoreComponent {

    @Reference
    private DataSource dataSource;

    @Reference
    private LogService logService;

    @Activate
    public void activate(final BundleContext context) {
        Blobstore blobstoreService = new JDBCBlobstoreImpl(dataSource, null);
    }

    public void bindDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void bindLogService(final LogService logService) {
        this.logService = logService;
    }

    @Deactivate
    public void deactivate() {

    }
}
