package at.ac.tuwien.lucombonet.config;

import org.springframework.stereotype.Component;

@Component
public interface Values {

    public String getDbUrl();

    public String getDbUser();

    public String getDbPassword();

    public String getDbDriver();

}
