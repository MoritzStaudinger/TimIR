package at.ac.tuwien.lucombonet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Connection;

@Service
@Profile("monet")
public class StartupServiceMonetDB {

    @Value("${db_url}")
    private String dbUrl;

    @PostConstruct
    public void setUpView() throws Exception {
        DriverManagerDataSource dataSource = getDataSource();
        Connection conn = dataSource.getConnection();
        ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/createView.sql"));
    }

    private DriverManagerDataSource getDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(dbUrl);
        dataSource.setDriverClassName("nl.cwi.monetdb.jdbc.MonetDriver");
        dataSource.setUsername("monetdb");
        dataSource.setPassword("monetdb");
        return dataSource;
    }
}
