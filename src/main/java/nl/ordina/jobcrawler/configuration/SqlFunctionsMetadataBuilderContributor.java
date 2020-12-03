package nl.ordina.jobcrawler.configuration;

import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.spi.MetadataBuilderContributor;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

/**
 * This class enables the use of the custom getDistance sql function (located in resources/data.sql) while using a CriteriaQuery
 */
public class SqlFunctionsMetadataBuilderContributor implements MetadataBuilderContributor {

    @Override
    public void contribute(MetadataBuilder metadataBuilder) {
        metadataBuilder.applySqlFunction(
                "getDistance",
                new StandardSQLFunction(
                        "getDistance",
                        StandardBasicTypes.DOUBLE
                )
        );
    }

}
