package com.example.expenses_tracker.config;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JdbcConfig {
  @Autowired
  private final Vertx vertx;

  JDBCPool jdbcPool = null;

  public JdbcConfig(Vertx vertx) {
    this.vertx = vertx;
  }

  @Bean
  public JDBCPool getJdbcPool() {
    if (jdbcPool == null) {
      jdbcPool = JDBCPool.pool(
        vertx,
        // configure the connection
        new JDBCConnectOptions()
          // H2 connection string
          .setJdbcUrl("jdbc:h2:~/test")
          // username
          .setUser("sa")
          // password
          .setPassword(""),
        // configure the pool
        new PoolOptions()
          .setMaxSize(16)
          .setName("expenses_tracker_pool")
      );
    }
    return jdbcPool;
  }


}
