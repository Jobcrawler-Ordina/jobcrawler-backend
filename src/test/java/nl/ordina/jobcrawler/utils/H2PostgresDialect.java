package nl.ordina.jobcrawler.utils;

import org.hibernate.dialect.H2Dialect;

import java.sql.Types;

public class H2PostgresDialect extends H2Dialect {

    public H2PostgresDialect() {
        super();
//        registerColumnType(Types.BINARY, "varchar");

    }

}
