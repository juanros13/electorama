package com.electorama.msvc.visits.config;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.v2.DbxClientV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DropboxConfig {
  @Value("${app.dropbox.access-token}")
  private String dropboxAccessToken;

  @Value("${app.dropbox.refresh-token}")
  private String dropboxRefreshToken;

  @Value("${app.dropbox.app-key}")
  private String dropboxAppKey;

  @Value("${app.dropbox.app-secret}")
  private String dropboxAppSecret;

  //	@Bean
  //	public DbxClientV2 dropboxClient() {
  //		DbxRequestConfig config = new DbxRequestConfig("dropbox/somoslamayoria");
  //		DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
  //		return client;
  //	}

  @Bean
  public DbxClientV2 DropboxClient() {
    DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/somoslamayoria").build();

    DbxCredential credentials =
        new DbxCredential(
            dropboxAccessToken, -1L, dropboxRefreshToken, dropboxAppKey, dropboxAppSecret);

    return new DbxClientV2(config, credentials);
  }
}
