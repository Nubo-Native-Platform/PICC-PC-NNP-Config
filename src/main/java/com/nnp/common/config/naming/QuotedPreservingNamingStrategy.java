package com.nnp.common.config.naming;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class QuotedPreservingNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {

    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        if (name != null && name.isQuoted()) {
            return name;
        }
        return super.toPhysicalCatalogName(name, jdbcEnvironment);
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        if (name != null && name.isQuoted()) {
            return name;
        }
        return super.toPhysicalSchemaName(name, jdbcEnvironment);
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        if (name != null && name.isQuoted()) {
            return name;
        }
        return super.toPhysicalTableName(name, jdbcEnvironment);
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        if (name != null && name.isQuoted()) {
            return name;
        }
        return super.toPhysicalSequenceName(name, jdbcEnvironment);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        if (name != null && name.isQuoted()) {
            return name;
        }
        return super.toPhysicalColumnName(name, jdbcEnvironment);
    }
}
