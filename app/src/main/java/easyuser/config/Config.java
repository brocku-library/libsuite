package easyuser.config;

import java.io.FileNotFoundException;

// import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
// import org.deeplearning4j.models.word2vec.Word2Vec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class Config {

    // public static Word2Vec W2V_MODEL;

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("almaUserGroupsJson");
    }
}
