package com.zayhu.server.linkpreview.guice;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;

/**
 * @author: daisyw
 * @data: 2019/4/27 下午2:06
 */
public class GuiceModule extends com.yeecall.yeetoken.yeeapi.guice.GuiceModule {


    protected void loadMoreConf(CompositeConfiguration conf) throws ConfigurationException {
        super.loadMoreConf(conf);
        loadConf(conf, "linkpreview.properties");
    }

    public GuiceModule() {
        super();
    }

    @Override
    protected void configure() {
        super.configure();
    }

//    @Provides
//    @Named("candybox")
//    @Singleton
//    public JedisPool provideJedisPool(Configuration configuration) {
//        return ConfigUtils.provideJedisPool(configuration.subset("candybox"));
//    }

//    @Singleton
//    @Named("candybox")
//    @Provides
//    public RedisService provideRedisService(@Named("candybox") JedisPool pool) {
//        return new ListRedisServiceImpl(pool, new JsonUtilRedisSerializer(),CACHE_PREFIX);
//    }
//
//    @Singleton @Named("candybox")
//    @Provides
//    public ListRedisService provideListRedisService(@Named("candybox")RedisService rs){
//        return (ListRedisService)rs;
//    }


//    @Singleton
//    @Named("candybox_write")
//    @Provides
//    public SqlSessionFactory provideWalletMysqlWriteService() {
//        final String write = "mysql-candybox-write.xml";
//        URL url = ConfigurationUtils.locate(ConfigUtils.DEF_CONF_DIR__, write);
//        try {
//            logger.info("mybatis write config:" + url);
//            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(url.openStream());
//            logger.info("mybatis write environment:" + sqlSessionFactory.getConfiguration().getEnvironment().getId());
//            return sqlSessionFactory;
//        } catch (IOException e) {
//            logger.error("failed to connect to write db:" + write, e);
//        }
//        return null;
//    }

//    @Singleton
//    @Named("candybox_read")
//    @Provides
//    public SqlSessionFactory provideWalletMysqlReadService() {
//        final String write = "mysql-candybox-read.xml";
//        URL url = ConfigurationUtils.locate(ConfigUtils.DEF_CONF_DIR__, write);
//        try {
//            logger.info("mybatis read config:" + url);
//            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(url.openStream());
//            logger.info("mybatis read environment:" + sqlSessionFactory.getConfiguration().getEnvironment().getId());
//            return sqlSessionFactory;
//        } catch (IOException e) {
//            logger.error("failed to connect to read db:" + write, e);
//        }
//        return null;
//    }

}
