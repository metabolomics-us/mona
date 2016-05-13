package swagger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Created by wohlgemuth on 5/13/16.
 */
@SpringBootApplication
@EnableSwagger2
public class App extends WebSecurityConfigurerAdapter {

    public static void main(String args[]) {
        SpringApplication.run(App.class,args);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/**");
    }
}
