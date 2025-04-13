package com.example.demomultitenantdualdatasource;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class TenantAwareDataSource implements DataSource {

    private final MultiTenantConnectionProvider connectionProvider;
    private final TenantIdentifierResolver tenantIdentifierResolver;

    public TenantAwareDataSource(MultiTenantConnectionProvider connectionProvider, 
                                TenantIdentifierResolver tenantIdentifierResolver) {
        this.connectionProvider = connectionProvider;
        this.tenantIdentifierResolver = tenantIdentifierResolver;
    }

    @Override
    public Connection getConnection() throws SQLException {
        String tenantId = tenantIdentifierResolver.resolveCurrentTenantIdentifier();
        return connectionProvider.getConnection(tenantId);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        // In multi-tenant setup, we ignore username/password as they are tenant-specific
        return getConnection();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException("Method not supported");
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException("Method not supported");
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException("Method not supported");
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Method not supported");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        throw new SQLException("DataSource cannot be unwrapped as " + iface.getName());
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this);
    }
}