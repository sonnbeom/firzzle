package com.firzzle.common.config;

import com.firzzle.common.library.MyBatisSqlSessionTemplate;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class DataSourceConfiguration {

    private final ApplicationContext applicationContext;

    private final SpringDataSourceProperties springDataSource;


    @Bean
    @Primary
    @Qualifier("dataSource")
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(springDataSource.getDriverClassName());
        config.setJdbcUrl(springDataSource.getJdbcUrl());
        config.setUsername(springDataSource.getUsername());
        config.setPassword(springDataSource.getPassword());
        config.setMaximumPoolSize(springDataSource.getMaximumPoolSize());
        config.setLeakDetectionThreshold(10000);
        config.setMaxLifetime(30000);
        config.setConnectionTimeout(20000);
        config.setPoolName("Firzzle-MySQL-HikariCP");

        return new HikariDataSource(config);
    }

    @Bean(name = "sqlSessionFactory")
    @Primary
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource());
        sqlSessionFactoryBean.setTypeAliasesPackage("com.firzzle.**.dto");
        sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("classpath:/mapper/**/*.xml"));
        sqlSessionFactoryBean.setConfigLocation(applicationContext.getResource("classpath:config/config-mybatis.xml"));

        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "sqlSession")
    @Primary
    public MyBatisSqlSessionTemplate sqlSession(@Qualifier("sqlSessionFactory")SqlSessionFactory sqlSessionFactory) {
        return new MyBatisSqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name="lockProvider")
    @Profile("!local")
    public LockProvider lockProvider() {
        return new JdbcTemplateLockProvider(dataSource());
    }

}
